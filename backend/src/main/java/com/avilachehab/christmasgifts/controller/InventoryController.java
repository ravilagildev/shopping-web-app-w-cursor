package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.InventorySummaryDto;
import com.avilachehab.christmasgifts.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping("/summary")
    public ResponseEntity<InventorySummaryDto> getInventorySummary() {
        return ResponseEntity.ok(inventoryService.getInventorySummary());
    }
}

