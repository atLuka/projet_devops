# Partie 1 — Introduction & Présentation du proje
**Durée estimée : ~1 min 30**

---

## Ce que tu présentes

Tu ouvres la présentation. Ton rôle est de poser le contexte, expliquer le sujet, et donner une vue macro de ce qu'on a construit — sans entrer dans les détails techniques (ce sont les parties suivantes).

---

## Plan de ta prise de parole

### 1. Le sujet (20 sec)
Le sujet demande de créer une **application de location de logement** basée sur l'architecture microservices. L'idée : reproduire quelque chose qui ressemble à Airbnb ou Booking, mais en mettant en avant la façon dont l'application est **découpée et structurée**, pas juste les fonctionnalités.

Le sujet imposait :
- Un découpage en services avec description des interactions
- La gestion de la configuration, de l'authentification, du déploiement
- La réalisation d'au moins 2 services — **on en a fait 6**

### 2. Pourquoi ce cas d'usage ? (20 sec)
La location de logement se prête particulièrement bien aux microservices parce que les domaines métier sont naturellement séparés :
- Gérer des **utilisateurs** (comptes, rôles)
- Gérer des **logements** et des **réservations**
- Gérer de la **messagerie**
- Gérer de l'**authentification**

Chacun de ces domaines peut vivre (et tomber en panne) indépendamment.

### 3. Ce qu'on a construit — vue macro (30 sec)
Notre application comporte :

| Composant | Nombre |
|---|---|
| Services backend | 6 (gateway, auth, users, property, messaging + 1 BDD chacun) |
| Interfaces frontend | 2 (locataire + propriétaire) |
| Bases de données | 3 PostgreSQL (une par service métier) |
| Stockage | 1 MinIO (photos des logements) |
| Conteneurs Docker au total | 12+ |

**Un seul point d'entrée** : tout passe par l'API Gateway sur le port 8443 en HTTPS.

### 4. Les deux profils utilisateur (20 sec)
L'application sert deux types d'utilisateurs :

**Le locataire** peut :
- Rechercher des logements (par ville, dates, type, prix)
- Réserver un logement et suivre ses réservations
- Communiquer avec le propriétaire via la messagerie

**Le propriétaire** peut :
- Ajouter, modifier, supprimer ses logements avec photos
- Accepter ou refuser des demandes de réservation
- Communiquer avec ses locataires

---

## Points clés à retenir pour les questions

- On est 6 dans le groupe, chacun a travaillé sur une partie de l'architecture
- Le projet tourne avec un seul prérequis : **Docker** (`make up` suffit pour tout lancer)
- Les données de démo se rechargent automatiquement au démarrage (fixtures)
- On a plus de 12 conteneurs Docker qui tournent ensemble

---

## Transition vers Ghislain
> "Maintenant que vous avez la vue d'ensemble, Ghislain va vous expliquer comment on a réfléchi au découpage de nos services et pourquoi on a fait ces choix."
