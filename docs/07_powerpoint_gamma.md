# Prompt Gamma — Présentation Microservices
**A utiliser directement dans Gamma (gamma.app)**

---

## Instructions d'utilisation

1. Aller sur [gamma.app](https://gamma.app)
2. Cliquer sur "Créer" → "Présentation"
3. Choisir "Générer avec l'IA"
4. Coller le prompt ci-dessous

---

## Prompt à coller dans Gamma

```
Crée une présentation professionnelle en français de 10 slides sur notre projet d'école d'ingénieurs.

Titre : "Application de Location de Logement — Architecture Microservices"
Sous-titre : "EFREI — Architecture des Systèmes d'Information — 2025-2026"
Groupe : Bastien, Ghislain, Nam, Luka, Ayoub, Kais

Style : moderne, sobre, couleurs bleu foncé et blanc, icônes techniques

---

SLIDE 1 — Titre
Titre : Application de Location de Logement
Sous-titre : Architecture Microservices
Mention : EFREI 2025-2026 | Groupe de 6 étudiants

---

SLIDE 2 — Introduction & Objectif
Titre : Un projet inspiré d'Airbnb
Contenu :
- Créer une plateforme de location de logement basée sur les microservices
- Deux profils : locataire (recherche, réservation) et propriétaire (gestion)
- Objectif pédagogique : mettre en pratique les principes d'architecture distribuée
- Contraintes : découpage des services, authentification, configuration, déploiement
Visuel suggéré : icône maison + réseau de services connectés

---

SLIDE 3 — Architecture globale
Titre : Vue d'ensemble — 12+ conteneurs Docker
Contenu :
- 1 seul point d'entrée : API Gateway (HTTPS 8443)
- 4 services backend métier : auth, user, property, messaging
- 2 frontends React : locataire (/), propriétaire (/owner/)
- 3 bases PostgreSQL (une par service métier)
- 1 stockage objet MinIO (photos)
Visuel suggéré : schéma en arbre avec la gateway au centre, services en branches

---

SLIDE 4 — Découpage des services
Titre : Principe de découpage — 1 service = 1 responsabilité
Contenu :
- Règle : si un domaine peut échouer indépendamment → service dédié
- auth-service : authentification seule (pas de BDD propre, délègue à user-service)
- user-service : source de vérité des comptes utilisateurs
- property-service : logements + réservations (couplés → même service)
- messaging-service : messagerie locataire ↔ propriétaire
- Isolation des données : chaque service a sa propre PostgreSQL
Visuel suggéré : tableau comparatif microservices vs monolithe

---

SLIDE 5 — API Gateway & Routage
Titre : L'API Gateway — chef d'orchestre des requêtes
Contenu :
- Spring Cloud Gateway : configuration 100% déclarative (YAML)
- Routing par préfixe URL : /api/auth/** → auth-service, /api/properties/** → property-service
- Filtre StripPrefix : supprime /api avant de transmettre au service
- Frontends servis via la gateway : / → locataire, /owner/ → propriétaire
- CORS géré une seule fois dans la gateway
Visuel suggéré : diagramme de flux de requête navigateur → gateway → service

---

SLIDE 6 — Authentification JWT
Titre : Authentification — JWT & propagation des rôles
Contenu :
- Flux : login → auth-service → user-service → génération JWT (24h)
- Token contient : email + rôle (TENANT ou OWNER)
- Validation dans la gateway uniquement (pas dans les services)
- Après validation : gateway injecte X-User-Email et X-User-Role
- Clé JWT partagée via variable d'environnement (JWT_SECRET)
Visuel suggéré : diagramme de séquence du flux d'authentification

---

SLIDE 7 — Sécurité réseau HTTPS
Titre : HTTPS entre tous les services — même en interne
Contenu :
- Tous les services backend communiquent en HTTPS (port 8443)
- Certificats auto-signés : CA locale + certificat RSA 2048 avec SAN
- SAN couvre tous les noms DNS Docker (api-gateway, auth-service, user-service...)
- Keystore PKCS12 partagé entre tous les services Spring Boot
- Génération automatisée via generate-certs.sh (conteneur Alpine, sans OpenSSL local)
Visuel suggéré : cadenas avec réseau de services connectés en HTTPS

---

SLIDE 8 — Gestion des photos (MinIO)
Titre : Upload de photos — le backend ne touche pas les fichiers
Contenu :
- Flux upload : frontend → demande presigned URL → MinIO directement
- Le property-service ne stocke que la clé d'objet en base (string)
- Flux affichage : property-service génère presigned download URL → navigateur → MinIO
- MinIO = compatible S3 (même API qu'Amazon S3)
- Avantage : backend léger, pas de fichiers volumineux en transit
Visuel suggéré : diagramme de séquence upload photo en 4 étapes

---

SLIDE 9 — Déploiement & Résilience
Titre : Docker Compose + Résilience par conception
Contenu :
Déploiement :
- 1 seul prérequis : Docker
- make up → génère certs + build + lance tout
- Dockerfiles multi-stage : build Maven → JRE Alpine
- Fixtures de démo auto-injectées au démarrage (CommandLineRunner)
Résilience :
- Gateway retourne un JSON 503 propre si un service est en panne
- Frontends affichent "Service indisponible" avec bouton Réessayer
- Page /status : état de chaque service en temps réel
Visuel suggéré : icône Docker + pastilles vertes/rouges pour la résilience

---

SLIDE 10 — Conclusion & Perspectives
Titre : Bilan — Ce qu'on a appris
Contenu :
Points forts de l'architecture :
- Résilience : un service en panne n'impacte pas les autres
- Isolation : une BDD par service, pas de couplage de données
- Déploiement : une commande suffit pour tout lancer
Défis rencontrés :
- Gestion du HTTPS entre services (certificats, truststore)
- Propagation de l'authentification JWT
- Complexité d'orchestration (12+ conteneurs)
Améliorations possibles en production :
- Spring Cloud Config (configuration centralisée)
- Eureka (service discovery)
- Message broker (RabbitMQ) pour les communications asynchrones
```

---

## Notes pour la personnalisation dans Gamma

Une fois la présentation générée, vous pouvez :
- Ajouter les captures d'écran de l'application dans les slides 3, 8, 9
- Insérer le schéma d'architecture du rapport (Figure 1) dans la slide 3
- Insérer le diagramme de séquence auth (Figure 2) dans la slide 6
- Insérer le diagramme de séquence MinIO (Figure 6) dans la slide 8
- Insérer le diagramme Docker Compose (Figure 7) dans la slide 9
- Adapter les couleurs au thème de votre école si nécessaire
