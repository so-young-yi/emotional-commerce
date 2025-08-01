package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "orders_item")
@Getter
public class OrderItemModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private Long quantity;
    @Column(nullable = false)
    private Long priceSnapshot;
    private String productNameSnapshot;

    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn(name = "order_id")
    private OrderModel order;

    protected OrderItemModel() {}

    public OrderItemModel(
            Long productId,
            Long quantity,
            Long priceSnapshot,
            String productNameSnapshot
    ) {
        if (productId == null || productId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수이며 1 이상이어야 합니다.");
        if (quantity == null || quantity < 1)
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다.");
        if (priceSnapshot == null || priceSnapshot < 1)
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격 스냅샷은 1원 이상이어야 합니다.");
        if (productNameSnapshot == null || productNameSnapshot.isBlank())
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명 스냅샷은 필수 입력값입니다.");

        this.productId = productId;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
        this.productNameSnapshot = productNameSnapshot;
    }

    protected void setOrder(OrderModel order) {
        this.order = order;
    }

}
