package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Table(name = "product_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId","productId"})
})
@Entity
@Getter
public class ProductLikeModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long productId;

    protected ProductLikeModel() {}

    public ProductLikeModel( Long userId, Long productId ) {
        this.userId = userId;
        this.productId = productId;
    }

}
