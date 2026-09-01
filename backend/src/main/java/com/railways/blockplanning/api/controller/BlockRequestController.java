package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.domain.BlockRequest;
import com.railways.blockplanning.repository.BlockRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/block-requests")
@RequiredArgsConstructor
@Tag(name = "Block Requests", description = "BDMS block request management")
@CrossOrigin(origins = "*")
public class BlockRequestController {

    private final BlockRequestRepository blockRequestRepository;

    @GetMapping
    @Operation(summary = "List all block requests")
    public ResponseEntity<ApiResponse<List<BlockRequest>>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long corridorId) {

        List<BlockRequest> requests = blockRequestRepository.findAll();

        if (status != null)
            requests = requests.stream()
                .filter(r -> r.getApprovalStatus().name().equalsIgnoreCase(status)).toList();
        if (department != null)
            requests = requests.stream()
                .filter(r -> r.getRequestingDepartment().equalsIgnoreCase(department)).toList();
        if (corridorId != null)
            requests = requests.stream()
                .filter(r -> r.getCorridor().getId().equals(corridorId)).toList();

        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get block request by ID")
    public ResponseEntity<ApiResponse<BlockRequest>> getById(@PathVariable Long id) {
        return blockRequestRepository.findById(id)
            .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a block request")
    public ResponseEntity<ApiResponse<BlockRequest>> approve(@PathVariable Long id) {
        return blockRequestRepository.findById(id).map(r -> {
            r.setApprovalStatus(com.railways.blockplanning.domain.ApprovalStatus.Approved);
            return ResponseEntity.ok(ApiResponse.ok(blockRequestRepository.save(r)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a block request")
    public ResponseEntity<ApiResponse<BlockRequest>> reject(@PathVariable Long id) {
        return blockRequestRepository.findById(id).map(r -> {
            r.setApprovalStatus(com.railways.blockplanning.domain.ApprovalStatus.Rejected);
            return ResponseEntity.ok(ApiResponse.ok(blockRequestRepository.save(r)));
        }).orElse(ResponseEntity.notFound().build());
    }
}
