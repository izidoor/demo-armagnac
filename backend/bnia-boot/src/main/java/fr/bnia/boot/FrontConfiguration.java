package fr.bnia.boot;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Front servi par l'application quand il est embarqué dans le jar (build Maven avec {@code -Pfront}).
 *
 * <p>En dev, Vite sert le front et ce câblage reste inactif. Les routes du front ({@code /operateurs/…})
 * n'existent pas en fichiers : toute adresse hors {@code /api} sans fichier correspondant renvoie
 * {@code index.html}, et le routeur du front prend le relais.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnResource(resources = FrontConfiguration.INDEX)
class FrontConfiguration implements WebMvcConfigurer {

    static final String INDEX = "classpath:/static/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registre) {
        registre.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String chemin, Resource emplacement) throws IOException {
                        Resource fichier = emplacement.createRelative(chemin);
                        if (fichier.exists() && fichier.isReadable()) {
                            return fichier;
                        }
                        return chemin.startsWith("api/") ? null : new ClassPathResource("static/index.html");
                    }
                });
    }
}
