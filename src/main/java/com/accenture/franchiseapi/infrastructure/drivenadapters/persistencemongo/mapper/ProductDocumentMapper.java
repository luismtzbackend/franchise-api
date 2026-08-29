package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.model.Stock;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductDocumentMapper {

    public ProductDocument toDocument(Product product) {
        return new ProductDocument(
            product.getId(),
            product.getName(),
            product.getBranchId(),
            product.getStock().value()
        );
    }

    public Product toDomain(ProductDocument document) {
        return Product.builder()
            .id(document.getId())
            .name(document.getName())
            .branchId(document.getBranchId())
            .stock(Stock.of(document.getStock()))
            .build();
    }
}
