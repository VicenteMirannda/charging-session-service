package pt.ipcb.ad.chargingsession.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** HTTP client (OpenFeign) for the charging-point (OPC) microservice. */
@FeignClient(name = "charging-point-service", url = "${clients.charging-point-service.url}")
public interface ChargingPointClient {

    @GetMapping("/api/charging-points/{id}")
    ChargingPointDto getChargingPoint(@PathVariable("id") Long id);

    @PutMapping("/api/charging-points/{id}/status")
    void updateStatus(@PathVariable("id") Long id, @RequestParam("status") ChargingPointStatus status);
}
