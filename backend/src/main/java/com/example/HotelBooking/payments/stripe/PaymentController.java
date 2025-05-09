package com.example.HotelBooking.payments.stripe;

import com.example.HotelBooking.payments.stripe.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName:PaymentController
 * Package:com.example.HotelBooking.payments.stripe.dto
 * Description:
 *
 * @date:2025-04-29 1:32 a.m.
 * @author:Qss
 */


@RequestMapping("/api/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(paymentRequest));
    }

    @PutMapping("/update")
    public void updatePaymentBooking(@RequestBody PaymentRequest paymentRequest) {
        paymentService.updatePaymentBooking(paymentRequest);
    }
}
