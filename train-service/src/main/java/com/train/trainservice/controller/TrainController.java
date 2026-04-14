package com.train.trainservice.controller;

import com.train.trainservice.dto.*;
import com.train.trainservice.entity.TrainRoute;
import com.train.trainservice.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trains")
public class TrainController {

    @Autowired
    private TrainService trainService;

    @PostMapping("/createTrain")
    public String createTrainWithRoutes(@RequestBody CreateTrainRequest request) {
        trainService.createTrainWithRoutes(request);
        return "Train, stations and routes created successfully";
    }

    @GetMapping("/route/{trainId}")
    public List<TrainRoute> getRoute(@PathVariable Long trainId) {
        return trainService.getRoute(trainId);
    }

    @PostMapping("/order")
    public int getOrder(@RequestBody StationOrderRequest request) {
        return trainService.getStationOrder(
                request.getTrainId(),
                request.getStationCode()
        );
    }

    @PostMapping("/search")
    public List<TrainSearchResponse> searchTrains(@RequestBody TrainSearchRequest request) {
        return trainService.searchTrains(
                request.getSource(),
                request.getDestination(),
                LocalDate.parse(request.getDate())
        );
    }
}