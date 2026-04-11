package com.train.trainservice.service;

import com.train.trainservice.client.SeatInventoryClient;
import com.train.trainservice.dto.*;
import com.train.trainservice.entity.Station;
import com.train.trainservice.entity.Train;
import com.train.trainservice.entity.TrainRoute;
import com.train.trainservice.repository.StationRepository;
import com.train.trainservice.repository.TrainRepository;
import com.train.trainservice.repository.TrainRouteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    TrainRouteRepository trainRouteRepository;

    @Autowired
    SeatInventoryClient seatInventoryClient;

    @Autowired
    FareService fareService;


    /*
     ---------------------------------------
     CREATE TRAIN + STATIONS + ROUTES
     ---------------------------------------
     */
    @Transactional
    public void createTrainWithRoutes(CreateTrainRequest request) {

        // 1️⃣ Create Train
        Train train = new Train();
        train.setTrainName(request.getTrainName());

        Train savedTrain = trainRepository.save(train);

        // 2️⃣ Create Stations + Routes
        for (RouteRequest routeReq : request.getRoutes()) {

            // check if station already exists
            Station station = stationRepository.findByCode(routeReq.getStationCode());

            if (station == null) {
                station = new Station();
                station.setName(routeReq.getStationName());
                station.setCode(routeReq.getStationCode());
                station = stationRepository.save(station);
            }

            TrainRoute route = new TrainRoute();
            route.setTrainId(savedTrain.getTrainId());
            route.setStationId(station.getStationId());
            route.setStopOrder(routeReq.getOrder());

            if (routeReq.getArrival() != null) {
                route.setArrivalTime(LocalTime.parse(routeReq.getArrival()));
            }

            if (routeReq.getDeparture() != null) {
                route.setDepartureTime(LocalTime.parse(routeReq.getDeparture()));
            }

            route.setDistanceFromSource((long) routeReq.getDistanceFromSource());

            trainRouteRepository.save(route);
        }

        // CALL SEAT SERVICE
        SeatSetupRequest seatRequest = new SeatSetupRequest();
        seatRequest.setTrainId(savedTrain.getTrainId());
        seatRequest.setSleeperCoaches(1);
        seatRequest.setAc3Coaches(1);
        seatRequest.setAc2Coaches(1);
        seatRequest.setSeatsPerCoach(3);

        seatInventoryClient.createSeats(seatRequest);
    }


    /*
     ---------------------------------------
     GET ROUTE
     ---------------------------------------
     */
    public List<TrainRoute> getRoute(Long trainId) {
        return trainRouteRepository.findByTrainIdOrderByStopOrderAsc(trainId);
    }


    /*
     ---------------------------------------
     GET STATION ORDER
     ---------------------------------------
     */
    public int getStationOrder(Long trainId, String stationCode) {

        Station station = stationRepository.findByCode(stationCode);

        TrainRoute route =
                trainRouteRepository.findByTrainIdAndStationId(trainId, station.getStationId());

        return route.getStopOrder();
    }


    /*
     ---------------------------------------
     VALIDATE ROUTE
     ---------------------------------------
     */
    public boolean isValidRoute(Long trainId, String from, String to) {

        int fromOrder = getStationOrder(trainId, from);
        int toOrder = getStationOrder(trainId, to);

        return fromOrder < toOrder;
    }


    /*
     ---------------------------------------
     SEARCH TRAINS
     ---------------------------------------
     */
    public List<TrainSearchResponse> searchTrains(
            String sourceCode,
            String destCode,
            LocalDate journeyDate) {

        Station source = stationRepository.findByCode(sourceCode);
        Station dest = stationRepository.findByCode(destCode);

        if (source == null || dest == null) {
            throw new RuntimeException("Invalid station");
        }

        List<Long> trainIds =
                trainRouteRepository.findTrainsBetweenStations(
                        source.getStationId(),
                        dest.getStationId()
                );

        List<Train> trains = trainRepository.findAllById(trainIds);

        DayOfWeek day = journeyDate.getDayOfWeek();

        // filter trains by running day
        List<Train> filteredTrains = trains.stream()
                .filter(train ->
                        train.getRunningDays() != null &&
                                train.getRunningDays().contains(day))
                .toList();


        // prepare bulk availability requests
        List<AvailabilityRequest> requests = new ArrayList<>();
        Map<Long, int[]> routeMap = new HashMap<>();

        for (Train train : filteredTrains) {

            int fromOrder =
                    trainRouteRepository
                            .findByTrainIdAndStationId(
                                    train.getTrainId(),
                                    source.getStationId())
                            .getStopOrder();

            int toOrder =
                    trainRouteRepository
                            .findByTrainIdAndStationId(
                                    train.getTrainId(),
                                    dest.getStationId())
                            .getStopOrder();

            requests.add(new AvailabilityRequest(
                    train.getTrainId(),
                    journeyDate,
                    fromOrder,
                    toOrder
            ));

            routeMap.put(train.getTrainId(), new int[]{fromOrder, toOrder});
        }


        // single API call to seat service
        Map<Long, Integer> availabilityMap =
                seatInventoryClient.getBulkAvailability(requests);


        // build response
        return filteredTrains.stream().map(train -> {

            int[] route = routeMap.get(train.getTrainId());
            int fromOrder = route[0];
            int toOrder = route[1];

            TrainRoute fromRoute =
                    trainRouteRepository
                            .findByTrainIdAndStationId(
                                    train.getTrainId(),
                                    source.getStationId());

            TrainRoute toRoute =
                    trainRouteRepository
                            .findByTrainIdAndStationId(
                                    train.getTrainId(),
                                    dest.getStationId());

            LocalTime departureTime = fromRoute.getDepartureTime();
            LocalTime arrivalTime = toRoute.getArrivalTime();

            long durationHours =
                    Duration.between(departureTime, arrivalTime).toHours();

            int availableSeats =
                    availabilityMap.getOrDefault(train.getTrainId(), 0);

            int distance =
                    Math.toIntExact(
                            toRoute.getDistanceFromSource() -
                                    fromRoute.getDistanceFromSource()
                    );


            Map<String, Double> fares = new HashMap<>();

            fares.put("GENERAL",
                    fareService.calculateFare(distance, "GENERAL"));

            fares.put("SLEEPER",
                    fareService.calculateFare(distance, "SLEEPER"));

            fares.put("AC3",
                    fareService.calculateFare(distance, "AC3"));

            fares.put("AC2",
                    fareService.calculateFare(distance, "AC2"));

            fares.put("AC1",
                    fareService.calculateFare(distance, "AC1"));


            return new TrainSearchResponse(
                    train.getTrainId(),
                    train.getTrainName(),
                    sourceCode,
                    destCode,
                    fromOrder,
                    toOrder,
                    journeyDate,
                    availableSeats,
                    arrivalTime.toString(),
                    departureTime.toString(),
                    durationHours,
                    fares
            );

        }).toList();
    }
}