# Partie 6 — Déploiement & Infrastructur
**Durée estimée : ~1 min 30** | **Slides : 9 & 10**

---

## Ce que tu présentes

Tu expliques comment toute l'application est conteneurisée et orchestrée avec Docker Compose, comment le réseau est isolé, comment les certificats sont générés automatiquement, et comment on lance tout avec une seule commande.

---

## Plan de ta prise de parole

### 1. Docker — un conteneur par service (20 sec)

Chaque service est empaqueté dans sa propre **image Docker**. On utilise un pattern **multi-stage build** pour garder des images légères :

```dockerfile
# Étape 1 : compilation
FROM maven:3-openjdk-17 AS builder
COPY . .
RUN mvn package -DskipTests

# Étape 2 : exécution (image légère)
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Pour les frontends : **build Node.js → Nginx Alpine**. Résultat : des images légères en production.

### 2. Docker Compose — orchestration de 12+ conteneurs (30 sec)

Docker Compose orchestre l'ensemble. Voici les conteneurs qui tournent :

```
┌─────────────────── Docker Compose ───────────────────────┐
│  Services backend        Frontends          Stockage      │
│  ┌──────────────┐    ┌──────────────┐   ┌──────────┐    │
│  │ api-gateway  │    │client-frontend│   │  minio   │    │
│  │ auth-service │    │owner-frontend │   │minio-init│    │
│  │ user-service │    └──────────────┘   └──────────┘    │
│  │ property-svc │    Bases de données                    │
│  │ messaging-svc│    ┌──────────────┐                    │
│  └──────────────┘    │   user-db    │                    │
│                      │ property-db  │                    │
│                      │ messaging-db │                    │
│                      └──────────────┘                    │
└──────────────────────────────────────────────────────────┘
```

**Réseau :** tous les conteneurs sont sur un réseau Docker interne `backend`. Les services se découvrent par **nom DNS** (`user-service`, `property-service`...). Seuls deux ports sont exposés à l'extérieur :
- `8443` → API Gateway
- `9000/9001` → MinIO

### 3. Volumes persistants (10 sec)

Les données sont persistées dans des **volumes Docker nommés** qui survivent aux redémarrages :
```
user-db-data, property-db-data, messaging-db-data, minio-data
```
`make reset-db` supprime ces volumes pour repartir d'une base vide (les fixtures se rechargent automatiquement).

### 4. Génération automatique des certificats (20 sec)

On n'a pas besoin d'OpenSSL sur la machine hôte. Le script `generate-certs.sh` :
1. Lance un **conteneur Alpine temporaire**
2. Génère en son sein : CA locale, certificat serveur (RSA 2048), keystore PKCS12
3. Copie les fichiers dans le dossier `certs/`
4. Le dossier `certs/` est monté en **volume read-only** dans chaque service

Les certificats sont valables **365 jours** et couvrent tous les noms DNS Docker via les SAN.

### 5. Makefile — simplicité d'utilisation (20 sec)

Le seul prérequis pour lancer l'application est **Docker**. Le Makefile simplifie tout :

| Commande | Action |
|---|---|
| `make up` | Génère les certs si besoin + build + lance tout |
| `make restart` | Stop + relance |
| `make reset-db` | Vide les BDD + relance (fixtures rechargées) |
| `make logs` | Logs de tous les services |
| `make log S=messaging-service` | Logs d'un service spécifique |
| `make clean` | Tout supprimer (conteneurs, volumes, certs) |

### 6. Données de démo automatiques (10 sec)

Au démarrage de chaque service, un `CommandLineRunner` Spring Boot vérifie si des données existent et les insère si nécessaire :
- Compte owner : `owner@test.com` / `password123`
- Compte tenant : `tenant@test.com` / `password123`
- 5 logements de démonstration avec descriptions et prix

Pas besoin de script SQL manuel — les fixtures sont dans le code Java.

---

## Points clés à retenir pour les questions

- Le `minio-init` est un conteneur temporaire qui crée le bucket `property-photos` et configure les droits anonymes en lecture — il s'exécute une seule fois au démarrage
- Les **`depends_on`** dans Docker Compose garantissent l'ordre de démarrage (ex: api-gateway attend auth-service, user-service, property-service, messaging-service)
- On aurait pu ajouter **Spring Cloud Config** pour la configuration centralisée, ou **Eureka** pour le service discovery — mais pour 6 services, la configuration par variables d'environnement est suffisante
- Les Dockerfiles Maven compilent le code **à l'intérieur du conteneur** — pas besoin de Java installé sur la machine hôte

---

## Conclusion de la présentation (à enchaîner)

> "Pour conclure, ce projet nous a permis de mettre en pratique les concepts d'architecture microservices dans un cas concret. On a pu constater les avantages — résilience, isolation, déploiement indépendant — mais aussi les défis : la gestion du HTTPS entre services, la propagation de l'authentification, et la complexité d'orchestrer plus de douze conteneurs. Des améliorations seraient possibles en production : Spring Cloud Config, Eureka pour le service discovery, ou un message broker pour les communications asynchrones. Mais dans le cadre de ce projet éducatif, l'architecture démontre clairement les principes des microservices."
