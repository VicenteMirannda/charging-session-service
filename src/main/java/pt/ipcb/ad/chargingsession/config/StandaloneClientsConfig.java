package pt.ipcb.ad.chargingsession.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.ipcb.ad.chargingsession.client.ChargingPointClient;
import pt.ipcb.ad.chargingsession.client.ChargingPointDto;
import pt.ipcb.ad.chargingsession.client.ChargingPointStatus;
import pt.ipcb.ad.chargingsession.client.VehicleClient;
import pt.ipcb.ad.chargingsession.client.VehicleDto;
import pt.ipcb.ad.chargingsession.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fakes of the charging-point and vehicle microservices, so this service
 * can be run and demonstrated on its own. Only active with the "standalone" profile.
 */
@Configuration
@Profile("standalone")
public class StandaloneClientsConfig {

    @Bean
    ChargingPointClient chargingPointClient() {
        return new InMemoryChargingPointClient();
    }

    @Bean
    VehicleClient vehicleClient() {
        Map<Long, VehicleDto> vehicles = Map.of(
                1L, new VehicleDto(1L, "Tesla", "Model 3", 60.0, 170.0),
                2L, new VehicleDto(2L, "Renault", "Zoe", 52.0, 22.0));

        return id -> {
            VehicleDto vehicle = vehicles.get(id);
            if (vehicle == null) {
                throw new ResourceNotFoundException("Vehicle", id);
            }
            return vehicle;
        };
    }

    static class InMemoryChargingPointClient implements ChargingPointClient {

        private final Map<Long, ChargingPointDto> points = new ConcurrentHashMap<>(Map.of(
                1L, new ChargingPointDto(1L, "Castelo Branco - Centro", 50.0, ChargingPointStatus.AVAILABLE),
                2L, new ChargingPointDto(2L, "Castelo Branco - IPCB", 22.0, ChargingPointStatus.AVAILABLE),
                3L, new ChargingPointDto(3L, "Covilha - A23", 150.0, ChargingPointStatus.OFFLINE)));

        @Override
        public ChargingPointDto getChargingPoint(Long id) {
            ChargingPointDto point = points.get(id);
            if (point == null) {
                throw new ResourceNotFoundException("Charging point", id);
            }
            return point;
        }

        @Override
        public void updateStatus(Long id, ChargingPointStatus status) {
            ChargingPointDto point = getChargingPoint(id);
            points.put(id, new ChargingPointDto(id, point.location(), point.maxPowerKw(), status));
        }
    }
}
