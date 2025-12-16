package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.GiftDto;
import com.avilachehab.christmasgifts.model.Gift;
import com.avilachehab.christmasgifts.model.Person;
import com.avilachehab.christmasgifts.repository.GiftRepository;
import com.avilachehab.christmasgifts.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftService {
    
    private final GiftRepository giftRepository;
    private final PersonRepository personRepository;
    
    public List<GiftDto> getAllGifts() {
        return giftRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public GiftDto getGiftById(Long id) {
        Gift gift = giftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gift not found with id: " + id));
        return convertToDto(gift);
    }
    
    public List<GiftDto> getGiftsByPersonId(Long personId) {
        return giftRepository.findByPersonId(personId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public GiftDto createGift(GiftDto giftDto) {
        Person person = personRepository.findById(giftDto.getPersonId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + giftDto.getPersonId()));
        
        Gift gift = new Gift();
        gift.setDescription(giftDto.getDescription());
        gift.setPrice(giftDto.getPrice());
        gift.setPerson(person);
        
        Gift saved = giftRepository.save(gift);
        return convertToDto(saved);
    }
    
    @Transactional
    public GiftDto updateGift(Long id, GiftDto giftDto) {
        Gift gift = giftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gift not found with id: " + id));
        
        gift.setDescription(giftDto.getDescription());
        gift.setPrice(giftDto.getPrice());
        
        if (!gift.getPerson().getId().equals(giftDto.getPersonId())) {
            Person person = personRepository.findById(giftDto.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Person not found with id: " + giftDto.getPersonId()));
            gift.setPerson(person);
        }
        
        Gift saved = giftRepository.save(gift);
        return convertToDto(saved);
    }
    
    @Transactional
    public void deleteGift(Long id) {
        if (!giftRepository.existsById(id)) {
            throw new RuntimeException("Gift not found with id: " + id);
        }
        giftRepository.deleteById(id);
    }
    
    private GiftDto convertToDto(Gift gift) {
        GiftDto dto = new GiftDto();
        dto.setId(gift.getId());
        dto.setDescription(gift.getDescription());
        dto.setPrice(gift.getPrice());
        dto.setPersonId(gift.getPerson().getId());
        dto.setPersonName(gift.getPerson().getName());
        return dto;
    }
}

