package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        final DateTimeFormatter TIMESTAMP_PARSER = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ"))
                .toFormatter();
        return LocalDateTime.parse(parse, TIMESTAMP_PARSER);
    }
}