# Projet Microservices — Application de Location de Logement
**Architecture des Systèmes d'Information — EFREI — Année 2025-2026**


---

## 1. Contexte et objectif

L'objectif est de concevoir et réaliser une application de location de logement (comparable à Airbnb ou Booking) en architecture microservices. Chaque service a une responsabilité claire et peut fonctionner (ou échouer) indépendamment des autres.

Le sujet impose :
- Un découpage en services identifiables avec description des interactions
- La gestion de la configuration, de l'authentification/habilitations et du déploiement
- La réalisation d'au moins 2 services

Le projet en comporte **6 services backend + 2 frontends + 3 BDD + 1 stockage objet**.

---

## 2. Vue d'ensemble de l'architecture

```
Navigateur (HTTPS)
        |
   API Gateway  (:8443)    ← seul point d'entrée exposé
   /      |      \      \        \
auth   user   property  messaging  frontends (client / owner)
        |         |   \      |
      user-db  prop-db MinIO  msg-db
```

| Service | Techno | Port interne | Rôle |
|---|---|---|---|
| api-gateway | Spring Cloud Gateway | 8443 (exposé) | Routage, validation JWT, CORS |
| auth-service | Spring Boot | 8443 | Login, register, génération JWT |
| user-service | Spring Boot + PostgreSQL | 8443 | CRUD utilisateurs |
| property-service | Spring Boot + PostgreSQL + MinIO | 8443 | Logements, réservations, photos |
| messaging-service | Spring Boot + PostgreSQL | 8443 | Messagerie locataire ↔ propriétaire |
| client-frontend | React + Nginx | 80 | Interface locataire |
| owner-frontend | React + Nginx | 80 | Interface propriétaire |
| minio | MinIO | 9000 (exposé) | Stockage photos (S3-compatible) |

---

## 3. Découpage des services

### Principe retenu
> Si un domaine peut fonctionner (ou échouer) indépendamment des autres, il mérite son propre service.

**Exemple concret :** si le messaging-service tombe, les utilisateurs peuvent continuer à chercher des logements et à réserver. Si l'auth-service est indisponible, les logements restent consultables sans connexion.

### Frontières métier
- **auth-service** : authentification transverse — ne gère ni utilisateurs ni logements
- **user-service** : source de vérité des comptes (email, mot de passe haché, rôle, nom)
- **property-service** : logements + réservations regroupés car fortement couplés (une réservation est toujours liée à un logement)
- **messaging-service** : messagerie indépendante, accessible uniquement pour les réservations confirmées
- **frontends** : deux apps séparées car les besoins (locataire vs propriétaire) sont distincts

### Isolation des données
Chaque service possède **sa propre base PostgreSQL**. Aucune base n'est partagée entre services. Le couplage au niveau des données est ainsi éliminé.

---

## 4. Interactions entre les services

### Appels inter-services

| Appelant | Appelé | Raison |
|---|---|---|
| Navigateur | API Gateway | Toutes les requêtes passent par la gateway |
| API Gateway | Tous les services backend | Routage selon le préfixe URL |
| auth-service | user-service | Vérification des identifiants à la connexion |
| Navigateur | MinIO | Upload/téléchargement photos via URLs pré-signées |

**Point clé :** les services backend ne s'appellent pas directement entre eux (sauf auth → user-service). La coordination est faite côté client (frontend). Communication : HTTP REST en HTTPS uniquement, pas de message broker.

### Flux d'authentification
```
Navigateur → POST /api/auth/login → API Gateway
  → auth-service → GET /users/email/{email} → user-service
  ← JWT Token ←
Navigateur → GET /api/properties (+ JWT) → API Gateway
  → [validation JWT] → ajout headers X-User-Email, X-User-Role
  → property-service ← liste des logements
```

### Flux upload photo (MinIO)
```
Navigateur → POST /api/properties/{id}/photos/upload-url → property-service
  → génère presigned URL (localhost:9000)
Navigateur → PUT fichier image (presigned URL) → MinIO directement
  (le backend ne reçoit jamais le fichier)
```

---

## 5. Gestion de la configuration

- Chaque service a son propre `application.yml`
- Les valeurs sensibles (clé JWT, credentials BDD, MinIO) sont externalisées via **variables d'environnement** dans `docker-compose.yml`
- Pattern : `${JWT_SECRET:-valeur_par_defaut}` — valeur par défaut pour le dev local, remplaçable en production
- Pas de Spring Cloud Config (inutile à ce nombre de services)

---

## 6. Gestion de l'authentification et des habilitations

- **auth-service** génère un JWT contenant l'email et le rôle (TENANT / OWNER), valable 24h
- La **gateway** valide le JWT sur toutes les routes protégées (sans appel réseau — clé partagée)
- Routes publiques : consultation des logements, register, login
- Après validation, la gateway injecte les headers `X-User-Email` et `X-User-Role` dans la requête transmise au service backend
- La clé JWT est partagée via une variable d'environnement commune (`JWT_SECRET`)

---

## 7. Sécurité réseau — HTTPS inter-services

- Tous les services backend communiquent en **HTTPS** (port 8443)
- Un script `generate-certs.sh` génère dans un conteneur Alpine :
  - Une CA locale (`ca-cert.pem`)
  - Un certificat serveur avec SAN (couvrant tous les noms de services Docker)
  - Un keystore PKCS12 partagé entre tous les services Spring Boot
  - Un truststore PKCS12
- Les frontends (HTTP interne) sont exposés en HTTPS uniquement via la gateway
- MinIO reste en HTTP (exposé directement pour les presigned URLs)

---

## 8. Gestion du déploiement

### Docker & Docker Compose
- Chaque service est conteneurisé avec un **Dockerfile multi-stage** :
  1. Build avec Maven (image `maven:3-openjdk-17`)
  2. Exécution avec JRE Alpine (image légère)
- Frontends : build Node.js, servi par Nginx
- Un seul réseau Docker `backend` — les services se découvrent par nom DNS
- MinIO init : conteneur `minio-init` qui crée le bucket et définit les droits anonymes

### Volumes persistants
```
user-db-data, property-db-data, messaging-db-data, minio-data
```

### Makefile (seul prérequis : Docker)
| Commande | Action |
|---|---|
| `make up` | Génère les certs + build + lance tout |
| `make restart` | Stop + relance |
| `make reset-db` | Vide les BDD + relance (fixtures rechargées) |
| `make logs` | Logs de tous les services |
| `make log S=xxx` | Logs d'un service spécifique |
| `make clean` | Tout supprimer (conteneurs, volumes, certs) |

### Fixtures automatiques
Données de démo injectées au démarrage via `CommandLineRunner` Spring Boot :
- Owner : `owner@test.com` / `password123`
- Tenant : `tenant@test.com` / `password123`
- 5 logements de démonstration

---

## 9. Résilience

- **Gateway** : gestionnaire d'erreurs qui intercepte les erreurs de connexion → retourne un JSON 503 avec le nom du service en panne (au lieu d'une erreur réseau incompréhensible)
- **Frontends** : composant `ServiceUnavailable` s'affiche avec bouton "Réessayer" si un service ne répond pas
- **Page `/status`** : état de chaque service en temps réel (pastilles vertes/rouges)
- **Format d'erreur uniforme** sur tous les services :
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Logement non trouvé : 42",
  "service": "property-service",
  "timestamp": "2026-04-07T12:00:00"
}
```

---

## 10. Stack technique complète

| Composant | Technologie |
|---|---|
| Services backend | Java 17, Spring Boot 3.2.5 |
| Gateway | Spring Cloud Gateway 2023.0.1 |
| Authentification | JWT (bibliothèque jjwt 0.12.5) |
| Bases de données | PostgreSQL 15 |
| Stockage photos | MinIO (compatible S3) |
| Frontends | React 18, Vite, React Router |
| Conteneurisation | Docker, Docker Compose |
| Communication | HTTPS avec certificats auto-signés (PKCS12) |
| Logging | SLF4J (inclus Spring Boot) |

---

## 11. Routes API (toutes préfixées `/api/`)

```
POST   /api/auth/register
POST   /api/auth/login

GET    /api/users
POST   /api/users
GET    /api/users/{id}
GET    /api/users/email/{email}

GET    /api/properties
POST   /api/properties
PUT    /api/properties/{id}
DELETE /api/properties/{id}
GET    /api/properties/owner/{ownerId}

POST   /api/properties/{id}/photos/upload-url
GET    /api/properties/{id}/photos
DELETE /api/properties/{id}/photos/{objectKey}

POST   /api/reservations
GET    /api/reservations/tenant/{id}
GET    /api/reservations/property/{id}
PUT    /api/reservations/{id}/status

GET/POST /api/messages/...
```

---

## 12. Accès

- Application locataire : https://localhost:8443
- Espace propriétaire : https://localhost:8443/owner/
- MinIO Console : http://localhost:9001 (minioadmin / minioadmin)
