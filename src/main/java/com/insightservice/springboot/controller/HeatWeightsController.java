package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.machinelearning.HeatWeights;
import com.insightservice.springboot.service.HeatWeightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/weights")
public class HeatWeightsController
{
    @Autowired
    private HeatWeightsService heatWeightsService;


    @PostMapping("/adjust")
    public ResponseEntity<?> adjustHeatWeights(@RequestBody HeatWeights heatWeightsAdjustment, BindingResult result)
    {
        return new ResponseEntity<>(heatWeightsService.storeHeatWeightAdjustments(heatWeightsAdjustment), HttpStatus.OK);
    }
}
