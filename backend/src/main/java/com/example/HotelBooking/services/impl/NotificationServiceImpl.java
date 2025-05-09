package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.entities.Notification;
import com.example.HotelBooking.enums.NotificationType;
import com.example.HotelBooking.repositories.NotificationRepository;
import com.example.HotelBooking.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * ClassName:NotificationServiceImpl
 * Package:com.example.HotelBooking.services.impl
 * Description:
 * 结构清晰：职责分离良好，邮件发送和通知保存都有明确逻辑。
 *
 * 异步发送：@Async 注解避免阻塞主线程，是发送通知的良好实践。
 *
 * 使用 Lombok 简化代码：
 *
 * @RequiredArgsConstructor 自动生成构造器注入依赖。
 *
 * @Slf4j 提供日志记录器
 *
 * @date:2025-04-25 6:59 p.m.
 * @author:Qss
 */

/**
 * 接受通知
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;

    private final NotificationRepository notificationRepository;


    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO) {

        log.info("Inside send mail");

//创建一个简单邮件对象 SimpleMailMessage。
//
//设置收件人、主题、正文内容

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(notificationDTO.getRecipient());
        simpleMailMessage.setSubject(notificationDTO.getSubject());
        simpleMailMessage.setText(notificationDTO.getBody());

//        调用 Spring 的 JavaMailSender 发出邮件。
        javaMailSender.send(simpleMailMessage);

//        构建一个 Notification 实体对象（用于保存到数据库），包含收件人、主题、正文、预订编号、通知类型等信息。
        Notification notificationToSave = Notification.builder()
                .recipient(notificationDTO.getRecipient())
                .subject(notificationDTO.getSubject())
                .body(notificationDTO.getBody())
                .bookingReference(notificationDTO.getBookingReference())
                .type(NotificationType.EMAIL)
                .build();

        notificationRepository.save(notificationToSave);

    }

    @Override
    public void sendSms(NotificationDTO notificationDTO) {

    }

    @Override
    public void sendWhatsapp(NotificationDTO notificationDTO) {

    }
}
