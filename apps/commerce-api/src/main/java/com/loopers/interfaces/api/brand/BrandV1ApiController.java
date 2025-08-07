package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1ApiController implements BrandV1ApiSpec {

    private final BrandFacade brandFacade;

    @GetMapping
    public ApiResponse<List<BrandInfo>> getBrands(@ModelAttribute BrandV1Dto.BrandRequest request) {
        return ApiResponse.success(brandFacade.getBrands(request).stream()
                .map(BrandInfo::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{brandId}")
    public ApiResponse<BrandInfo> getBrand(@PathVariable Long brandId) {
        return ApiResponse.success(BrandInfo.from(brandFacade.getBrand(brandId)));

    }
}
