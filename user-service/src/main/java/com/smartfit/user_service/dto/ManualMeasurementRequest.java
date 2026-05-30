package com.smartfit.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualMeasurementRequest {
    private Float height;
    private Float shoulder;
    private Float waist;
    private Float hip;
    private Float chestDepth;
    private Float hipDepth;
}
