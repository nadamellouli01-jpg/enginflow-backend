package com.stage.EnginFlow.security;

import com.stage.EnginFlow.model.Utilisateur;
import com.stage.EnginFlow.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor // ← Injection par constructeur
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository; // ← plus besoin de @Autowired

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Tentative de connexion avec email: " + email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ Utilisateur non trouvé: " + email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec email: " + email);
                });

        System.out
                .println("✅ Utilisateur trouvé: " + utilisateur.getEmail() + " (rôle: " + utilisateur.getRole() + ")");

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole())));
    }
}