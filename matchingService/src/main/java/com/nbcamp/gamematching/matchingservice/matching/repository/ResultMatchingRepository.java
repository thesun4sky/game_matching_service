package com.nbcamp.gamematching.matchingservice.matching.repository;

import com.nbcamp.gamematching.matchingservice.matching.entity.ResultMatching;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultMatchingRepository extends JpaRepository<ResultMatching,Long> {
}
