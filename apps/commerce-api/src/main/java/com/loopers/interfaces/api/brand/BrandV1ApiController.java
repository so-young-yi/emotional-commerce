package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1ApiController implements BrandV1ApiSpec {

    private final BrandFacade brandFacade;


    @GetMapping
    @Override
    public ApiResponse<BrandV1Dto.BrandListPageResponse> getBrands( BrandV1Dto.BrandRequest request ) {
        List<BrandInfo> infos = brandFacade.getBrands(request);
        List<BrandV1Dto.BrandSummaryResponse> items = infos.stream()
                .map(info -> new BrandV1Dto.BrandSummaryResponse(
                        info.id(),
                        info.name(),
                        info.description()
                ))
                .toList();

        BrandV1Dto.BrandListPageResponse response = new BrandV1Dto.BrandListPageResponse(
                items,
                items.size(),
                1,
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 20
        );
        return ApiResponse.success(response);

    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.BrandDetailResponse> getBrand(@PathVariable Long brandId) {
        BrandInfo info = brandFacade.getBrand(brandId);
        BrandV1Dto.BrandDetailResponse response = new BrandV1Dto.BrandDetailResponse(
                info.id(),
                info.name(),
                info.description()
        );
        return ApiResponse.success(response);
    }
}
