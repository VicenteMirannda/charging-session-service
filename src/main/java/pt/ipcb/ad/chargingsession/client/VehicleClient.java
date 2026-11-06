package pt.ipcb.ad.chargingsession.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** HTTP client (OpenFeign) for the vehicle microservice. */
@FeignClient(name = "vehicle-service", url = "${clients.vehicle-service.url}")
public interface VehicleClient {

    @GetMapping("/api/vehicles/{id}")
    VehicleDto getVehicle(@PathVariable("id") Long id);
}
