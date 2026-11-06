package pt.ipcb.ad.chargingsession.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartSessionRequest(
        @NotNull Long vehicleId,
        @NotNull Long chargingPointId,
        @NotNull @Min(0) @Max(99) Integer startBatteryPercent,
        @NotNull @Min(1) @Max(100) Integer targetBatteryPercent) {

    @JsonIgnore
    @AssertTrue(message = "targetBatteryPercent must be greater than startBatteryPercent")
    public boolean isTargetAboveStart() {
        // Null values are already reported by @NotNull
        return startBatteryPercent == null || targetBatteryPercent == null
                || targetBatteryPercent > startBatteryPercent;
    }
}
