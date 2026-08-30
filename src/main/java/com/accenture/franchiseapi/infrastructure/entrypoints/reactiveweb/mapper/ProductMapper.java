package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.AddProductRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProductDomain(AddProductRequest request, String branchId) {
        return Product.builder()
                .name(request.nombre())
                .branchId(branchId)
                .stock(request.stock())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getBranchId(), product.getStock().value());
    }

    public String extractName(UpdateNameRequest request) {
        return request.nombre();
    }
}

