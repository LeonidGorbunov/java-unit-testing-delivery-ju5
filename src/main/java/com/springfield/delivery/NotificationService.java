package com.springfield.delivery;

public class NotificationService {

    public boolean isSmsGatewayAvailable() {
        throw new UnsupportedOperationException("SMS-шлюз не подключен в тестовом окружении!");
    }

    public void sendSms(String phone, String text) {
        System.out.println("Отправлено сообщение '" + text + "' на номер " + phone);
    }
}