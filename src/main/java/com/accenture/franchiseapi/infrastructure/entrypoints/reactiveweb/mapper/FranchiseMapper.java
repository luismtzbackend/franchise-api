package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.CreateFranchiseRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.response.FranchiseResponse;
import org.springframework.stereotype.Component;

@Component
public class FranchiseMapper {

    public Franchise toFranchiseDomain(CreateFranchiseRequest request) {
        return Franchise.builder()
                .name(request.nombre())
                .build();
    }

    public FranchiseResponse toFranchiseResponse(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }

    public String extractName(UpdateNameRequest request) {
        return request.nombre();
    }
}
