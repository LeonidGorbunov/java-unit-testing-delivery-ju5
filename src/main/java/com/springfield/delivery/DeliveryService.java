package com.springfield.delivery;

import java.util.List;

public class DeliveryService {

    private final DistanceCalculator distanceCalculator;
    private final CourierRepository courierRepository;
    private final NotificationService notificationService;
    private final DiscountPolicy discountPolicy;

    public DeliveryService(DistanceCalculator distanceCalculator, CourierRepository courierRepository,
                           NotificationService notificationService, DiscountPolicy discountPolicy) {
        this.distanceCalculator = distanceCalculator;
        this.courierRepository = courierRepository;
        this.notificationService = notificationService;
        this.discountPolicy = discountPolicy;
    }

    public double calculatePrice(String from, String to) throws IllegalArgumentException {
        double distance = distanceCalculator.calculateDistanceInKm(from, to);
        if (distance < 0) {
            throw new IllegalArgumentException("Расстояние не может быть меньше 0!");
        }
        double basePrice = distance * 50.0 + 150.0;
        return discountPolicy.applyDiscount(basePrice);
    }

    public String processDelivery(String orderId, String clientPhone, String from, String to) throws IllegalStateException, IllegalArgumentException {
        double totalPrice = calculatePrice(from, to);
        List<Courier> availableCouriers = courierRepository.findAvailableCouriers();

        if (availableCouriers.isEmpty()) {
            throw new IllegalStateException("Нет свободных курьеров!");
        }

        Courier chosenCourier = availableCouriers.get(0);
        courierRepository.assignOrderToCourier(chosenCourier, orderId);

        if ("Пешком".equals(chosenCourier.getTransportType())) {
            notificationService.sendSms(clientPhone, "К вам выехал курьер " + chosenCourier.getName()
                    + ". Общая стоимость, руб: " + totalPrice);
        } else {
            notificationService.sendSms(clientPhone, "К вам выехал курьер " +
                    chosenCourier.getName() + " на машине. Доставим молниеносно!" + " Общая стоимость, руб: " + totalPrice);
        }
        return chosenCourier.getName();
    }
}