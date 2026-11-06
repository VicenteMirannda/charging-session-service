package pt.ipcb.ad.chargingsession.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.ipcb.ad.chargingsession.exception.InvalidSessionStateException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A charging session of one vehicle at one charging point.
 * The entity protects its own rules: it is always created "in progress"
 * and can only be completed once.
 */
@Entity
@Table(name = "charging_sessions")
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long chargingPointId;

    private int startBatteryPercent;
    private int targetBatteryPercent;

    /** Power used during the session: the lowest of the vehicle and charging point limits. */
    private double chargingPowerKw;

    /** Energy needed to go from the start to the target battery level. */
    private double energyNeededKwh;

    private double energyDeliveredKwh;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    protected ChargingSession() {
        // required by JPA
    }

    public static ChargingSession start(Long vehicleId, Long chargingPointId,
                                        int startBatteryPercent, int targetBatteryPercent,
                                        ChargingPlan plan, LocalDateTime startTime) {
        ChargingSession session = new ChargingSession();
        session.vehicleId = vehicleId;
        session.chargingPointId = chargingPointId;
        session.startBatteryPercent = startBatteryPercent;
        session.targetBatteryPercent = targetBatteryPercent;
        session.chargingPowerKw = plan.chargingPowerKw();
        session.energyNeededKwh = plan.energyNeededKwh();
        session.startTime = startTime;
        session.status = SessionStatus.IN_PROGRESS;
        return session;
    }

    public void complete(LocalDateTime endTime, double energyDeliveredKwh) {
        if (status != SessionStatus.IN_PROGRESS) {
            throw new InvalidSessionStateException(id, status);
        }
        this.endTime = endTime;
        this.energyDeliveredKwh = energyDeliveredKwh;
        this.status = SessionStatus.COMPLETED;
    }

    /** Duration of the session, or {@code null} while it is still in progress. */
    public Duration getDuration() {
        return endTime == null ? null : Duration.between(startTime, endTime);
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public Long getChargingPointId() { return chargingPointId; }
    public int getStartBatteryPercent() { return startBatteryPercent; }
    public int getTargetBatteryPercent() { return targetBatteryPercent; }
    public double getChargingPowerKw() { return chargingPowerKw; }
    public double getEnergyNeededKwh() { return energyNeededKwh; }
    public double getEnergyDeliveredKwh() { return energyDeliveredKwh; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public SessionStatus getStatus() { return status; }
}
