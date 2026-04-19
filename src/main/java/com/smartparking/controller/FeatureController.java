package com.smartparking.controller;

import com.smartparking.entities.parking.Feature;
import com.smartparking.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/features")

public class FeatureController {

    @Autowired
    private FeatureRepository featureRepository;

    // Creates a new global feature (e.g., "CCTV")
    @PostMapping("/add")
    public ResponseEntity<Feature> addFeature(@RequestBody Feature feature) {
        return new ResponseEntity<>(featureRepository.save(feature), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(featureRepository.findAll());
    }
}