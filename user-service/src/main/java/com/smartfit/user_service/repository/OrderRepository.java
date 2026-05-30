package com.smartfit.user_service.repository;

import com.smartfit.user_service.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByTailorIdOrderByCreatedAtDesc(String tailorId);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
