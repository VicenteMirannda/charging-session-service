package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.modelos.SessaoCarregamento;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.proxies.PostoCarregamentoFeignClient;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.repositorios.SessaoCarregamentoRepository;

import java.time.LocalDateTime;

@Service
public class ServicosessaoCarregamento {
    @Autowired
    private SessaoCarregamentoRepository repositorio;
    private final PostoCarregamentoFeignClient postos;

    public ServicosessaoCarregamento(SessaoCarregamentoRepository repositorio, PostoCarregamentoFeignClient feignClient) {
        this.repositorio = repositorio;
        this.postos = feignClient;
    }

    public SessaoCarregamento iniciarSessao(Long veiculoId, Long pontoCarregamentoId) {
        // Verificar se o posto está disponível via Feign Client
       /* boolean disponivel = postos.verificarDisponibilidade(pontoCarregamentoId);
        if (!disponivel) {
            throw new RuntimeException("O posto de carregamento não está disponível.");
        }*/

        SessaoCarregamento sessao = new SessaoCarregamento();
        sessao.setVehicleId(veiculoId);
        sessao.setChargingPointId(pontoCarregamentoId);
        sessao.setStartTime(LocalDateTime.now());
        sessao.setStatus(SessaoCarregamento.StatusSessao.EM_PROGRESSO);
        return repositorio.save(sessao);
    }

    public void simulacaoCarregamento(SessaoCarregamento sessao) {

        //buscar ao veiculo qual a capacidade maxima da bateria ou a % definida

        //O sistema regista os dados finais da sessão, incluindo energia consumida (kWh) e
        //duração total.

        //A simulação da velocidade de carregamento deve a menor velocidade (suportada pelo
        //veiculo ou disponível no posto).




        // Verificar se a sessão está em progresso antes de realizar a simulação
        if (sessao.getStatus() == SessaoCarregamento.StatusSessao.EM_PROGRESSO) {
            // Atualizar o tempo de início, caso ainda não tenha sido definido
            if (sessao.getStartTime() == null) {
                sessao.setStartTime(LocalDateTime.now());
            }

            // Calcular o consumo de energia e o custo (isso pode ser ajustado conforme sua lógica de negócio)
            double energiaConsumida = calcularEnergiaConsumida(sessao.getVehicleId(), sessao.getChargingPointId());
            double opcCusto = calcularCustoOPC(sessao.getVehicleId(), energiaConsumida);

            // Atualizar os valores da sessão
            sessao.setEnergyConsumed(energiaConsumida);


            // Atualizar o status da sessão, se necessário
            // Por exemplo, se a sessão atingir o valor máximo de energia ou tempo, finalizar
            if (sessao.getEnergyConsumed() >= 100) {
                sessao.setStatus(SessaoCarregamento.StatusSessao.COMPLETO);
                sessao.setEndTime(LocalDateTime.now());
            }


        }
    }

    // Método para calcular a energia consumida (exemplo, pode variar conforme a lógica do seu sistema)
    private double calcularEnergiaConsumida(Long vehicleId, Long chargingPointId) {
        // Exemplo fictício de cálculo
        return 20.0; // Retorne o valor calculado de energia consumida
    }

    // Método para calcular o custo OPC (exemplo)
    private double calcularCustoOPC(Long vehicleId, double energiaConsumida) {
        // Exemplo fictício de cálculo
        return energiaConsumida * 0.15; // Retorne o custo baseado na energia consumida
    }







    public SessaoCarregamento finalizarSessao(Integer sessionId, double energyConsumed, double cost) {
        SessaoCarregamento sessao = repositorio.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sessao não encontrada"));
        sessao.setEndTime(LocalDateTime.now());
        sessao.setEnergyConsumed(energyConsumed);
        sessao.setCost(cost);
        sessao.setStatus(SessaoCarregamento.StatusSessao.COMPLETO);
        return repositorio.save(sessao);
    }



}
