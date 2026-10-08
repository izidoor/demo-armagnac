package fr.bnia.commun.adapter.rest;

import java.util.List;
import java.util.Optional;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Résout un paramètre {@link Connecte} (ou {@code Optional<Connecte>}) depuis la {@link SessionUtilisateur}.
 * Sans implémentation de la session (aucun contexte d'authentification), personne n'est connecté.
 */
@Component
class ConnecteResolver implements HandlerMethodArgumentResolver, WebMvcConfigurer {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Connecte.class);
    }

    private final ObjectProvider<SessionUtilisateur> session;

    ConnecteResolver(ObjectProvider<SessionUtilisateur> session) {
        this.session = session;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this);
    }

    @Override
    public boolean supportsParameter(MethodParameter parametre) {
        return parametre.getParameterType() == Connecte.class || facultatif(parametre);
    }

    @Override
    public Object resolveArgument(MethodParameter parametre, ModelAndViewContainer mavContainer,
            NativeWebRequest requete, WebDataBinderFactory binderFactory) {
        Optional<Connecte> connecte = Optional.ofNullable(session.getIfAvailable())
                .flatMap(SessionUtilisateur::utilisateurConnecte)
                .map(Connecte::new);
        if (facultatif(parametre)) {
            return connecte;
        }
        return connecte.orElseThrow(NonConnecte::new);
    }

    private static boolean facultatif(MethodParameter parametre) {
        if (parametre.getParameterType() != Optional.class) {
            return false;
        }
        Class<?> contenu = ResolvableType.forMethodParameter(parametre).getGeneric(0).resolve();
        return contenu == Connecte.class;
    }
}
