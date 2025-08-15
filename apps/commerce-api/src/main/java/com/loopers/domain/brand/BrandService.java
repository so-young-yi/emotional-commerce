package com.loopers.domain.brand;

import com.loopers.interfaces.api.brand.BrandV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandModel getBrand(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드가 존재하지 않습니다."));
    }

    public List<BrandModel> getBrands(BrandV1Dto.BrandRequest request) {
        if (request != null && request.name() != null && !request.name().isBlank()) {
            return brandRepository.findByNameContaining(request.name());
        }
        return brandRepository.findAll();
    }


    public BrandModel createBrand(String name, String description) {
        BrandModel brand = new BrandModel(name, description);
        return brandRepository.save(brand);
    }
}
