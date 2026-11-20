package cl.duoc.tiotec.solicitudes;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SolicitudesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolicitudesApplication.class, args);
    }

    /** Reloj inyectable: permite fijar la hora en las pruebas. */
    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }
}
