package com.springfield.delivery;

public class Courier {

    private final String name;

    public Courier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTransportType() {
        return "Пешком";
    }
}