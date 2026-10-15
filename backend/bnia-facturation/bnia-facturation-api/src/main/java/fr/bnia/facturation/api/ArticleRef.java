package fr.bnia.facturation.api;

/** Un article désigné par sa source et son code (spec 08 §3.2), résolu par le catalogue de la source (RG-FAC-03). */
public record ArticleRef(String source, String code) {
}
