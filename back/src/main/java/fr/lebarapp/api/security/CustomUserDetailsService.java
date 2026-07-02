package fr.lebarapp.api.security;

import fr.lebarapp.api.domain.User;
import fr.lebarapp.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Fait le lien entre nos comptes en base et Spring Security : charge un utilisateur par son email
// et expose son mot de passe haché et son rôle pour l'authentification.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(email)
            .password(user.getPasswordHash())
            .authorities("ROLE_" + user.getRole().name())
            .build();
    }
}
