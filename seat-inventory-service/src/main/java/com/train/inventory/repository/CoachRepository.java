package com.train.inventory.repository;

import com.train.inventory.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    List<Coach> findByTrainId(Long trainId);
}

