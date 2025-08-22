package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PgType;
import com.loopers.infrastructure.external.pg.PgClient;
import com.loopers.infrastructure.external.pg.PgClientDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("결제 통합 테스트")
class PaymentIntegrationTest {

    @Autowired
    PaymentFacade paymentFacade;

    @Autowired
    PaymentService paymentService;

    @MockitoBean
    PgClient pgClient;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("카드 결제 사이클")
    class CardPaymentCycle {

        final long orderId = 1351039135L;
        final long userId = 135135L;
        final long amount = 5000L;
        final String cardNo = "1234-5678-9814-1451";
        final CardType cardType = CardType.SAMSUNG;
        final PgType pgType = PgType.PG_SIMULATOR;

        @Test
        @DisplayName("성공: 결제 요청 후 PG 응답이 성공이면 결제 상태가 PAID가 된다")
        void 결제_성공_사이클() {
            // given: 결제 정보 생성
            paymentService.pay(orderId, amount);

            Payment.CardRequest request = new Payment.CardRequest(
                    orderId, userId, amount, cardNo, cardType, pgType
            );

            // PG 응답 Mock (성공)
            PgClientDto.PgResponse pgResponse = new PgClientDto.PgResponse(
                    "20250816:TR:9577c5", String.valueOf(orderId), "SUCCESS", null
            );
            when(pgClient.request(any()))
                    .thenReturn(ApiResponse.success(pgResponse));

            // when: 결제 요청
            Payment.CardResponse response = (Payment.CardResponse) paymentFacade.pay(request);

            // then: 결제 상태가 PAID로 저장됨
            PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertEquals("20250816:TR:9577c5", payment.getTransactionKey());
        }

        @Test
        @DisplayName("실패: 결제 요청 후 PG 응답이 실패면 결제 상태가 FAILED가 된다")
        void 결제_실패_사이클() {
            // given: 결제 정보 생성
            paymentService.pay(orderId, amount);

            Payment.CardRequest request = new Payment.CardRequest(
                    orderId, userId, amount, cardNo, cardType, pgType
            );

            // PG 응답 Mock (실패)
            PgClientDto.PgResponse pgResponse = new PgClientDto.PgResponse(
                    null, String.valueOf(orderId), "FAILED", "카드 한도 초과"
            );
            when(pgClient.request(any()))
                    .thenReturn(ApiResponse.success(pgResponse));

            // when: 결제 요청
            Payment.CardResponse response = (Payment.CardResponse) paymentFacade.pay(request);

            // then: 결제 상태가 FAILED로 저장됨
            PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
            assertEquals("카드 한도 초과", payment.getReason());
        }
    }
}
