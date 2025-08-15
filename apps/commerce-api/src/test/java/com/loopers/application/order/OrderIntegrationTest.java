package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderIntegrationTest {

    @Autowired private OrderRepository orderRepository;

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @AfterEach void tearDown() { databaseCleanUp.truncateAllTables(); }

    @Nested
    @DisplayName("주문 생성/조회 통합 테스트")
    class CreateAndFindOrder {

        @Test
        @DisplayName("주문을 생성하면 DB에 저장되고, 조회할 수 있다")
        void createOrder_shouldSaveAndFind() {
            // arrange
            OrderModel order = new OrderModel(1L, OrderStatus.ORDERED);
            OrderItemModel item1 = new OrderItemModel(10L, 2L, 10000L, "에어맥스");
            OrderItemModel item2 = new OrderItemModel(20L, 1L, 12000L, "에어포스");
            order.addOrderItem(item1);
            order.addOrderItem(item2);

            // act
            OrderModel saved = orderRepository.save(order);

            // assert
            OrderModel found = orderRepository.findById(saved.getId()).orElse(null);
            assertThat(found).isNotNull();
            assertThat(found.getUserId()).isEqualTo(1L);
            assertThat(found.getOrderItems()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("주문 상태/취소 통합 테스트")
    class OrderStatusAndCancel {

        @Test
        @DisplayName("주문을 취소하면 상태가 CANCELLED로 변경된다")
        void cancelOrder_shouldSetStatusToCancelled() {
            // arrange
            OrderModel order = new OrderModel(1L, OrderStatus.ORDERED);
            orderRepository.save(order);

            // act
            order.cancel();
            orderRepository.save(order);

            // assert
            OrderModel found = orderRepository.findById(order.getId()).orElse(null);
            assertThat(found.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회 통합 테스트")
    class FindOrderList {

        @Test
        @DisplayName("유저별 주문 목록을 조회할 수 있다")
        void findOrdersByUserId_shouldReturnOrderList() {
            // arrange
            Long userId = 1L;
            OrderModel order1 = new OrderModel(userId, OrderStatus.ORDERED);
            OrderModel order2 = new OrderModel(userId, OrderStatus.PAID);
            orderRepository.save(order1);
            orderRepository.save(order2);

            // act
            List<OrderModel> orders = orderRepository.findByUserId(userId);

            // assert
            assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
