package es.safareyes.swapcloset;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SwapClosetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapClosetApplication.class, args);
    }

    @PostConstruct
    void zonaHoraria() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    }
}
