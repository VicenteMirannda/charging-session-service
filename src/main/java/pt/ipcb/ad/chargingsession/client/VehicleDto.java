package pt.ipcb.ad.chargingsession.client;

/** Vehicle data as returned by the vehicle microservice. */
public record VehicleDto(Long id, String brand, String model, double batteryCapacityKwh, double maxChargingPowerKw) {
}
