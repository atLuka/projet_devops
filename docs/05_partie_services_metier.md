# Partie 5 — Services métier & Persistance des donnée
**Durée estimée : ~1 min 30** | **Slide : 8**

---

## Ce que tu présentes

Tu présentes ce que font les services backend métier (user, property, messaging), comment chacun gère ses données de façon isolée, et le cas particulier des photos avec MinIO.

---

## Plan de ta prise de parole

### 1. Trois services métier indépendants (20 sec)

Au-delà de la gateway et de l'auth-service, on a trois services métier :

| Service | Ce qu'il fait | Sa BDD |
|---|---|---|
| **user-service** | Stocke les comptes (email, mot de passe haché BCrypt, rôle, nom, téléphone) | user-db (PostgreSQL) |
| **property-service** | Gère les logements, les réservations et les photos | property-db (PostgreSQL) + MinIO |
| **messaging-service** | Gère les conversations locataire ↔ propriétaire | messaging-db (PostgreSQL) |

Chaque service expose une **API REST en HTTPS** et possède sa propre base de données. Il n'y a **aucune base partagée**.

### 2. User-service (15 sec)

Le user-service est la **source de vérité** pour les informations de compte. Il expose des endpoints CRUD classiques :
- `GET /users/{id}` — récupérer un utilisateur
- `GET /users/email/{email}` — utilisé par l'auth-service à la connexion
- `POST /users` — créer un compte (appelé lors du register)

Le mot de passe est **haché avec BCrypt** — le user-service ne stocke jamais le mot de passe en clair.

### 3. Property-service — le service principal (30 sec)

C'est le service le plus riche. Il gère deux domaines couplés :

**Les logements** (CRUD) :
- Titre, description, ville, type, prix par nuit, disponibilité
- Recherche avec filtres (ville, dates, type, prix max)

**Les réservations** avec un cycle de vie complet :
```
PENDING → CONFIRMED → CANCELLATION_REQUESTED → CANCELLED
       ↘ (refus)                             ↘ CANCELLATION_REFUSED
```
- Le locataire réserve → statut `PENDING`
- Le propriétaire accepte → `CONFIRMED`
- Le locataire peut demander une annulation → `CANCELLATION_REQUESTED`
- Le propriétaire accepte ou refuse l'annulation

**Les photos** via MinIO (voir point 4).

### 4. Gestion des photos avec MinIO (30 sec)

MinIO est un serveur de stockage d'objets compatible S3. On l'utilise pour stocker les photos des logements.

**Le flux upload — le backend ne reçoit jamais le fichier :**

```
1. Frontend → POST /api/properties/{id}/photos/upload-url
2. property-service → génère une presigned URL MinIO (valide 15 min)
3. property-service → retourne l'URL au frontend
4. Frontend → PUT image directement vers MinIO (localhost:9000)
   (le backend n'est pas impliqué dans le transfert du fichier)
5. property-service stocke seulement la clé d'objet en BDD
```

**Pour afficher les photos :**
```
Frontend → GET /api/properties/{id}/photos
property-service → génère des presigned download URLs
Frontend → GET image directement depuis MinIO
```

**Pourquoi cette architecture ?**
- Evite de faire transiter des fichiers volumineux par le backend
- Le backend stocke seulement les clés d'objets (des strings)
- Scalable : MinIO gère le stockage, le backend reste léger

**Détail technique :** le property-service utilise **deux clients MinIO** :
- Un client interne (`minio:9000`) pour les opérations admin
- Un client public (`localhost:9000`) pour générer les presigned URLs — avec `region=us-east-1` pour éviter les appels réseau (calcul local de l'URL signée)

### 5. Messaging-service (15 sec)

La messagerie est accessible uniquement pour les **réservations confirmées**. Une conversation est liée à une réservation. Chaque message contient :
- L'identifiant de l'expéditeur
- Son rôle (locataire ou propriétaire)
- Le contenu
- Un indicateur de lecture

Si ce service tombe en panne, les utilisateurs peuvent continuer à chercher et réserver — c'est la résilience par conception.

---

## Points clés à retenir pour les questions

- **Pas de transaction distribuée** entre services — chaque service gère ses propres transactions
- Les services ne partagent pas de code métier commun — chaque service est un projet Maven indépendant
- **Hibernate** gère le schéma BDD automatiquement (`ddl-auto: update`) au démarrage
- Les **fixtures** (données de démo) sont injectées via `CommandLineRunner` Spring Boot — elles se rechargent automatiquement si la BDD est vidée

---

## Transition vers Kais
> "On a donc des services qui tournent, qui persistent leurs données, et qui communiquent. Kais va nous expliquer comment tout cela est déployé et orchestré avec Docker."
