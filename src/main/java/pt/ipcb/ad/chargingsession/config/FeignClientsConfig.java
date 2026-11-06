package pt.ipcb.ad.chargingsession.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.ipcb.ad.chargingsession.client.ChargingPointClient;

/** Real HTTP clients for the other microservices. Not used in standalone mode. */
@Configuration
@Profile("!standalone")
@EnableFeignClients(basePackageClasses = ChargingPointClient.class)
public class FeignClientsConfig {
}
