package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.AddBranchRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.response.BranchResponse;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public Branch toBranchDomain(AddBranchRequest request, String franchiseId) {
        return Branch.builder()
                .name(request.nombre())
                .franchiseId(franchiseId)
                .build();
    }

    public BranchResponse toBranchResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getFranchiseId());
    }

    public String extractName(UpdateNameRequest request) {
        return request.nombre();
    }
}
