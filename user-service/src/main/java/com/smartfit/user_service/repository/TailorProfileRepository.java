package com.smartfit.user_service.repository;

import com.smartfit.user_service.entity.TailorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TailorProfileRepository extends MongoRepository<TailorProfile, String> {
    Optional<TailorProfile> findByUserId(String userId);
}
