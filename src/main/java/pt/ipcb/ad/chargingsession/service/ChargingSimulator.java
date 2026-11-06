package pt.ipcb.ad.chargingsession.service;

import org.springframework.stereotype.Component;
import pt.ipcb.ad.chargingsession.client.ChargingPointDto;
import pt.ipcb.ad.chargingsession.client.VehicleDto;
import pt.ipcb.ad.chargingsession.model.ChargingPlan;

import java.time.Duration;

/**
 * Pure charging calculations, with no database or HTTP calls.
 * Keeping them here makes the business rules easy to read and to unit test.
 */
@Component
public class ChargingSimulator {

    private static final double SECONDS_PER_HOUR = 3600.0;

    public ChargingPlan plan(VehicleDto vehicle, ChargingPointDto chargingPoint,
                             int startBatteryPercent, int targetBatteryPercent) {
        // The charge runs at the lowest speed supported by the vehicle or offered by the charging point
        double chargingPowerKw = Math.min(vehicle.maxChargingPowerKw(), chargingPoint.maxPowerKw());

        double energyNeededKwh = vehicle.batteryCapacityKwh() * (targetBatteryPercent - startBatteryPercent) / 100.0;

        long estimatedSeconds = Math.round(energyNeededKwh / chargingPowerKw * SECONDS_PER_HOUR);

        return new ChargingPlan(chargingPowerKw, energyNeededKwh, Duration.ofSeconds(estimatedSeconds));
    }

    /**
     * Energy delivered after charging for {@code elapsed} time.
     * It never exceeds the energy needed, because charging stops when the target battery level is reached.
     */
    public double energyDelivered(double chargingPowerKw, Duration elapsed, double energyNeededKwh) {
        double hours = elapsed.toSeconds() / SECONDS_PER_HOUR;
        return Math.min(chargingPowerKw * hours, energyNeededKwh);
    }
}
