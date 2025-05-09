package com.example.HotelBooking.payments.stripe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ClassName:PaymentRequest
 * Package:com.example.HotelBooking.payments.stripe.dto
 * Description:
 *
 * @date:2025-04-29 1:29 a.m.
 * @author:Qss
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {

    @NotBlank(message = "Booking reference is required")
    private String bookingReference;

    private BigDecimal amount;

    private String transactionId;

    private boolean success;

    private String failureReason;
}
