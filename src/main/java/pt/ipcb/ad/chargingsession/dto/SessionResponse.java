package pt.ipcb.ad.chargingsession.dto;

import pt.ipcb.ad.chargingsession.model.ChargingSession;
import pt.ipcb.ad.chargingsession.model.SessionStatus;

import java.time.Duration;
import java.time.LocalDateTime;

/** What the API returns. Keeps the JPA entity out of the HTTP contract. */
public record SessionResponse(
        Long id,
        Long vehicleId,
        Long chargingPointId,
        SessionStatus status,
        int startBatteryPercent,
        int targetBatteryPercent,
        double chargingPowerKw,
        double energyNeededKwh,
        double energyDeliveredKwh,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationMinutes) {

    public static SessionResponse from(ChargingSession session) {
        Duration duration = session.getDuration();
        return new SessionResponse(
                session.getId(),
                session.getVehicleId(),
                session.getChargingPointId(),
                session.getStatus(),
                session.getStartBatteryPercent(),
                session.getTargetBatteryPercent(),
                session.getChargingPowerKw(),
                roundToTwoDecimals(session.getEnergyNeededKwh()),
                roundToTwoDecimals(session.getEnergyDeliveredKwh()),
                session.getStartTime(),
                session.getEndTime(),
                duration == null ? null : duration.toMinutes());
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
