package pt.ipcb.ad.chargingsession.event;

import java.time.Duration;

/** Published when a charging session ends. Billing (CEME) needs this data to issue the invoice. */
public record SessionCompletedEvent(Long sessionId, Long vehicleId, Long chargingPointId,
                                    double energyDeliveredKwh, Duration duration) {
}
