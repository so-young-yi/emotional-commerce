package com.loopers.interfaces.api.payment;


import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.infrastructure.external.pg.PgClientDto;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentV1ApiController implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    @GetMapping("/{paymentId}")
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> getPayment(@PathVariable Long paymentId) {
        PaymentInfo info = paymentFacade.getPayment(paymentId);
        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(info));
    }

    @GetMapping("/order/{orderId}")
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentInfo info = paymentFacade.getPaymentByOrderId(orderId);
        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(info));
    }

    @PostMapping("/{paymentId}/refund")
    @Override
    public ApiResponse<Void> refund(@PathVariable Long paymentId) {
        paymentFacade.refund(paymentId);
        return ApiResponse.success(null);
    }

    /**
     * PG 결제 결과 콜백(Webhook) 엔드포인트
     */
    @PostMapping("/callback")
    public ApiResponse<Void> handlePgCallback(@RequestBody PgClientDto.PgResponse pgResponse) {
        // 결제 결과를 PaymentFacade에 전달하여 상태 갱신
        paymentFacade.handlePgCallback(pgResponse);
        return ApiResponse.success(null);
    }

}
