#Always run this script from home dir

docker build -t codebase-insights-service .
aws ecr get-login-password | docker login --username AWS --password-stdin 099942544481.dkr.ecr.us-east-1.amazonaws.com
docker tag codebase-insights:latest 099942544481.dkr.ecr.us-east-1.amazonaws.com/codebase-insights:latest
docker push 099942544481.dkr.ecr.us-east-1.amazonaws.com/codebase-insights:latest
#docker run -p 8080:8080 codebase-insights-service
