package com.learn2code.kafka.streams.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {
    @JsonProperty("bookId")
    private Integer bookId;
    @JsonProperty("bookName")
    private String bookName;
    @JsonProperty("bookAuthor")
    private String bookAuthor;
    @JsonProperty("isGood")
    private boolean isGood;
}
