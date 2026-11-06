package pt.ipcb.ad.chargingsession.service;

import org.junit.jupiter.api.Test;
import pt.ipcb.ad.chargingsession.client.ChargingPointDto;
import pt.ipcb.ad.chargingsession.client.ChargingPointStatus;
import pt.ipcb.ad.chargingsession.client.VehicleDto;
import pt.ipcb.ad.chargingsession.model.ChargingPlan;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ChargingSimulatorTest {

    private final ChargingSimulator simulator = new ChargingSimulator();

    @Test
    void usesChargingPointPowerWhenItIsLowerThanVehiclePower() {
        VehicleDto vehicle = vehicle(60.0, 170.0);
        ChargingPointDto point = chargingPoint(50.0);

        ChargingPlan plan = simulator.plan(vehicle, point, 20, 80);

        assertThat(plan.chargingPowerKw()).isEqualTo(50.0);
    }

    @Test
    void usesVehiclePowerWhenItIsLowerThanChargingPointPower() {
        VehicleDto vehicle = vehicle(52.0, 22.0);
        ChargingPointDto point = chargingPoint(150.0);

        ChargingPlan plan = simulator.plan(vehicle, point, 20, 80);

        assertThat(plan.chargingPowerKw()).isEqualTo(22.0);
    }

    @Test
    void calculatesEnergyNeededAndEstimatedDuration() {
        // 60 kWh battery, from 20% to 80% -> 36 kWh. At 50 kW -> 0.72 h = 43 min 12 s
        ChargingPlan plan = simulator.plan(vehicle(60.0, 170.0), chargingPoint(50.0), 20, 80);

        assertThat(plan.energyNeededKwh()).isCloseTo(36.0, within(0.001));
        assertThat(plan.estimatedDuration()).isEqualTo(Duration.ofMinutes(43).plusSeconds(12));
    }

    @Test
    void energyDeliveredGrowsWithElapsedTime() {
        double energy = simulator.energyDelivered(50.0, Duration.ofMinutes(30), 36.0);

        assertThat(energy).isCloseTo(25.0, within(0.001));
    }

    @Test
    void energyDeliveredNeverExceedsEnergyNeeded() {
        double energy = simulator.energyDelivered(50.0, Duration.ofHours(3), 36.0);

        assertThat(energy).isEqualTo(36.0);
    }

    private static VehicleDto vehicle(double batteryCapacityKwh, double maxChargingPowerKw) {
        return new VehicleDto(1L, "Brand", "Model", batteryCapacityKwh, maxChargingPowerKw);
    }

    private static ChargingPointDto chargingPoint(double maxPowerKw) {
        return new ChargingPointDto(1L, "Somewhere", maxPowerKw, ChargingPointStatus.AVAILABLE);
    }
}
