package hk.edu.polyu.comp.comp2021.crobs.model;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Test clock whose current instant can be moved, so that "past" and "future"
 * bookings can be produced without relying on booking in the past.
 */
class MutableClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    MutableClock(LocalDate date, LocalTime time) {
        this(LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    private MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    void setDateTime(LocalDate date, LocalTime time) {
        this.instant = LocalDateTime.of(date, time).atZone(zone).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new MutableClock(instant, newZone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
