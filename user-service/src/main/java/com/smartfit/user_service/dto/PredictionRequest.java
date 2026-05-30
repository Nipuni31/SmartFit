package com.smartfit.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    private String topSize;
    private String bottomSize;
    private String fit;
    private Float confidence;
    private String height;
    private String frontImageUrl;
    private String sideImageUrl;
}
