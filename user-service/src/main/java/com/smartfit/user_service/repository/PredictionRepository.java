package com.smartfit.user_service.repository;

import com.smartfit.user_service.entity.Prediction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PredictionRepository extends MongoRepository<Prediction, String> {
    List<Prediction> findByUserIdOrderByCreatedAtDesc(String userId);
}
