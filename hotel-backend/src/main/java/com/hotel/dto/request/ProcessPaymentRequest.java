package com.hotel.dto.request;

import com.hotel.entity.Payment.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProcessPaymentRequest(
    @NotNull Long reservationId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull PaymentMethod paymentMethod,
    String transactionId,
    String notes
) {}
