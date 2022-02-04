check_cf_stack_creation () {
    CF_STACK_STATUS=$(aws cloudformation describe-stacks --stack-name "ci-service" --region "us-east-1" --query 'Stacks[0]'.{StackStatus:StackStatus} --output text)
    if [[ ${CF_STACK_STATUS} == "CREATE_COMPLETE" ]]; then
        echo "Successfully created cf stack- ci-service"
        aws cloudformation update-termination-protection --enable-termination-protection --stack-name "ci-service" --region "us-east-1"
        echo "TerminationProtection enabled for the created cf stack- ci-service"
    else
        echo "There are problems with CF stack ci-service. Exiting"
        exit 1;
    fi
}

STACKSTATUS=$(aws cloudformation describe-stacks --stack-name "ci-service" --region "us-east-1" --query 'Stacks[0]'.{StackStatus:StackStatus} --output text||echo "Failed")
if [[ ${STACKSTATUS} == "CREATE_COMPLETE" ]]; then
    aws cloudformation update-termination-protection --no-enable-termination-protection --stack-name ci-service --region us-east-1
    aws cloudformation delete-stack --stack-name ci-service --region us-east-1
    aws cloudformation wait stack-delete-complete --stack-name ci-service --region us-east-1
    aws cloudformation create-stack --stack-name "ci-service" --template-url "https://ci-service.s3.amazonaws.com/cf-template-new.json" --parameters file://deployment/cloudformation/cf-parameters-new.json --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND CAPABILITY_NAMED_IAM --region "us-east-1"
    aws cloudformation wait stack-create-complete --stack-name "ci-service" --region "us-east-1"
    check_cf_stack_creation
elif [[ ${STACKSTATUS} == "CREATE_IN_PROGRESS" ]]; then
    echo "A CF Stack with name [ci-service] is already getting created. Waiting for it to finish."
    aws cloudformation wait stack-create-complete --stack-name "ci-service" --region "us-east-1"
    check_cf_stack_creation
    exit 0;
elif [[ ${STACKSTATUS} == ROLLBACK* ]]; then
    echo "A CF Stack with name [ci-service] is already in state ${STACKSTATUS}. Please remove it and rerun this stage."
    exit 1;
elif [[ ${STACKSTATUS} == DELETE* ]]; then
    echo "A CF Stack with name [ci-service] is already in state ${STACKSTATUS}. Please remove it and rerun this stage."
    exit 1;
else
    echo "Creating [ci-service]"
    aws cloudformation create-stack --stack-name "ci-service" --template-url "https://ci-service.s3.amazonaws.com/cf-template-new.json" --parameters file://deployment/cloudformation/cf-parameters-new.json --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND CAPABILITY_NAMED_IAM --region "us-east-1"
    aws cloudformation wait stack-create-complete --stack-name "ci-service" --region "us-east-1"
    check_cf_stack_creation
fi
