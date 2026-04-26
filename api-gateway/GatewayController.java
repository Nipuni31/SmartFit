@RestController
@RequestMapping("/api")
public class GatewayController {

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    @PostMapping("/predict")
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