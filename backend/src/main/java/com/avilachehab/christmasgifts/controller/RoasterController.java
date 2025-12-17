package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.RoasterDto;
import com.avilachehab.christmasgifts.service.RoasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roasters")
@RequiredArgsConstructor
public class RoasterController {
    
    private final RoasterService roasterService;
    
    @GetMapping
    public ResponseEntity<List<RoasterDto>> getAllRoasters() {
        return ResponseEntity.ok(roasterService.getAllRoasters());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoasterDto> getRoasterById(@PathVariable Long id) {
        return ResponseEntity.ok(roasterService.getRoasterById(id));
    }
    
    @PostMapping
    public ResponseEntity<RoasterDto> createRoaster(@Valid @RequestBody RoasterDto roasterDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roasterService.createRoaster(roasterDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RoasterDto> updateRoaster(@PathVariable Long id, 
                                                    @Valid @RequestBody RoasterDto roasterDto) {
        return ResponseEntity.ok(roasterService.updateRoaster(id, roasterDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoaster(@PathVariable Long id) {
        roasterService.deleteRoaster(id);
        return ResponseEntity.noContent().build();
    }
}

