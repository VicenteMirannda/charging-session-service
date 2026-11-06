package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessaoCarregamento {

    public enum StatusSessao {
        EM_PROGRESSO,
        COMPLETO;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long chargingPointId;

    private double energyConsumed;
    private double cost;


    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;


    public void validarDados() {
        if (energyConsumed < 0 ) {
            throw new IllegalArgumentException("O valor de energia consumida deve ser positivo.");
        }
    }









}
