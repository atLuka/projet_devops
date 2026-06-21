# Projet DevOps - RentApp

Mise en place d'une chaîne d'intégration continue (CI) pour l'application de
location de logement **RentApp**, construite en microservices.

Groupe :  Luka SAMAC, Ayoub LABIB,

---

## Architecture

- **5 services backend** (Spring Boot / Java 17) : `api-gateway`, `auth-service`,
  `messaging-service`, `property-service`, `user-service`
- **2 frontends** : `client-frontend` (locataire), `owner-frontend` (propriétaire)
- Chaque service est un module Maven indépendant, agrégé par un `pom.xml` racine.

## Intégration continue

Le pipeline GitHub Actions (`.github/workflows/ci.yml`) s'exécute à chaque
`push` et `pull request` sur `main` :

| Job | Rôle |
|---|---|
| **backend** | Build + tests unitaires + couverture JaCoCo des 5 services (en parallèle) |
| **frontend** | Build des 2 frontends |
| **sonar** | Analyse qualité et couverture via SonarQube Cloud |
| **docker-build** | Vérifie que toutes les images Docker se construisent |

- **Tests** : JUnit 5 + Mockito, couverture mesurée par JaCoCo
  (rapport agrégé dans `coverage-aggregate/`).
- **Qualité** : SonarQube Cloud (organisation `atluka`,
  projet `atLuka_projet_devops`), avec Quality Gate sur le nouveau code.

---

## Lancer le projet

**Seul prérequis : Docker**

```bash
make up
```

Ça génère les certificats SSL, build les images et lance tout.

Vous pouvez accedez à l'app ici :

https://localhost:8443

> Le navigateur affichera un avertissement SSL (certificat auto-signé), cliquer sur "Continuer quand même".

## Comptes de test

| Rôle | Email | Mot de passe |
|---|---|---|
| Propriétaire | owner@test.com | password123 |
| Locataire | tenant@test.com | password123 |

Les données de démo sont chargées automatiquement au premier démarrage.

## Autres commandes

```bash
make restart    # Redémarre tout
make reset-db   # Remet les BDD à zéro (fixtures rechargées)
make logs       # Logs de tous les services
make log S=api-gateway  # Logs d'un service spécifique
make clean      # Supprime tout (conteneurs, volumes, certificats)
```
