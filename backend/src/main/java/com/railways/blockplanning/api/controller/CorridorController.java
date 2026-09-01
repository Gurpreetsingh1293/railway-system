package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.domain.Corridor;
import com.railways.blockplanning.repository.CorridorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/corridors")
@RequiredArgsConstructor
@Tag(name = "Corridors", description = "Railway corridor management")
@CrossOrigin(origins = "*")
public class CorridorController {

    private final CorridorRepository corridorRepository;

    @GetMapping
    @Operation(summary = "List all corridors")
    public ResponseEntity<ApiResponse<List<Corridor>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(corridorRepository.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get corridor by ID")
    public ResponseEntity<ApiResponse<Corridor>> getById(@PathVariable Long id) {
        return corridorRepository.findById(id)
            .map(c -> ResponseEntity.ok(ApiResponse.ok(c)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new corridor")
    public ResponseEntity<ApiResponse<Corridor>> create(@RequestBody Corridor corridor) {
        return ResponseEntity.ok(ApiResponse.ok(corridorRepository.save(corridor)));
    }
}
