package com.avilachehab.christmasgifts.repository;

import com.avilachehab.christmasgifts.model.Coffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
    List<Coffee> findByRoasterId(Long roasterId);
}

