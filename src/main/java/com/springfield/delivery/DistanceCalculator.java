package com.springfield.delivery;

public class DistanceCalculator {

    public double calculateDistanceInKm(String from, String to) {
        if ("Москва".equals(from) && "Питер".equals(to)) {
            return 700.0;
        }
        if ("Ошибка".equals(from)) {
            return -1.0;
        }
        return 5.0;
    }
}