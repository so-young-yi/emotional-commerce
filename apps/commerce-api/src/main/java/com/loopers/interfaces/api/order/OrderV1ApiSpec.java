package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order V1 Api", description = "주문 api")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문", description = "상품을 주문합니다.")
    @PostMapping
    public ApiResponse<OrderV1Dto.OrderResponse> order(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody OrderV1Dto.OrderRequest request
    );

    @Operation(summary = "주문 목록", description = "주문 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") Long userId
    );

    @Operation(summary = "주문 상세", description = "주문 단건을 상세조회합니다.")
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long orderId
    );
}
