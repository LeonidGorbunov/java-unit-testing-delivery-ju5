package com.springfield.delivery;

import java.util.List;

public class CourierRepository {

    public List<Courier> findAvailableCouriers() {
        return List.of();
    }

    public void assignOrderToCourier(Courier courier, String orderId) {
        System.out.println("Курьер " + courier.getName() + " на " + courier.getTransportType() + " взял заказ " + orderId);
    }
}