package es.safareyes.swapcloset.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

/** Escribe un ProblemDetail desde los filtros de seguridad (donde no llega el @RestControllerAdvice). */
final class ProblemaJson {

    private ProblemaJson() {
    }

    static void escribir(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                         HttpStatus estado, String titulo, String detalle) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", LocalDateTime.now().withNano(0));
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), problema);
    }
}
