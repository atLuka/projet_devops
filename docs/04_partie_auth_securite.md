# Partie 4 — Authentification & Sécurit
**Durée estimée : ~2 min** | **Slides : 6 & 7**

---

## Ce que tu présentes

Tu couvres deux axes : l'authentification (JWT, auth-service, rôles) et la sécurité réseau (HTTPS entre services, certificats). C'est la partie la plus technique — c'est un point fort du projet.

---

## Plan de ta prise de parole

### 1. Vue d'ensemble du mécanisme (15 sec)

Notre sécurité repose sur deux couches :
1. **JWT** pour l'authentification des utilisateurs (qui êtes-vous ?)
2. **HTTPS** pour sécuriser toutes les communications réseau (même internes)

### 2. Le flux d'authentification JWT (45 sec)

```
1. Navigateur → POST /api/auth/login (email + mot de passe)
2. Gateway → forward vers auth-service
3. auth-service → GET /users/email/{email} → user-service
                  (vérifie les identifiants)
4. auth-service → génère un JWT (signé avec la clé secrète)
5. JWT retourné au navigateur

Ensuite, pour chaque requête protégée :
6. Navigateur → GET /api/properties + header Authorization: Bearer <JWT>
7. Gateway → valide le JWT (sans appel réseau, clé partagée)
8. Gateway → ajoute les headers X-User-Email et X-User-Role
9. Gateway → forward vers property-service avec les headers
10. property-service → connaît l'identité sans redemander
```

**Pourquoi le auth-service n'a pas sa propre BDD ?**
Il délègue le stockage au user-service. Cela évite la duplication des données utilisateurs. L'auth-service ne fait que vérifier et signer.

### 3. Contenu et durée du JWT (20 sec)

Le token JWT contient :
- L'**email** de l'utilisateur
- Le **rôle** : `TENANT` (locataire) ou `OWNER` (propriétaire)
- Une **date d'expiration** : 24 heures

La bibliothèque utilisée est **jjwt 0.12.5**.

La clé secrète est partagée entre l'auth-service (qui signe) et la gateway (qui vérifie). Elle est injectée via une variable d'environnement commune `JWT_SECRET` dans Docker Compose. La validation par la gateway est **locale** — pas de round-trip réseau pour chaque requête.

### 4. Routes publiques vs protégées (15 sec)

| Routes publiques (pas de JWT requis) | Routes protégées (JWT obligatoire) |
|---|---|
| GET /api/properties (consultation) | POST /api/reservations |
| POST /api/auth/login | Toute modification de profil |
| POST /api/auth/register | Accès à la messagerie |

La gateway distingue automatiquement ces routes dans sa configuration.

### 5. HTTPS entre tous les services backend (30 sec)

Tous les services backend communiquent en **HTTPS** — y compris les appels internes entre la gateway et les services, et entre l'auth-service et le user-service.

**Comment les certificats sont-ils générés ?**

Le script `generate-certs.sh` s'exécute dans un conteneur Alpine (pas besoin d'OpenSSL sur la machine hôte) et produit :

```
CA locale (MicroservicesCA)
  └── Certificat serveur RSA 2048 bits
        → SAN couvrant tous les noms Docker :
          localhost, api-gateway, auth-service,
          user-service, property-service,
          client-frontend, owner-frontend, minio
        → keystore.p12 (PKCS12, partagé entre tous les services)
        → truststore.p12
```

Le même `keystore.p12` est monté en volume dans chaque conteneur Spring Boot (`/app/certs/keystore.p12`). La configuration est identique dans tous les `application.yml` :

```yaml
server:
  ssl:
    key-store: /app/certs/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

**Note :** La gateway utilise `useInsecureTrustManager: true` pour les appels vers les services internes (acceptable en développement avec des certificats auto-signés).

### 6. Habilitations : rôle dans le token (15 sec)

Le rôle est encodé dans le JWT. La gateway l'extrait et l'injecte dans le header `X-User-Role`. Les services backend peuvent ainsi contrôler l'accès selon le rôle sans valider eux-mêmes le token.

Exemple : le property-service vérifie que `X-User-Role: OWNER` pour autoriser la création d'un logement.

---

## Points clés à retenir pour les questions

- La validation JWT se fait **dans la gateway** — les services backend n'ont pas à implémenter la sécurité individuellement
- Les certificats couvrent **tous les noms DNS Docker** via le champ SAN (Subject Alternative Name)
- On n'a **pas mis en place Spring Security** dans les services backend — la gateway fait office de premier rempart
- La clé JWT est la même en production et en dev (variable d'environnement à changer en prod)

---

## Transition vers Ayoub
> "L'authentification est sécurisée, les requêtes arrivent aux bons services. Ayoub va nous présenter ce que font concrètement ces services métier et comment ils gèrent leurs données."
