package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Présentation commerciale et œnotouristique d'un opérateur (§4.3.2.3 à 4.3.2.5 p.19-20, C.1.4 à C.1.7
 * p.103-104) : marques, circuits, visites, labels, œnotourisme, médailles. Agrégat distinct de
 * {@code Operateur}, de même identifiant (1 pour 1), modifié sans réécrire la fiche (ADR-012). Saisie et
 * consultable, sans flux vers l'annuaire public (Q-OPE-1).
 */
public final class PresentationOperateur {

    private final long id;
    private List<MarquePresentee> marques;
    private List<CircuitPresente> circuits;
    private List<VisiteProposee> visites;
    private List<LabelObtenu> labels;
    private Oenotourisme oenotourisme;
    private List<MedailleConcours> medailles;

    private PresentationOperateur(long id, List<MarquePresentee> marques, List<CircuitPresente> circuits,
            List<VisiteProposee> visites, List<LabelObtenu> labels, Oenotourisme oenotourisme,
            List<MedailleConcours> medailles) {
        this.id = id;
        modifier(marques, circuits, visites, labels, oenotourisme, medailles);
    }

    /** La présentation d'un opérateur qui n'a encore rien saisi. */
    public static PresentationOperateur vide(long operateur) {
        return new PresentationOperateur(operateur, List.of(), List.of(), List.of(), List.of(), Oenotourisme.aucun(),
                List.of());
    }

    public static PresentationOperateur reconstituer(long operateur, List<MarquePresentee> marques,
            List<CircuitPresente> circuits, List<VisiteProposee> visites, List<LabelObtenu> labels,
            Oenotourisme oenotourisme, List<MedailleConcours> medailles) {
        return new PresentationOperateur(operateur, marques, circuits, visites, labels, oenotourisme, medailles);
    }

    /** RG-OPE-16 à 19 : tout l'ensemble, d'un bloc. */
    public void modifier(List<MarquePresentee> nouvellesMarques, List<CircuitPresente> nouveauxCircuits,
            List<VisiteProposee> nouvellesVisites, List<LabelObtenu> nouveauxLabels, Oenotourisme nouvelOenotourisme,
            List<MedailleConcours> nouvellesMedailles) {
        sansDoublon(nouvellesMarques, m -> m.portee() + "/" + m.marque(), "RG-OPE-16",
                m -> "Une marque figure deux fois dans la portée " + libelle(m.portee()) + ".");
        sansDoublon(nouveauxCircuits, c -> c.portee() + "/" + c.circuit(), "RG-OPE-16",
                c -> "Un circuit de distribution figure deux fois dans la portée " + libelle(c.portee()) + ".");
        sansDoublon(nouvellesVisites, VisiteProposee::typeVisite, "RG-OPE-17",
                v -> "Un type de visite figure deux fois sur la fiche.");
        sansDoublon(nouveauxLabels, LabelObtenu::label, "RG-OPE-17", l -> "Un label figure deux fois sur la fiche.");
        this.marques = List.copyOf(nouvellesMarques);
        this.circuits = List.copyOf(nouveauxCircuits);
        this.visites = List.copyOf(nouvellesVisites);
        this.labels = List.copyOf(nouveauxLabels);
        this.oenotourisme = nouvelOenotourisme == null ? Oenotourisme.aucun() : nouvelOenotourisme;
        this.medailles = List.copyOf(nouvellesMedailles);
    }

    private static <T> void sansDoublon(List<T> elements, Function<T, Object> cle, String regle,
            Function<T, String> message) {
        Set<Object> vues = new HashSet<>();
        for (T element : elements) {
            if (!vues.add(cle.apply(element))) {
                throw new RegleEnfreinte(regle, message.apply(element));
            }
        }
    }

    private static String libelle(Portee portee) {
        return portee == Portee.FRANCE ? "France" : "Export";
    }

    public long id() {
        return id;
    }

    public List<MarquePresentee> marques() {
        return marques;
    }

    public List<CircuitPresente> circuits() {
        return circuits;
    }

    public List<VisiteProposee> visites() {
        return visites;
    }

    public List<LabelObtenu> labels() {
        return labels;
    }

    public Oenotourisme oenotourisme() {
        return oenotourisme;
    }

    public List<MedailleConcours> medailles() {
        return medailles;
    }
}
