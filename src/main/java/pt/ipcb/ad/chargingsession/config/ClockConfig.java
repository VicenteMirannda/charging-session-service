package pt.ipcb.ad.chargingsession.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Injecting a Clock (instead of calling LocalDateTime.now() directly) makes time-based logic testable. */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
