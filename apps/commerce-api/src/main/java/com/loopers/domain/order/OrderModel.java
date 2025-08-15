package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Table(name = "orders")
@Entity
@Getter
public class OrderModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemModel> orderItems = new ArrayList<>();

    protected OrderModel() {}

    public OrderModel(
            Long userId,
            OrderStatus status
    ) {

        if (userId == null || userId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "주문자 ID는 필수이며 1 이상이어야 합니다.");
        if (status == null)
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태는 필수 입력값입니다.");

        this.userId = userId;
        this.status = status;
    }

    public void addOrderItem(OrderItemModel orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this); // 자식에도 부모 세팅
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
