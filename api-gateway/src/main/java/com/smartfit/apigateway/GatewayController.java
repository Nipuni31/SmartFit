package com.smartfit.apigateway;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> predict(
            @RequestPart("front") MultipartFile front,
            @RequestPart("side") MultipartFile side,
            @RequestParam("height_cm") String height
    ) throws IOException {

        return webClient.post()
                .uri("/predict")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("front", front.getResource())
                        .with("side", side.getResource())
                        .with("height_cm", height))
                .retrieve()
                .bodyToMono(String.class);
    }
}
