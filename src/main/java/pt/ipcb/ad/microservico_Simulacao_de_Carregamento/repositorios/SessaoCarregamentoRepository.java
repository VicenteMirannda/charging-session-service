package pt.ipcb.ad.microservico_Simulacao_de_Carregamento.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ipcb.ad.microservico_Simulacao_de_Carregamento.modelos.SessaoCarregamento;

@Repository
public interface SessaoCarregamentoRepository extends JpaRepository<SessaoCarregamento, Integer> {

}
