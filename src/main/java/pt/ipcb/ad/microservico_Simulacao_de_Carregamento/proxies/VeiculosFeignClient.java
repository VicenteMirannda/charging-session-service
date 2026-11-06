package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.proxies;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "microservice1-ev-database", url = "http://localhost:8010")
public interface VeiculosFeignClient {

   /* @GetMapping("/veiculos/{marca}/{modelo}")
    Veiculo getVeiculo(@PathVariable String marca, @PathVariable String modelo);*/


}
