package com.train.trainservice.service;

import com.train.trainservice.client.SeatInventoryClient;
import com.train.trainservice.dto.TrainSearchResponse;
import com.train.trainservice.dto.AvailabilityRequest;
import com.train.trainservice.entity.Station;
import com.train.trainservice.entity.Train;
import com.train.trainservice.entity.TrainRoute;
import com.train.trainservice.repository.StationRepository;
import com.train.trainservice.repository.TrainRepository;
import com.train.trainservice.repository.TrainRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Train createTrain(String name) {

        Train train = new Train();
        train.setTrainName(name);

        return trainRepository.save(train);
    }

    public Station createStation(String name, String code) {

        Station station = new Station();
        station.setName(name);
        station.setCode(code);

        return stationRepository.save(station);
    }

    public void addRoute(Long trainId,
                         Long stationId,
                         int order,
                         String arrival,
                         String departure) {

        TrainRoute route = new TrainRoute();
        route.setTrainId(trainId);
        route.setStationId(stationId);
        route.setStopOrder(order);
        if(arrival != null){
            route.setArrivalTime(LocalTime.parse(arrival));
        }
        if(departure != null){
            route.setDepartureTime(LocalTime.parse(departure));
        }
        trainRouteRepository.save(route);
    }

    public List<TrainRoute> getRoute(Long trainId) {
        return trainRouteRepository.findByTrainIdOrderByStopOrderAsc(trainId);
    }

    public int getStationOrder(Long trainId, String stationCode) {

        Station station = stationRepository.findByCode(stationCode);

        TrainRoute route =
                trainRouteRepository.findByTrainIdAndStationId(
                        trainId,
                        station.getStationId()
                );

        return route.getStopOrder();
    }

    public boolean isValidRoute(Long trainId,
                                String from,
                                String to) {

        int fromOrder = getStationOrder(trainId, from);
        int toOrder = getStationOrder(trainId, to);

        return fromOrder < toOrder;
    }

    public List<TrainSearchResponse> searchTrains(String sourceCode,
                                                  String destCode,
                                                  LocalDate journeyDate) {

        Station source = stationRepository.findByCode(sourceCode);
        Station dest = stationRepository.findByCode(destCode);

        if (source == null || dest == null) {
            throw new RuntimeException("Invalid station");
        }

        List<Long> trainIds = trainRouteRepository.findTrainsBetweenStations(
                source.getStationId(),
                dest.getStationId()
        );

        List<Train> trains = trainRepository.findAllById(trainIds);

        DayOfWeek day = journeyDate.getDayOfWeek();

        // filter trains based on running day
        List<Train> filteredTrains = trains.stream()
                .filter(train -> train.getRunningDays() != null &&
                        train.getRunningDays().contains(day))
                .toList();

        // prepare bulk availability requests
        List<AvailabilityRequest> requests = new ArrayList<>();
        Map<Long, int[]> routeMap = new HashMap<>();

        for (Train train : filteredTrains) {

            int fromOrder = trainRouteRepository
                    .findByTrainIdAndStationId(train.getTrainId(), source.getStationId())
                    .getStopOrder();

            int toOrder = trainRouteRepository
                    .findByTrainIdAndStationId(train.getTrainId(), dest.getStationId())
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

        // build final response
        return filteredTrains.stream().map(train -> {

            int[] route = routeMap.get(train.getTrainId());

            int fromOrder = route[0];
            int toOrder = route[1];

            TrainRoute fromRoute = trainRouteRepository
                    .findByTrainIdAndStationId(train.getTrainId(), source.getStationId());

            TrainRoute toRoute = trainRouteRepository
                    .findByTrainIdAndStationId(train.getTrainId(), dest.getStationId());

            LocalTime departureTime = fromRoute.getDepartureTime();
            LocalTime arrivalTime = toRoute.getArrivalTime();

            long durationHours = Duration.between(departureTime, arrivalTime).toHours();

            int availableSeats = availabilityMap.getOrDefault(train.getTrainId(), 0);

            int distance = Math.toIntExact(toRoute.getDistanceFromSource() - fromRoute.getDistanceFromSource());

            Map<String, Double> fares = new HashMap<>();
            fares.put("GENERAL", fareService.calculateFare(distance, "GENERAL"));
            fares.put("SLEEPER", fareService.calculateFare(distance, "SLEEPER"));
            fares.put("AC3", fareService.calculateFare(distance, "AC3"));
            fares.put("AC2", fareService.calculateFare(distance, "AC2"));
            fares.put("AC1", fareService.calculateFare(distance, "AC1"));

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
