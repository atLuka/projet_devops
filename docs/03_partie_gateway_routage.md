# Partie 3 — API Gateway & Routag
**Durée estimée : ~1 min 30** | **Slide : 5**

---

## Ce que tu présentes

Tu expliques le rôle de l'API Gateway : pourquoi c'est le seul point d'entrée, comment le routage fonctionne concrètement, et comment les deux frontends sont servis depuis la même URL.

---

## Plan de ta prise de parole

### 1. Pourquoi un point d'entrée unique ? (20 sec)

Sans gateway, le navigateur devrait connaître l'adresse de chaque service. Cela pose plusieurs problèmes :
- Les services internes seraient exposés directement → **surface d'attaque plus grande**
- La configuration réseau serait complexe côté client
- La gestion du CORS, de l'authentification serait dupliquée dans chaque service

Avec une gateway : **un seul port exposé (8443)**, les services internes sont invisibles depuis l'extérieur.

### 2. Technologie : Spring Cloud Gateway (20 sec)

On a choisi **Spring Cloud Gateway** car c'est la solution standard de l'écosystème Spring Boot — cohérent avec nos services backend. Il permet de configurer toutes les routes dans un seul fichier YAML sans écrire de logique complexe.

La gateway tourne sur le **port 8443 en HTTPS**. C'est le seul port exposé à l'extérieur (avec MinIO sur 9000 pour les photos).

### 3. Le routage par préfixe URL (40 sec)

Le routage fonctionne par préfixe d'URL. Extrait du fichier `application.yml` de la gateway :

```yaml
routes:
  - id: auth-service
    uri: https://auth-service:8443
    predicates:
      - Path=/api/auth/**
    filters:
      - StripPrefix=1        # supprime le /api avant de forwarder

  - id: property-service
    uri: https://property-service:8443
    predicates:
      - Path=/api/properties/**
    filters:
      - StripPrefix=1

  - id: owner-frontend
    uri: http://owner-frontend:80
    predicates:
      - Path=/owner/**       # → interface propriétaire

  - id: client-frontend
    uri: http://client-frontend:80
    predicates:
      - Path=/**             # → tout le reste = interface locataire
```

Ce que ça donne en pratique :

| URL reçue par la gateway | Forwardée vers |
|---|---|
| `/api/auth/login` | `https://auth-service:8443/auth/login` |
| `/api/properties` | `https://property-service:8443/properties` |
| `/api/messages` | `https://messaging-service:8443/messages` |
| `/owner/` | `http://owner-frontend:80/owner/` |
| `/` (tout le reste) | `http://client-frontend:80/` |

**Le filtre `StripPrefix=1`** supprime le préfixe `/api` avant de transmettre la requête au service. Ainsi les services n'ont pas à connaître le préfixe — ils reçoivent juste `/properties`, `/auth`, etc.

### 4. Les frontends servis via la gateway (20 sec)

Les deux applications React sont servies par **Nginx en HTTP interne** (port 80). Elles ne sont pas exposées directement — elles passent par la gateway HTTPS.

Résultat : depuis le navigateur, tout est sur `https://localhost:8443`. La gateway se charge de router vers le bon frontend selon le chemin :
- `https://localhost:8443/` → interface locataire
- `https://localhost:8443/owner/` → interface propriétaire

### 5. CORS & gestion centralisée (10 sec)

La configuration CORS est gérée **une seule fois dans la gateway** pour toute l'application :
```yaml
globalcors:
  corsConfigurations:
    "[/**]":
      allowedOrigins: "https://localhost:8443"
      allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
      allowCredentials: true
```
Les services backend n'ont pas à gérer le CORS individuellement.

---

## Points clés à retenir pour les questions

- La gateway est le **seul composant** qui fait la validation JWT (pas les services)
- Si un service backend tombe, la gateway retourne un **JSON 503** propre avec le nom du service — pas une erreur réseau brute
- Le fichier de configuration de la gateway est entièrement **déclaratif (YAML)** — pas de code Java pour le routage
- `useInsecureTrustManager: true` est configuré pour accepter les certificats auto-signés des services internes (acceptable en développement)

---

## Transition vers Luka
> "La gateway route les requêtes, mais elle fait aussi la validation des tokens d'authentification. Luka va nous expliquer comment fonctionne l'authentification et la sécurité de nos communications."
