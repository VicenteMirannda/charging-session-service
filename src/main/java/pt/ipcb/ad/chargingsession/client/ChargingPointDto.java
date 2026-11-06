package pt.ipcb.ad.chargingsession.client;

/** Charging point data as returned by the charging-point (OPC) microservice. */
public record ChargingPointDto(Long id, String location, double maxPowerKw, ChargingPointStatus status) {

    public boolean isAvailable() {
        return status == ChargingPointStatus.AVAILABLE;
    }
}
