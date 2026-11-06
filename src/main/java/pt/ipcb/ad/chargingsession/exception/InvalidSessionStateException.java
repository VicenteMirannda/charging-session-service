package pt.ipcb.ad.chargingsession.exception;

import pt.ipcb.ad.chargingsession.model.SessionStatus;

public class InvalidSessionStateException extends RuntimeException {

    public InvalidSessionStateException(Long sessionId, SessionStatus currentStatus) {
        super("Session " + sessionId + " cannot be stopped because it is " + currentStatus);
    }
}
