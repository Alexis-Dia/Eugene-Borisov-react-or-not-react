package ru.spring.demo.reactive.pechkin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.spring.demo.reactive.pechkin.producer.LetterProducer;
import ru.spring.demo.reactive.starter.speed.AdjustmentProperties;
import ru.spring.demo.reactive.starter.speed.model.Letter;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Service
public class LetterDistributor {
    private final LetterSender         sender;
    private final AdjustmentProperties adjustmentProperties;
    private final WebClient.Builder webClientBuilder;
    private final LetterProducer       producer;
    private final Counter              counter;
    private final ThreadPoolExecutor   letterProcessorExecutor;
    private final ObjectMapper         objectMapper;

    public LetterDistributor(
            LetterSender sender,
            AdjustmentProperties adjustmentProperties,
            WebClient.Builder webClientBuilder, LetterProducer producer,
            MeterRegistry meterRegistry,
            ThreadPoolExecutor letterProcessorExecutor,
            ObjectMapper objectMapper) {
        this.sender = sender;
        this.adjustmentProperties = adjustmentProperties;
        this.webClientBuilder = webClientBuilder;
        this.producer = producer;
        this.counter = meterRegistry.counter("letter.rps");
        this.letterProcessorExecutor = letterProcessorExecutor;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        while (true) {
            distribute();
            counter.increment();
        }
    }

    @SneakyThrows
    public void distribute() {
        webClientBuilder.baseUrl("http://localhost:8081/").build()
                .post().uri("/analyse/letter")
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .body(
                        producer.letterFlux().log(),
                        Letter.class)
                .exchange()
                .doOnError(throwable -> log.error("Sth went wrong {}", throwable.getMessage()))
                .retryBackoff(Long.MAX_VALUE, Duration.ofMillis(500))
                .log()
                .subscribe(clientResponse -> log.info("clientResponse = ", clientResponse));
    }
}
