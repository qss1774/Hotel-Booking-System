package com.example.HotelBooking.payments.stripe;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.PaymentEntity;
import com.example.HotelBooking.enums.NotificationType;
import com.example.HotelBooking.enums.PaymentGateway;
import com.example.HotelBooking.enums.PaymentStatus;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.payments.stripe.dto.PaymentRequest;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.PaymentRepository;
import com.example.HotelBooking.services.NotificationService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ClassName:PaymentService
 * Package:com.example.HotelBooking.payments.stripe
 * Description:
 *
 * @date:2025-04-29 1:34 a.m.
 * @author:Qss
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;

    private final PaymentRepository paymentRepository;

    private final NotificationService notificationService;

    @Value("${stripe.api.secret.key}")
    private String secreteKey;

    public String createPaymentIntent(PaymentRequest paymentRequest){

        log.info("Creating payment intent for booking");
        Stripe.apiKey = secreteKey;
        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.COMPLETED){
            throw new NotFoundException("Payment already made for this booking");

        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // amount cents
                    .setCurrency("usd")
                    .putMetadata("bookingReference", bookingReference)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return intent.getClientSecret();

        }catch (Exception e){
            throw new RuntimeException("Error creating payment intent");
        }
    }

    public void updatePaymentBooking(PaymentRequest paymentRequest){

        log.info("UpdatePaymentBooking inside");

        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentRequest.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentStatus(paymentRequest.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingReference(bookingReference);
        payment.setUser(booking.getUser());

        if (!paymentRequest.isSuccess()){
            payment.setFailureReason(paymentRequest.getFailureReason());
        }

        paymentRepository.save(payment); // save payment to database

//        create and send notification
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(booking.getUser().getEmail())
                .type(NotificationType.EMAIL)
                .bookingReference(bookingReference)
                .build();

        log.info("Sending notification to user");

        if (paymentRequest.isSuccess()){

            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking); // update the booking

            notificationDTO.setSubject("Booking Payment Successful");
            notificationDTO.setBody("Your payment for booking with reference: " + bookingReference + "is successful");
            notificationService.sendEmail(notificationDTO);

        }else {

            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking); // update the booking

            notificationDTO.setSubject("Booking Payment Failed");
            notificationDTO.setBody("Your payment for booking with reference: " + bookingReference
                    + "is failed with reason:" + paymentRequest.getFailureReason());
            notificationService.sendEmail(notificationDTO);

        }
    }
}
