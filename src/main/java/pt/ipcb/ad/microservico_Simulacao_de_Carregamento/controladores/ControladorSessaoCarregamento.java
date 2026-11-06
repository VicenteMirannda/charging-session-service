package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.modelos.SessaoCarregamento;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.servicos.ServicosessaoCarregamento;

@RestController
@RequestMapping("/api/sessoes")
public class ControladorSessaoCarregamento {

    @Autowired
    private ServicosessaoCarregamento servicosessaoCarregamento;

    @GetMapping("/start")
    public ResponseEntity<SessaoCarregamento> startSession(@RequestParam Long vehicleId, @RequestParam Long chargingPointId) {
        // Chama o serviço para iniciar a sessão
        SessaoCarregamento sessao = servicosessaoCarregamento.iniciarSessao(vehicleId, chargingPointId);
        return ResponseEntity.ok().body(sessao); // Retorna a sessão iniciada
    }

    @GetMapping("/end")
    public ResponseEntity<SessaoCarregamento> endSession(@RequestParam Integer sessionId, @RequestParam double energyConsumed, @RequestParam double cost) {
        // Chama o serviço para finalizar a sessão
        SessaoCarregamento sessao = servicosessaoCarregamento.finalizarSessao(sessionId, energyConsumed, cost);
        return ResponseEntity.ok().body(sessao); // Retorna a sessão finalizada
    }




    /*@PostMapping("/start")
    public SessaoCarregamento startSession(@RequestParam Long vehicleId, @RequestParam Long chargingPointId) {
        return servicosessaoCarregamento.iniciarSessao(vehicleId, chargingPointId);
    }

    @PostMapping("/end")
    public SessaoCarregamento endSession(@RequestParam Integer sessionId, @RequestParam double energyConsumed, @RequestParam double cost) {
        return servicosessaoCarregamento.finalizarSessao(sessionId, energyConsumed, cost);
    }*/


}

