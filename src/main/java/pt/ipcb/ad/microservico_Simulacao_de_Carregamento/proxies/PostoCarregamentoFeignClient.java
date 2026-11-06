package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservicoOPC", url = "http://localhost:8030")
public interface PostoCarregamentoFeignClient {

   /*@GetMapping("/{id}/disponibilidade")
    Boolean verificarDisponibilidade(@PathVariable("id") Long id);*/
}
