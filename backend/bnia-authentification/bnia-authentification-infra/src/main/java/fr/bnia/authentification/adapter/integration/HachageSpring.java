package fr.bnia.authentification.adapter.integration;

import fr.bnia.authentification.application.HachageMotDePasse;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Hachage lent des mots de passe par Spring Security (bcrypt, préfixé de son algorithme) (RG-AUT-07). */
@Component
class HachageSpring implements HachageMotDePasse {

    private final PasswordEncoder encodeur = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public String hacher(String clair) {
        return encodeur.encode(clair);
    }

    @Override
    public boolean correspond(String clair, String empreinte) {
        return encodeur.matches(clair, empreinte);
    }
}
