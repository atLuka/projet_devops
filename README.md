# Projet Architecture des systèmes d'information - RentApp

Application de location de logement RentApp en microservices.  
Groupe : Bastien, Ghislain, Nam, Luka, Ayoub, Kais

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
