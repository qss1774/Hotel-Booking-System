package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.NotificationDTO;

/**
 * ClassName:NotificationService
 * Package:com.example.HotelBooking.services
 * Description:
 *
 * @date:2025-04-25 6:57 p.m.
 * @author:Qss
 */


public interface NotificationService {

    void sendEmail(NotificationDTO notificationDTO);

    void sendSms(NotificationDTO notificationDTO);

    void sendWhatsapp(NotificationDTO notificationDTO);
}
