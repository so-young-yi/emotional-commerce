package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.dto.PgType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Table(name = "payment")
@Entity
@Getter
public class PaymentModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long amount;

    private ZonedDateTime paidAt;

    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Setter
    @Column(name = "transaction_key")
    private String transactionKey;

    @Setter
    @Column(name = "reason")
    private String reason;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "pg_type")
    private PgType pgType;

    protected PaymentModel() {}

    public PaymentModel(Long orderId, Long amount, ZonedDateTime paidAt, PaymentStatus status) {
        if (orderId == null || orderId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST,"주문 ID는 필수이며 1 이상이어야 합니다.");
        if (amount == null || amount < 1)
            throw new CoreException(ErrorType.BAD_REQUEST,"결제 금액은 1원 이상이어야 합니다.");
        if (paidAt == null)
            throw new CoreException(ErrorType.BAD_REQUEST,"결제 일시는 필수입니다.");
        if (status == null)
            throw new CoreException(ErrorType.BAD_REQUEST,"결제 상태는 필수입니다.");
        this.orderId = orderId;
        this.amount = amount;
        this.paidAt = paidAt;
        this.status = status;
    }

    public void apply(PaymentResult result) {
        this.status = result.status();
        this.reason = result.reason();
        this.transactionKey = result.transactionKey();
    }

    public void pay() {
        this.status = PaymentStatus.PAID;
        this.paidAt = ZonedDateTime.now();
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}

