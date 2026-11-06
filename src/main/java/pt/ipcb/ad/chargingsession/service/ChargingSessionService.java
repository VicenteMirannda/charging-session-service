package pt.ipcb.ad.chargingsession.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pt.ipcb.ad.chargingsession.repository.ChargingSessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChargingSessionService {

    private final ChargingSessionRepository repository;
    private final ChargingPointClient chargingPointClient;
    private final VehicleClient vehicleClient;
    private final ChargingSimulator simulator;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public ChargingSessionService(ChargingSessionRepository repository,
                                  ChargingPointClient chargingPointClient,
                                  VehicleClient vehicleClient,
                                  ChargingSimulator simulator,
                                  ApplicationEventPublisher eventPublisher,
                                  Clock clock) {
        this.repository = repository;
        this.chargingPointClient = chargingPointClient;
        this.vehicleClient = vehicleClient;
        this.simulator = simulator;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public ChargingSession startSession(StartSessionRequest request) {
        ChargingPointDto chargingPoint = chargingPointClient.getChargingPoint(request.chargingPointId());
        if (!chargingPoint.isAvailable()) {
            throw new ChargingPointUnavailableException(chargingPoint.id(), chargingPoint.status());
        }
        VehicleDto vehicle = vehicleClient.getVehicle(request.vehicleId());

        ChargingPlan plan = simulator.plan(vehicle, chargingPoint,
                request.startBatteryPercent(), request.targetBatteryPercent());

        ChargingSession session = ChargingSession.start(vehicle.id(), chargingPoint.id(),
                request.startBatteryPercent(), request.targetBatteryPercent(),
                plan, LocalDateTime.now(clock));

        ChargingSession savedSession = repository.save(session);
        chargingPointClient.updateStatus(chargingPoint.id(), ChargingPointStatus.IN_USE);
        return savedSession;
    }

    @Transactional
    public ChargingSession stopSession(Long sessionId) {
        ChargingSession session = getSession(sessionId);

        LocalDateTime now = LocalDateTime.now(clock);
        Duration elapsed = Duration.between(session.getStartTime(), now);
        double energyDelivered = simulator.energyDelivered(
                session.getChargingPowerKw(), elapsed, session.getEnergyNeededKwh());

        session.complete(now, energyDelivered);
        chargingPointClient.updateStatus(session.getChargingPointId(), ChargingPointStatus.AVAILABLE);

        eventPublisher.publishEvent(new SessionCompletedEvent(session.getId(), session.getVehicleId(),
                session.getChargingPointId(), energyDelivered, session.getDuration()));
        return session;
    }

    @Transactional(readOnly = true)
    public ChargingSession getSession(Long sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session", sessionId));
    }

    @Transactional(readOnly = true)
    public List<ChargingSession> getSessionsByVehicle(Long vehicleId) {
        return repository.findByVehicleIdOrderByStartTimeDesc(vehicleId);
    }
}
