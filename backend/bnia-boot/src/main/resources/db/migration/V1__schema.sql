-- Schéma de la plateforme BNIA.
--
-- Migration unique pendant le prototype (ADR-005) : ce fichier est modifié sur place à chaque
-- incrément, jamais complété par une V2. En profil dev, la base est recréée
-- automatiquement au démarrage si ce fichier a changé (MigrationDevConfiguration).
-- Les données de démonstration n'y figurent pas (profil demo).
--
-- Convention (ADR-001) : tables et colonnes en français, sans accents, en snake_case.
-- Une section et un schéma PostgreSQL par bounded context, dans l'ordre de docs/contextes.md.
-- Les schémas sont créés par Flyway (spring.flyway.schemas dans application.yml), pas ici.

-- =====================================================================================================
-- 1. referentiels (spec 01) : données de référence administrées par le BNIA.
--    Référencées par code depuis les autres contextes (ADR-008). Aucune ligne n'est supprimée :
--    elle est désactivée (RG-REF-04). Code unique sans distinction de casse (RG-REF-01).
-- =====================================================================================================
create table referentiels.zone_commerciale (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index zone_commerciale_code_unique on referentiels.zone_commerciale (lower(code));

-- Qualification (C.10.8)
create table referentiels.titre (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index titre_code_unique on referentiels.titre (lower(code));

create table referentiels.label (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index label_code_unique on referentiels.label (lower(code));

create table referentiels.type_visite (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index type_visite_code_unique on referentiels.type_visite (lower(code));

create table referentiels.langue (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index langue_code_unique on referentiels.langue (lower(code));

create table referentiels.centre_interet (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index centre_interet_code_unique on referentiels.centre_interet (lower(code));

-- Présentation et commercialisation (C.10.9)
create table referentiels.circuit_distribution (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index circuit_distribution_code_unique on referentiels.circuit_distribution (lower(code));

create table referentiels.produit_commercialise (
    code    text    primary key,
    libelle text    not null,
    actif   boolean not null default true
);
create unique index produit_commercialise_code_unique on referentiels.produit_commercialise (lower(code));

-- Libellé unique à la casse, aux accents et aux espaces près, inactives comprises (RG-REF-15).
create table referentiels.marque (
    code              text    primary key,
    libelle           text    not null,
    libelle_normalise text    not null unique,
    actif             boolean not null default true
);
create unique index marque_code_unique on referentiels.marque (lower(code));
