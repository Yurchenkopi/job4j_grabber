package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(parse, FORMATTER);
    }
}