package com.smartfit.user_service.repository;

import com.smartfit.user_service.entity.Measurement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MeasurementRepository extends MongoRepository<Measurement, String> {
    List<Measurement> findByUserIdOrderByCreatedAtDesc(String userId);
}
