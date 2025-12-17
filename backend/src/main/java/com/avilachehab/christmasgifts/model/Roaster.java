package com.avilachehab.christmasgifts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roasters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Roaster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Roaster name is required")
    @Column(nullable = false)
    private String name;
    
    @Column
    private String location;
    
    @Column
    private String website;
    
    @Column(length = 1000)
    private String notes;
    
    @OneToMany(mappedBy = "roaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coffee> coffees = new ArrayList<>();
}

