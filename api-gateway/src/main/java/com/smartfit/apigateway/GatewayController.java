package com.smartfit.apigateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final WebClient aiClient = WebClient.create("http://localhost:8000");
    private final WebClient userClient = WebClient.create("http://localhost:8081");

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> predict(
            @RequestPart("front") MultipartFile front,
            @RequestPart("side") MultipartFile side,
            @RequestParam("height_cm") String height
    ) throws IOException {

        return aiClient.post()
                .uri("/predict")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("front", front.getResource())
                        .with("side", side.getResource())
                        .with("height_cm", height))
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> register(@RequestBody Map<String, Object> body) {
        return userClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> login(@RequestBody Map<String, Object> body) {
        return userClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/auth/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> validateToken(@RequestBody Map<String, Object> body) {
        return userClient.post()
                .uri("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/buyer/profile")
    public Mono<String> buyerProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/buyer/profile")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/buyer/predictions")
    public Mono<String> buyerPredictions(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/buyer/predictions")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PutMapping(value = "/buyer/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> updateBuyerProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.put()
                .uri("/buyer/profile")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/buyer/view-tailors")
    public Mono<String> viewTailors(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/buyer/view-tailors")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/buyer/upload-image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> uploadImage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri("/buyer/upload-image")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/buyer/save-prediction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> savePrediction(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri("/buyer/save-prediction")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/profile")
    public Mono<String> tailorProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/tailor/profile")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PutMapping(value = "/tailor/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> updateTailorProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.put()
                .uri("/tailor/profile")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/customers")
    public Mono<String> tailorCustomers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/tailor/customers")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/customer/{customerId}/measurements")
    public Mono<String> customerMeasurements(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String customerId
    ) {
        return userClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tailor/customer/{customerId}/measurements").build(customerId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/tailor/customer/{customerId}/measurements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> addCustomerMeasurement(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String customerId,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri(uriBuilder -> uriBuilder.path("/tailor/customer/{customerId}/measurements").build(customerId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/customer/{customerId}/history")
    public Mono<String> customerHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String customerId
    ) {
        return userClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tailor/customer/{customerId}/history").build(customerId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/customer/{customerId}/predictions")
    public Mono<String> customerPredictions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String customerId
    ) {
        return userClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tailor/customer/{customerId}/predictions").build(customerId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/tailor/recommend/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> recommendCustomer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String customerId,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri(uriBuilder -> uriBuilder.path("/tailor/recommend/{customerId}").build(customerId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/tailor/order/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> createTailorOrder(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri("/tailor/order/create")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/orders")
    public Mono<String> tailorOrders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/tailor/orders")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/tailor/order/{orderId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> updateTailorOrderStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String orderId,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri(uriBuilder -> uriBuilder.path("/tailor/order/{orderId}/status").build(orderId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/all")
    public Mono<String> allTailors(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/tailor/all")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/tailor/search")
    public Mono<String> searchTailors(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam String specialization
    ) {
        return userClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tailor/search").queryParam("specialization", specialization).build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/users/all")
    public Mono<String> adminAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/admin/users/all")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/users/buyers")
    public Mono<String> adminBuyers(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/admin/users/buyers")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/users/tailors")
    public Mono<String> adminTailors(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/admin/users/tailors")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/user/{userId}")
    public Mono<String> adminUserDetails(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String userId
    ) {
        return userClient.get()
                .uri(uriBuilder -> uriBuilder.path("/admin/user/{userId}").build(userId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping("/admin/user/{userId}/deactivate")
    public Mono<String> adminDeactivateUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String userId
    ) {
        return userClient.post()
                .uri(uriBuilder -> uriBuilder.path("/admin/user/{userId}/deactivate").build(userId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping("/admin/user/{userId}/activate")
    public Mono<String> adminActivateUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable String userId
    ) {
        return userClient.post()
                .uri(uriBuilder -> uriBuilder.path("/admin/user/{userId}/activate").build(userId))
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/analytics/user-count")
    public Mono<String> adminUserCount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/admin/analytics/user-count")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/admin/analytics/dashboard")
    public Mono<String> adminDashboard(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return userClient.get()
                .uri("/admin/analytics/dashboard")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/admin/reports/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> generateReport(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        return userClient.post()
                .uri("/admin/reports/generate")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }
}
