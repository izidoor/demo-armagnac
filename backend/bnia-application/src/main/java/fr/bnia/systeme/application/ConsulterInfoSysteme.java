package fr.bnia.systeme.application;

import fr.bnia.commun.application.UseCase;

/**
 * Sonde technique : renvoie le nom et la version de l'application, et la version de la base.
 *
 * <p>Sans valeur métier, ce cas d'usage traverse toute la chaîne (REST, application,
 * persistance) et sert de modèle aux suivants.
 */
@UseCase
public class ConsulterInfoSysteme {

    static final String APPLICATION = "BNIA";

    private final InfoBaseReader infoBaseReader;
    private final String version;

    public ConsulterInfoSysteme(InfoBaseReader infoBaseReader) {
        this.infoBaseReader = infoBaseReader;
        this.version = versionDuPaquet();
    }

    public InfoSysteme executer() {
        return new InfoSysteme(APPLICATION, version, infoBaseReader.versionBase());
    }

    private static String versionDuPaquet() {
        String version = ConsulterInfoSysteme.class.getPackage().getImplementationVersion();
        return version != null ? version : "dev";
    }
}
