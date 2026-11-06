package pt.ipcb.ad.chargingsession.model;

import org.junit.jupiter.api.Test;
import pt.ipcb.ad.chargingsession.exception.InvalidSessionStateException;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargingSessionTest {

    private static final LocalDateTime START = LocalDateTime.of(2025, 1, 10, 10, 0);
    private static final ChargingPlan PLAN = new ChargingPlan(50.0, 36.0, Duration.ofMinutes(43));

    @Test
    void newSessionIsInProgress() {
        ChargingSession session = ChargingSession.start(1L, 2L, 20, 80, PLAN, START);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(session.getEndTime()).isNull();
        assertThat(session.getDuration()).isNull();
    }

    @Test
    void completingSessionRecordsEndTimeEnergyAndDuration() {
        ChargingSession session = ChargingSession.start(1L, 2L, 20, 80, PLAN, START);

        session.complete(START.plusMinutes(30), 25.0);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getEnergyDeliveredKwh()).isEqualTo(25.0);
        assertThat(session.getDuration()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void sessionCannotBeCompletedTwice() {
        ChargingSession session = ChargingSession.start(1L, 2L, 20, 80, PLAN, START);
        session.complete(START.plusMinutes(30), 25.0);

        assertThatThrownBy(() -> session.complete(START.plusMinutes(40), 30.0))
                .isInstanceOf(InvalidSessionStateException.class);
    }
}
