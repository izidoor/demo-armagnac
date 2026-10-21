package fr.bnia.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Réponse à l'appel d'offres, lue dans l'application (« Voir l'offre » de la barre latérale) : une page par section,
 * publiée depuis {@code echanges-bnia/reponse} par {@code pnpm publier}. Servie sous {@code /api}, elle exige une
 * session, comme toute l'API ; l'application l'affiche dans une iframe, permise sur la même origine.
 */
@Configuration(proxyBeanMethods = false)
class OffreConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registre) {
        registre.addResourceHandler("/api/offre/**").addResourceLocations("classpath:/offre/");
    }
}
