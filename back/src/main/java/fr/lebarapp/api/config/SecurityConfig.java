package fr.lebarapp.api.config;

import fr.lebarapp.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Configuration de la sécurité : API sans session (stateless), authentifiée par JWT.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API stateless authentifiée par JWT : pas de session serveur ni de CSRF.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Le client commande sans compte : lecture de la carte et création/suivi de
            // commande sont publics ; l'administration est réservée au rôle BARMAKER.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/ingredients/**", "/api/cocktails/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tables/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tables").hasRole("BARMAKER")
                .requestMatchers(HttpMethod.GET, "/api/orders/queue").hasRole("BARMAKER")
                .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/*").permitAll()
                .requestMatchers("/api/categories/**", "/api/ingredients/**", "/api/cocktails/**").hasRole("BARMAKER")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/**").hasRole("BARMAKER")
                .requestMatchers("/api/external/**").hasRole("BARMAKER")
                .anyRequest().authenticated()
            )
            // Valide le jeton JWT avant le traitement standard de l'authentification.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
