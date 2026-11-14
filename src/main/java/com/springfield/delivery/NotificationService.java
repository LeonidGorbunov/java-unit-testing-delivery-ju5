package com.springfield.delivery;

public class NotificationService {

    public void sendSms(String phone, String text) {
        System.out.println("Отправлено сообщение '" + text + "' на номер " + phone);
    }
}