# Le Bar'app

Application web de commande de cocktails à table par QR code, avec suivi de préparation en temps réel.

Projet réalisé dans le cadre du titre Concepteur Développeur d'Applications (ForEach Academy).

## Deux usages

- **Client** : scanne le QR code de sa table, consulte la carte, compose son panier, commande et suit l'avancement de la préparation. Sans compte.
- **Barmaker** : authentifié, paramètre la carte (catégories, cocktails, tailles et prix) et fait avancer chaque cocktail étape par étape.

## Stack

- **Back** : Java 21 / Spring Boot 3, PostgreSQL
- **Front** : Vue 3 + TypeScript (Vite)
- **Déploiement** : Docker / docker-compose

## Structure

- `back/` — API REST Spring Boot
- `front/` — application Vue
- `docs/` — conception (MCD, maquettes)

## Lancer le projet

À venir : un simple `docker compose up` lancera la base, l'API et le front.
