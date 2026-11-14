package com.springfield.delivery;

public class CarCourier extends Courier {

    public CarCourier(String name) {
        super(name);
    }

    @Override
    public String getTransportType() {
        return "Автомобиль";
    }
}