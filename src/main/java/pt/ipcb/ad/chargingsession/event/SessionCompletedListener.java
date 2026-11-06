package pt.ipcb.ad.chargingsession.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to finished sessions. It runs only after the database transaction commits,
 * so it never reacts to a session that was rolled back.
 *
 * Here it only logs. In a production system this is where the event would be sent to a
 * message broker (e.g. Kafka or RabbitMQ) for the billing service to consume.
 */
@Component
public class SessionCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(SessionCompletedListener.class);

    @TransactionalEventListener
    public void onSessionCompleted(SessionCompletedEvent event) {
        log.info("Session {} completed: vehicle {} received {} kWh at charging point {} in {} min",
                event.sessionId(), event.vehicleId(),
                String.format("%.2f", event.energyDeliveredKwh()),
                event.chargingPointId(), event.duration().toMinutes());
    }
}
