package es.safareyes.swapcloset.config;

import es.safareyes.swapcloset.security.JwtAuthenticationFilter;
import es.safareyes.swapcloset.security.JwtService;
import es.safareyes.swapcloset.security.RestAccessDeniedHandler;
import es.safareyes.swapcloset.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // activa @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)            // API sin sesión ni cookies: no hace falta CSRF
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)      // 401
                        .accessDeniedHandler(accessDeniedHandler)) // 403
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // El catálogo se puede mirar sin cuenta
                        .requestMatchers(HttpMethod.GET, "/api/v1/prendas", "/api/v1/prendas/*",
                                "/api/v1/categorias", "/api/v1/fotos/*").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
