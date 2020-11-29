package com.example.spring.boot.kafka.kafkatest.models;

import java.time.Instant;
import java.time.LocalDateTime;

public class KafkaAdvancedModel {
    private String string;

    private Integer integer;

    private Instant instant;

    private LocalDateTime localDateTime;

    public String getString() {
        return this.string;
    }

    public Integer getInteger() {
        return this.integer;
    }

    public Instant getInstant() {
        return this.instant;
    }

    public LocalDateTime getLocalDateTime() {
        return this.localDateTime;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
}
