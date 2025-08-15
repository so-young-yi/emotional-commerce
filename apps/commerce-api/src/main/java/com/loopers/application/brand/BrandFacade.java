package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.brand.BrandV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;

    // 브랜드 목록 조회
    public List<BrandInfo> getBrands(BrandV1Dto.BrandRequest request) {
        List<BrandModel> brands = brandService.getBrands(request);
        return brands.stream()
                .map(BrandInfo::from)
                .collect(Collectors.toList());
    }


    // 브랜드 단건 조회
    public BrandInfo getBrand(Long brandId) {
        BrandModel brand = brandService.getBrand(brandId);
        if (brand == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "[id = " + brandId + "] 브랜드를 찾을 수 없습니다.");
        }
        return BrandInfo.from(brand);
    }
}
