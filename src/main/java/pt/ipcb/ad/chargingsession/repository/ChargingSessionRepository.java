package pt.ipcb.ad.chargingsession.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipcb.ad.chargingsession.model.ChargingSession;

import java.util.List;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByVehicleIdOrderByStartTimeDesc(Long vehicleId);
}
