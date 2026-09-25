package com.tafhdev.split_bill_app.payment.repository;

import com.tafhdev.split_bill_app.payment.domain.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    List<Payment> findByGroupId(UUID groupId);
}
