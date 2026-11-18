package com.springfield.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    private DeliveryService deliveryService;

    @Mock
    private DistanceCalculator distanceCalculatorMock;
    @Mock
    private CourierRepository courierRepositoryMock;
    @Mock
    private DiscountPolicy discountPolicyMock;

    @Spy
    private NotificationService notificationServiceSpy;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(distanceCalculatorMock, courierRepositoryMock,
                notificationServiceSpy, discountPolicyMock);
    }

    @Test
    void shouldCalculatePriceThrowExceptionWhenCalculateDistanceInKmReturnNegative() {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(-1.0);

        assertThrows(IllegalArgumentException.class, () -> deliveryService.calculatePrice("Пункт А", "Пункт Б"),
                "Метод должен выкинуть ошибку IllegalArgumentException!");
    }

    @ParameterizedTest
    @CsvSource({
            "Нью-Йорк, Пэлам, 31.0, 1700.00, 1615.00",
            "Салоники, Катерини, 55.0, 2900.00, 2755.00",
            "Вена, Братислава, 65.0, 3400.00, 3230.00",
            "Вена, Вена, 0.0, 150.00, 142.50"
    })
    void shouldCalculatePriceReturnPrice(String from, String to, double calculatedDistance, double calculatedBasePrice, double expectedPrice) {
        when(distanceCalculatorMock.calculateDistanceInKm(from, to)).thenReturn(calculatedDistance);
        when(discountPolicyMock.applyDiscount(calculatedBasePrice)).thenReturn(expectedPrice);
        double actualPrice = deliveryService.calculatePrice(from, to);

        assertEquals(expectedPrice, actualPrice, 0.01,
                "Метод должен возвращать значение цены, руб: " + expectedPrice);
    }

    @Test
    void shouldProcessDeliveryThrowExceptionWhenAvailableCouriersIsEmpty() {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(500.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> deliveryService.processDelivery("Test", "88888888888",
                "Пункт А", "Пункт Б"), "Метод должен выкинуть ошибку IllegalStateException!");
    }

    private static Stream<Arguments> provideDataForProcessDelivery() {
        return Stream.of(
                arguments("Test1_Courier", "Test1_PhoneNumber", "Пункт1 А", "Пункт1 Б", new Courier("Барт"),
                        "К вам выехал курьер Барт. Общая стоимость, руб: 400.0"),
                arguments("Test2_CarCourier", "Test2_PhoneNumber", "Пункт2 А", "Пункт2 Б", new CarCourier("Гомер"),
                        "К вам выехал курьер Гомер на машине. Доставим молниеносно! Общая стоимость, руб: 400.0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForProcessDelivery")
    void shouldProcessDeliverySendNotificationServiceSms(String orderId, String clientPhone, String from,
                                                                String to, Courier courier, String expectedText) {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(400.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of(courier));
        doReturn(true).when(notificationServiceSpy).isSmsGatewayAvailable();

        deliveryService.processDelivery(orderId, clientPhone, from, to);

        verify(notificationServiceSpy, description("Метод должен отправить смс!")).sendSms(clientPhone, expectedText);
    }

    @ParameterizedTest
    @MethodSource("provideDataForProcessDelivery")
    void shouldProcessDeliveryReturnCourierName(String orderId, String clientPhone,
                                                       String from, String to, Courier courier, String expectedText) {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(400.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of(courier));
        doReturn(true).when(notificationServiceSpy).isSmsGatewayAvailable();

        String expectedCourierName = courier.getName();
        String actualCourierName = deliveryService.processDelivery(orderId, clientPhone, from, to);

        assertEquals(expectedCourierName, actualCourierName, "Метод должен возвращать имя курьера: " + expectedCourierName);
    }

    @ParameterizedTest
    @MethodSource("provideDataForProcessDelivery")
    void shouldReturnCourierNameWhenSmsGatewayUnavailable(String orderId, String clientPhone,
                                                          String from, String to, Courier courier, String expectedText) {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(400.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of(courier));
        doReturn(false).when(notificationServiceSpy).isSmsGatewayAvailable();

        String expectedCourierName = courier.getName();
        String actualCourierName = deliveryService.processDelivery(orderId, clientPhone, from, to);

        assertEquals(expectedCourierName, actualCourierName, "Метод должен возвращать имя курьера: " + expectedCourierName);
    }

    @ParameterizedTest
    @MethodSource("provideDataForProcessDelivery")
    void shouldProcessDeliveryAssignOrderToCourier(String orderId, String clientPhone,
                                                String from, String to, Courier courier, String expectedText) {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(400.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of(courier));
        doReturn(true).when(notificationServiceSpy).isSmsGatewayAvailable();

        deliveryService.processDelivery(orderId, clientPhone, from, to);

        verify(courierRepositoryMock, description("Метод должен назначить заказ " + orderId + " курьеру " + courier.getName()))
                .assignOrderToCourier(courier, orderId);
    }

    @Test
    void shouldSkipSmsSendingWhenGatewayUnavailable() {
        when(distanceCalculatorMock.calculateDistanceInKm(anyString(), anyString())).thenReturn(5.0);
        when(discountPolicyMock.applyDiscount(anyDouble())).thenReturn(500.0);
        when(courierRepositoryMock.findAvailableCouriers()).thenReturn(List.of(new Courier("Барт")));
        doReturn(false).when(notificationServiceSpy).isSmsGatewayAvailable();

        deliveryService.processDelivery("Test", "88888888888", "Пункт А", "Пункт Б");

        verify(notificationServiceSpy, never().description("При недоступном SMS-шлюзе отправка SMS не должна вызываться!"))
                .sendSms(anyString(), anyString());
    }
}