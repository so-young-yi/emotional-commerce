package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand")
@Getter
@NoArgsConstructor
public class BrandModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    public BrandModel(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 필수 입력값입니다.");
        }
        if (name.length() > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드명은 100자 이내여야 합니다.");
        }
        if (description != null && description.length() > 1000) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 설명은 1000자 이내여야 합니다.");
        }
        this.name = name;
        this.description = description;
    }
}
