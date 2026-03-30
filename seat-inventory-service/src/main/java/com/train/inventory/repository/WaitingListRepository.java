package com.train.inventory.repository;

import com.train.inventory.entity.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {

    List<WaitingList> findByTrainIdOrderByPriorityNumberAsc(Long trainId);

    long countByTrainId(Long trainId);
}
