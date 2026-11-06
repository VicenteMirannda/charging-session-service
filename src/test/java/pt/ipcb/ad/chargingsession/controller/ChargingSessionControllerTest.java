package pt.ipcb.ad.chargingsession.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pt.ipcb.ad.chargingsession.dto.StartSessionRequest;
import pt.ipcb.ad.chargingsession.exception.ChargingPointUnavailableException;
import pt.ipcb.ad.chargingsession.exception.ResourceNotFoundException;
import pt.ipcb.ad.chargingsession.client.ChargingPointStatus;
import pt.ipcb.ad.chargingsession.model.ChargingPlan;
import pt.ipcb.ad.chargingsession.model.ChargingSession;
import pt.ipcb.ad.chargingsession.service.ChargingSessionService;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tests only the web layer: JSON mapping, validation and HTTP status codes. The service is mocked. */
@WebMvcTest(ChargingSessionController.class)
class ChargingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChargingSessionService sessionService;

    @Test
    void startSessionReturns201WithLocationHeader() throws Exception {
        ChargingPlan plan = new ChargingPlan(50.0, 36.0, Duration.ofMinutes(43));
        ChargingSession session = ChargingSession.start(1L, 2L, 20, 80, plan, LocalDateTime.of(2025, 1, 10, 10, 0));
        when(sessionService.startSession(any(StartSessionRequest.class))).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId": 1, "chargingPointId": 2, "startBatteryPercent": 20, "targetBatteryPercent": 80}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.chargingPowerKw").value(50.0));
    }

    @Test
    void startSessionReturns400WhenTargetIsNotAboveStart() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId": 1, "chargingPointId": 2, "startBatteryPercent": 80, "targetBatteryPercent": 50}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.targetAboveStart").exists());
    }

    @Test
    void startSessionReturns400WhenFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vehicleId").exists())
                .andExpect(jsonPath("$.errors.chargingPointId").exists());
    }

    @Test
    void startSessionReturns409WhenChargingPointIsUnavailable() throws Exception {
        when(sessionService.startSession(any(StartSessionRequest.class)))
                .thenThrow(new ChargingPointUnavailableException(3L, ChargingPointStatus.OFFLINE));

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId": 1, "chargingPointId": 3, "startBatteryPercent": 20, "targetBatteryPercent": 80}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getSessionReturns404WhenSessionDoesNotExist() throws Exception {
        when(sessionService.getSession(99L)).thenThrow(new ResourceNotFoundException("Charging session", 99L));

        mockMvc.perform(get("/api/sessions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Charging session with id 99 was not found"));
    }
}
