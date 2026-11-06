package pt.ipcb.ad.microservico_Simulacao_de_Carregamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.modelos.SessaoCarregamento;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableFeignClients
public class MicroservicoSimulacaoDeCarregamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicoSimulacaoDeCarregamentoApplication.class, args);

		SessaoCarregamento sessao = new SessaoCarregamento();

		// Definir valores
		sessao.setVehicleId(1L);
		sessao.setChargingPointId(2L);
		sessao.setEnergyConsumed(20.0);  // Energia consumida (kWh)
		sessao.setStartTime(LocalDateTime.now());
		sessao.setStatus(SessaoCarregamento.StatusSessao.EM_PROGRESSO);

		
	}

}
