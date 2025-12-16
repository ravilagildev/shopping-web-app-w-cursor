package com.avilachehab.christmasgifts.repository;

import com.avilachehab.christmasgifts.model.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {
    List<Gift> findByPersonId(Long personId);
}

