package com.train.trainservice.service;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    public double calculateFare(int distance, String coachType) {

        double baseFare = distance * 0.5;

        double multiplier = switch (coachType) {
            case "GENERAL" -> 1;
            case "SLEEPER" -> 1.5;
            case "AC3" -> 2;
            case "AC2" -> 3;
            case "AC1" -> 4;
            default -> 1;
        };


        return baseFare * multiplier;
    }
}
