package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.machinelearning.HeatWeights;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HeatWeightsRepository extends MongoRepository<HeatWeights, String>
{
}