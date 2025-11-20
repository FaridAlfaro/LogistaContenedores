package utn.backend.tpi.tpi_flota_viajes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // IMPORTAR

@SpringBootApplication
@EnableDiscoveryClient 
public class TpiFlotaViajesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TpiFlotaViajesApplication.class, args);
	}

}
