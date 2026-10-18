package fr.bnia.messagerie.application;

import fr.bnia.commun.application.FormatDocument;
import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.DocumentRefuse;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import fr.bnia.messagerie.domain.ObjetMetier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** Faux des ports de la messagerie, pour les tests de cas d'usage. */
final class Faux {

    private Faux() {
    }

    static final class Conversations implements ConversationRepository {

        private final AtomicLong sequence = new AtomicLong(100);
        final Map<Long, Conversation> parId = new LinkedHashMap<>();
        private final Messages messages;

        Conversations(Messages messages) {
            this.messages = messages;
        }

        @Override
        public Conversation inserer(Conversation conversation) {
            Conversation avecId = conversation.avecId(sequence.getAndIncrement());
            parId.put(avecId.id(), avecId);
            return avecId;
        }

        @Override
        public Optional<Conversation> parId(long id) {
            return Optional.ofNullable(parId.get(id));
        }

        @Override
        public Optional<Conversation> parObjet(ObjetRef objet) {
            return parId.values().stream()
                    .filter(c -> c.sujet() instanceof ObjetMetier o && o.objet().equals(objet)).findFirst();
        }

        @Override
        public void supprimer(long id) {
            parId.remove(id);
            messages.parId.values().removeIf(m -> m.conversation() == id);
        }
    }

    static final class Messages implements MessageRepository {

        private final AtomicLong sequence = new AtomicLong(1000);
        final Map<Long, Message> parId = new LinkedHashMap<>();

        @Override
        public Message inserer(Message m) {
            Message copie = copie(m, sequence.getAndIncrement());
            parId.put(copie.id(), copie);
            return copie(copie, copie.id());
        }

        @Override
        public void mettreAJour(Message m) {
            parId.put(m.id(), copie(m, m.id()));
        }

        @Override
        public Optional<Message> parId(long id) {
            return Optional.ofNullable(parId.get(id)).map(m -> copie(m, id));
        }

        @Override
        public List<Message> deLaConversation(long conversation) {
            return parId.values().stream().filter(m -> m.conversation() == conversation).map(m -> copie(m, m.id()))
                    .toList();
        }

        @Override
        public int compter(long conversation) {
            return deLaConversation(conversation).size();
        }

        @Override
        public void supprimer(long id) {
            parId.remove(id);
        }

        @Override
        public void marquerLus(long conversation, Cote cote) {
            for (Message m : deLaConversation(conversation)) {
                parId.put(m.id(), Message.reconstituer(m.id(), m.conversation(), m.auteur(), m.cote(), m.ecritLe(),
                        m.modifieLe(), m.texte(), m.protege(), cote == Cote.OPERATEUR || m.luParOperateur(),
                        cote == Cote.BNIA || m.luParBnia(), m.pieces()));
            }
        }

        private static Message copie(Message m, long id) {
            return Message.reconstituer(id, m.conversation(), m.auteur(), m.cote(), m.ecritLe(), m.modifieLe(),
                    m.texte(), m.protege(), m.luParOperateur(), m.luParBnia(), m.pieces());
        }
    }

    /**
     * Les droits du monde des tests : chaque utilisateur a un rôle local par opérateur (lecture ou gestion, par type),
     * ou appartient au personnel du BNIA (lecture partout, gestion selon le cas).
     */
    static final class Droits implements DroitsMessagerie {

        /** Membres actifs : utilisateur → opérateur → types gérés (un type absent est seulement lu). */
        final Map<Long, Map<Long, Set<TypeObjet>>> membres = new HashMap<>();
        /** Personnel du BNIA : utilisateur → types gérés. */
        final Map<Long, Set<TypeObjet>> bnia = new HashMap<>();

        void membre(long utilisateur, long operateur, TypeObjet... geres) {
            Set<TypeObjet> types = EnumSet.noneOf(TypeObjet.class);
            types.addAll(List.of(geres));
            membres.computeIfAbsent(utilisateur, u -> new HashMap<>()).put(operateur, types);
        }

        void bnia(long utilisateur, TypeObjet... geres) {
            Set<TypeObjet> types = EnumSet.noneOf(TypeObjet.class);
            types.addAll(List.of(geres));
            bnia.put(utilisateur, types);
        }

        void retirer(long utilisateur, long operateur) {
            membres.getOrDefault(utilisateur, new HashMap<>()).remove(operateur);
        }

        @Override
        public Optional<Cote> cote(Acteur acteur, long operateur) {
            if (membres.getOrDefault(acteur.utilisateur(), Map.of()).containsKey(operateur)) {
                return Optional.of(Cote.OPERATEUR);
            }
            return bnia.containsKey(acteur.utilisateur()) ? Optional.of(Cote.BNIA) : Optional.empty();
        }

        @Override
        public boolean lit(Acteur acteur, TypeObjet type, long operateur) {
            return cote(acteur, operateur).isPresent();
        }

        @Override
        public boolean gere(Acteur acteur, TypeObjet type, long operateur) {
            Set<TypeObjet> local = membres.getOrDefault(acteur.utilisateur(), Map.of()).get(operateur);
            if (local != null && local.contains(type)) {
                return true;
            }
            return bnia.getOrDefault(acteur.utilisateur(), Set.of()).contains(type);
        }

        @Override
        public boolean gereMessagerie(Acteur acteur, long operateur) {
            return cote(acteur, operateur).isPresent();
        }

        @Override
        public boolean personnelBnia(Acteur acteur) {
            return bnia.containsKey(acteur.utilisateur());
        }

        @Override
        public Set<TypeObjet> typesLusParLeBnia(Acteur acteur) {
            return bnia.containsKey(acteur.utilisateur()) ? EnumSet.allOf(TypeObjet.class) : Set.of();
        }

        @Override
        public Set<TypeObjet> typesLus(Acteur acteur, long operateur) {
            return membres.getOrDefault(acteur.utilisateur(), Map.of()).containsKey(operateur)
                    ? EnumSet.allOf(TypeObjet.class) : Set.of();
        }

        @Override
        public boolean membreActif(long utilisateur, long operateur) {
            return membres.getOrDefault(utilisateur, Map.of()).containsKey(operateur);
        }

        @Override
        public Optional<String> nom(long utilisateur) {
            return Optional.of("Utilisateur " + utilisateur);
        }

        @Override
        public Map<Long, OperateurNomme> operateurs(Collection<Long> ids) {
            Map<Long, OperateurNomme> noms = new HashMap<>();
            new HashSet<>(ids).forEach(id -> noms.put(id, new OperateurNomme(id, "N" + id, "Opérateur " + id)));
            return noms;
        }
    }

    /** Des objets conversables : identifiant → description ; un absent n'existe pas. */
    static final class Objets implements ObjetsConversables {

        private final TypeObjet type;
        final Map<String, DescriptionObjet> descriptions = new HashMap<>();

        Objets(TypeObjet type) {
            this.type = type;
        }

        @Override
        public TypeObjet type() {
            return type;
        }

        @Override
        public Optional<DescriptionObjet> decrire(String identifiant) {
            return Optional.ofNullable(descriptions.get(identifiant));
        }
    }

    /** Le stockage commun : garde les documents, refuse ce qui ne commence pas par « %PDF » ou « PK ». */
    static final class Stockage implements StockageDocuments {

        private final AtomicLong sequence = new AtomicLong(900);
        final Map<Long, DocumentLu> documents = new LinkedHashMap<>();

        @Override
        public DocumentDepose deposer(String nom, byte[] contenu, Set<FormatDocument> admis) {
            String debut = new String(contenu, 0, Math.min(4, contenu.length), java.nio.charset.StandardCharsets.ISO_8859_1);
            FormatDocument format = debut.startsWith("%PDF") ? FormatDocument.PDF
                    : debut.startsWith("PK") ? FormatDocument.XLSX : null;
            if (format == null || !admis.contains(format)) {
                throw new DocumentRefuse("Le fichier « " + nom + " » n'est pas d'un format admis.");
            }
            DocumentDepose document = new DocumentDepose(sequence.getAndIncrement(), nom, format.type(), contenu.length);
            documents.put(document.cle(), new DocumentLu(document, contenu));
            return document;
        }

        @Override
        public DocumentLu lire(long cle) {
            return Optional.ofNullable(documents.get(cle)).orElseThrow();
        }

        @Override
        public void supprimer(long cle) {
            documents.remove(cle);
        }

        @Override
        public void supprimer(Collection<Long> cles) {
            cles.forEach(documents::remove);
        }
    }

    static List<ObjetsConversables> liste(ObjetsConversables... objets) {
        return new ArrayList<>(List.of(objets));
    }
}
