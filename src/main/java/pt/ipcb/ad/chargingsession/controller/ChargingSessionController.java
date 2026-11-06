package pt.ipcb.ad.chargingsession.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ipcb.ad.chargingsession.dto.SessionResponse;
import pt.ipcb.ad.chargingsession.dto.StartSessionRequest;
import pt.ipcb.ad.chargingsession.model.ChargingSession;
import pt.ipcb.ad.chargingsession.service.ChargingSessionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class ChargingSessionController {

    private final ChargingSessionService sessionService;

    public ChargingSessionController(ChargingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        ChargingSession session = sessionService.startSession(request);
        URI location = URI.create("/api/sessions/" + session.getId());
        return ResponseEntity.created(location).body(SessionResponse.from(session));
    }

    @PostMapping("/{id}/stop")
    public SessionResponse stopSession(@PathVariable Long id) {
        return SessionResponse.from(sessionService.stopSession(id));
    }

    @GetMapping("/{id}")
    public SessionResponse getSession(@PathVariable Long id) {
        return SessionResponse.from(sessionService.getSession(id));
    }

    @GetMapping
    public List<SessionResponse> getSessionsByVehicle(@RequestParam Long vehicleId) {
        return sessionService.getSessionsByVehicle(vehicleId).stream()
                .map(SessionResponse::from)
                .toList();
    }
}
