package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> order(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody OrderV1Dto.OrderRequest request
    ) {
        return ApiResponse.success(orderFacade.orderAndPay(userId, request));
    }

    @GetMapping
    @Override
    public ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return ApiResponse.success(orderFacade.getOrders(userId));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.success(orderFacade.getOrder(userId, orderId));
    }



}
