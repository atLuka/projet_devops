# Partie 2 — Architecture & Découpage des service
**Durée estimée : ~2 min**

---

## Ce que tu présentes

Tu expliques **pourquoi** on a choisi ce découpage, les principes qui nous ont guidés, et la différence avec un monolithe. C'est la partie la plus "conceptuelle" — c'est celle qui répond directement aux critères du sujet.

---

## Plan de ta prise de parole

### 1. Le principe directeur (25 sec)
Avant de commencer le développement, on a identifié les **domaines métier** de l'application. Notre règle était simple :

> **Si un domaine peut fonctionner (ou échouer) indépendamment des autres, il mérite son propre service.**

Exemples concrets qu'on peut citer :
- Si le **messaging-service tombe**, les utilisateurs peuvent quand même chercher des logements et réserver — la messagerie ne bloque rien
- Si l'**auth-service est indisponible**, les logements restent consultables sans connexion
- Si le **property-service tombe**, les utilisateurs peuvent toujours se connecter

Ce raisonnement nous a conduit à identifier **6 services backend** et **2 frontends**.

### 2. Présentation de chaque service (45 sec)

```
┌─────────────────────────────────────────────────────────┐
│                      API Gateway                         │
│              (seul point d'entrée, HTTPS 8443)           │
└────┬──────┬──────────┬──────────────┬───────────────────┘
     │      │          │              │
  auth   user      property      messaging
  service service   service       service
     │      │       /    \            │
     │   user-db  prop-db MinIO   msg-db
     └──→ user-service
          (auth appelle user)
```

| Service | Responsabilité | BDD propre |
|---|---|---|
| **api-gateway** | Routage, validation JWT, point d'entrée unique | Non |
| **auth-service** | Login/register, génération de tokens JWT | Non (délègue à user-service) |
| **user-service** | Stockage et gestion des comptes utilisateurs | Oui (user-db) |
| **property-service** | Logements, réservations, photos | Oui (property-db + MinIO) |
| **messaging-service** | Conversations locataire ↔ propriétaire | Oui (messaging-db) |
| **frontends** | Interfaces React (locataire / propriétaire) | — |

### 3. Pourquoi ces regroupements ? (30 sec)

**Pourquoi auth-service n'a pas sa propre BDD ?**
L'auth-service ne gère pas les utilisateurs — il les *vérifie*. Stocker les credentials dans une base séparée aurait créé une duplication. Il délègue au user-service et génère un token. Ce choix évite toute duplication de données.

**Pourquoi logements + réservations sont dans le même service ?**
Ces deux domaines sont **fortement couplés** : une réservation est toujours liée à un logement. Séparer les deux aurait généré énormément de communications inter-services pour des opérations simples (vérifier la disponibilité lors d'une réservation, par exemple).

**Pourquoi deux frontends séparés ?**
Les besoins sont différents : le locataire cherche et réserve, le propriétaire gère. Ce sont deux applications React distinctes, mais elles partagent le même design system pour la cohérence visuelle.

### 4. Isolation des données (20 sec)

C'est un principe fondamental des microservices : **chaque service possède sa propre base de données**. Aucune base n'est partagée.

- user-service → `user-db` (PostgreSQL)
- property-service → `property-db` (PostgreSQL) + MinIO (photos)
- messaging-service → `messaging-db` (PostgreSQL)

Cela empêche le **couplage fort au niveau des données**. Si une BDD tombe, seul le service correspondant est impacté.

### 5. Comparaison avec un monolithe (20 sec)

| | Monolithe | Notre architecture microservices |
|---|---|---|
| Déploiement | Un seul déploiement global | Chaque service indépendant |
| Panne | Toute l'app tombe | Seul le service concerné est impacté |
| BDD | Une base partagée | Une BDD par service |
| Complexité | Simple à démarrer | Plus complexe, mais résilient |

---

## Points clés à retenir pour les questions

- On n'a **pas utilisé de message broker** (RabbitMQ, Kafka) — les interactions sont synchrones et simples pour notre cas d'usage
- Le **frontend orchestre** les appels : il appelle directement les services via la gateway, il n'y a pas de service d'orchestration central
- Les services ne se connaissent pas entre eux (sauf auth → user-service)

---

## Transition vers Nam
> "On a donc 6 services backend. Mais comment une requête d'un navigateur arrive-t-elle au bon service ? Nam va nous expliquer le rôle de l'API Gateway."
