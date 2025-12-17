package com.avilachehab.christmasgifts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoasterDto {
    private Long id;
    private String name;
    private String location;
    private String website;
    private String notes;
    private List<CoffeeDto> coffees = new ArrayList<>();
    private BigDecimal totalSpent;
    private Integer coffeeCount;
}

