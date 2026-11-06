package pt.ipcb.ad.chargingsession.model;

import java.time.Duration;

/** Result of planning a charge: how fast, how much energy and how long it should take. */
public record ChargingPlan(double chargingPowerKw, double energyNeededKwh, Duration estimatedDuration) {
}
