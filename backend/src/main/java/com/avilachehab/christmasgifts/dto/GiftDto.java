package com.avilachehab.christmasgifts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftDto {
    private Long id;
    private String description;
    private BigDecimal price;
    private Long personId;
    private String personName;
}

