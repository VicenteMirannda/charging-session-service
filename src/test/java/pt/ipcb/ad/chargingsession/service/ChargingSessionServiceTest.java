package pt.ipcb.ad.chargingsession.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pt.ipcb.ad.chargingsession.client.ChargingPointClient;
import pt.ipcb.ad.chargingsession.client.ChargingPointDto;
import pt.ipcb.ad.chargingsession.client.ChargingPointStatus;
import pt.ipcb.ad.chargingsession.client.VehicleClient;
import pt.ipcb.ad.chargingsession.client.VehicleDto;
import pt.ipcb.ad.chargingsession.dto.StartSessionRequest;
import pt.ipcb.ad.chargingsession.event.SessionCompletedEvent;
import pt.ipcb.ad.chargingsession.exception.ChargingPointUnavailableException;
import pt.ipcb.ad.chargingsession.exception.ResourceNotFoundException;
import pt.ipcb.ad.chargingsession.model.ChargingPlan;
import pt.ipcb.ad.chargingsession.model.ChargingSession;
import pt.ipcb.ad.chargingsession.model.SessionStatus;
import pt.ipcb.ad.chargingsession.repository.ChargingSessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargingSessionServiceTest {

    /** A fixed clock makes the "current time" predictable in tests. */
    private static final Instant NOW = Instant.parse("2025-01-10T10:30:00Z");
    private static final LocalDateTime NOW_LOCAL = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);

    @Mock
    private ChargingSessionRepository repository;
    @Mock
    private ChargingPointClient chargingPointClient;
    @Mock
    private VehicleClient vehicleClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ChargingSessionService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new ChargingSessionService(repository, chargingPointClient, vehicleClient,
                new ChargingSimulator(), eventPublisher, fixedClock);
    }

    @Test
    void startSessionSavesSessionAndMarksChargingPointInUse() {
        when(chargingPointClient.getChargingPoint(2L))
                .thenReturn(new ChargingPointDto(2L, "IPCB", 50.0, ChargingPointStatus.AVAILABLE));
        when(vehicleClient.getVehicle(1L))
                .thenReturn(new VehicleDto(1L, "Tesla", "Model 3", 60.0, 170.0));
        when(repository.save(any(ChargingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargingSession session = service.startSession(new StartSessionRequest(1L, 2L, 20, 80));

        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(session.getStartTime()).isEqualTo(NOW_LOCAL);
        assertThat(session.getChargingPowerKw()).isEqualTo(50.0);
        assertThat(session.getEnergyNeededKwh()).isCloseTo(36.0, within(0.001));
        verify(chargingPointClient).updateStatus(2L, ChargingPointStatus.IN_USE);
    }

    @Test
    void startSessionFailsWhenChargingPointIsNotAvailable() {
        when(chargingPointClient.getChargingPoint(3L))
                .thenReturn(new ChargingPointDto(3L, "A23", 150.0, ChargingPointStatus.OFFLINE));

        assertThatThrownBy(() -> service.startSession(new StartSessionRequest(1L, 3L, 20, 80)))
                .isInstanceOf(ChargingPointUnavailableException.class);

        verify(repository, never()).save(any());
        verify(chargingPointClient, never()).updateStatus(any(), any());
    }

    @Test
    void stopSessionCompletesSessionFreesChargingPointAndPublishesEvent() {
        ChargingPlan plan = new ChargingPlan(50.0, 36.0, Duration.ofMinutes(43));
        ChargingSession session = ChargingSession.start(1L, 2L, 20, 80, plan, NOW_LOCAL.minusMinutes(30));
        when(repository.findById(7L)).thenReturn(Optional.of(session));

        ChargingSession stopped = service.stopSession(7L);

        // 30 minutes at 50 kW = 25 kWh
        assertThat(stopped.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(stopped.getEndTime()).isEqualTo(NOW_LOCAL);
        assertThat(stopped.getEnergyDeliveredKwh()).isCloseTo(25.0, within(0.001));
        verify(chargingPointClient).updateStatus(2L, ChargingPointStatus.AVAILABLE);

        ArgumentCaptor<SessionCompletedEvent> event = ArgumentCaptor.forClass(SessionCompletedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().energyDeliveredKwh()).isCloseTo(25.0, within(0.001));
        assertThat(event.getValue().duration()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void stopSessionFailsWhenSessionDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.stopSession(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
