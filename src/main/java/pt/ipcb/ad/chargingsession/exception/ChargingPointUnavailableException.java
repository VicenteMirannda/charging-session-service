package pt.ipcb.ad.chargingsession.exception;

import pt.ipcb.ad.chargingsession.client.ChargingPointStatus;

public class ChargingPointUnavailableException extends RuntimeException {

    public ChargingPointUnavailableException(Long chargingPointId, ChargingPointStatus status) {
        super("Charging point " + chargingPointId + " is not available (current status: " + status + ")");
    }
}
