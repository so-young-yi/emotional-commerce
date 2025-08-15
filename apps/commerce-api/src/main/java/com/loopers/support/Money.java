package com.loopers.support;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class Money implements Serializable {

    private long amount; // 1원 단위

    public Money(long amount) {
        if (amount < 0) throw new CoreException(ErrorType.BAD_REQUEST, "금액은 0 이상이어야 합니다.");
        this.amount = amount;
    }

    protected Money() {}

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }
    public Money subtract(Money other) {
        return new Money(this.amount - other.amount);
    }
    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount == money.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount + "원";
    }
}
//고민포인트
//국내 이커머스 환경에서는 소수점이 필요하지않다고해서 long을 권한던데,
//통화나, 복잡한 계산을 고려했을때는 소숫점이 필요하다고.. 이부분 고민
//머니를 상속해서 각자 객체를 만드는거는 관리포인트를 늘린닫고 공통적으로 말리고있음
