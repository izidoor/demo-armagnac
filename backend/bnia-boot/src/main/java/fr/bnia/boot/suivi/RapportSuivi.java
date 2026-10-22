package fr.bnia.boot.suivi;

import java.util.Locale;
import java.util.Optional;

/**
 * Rapports de l'export du suivi, en SQL. Une visite regroupe les événements du front d'une session, coupée par toute
 * pause de plus de 30 minutes ; le temps passé sur un événement va jusqu'au suivant de la session, 90 secondes au plus
 * (le front signale sa présence chaque minute), et le dernier compte pour rien. Les téléchargements et imports, relevés
 * par le serveur, se rangent dans l'étape du parcours où ils ont eu lieu. Heures de Paris.
 */
enum RapportSuivi {

    /**
     * Une ligne par visite, avec son parcours : les écrans dans l'ordre, chacun avec ses minutes et, entre crochets, les
     * fichiers téléchargés ou importés. Des événements consécutifs au même lieu (présence, même écran) forment une
     * seule étape.
     */
    VISITES(Sql.EN_VISITES + """
            , p as (
                select *, lag(lieu) over (partition by session, visite order by instant) as precedent
                from v
            ), q as (
                select *, count(*) filter (where precedent is null or lieu <> precedent)
                              over (partition by session, visite order by instant) as etape
                from p
            ), etapes as (
                select session, visite, email, appareil, etape, min(lieu) as lieu,
                       min(instant) as debut, max(instant) as fin, sum(temps) as temps
                from q
                group by session, visite, email, appareil, etape
            ), bornes as (
                select *, coalesce(lead(debut) over (partition by session, visite order by debut),
                                   fin + interval '30 minutes') as jusqua
                from etapes
            )
            select b.email, b.appareil,
                   to_char(min(b.debut) at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI') as debut,
                   to_char(max(b.fin) at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI') as fin,
                   round(extract(epoch from max(b.fin) - min(b.debut)) / 60) as minutes,
                   string_agg(b.lieu || ' (' || round(extract(epoch from b.temps) / 60, 1) || ')'
                              || coalesce(' [' || f.fichiers || ']', ''), ' > ' order by b.debut) as parcours
            from bornes b
            left join lateral (
                select string_agg(case a.type when 'IMPORT' then 'import ' else 'téléchargement ' end
                                  || coalesce(nullif(a.detail, ''), a.lieu)
                                  || case when a.statut >= 400 then ' refusé ' || a.statut else '' end,
                                  ', ' order by a.instant) as fichiers
                from suivi.evenement a
                where a.session = b.session and a.type in ('TELECHARGEMENT', 'IMPORT')
                  and a.instant >= b.debut and a.instant < b.jusqua
            ) f on true
            group by b.session, b.visite, b.email, b.appareil
            order by min(b.debut)"""),

    /** Les écrans de l'application, hors offre, du plus regardé au moins regardé. */
    ECRANS(Sql.EN_VISITES + """
            select lieu as ecran,
                   count(*) filter (where type = 'ECRAN') as vues,
                   count(distinct email) as visiteurs,
                   round(extract(epoch from sum(temps)) / 60, 1) as minutes
            from v
            where lieu not like 'offre/%'
            group by lieu
            order by minutes desc, vues desc"""),

    /** Les sections de « Voir l'offre », dans l'ordre du document. */
    OFFRE(Sql.EN_VISITES + """
            select substr(lieu, 7) as section,
                   count(*) filter (where type = 'OFFRE') as affichages,
                   count(distinct email) as lecteurs,
                   round(extract(epoch from sum(temps)) / 60, 1) as minutes
            from v
            where lieu like 'offre/%'
            group by lieu
            order by lieu"""),

    /** Une ligne par e-mail, du plus récent au plus ancien. */
    PERSONNES(Sql.EN_VISITES + """
            select email,
                   string_agg(distinct appareil, ', ') as appareils,
                   count(distinct session || '/' || visite) as visites,
                   round(extract(epoch from sum(temps)) / 60) as minutes,
                   (select count(*) from suivi.evenement a
                    where a.email = v.email and a.type = 'TELECHARGEMENT' and a.instant >= """ + Sql.DEPUIS + """
                   ) as telechargements,
                   (select count(*) from suivi.evenement a
                    where a.email = v.email and a.type = 'IMPORT' and a.instant >= """ + Sql.DEPUIS + """
                   ) as imports,
                   to_char(min(instant) at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI') as premiere,
                   to_char(max(instant) at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI') as derniere
            from v
            group by email
            order by max(instant) desc"""),

    /** Les fichiers téléchargés (exports compris) et importés, du plus ancien au plus récent. */
    FICHIERS("""
            select to_char(instant at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI:SS') as instant,
                   email, appareil, type, detail as fichier, statut, lieu as adresse
            from suivi.evenement
            where type in ('TELECHARGEMENT', 'IMPORT') and instant >= """ + Sql.DEPUIS + """

            order by instant"""),

    /** Les événements bruts. */
    EVENEMENTS("""
            select to_char(instant at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI:SS') as instant,
                   email, appareil, type, lieu, detail, statut, session
            from suivi.evenement
            where instant >= """ + Sql.DEPUIS + """

            order by instant"""),

    /**
     * Dernière connexion de chaque compte, lue dans la base courante, pas dans le suivi : ce qui précède sa mise en
     * place, tant que la démonstration n'est pas remise à zéro.
     */
    CONNEXIONS("""
            select u.email,
                   to_char(c.derniere_connexion at time zone 'Europe/Paris', 'YYYY-MM-DD HH24:MI') as derniere_connexion
            from authentification.compte c
            join operateurs.utilisateur u on u.id = c.utilisateur_id
            where c.derniere_connexion >= """ + Sql.DEPUIS + """

            order by c.derniere_connexion desc""");

    private final String sql;

    RapportSuivi(String sql) {
        this.sql = sql;
    }

    String sql() {
        return sql;
    }

    static Optional<RapportSuivi> parNom(String nom) {
        try {
            return Optional.of(valueOf(nom.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException inconnu) {
            return Optional.empty();
        }
    }

    private static final class Sql {

        /** Minuit, heure de Paris, du jour {@code :depuis}. */
        static final String DEPUIS = "cast(:depuis as date)::timestamp at time zone 'Europe/Paris'";

        /**
         * Les événements du front depuis {@code :depuis}, avec leur pause, leur visite et le temps passé. Les
         * téléchargements et imports n'y sont pas : ils ne marquent ni étape ni présence.
         */
        static final String EN_VISITES = """
            with e as (
                select instant, session, email, appareil, type, lieu,
                       instant - lag(instant) over (partition by session order by instant) as pause,
                       lead(instant) over (partition by session order by instant) as suivant
                from suivi.evenement
                where type in ('ECRAN', 'OFFRE', 'PRESENCE') and instant >= """ + DEPUIS + """

            ), v as (
                select *,
                       count(*) filter (where pause is null or pause > interval '30 minutes')
                           over (partition by session order by instant) as visite,
                       case when suivant is null then interval '0'
                            else least(suivant - instant, interval '90 seconds') end as temps
                from e
            )
            """;
    }
}
