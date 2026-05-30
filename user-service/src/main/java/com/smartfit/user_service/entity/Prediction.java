package com.smartfit.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {
    @Id
    private String id;
    private String userId;
    private String topSize;
    private String bottomSize;
    private String fit;
    private Float confidence;
    @CreatedDate
    private Instant createdAt;
}
