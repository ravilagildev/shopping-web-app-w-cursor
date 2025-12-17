package com.avilachehab.christmasgifts.repository;

import com.avilachehab.christmasgifts.model.Roaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoasterRepository extends JpaRepository<Roaster, Long> {
}

