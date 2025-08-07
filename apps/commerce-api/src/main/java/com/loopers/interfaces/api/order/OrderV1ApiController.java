package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1ApiController implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> order(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody OrderV1Dto.OrderRequest request
    ) {
        OrderInfo orderInfo = orderFacade.orderAndPay(userId, request);
        return ApiResponse.success(OrderV1Dto.OrderResponse.of(orderInfo.order(), orderInfo.payment()));
    }

    @GetMapping
    @Override
    public ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        List<OrderInfo> results = orderFacade.getOrders(userId);
        List<OrderV1Dto.OrderResponse> responses = results.stream()
                .map(result -> OrderV1Dto.OrderResponse.of(result.order(), result.payment()))
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long orderId
    ) {
        OrderInfo orderInfo = orderFacade.getOrder(userId, orderId);
        return ApiResponse.success(OrderV1Dto.OrderResponse.of(orderInfo.order(), orderInfo.payment()));
    }
}
