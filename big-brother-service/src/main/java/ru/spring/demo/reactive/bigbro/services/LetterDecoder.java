package ru.spring.demo.reactive.bigbro.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spring.demo.reactive.starter.speed.AdjustmentProperties;
import ru.spring.demo.reactive.starter.speed.model.DecodedLetter;
import ru.spring.demo.reactive.starter.speed.model.Letter;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
@Slf4j
public class LetterDecoder {
    private final AdjustmentProperties adjustmentProperties;
    private final Counter              counter;

    public LetterDecoder(AdjustmentProperties adjustmentProperties,
                         MeterRegistry meterRegistry) {
        this.adjustmentProperties = adjustmentProperties;
        counter = meterRegistry.counter("letter.rps");
    }

    @SneakyThrows
    public DecodedLetter decode(Letter letter) {
        MILLISECONDS.sleep(
                adjustmentProperties.getProcessingTime()
        );

        counter.increment();

        return DecodedLetter.builder()
                .author(letter.secretMethodForDecodeSignature())
                .location(letter.getLocation())
                .content(letter.getContent())
                .build();

    }
}
