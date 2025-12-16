package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.GiftDto;
import com.avilachehab.christmasgifts.dto.PersonDto;
import com.avilachehab.christmasgifts.model.Gift;
import com.avilachehab.christmasgifts.model.Person;
import com.avilachehab.christmasgifts.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {
    
    private final PersonRepository personRepository;
    
    public List<PersonDto> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PersonDto getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        return convertToDto(person);
    }
    
    @Transactional
    public PersonDto createPerson(PersonDto personDto) {
        Person person = new Person();
        person.setName(personDto.getName());
        Person saved = personRepository.save(person);
        return convertToDto(saved);
    }
    
    @Transactional
    public PersonDto updatePerson(Long id, PersonDto personDto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        person.setName(personDto.getName());
        Person saved = personRepository.save(person);
        return convertToDto(saved);
    }
    
    @Transactional
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new RuntimeException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }
    
    private PersonDto convertToDto(Person person) {
        PersonDto dto = new PersonDto();
        dto.setId(person.getId());
        dto.setName(person.getName());
        
        BigDecimal totalSpent = person.getGifts().stream()
                .map(Gift::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSpent(totalSpent);
        
        List<GiftDto> giftDtos = person.getGifts().stream()
                .map(gift -> {
                    GiftDto giftDto = new GiftDto();
                    giftDto.setId(gift.getId());
                    giftDto.setDescription(gift.getDescription());
                    giftDto.setPrice(gift.getPrice());
                    giftDto.setPersonId(person.getId());
                    giftDto.setPersonName(person.getName());
                    return giftDto;
                })
                .collect(Collectors.toList());
        dto.setGifts(giftDtos);
        
        return dto;
    }
}

