package de.qaware.mercury.business.time.impl;

import de.qaware.mercury.business.time.Clock;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
public class WallClock implements Clock {
    @Override
    public ZonedDateTime nowZoned() {
        return ZonedDateTime.now();
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
