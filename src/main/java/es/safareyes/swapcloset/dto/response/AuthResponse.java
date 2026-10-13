package es.safareyes.swapcloset.dto.response;

public record AuthResponse(String token, String tipo, long expiraEnSegundos, SesionUsuarioResponse usuario) {
}
