package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.brand.BrandV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;

    public List<BrandModel> getBrands(BrandV1Dto.BrandRequest request) {
        return brandService.getBrands(request);
    }

    public BrandModel getBrand(Long brandId) {
        return brandService.getBrand(brandId);
    }
}
