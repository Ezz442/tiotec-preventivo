package cl.duoc.tiotec.dispositivos;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// #inicio del microservicio dispositivos (puerto 8082)
@SpringBootApplication
public class DispositivosApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispositivosApplication.class, args);
    }

    /** Reloj inyectable: permite fijar la hora en las pruebas. */
    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }
}
