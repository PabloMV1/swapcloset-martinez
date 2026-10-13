package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.LoginRequest;
import es.safareyes.swapcloset.dto.request.RegistroRequest;
import es.safareyes.swapcloset.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);
}
