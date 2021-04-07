package ru.spring.demo.reactive.starter.speed.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecodedLetter {
    private String author;
    private String content;
    private String location;

}
