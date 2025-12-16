package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.GiftDto;
import com.avilachehab.christmasgifts.service.GiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {
    
    private final GiftService giftService;
    
    @GetMapping
    public ResponseEntity<List<GiftDto>> getAllGifts() {
        return ResponseEntity.ok(giftService.getAllGifts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GiftDto> getGiftById(@PathVariable Long id) {
        return ResponseEntity.ok(giftService.getGiftById(id));
    }
    
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<GiftDto>> getGiftsByPersonId(@PathVariable Long personId) {
        return ResponseEntity.ok(giftService.getGiftsByPersonId(personId));
    }
    
    @PostMapping
    public ResponseEntity<GiftDto> createGift(@Valid @RequestBody GiftDto giftDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(giftService.createGift(giftDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GiftDto> updateGift(@PathVariable Long id, 
                                               @Valid @RequestBody GiftDto giftDto) {
        return ResponseEntity.ok(giftService.updateGift(id, giftDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGift(@PathVariable Long id) {
        giftService.deleteGift(id);
        return ResponseEntity.noContent().build();
    }
}

