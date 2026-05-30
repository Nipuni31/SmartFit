package com.smartfit.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "measurements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    @Id
    private String id;
    private String userId;
    private Float height;
    private Float shoulder;
    private Float waist;
    private Float hip;
    private Float chestDepth;
    private Float hipDepth;
    @CreatedDate
    private Instant createdAt;
}
