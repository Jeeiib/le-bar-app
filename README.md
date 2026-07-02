# Le Bar'app

Application web de commande de cocktails à table par QR code, avec suivi de préparation en temps réel.

Projet réalisé dans le cadre du titre Concepteur Développeur d'Applications (ForEach Academy).

## Deux usages

- **Client** : scanne le QR code de sa table, consulte la carte, compose son panier, commande et suit l'avancement de la préparation. Sans compte.
- **Barmaker** : authentifié, paramètre la carte (catégories, cocktails, tailles et prix) et fait avancer chaque cocktail étape par étape.

## Stack

- **Back** : Java 25 / Spring Boot 4.1, PostgreSQL
- **Front** : Vue 3 + TypeScript (Vite), servi par nginx
- **Déploiement** : Docker / docker-compose

## Structure

- `back/` — API REST Spring Boot
- `front/` — application Vue
- `docs/` — conception (MCD, maquettes)

## Lancer le projet

Pré-requis : **Docker** et **Docker Compose** (Docker Desktop sous Windows/Mac).

Depuis la racine du projet :

```bash
docker compose up --build
```

Trois conteneurs démarrent : la base PostgreSQL, l'API et le front.

> Au premier démarrage, PostgreSQL exécute le script `db/init.sql` qui crée le schéma et charge une carte de démonstration complète (cocktails avec photos, catégories, compte barmaker). **Aucune connexion internet n'est requise** : tout est embarqué dans le script. Les données sont ensuite persistées dans un volume Docker.

Une fois le message de démarrage de l'API affiché :

| Service | URL | Remarque |
| --- | --- | --- |
| Application (front) | http://localhost:8080 | Point d'entrée unique. C'est l'URL à ouvrir. |
| API (accès direct) | http://localhost:8081/api/cocktails | Optionnel, pour inspecter l'API. |

Le front relaie automatiquement les appels `/api` vers l'API : il n'y a rien à configurer côté navigateur.

### Tester avec deux appareils

Les conteneurs étant lancés sur une machine, les autres appareils du même réseau accèdent à l'application via l'IP de cette machine, par exemple `http://192.168.x.x:8080`.

- **Device client** : ouvrir une table, par exemple http://localhost:8080/t/table-1 (tables `table-1` à `table-12`).
- **Device barmaker** : ouvrir http://localhost:8080/barmaker/login puis se connecter.

### Identifiants barmaker

- Email : `barmaker@lebarapp.fr`
- Mot de passe : `barmaker123`

### Traduction des imports (optionnel)

La carte de démonstration est déjà en français (figée dans `db/init.sql`). Lorsque le barmaker importe un nouveau cocktail depuis TheCocktailDB, sa fiche est traduite à la volée : par défaut via MyMemory (gratuit, sans clé), ou via DeepL si une clé est fournie :

```bash
DEEPL_API_KEY="votre-cle:fx" docker compose up --build
```

### Arrêter / réinitialiser

```bash
docker compose down        # arrête les conteneurs (la base est conservée)
docker compose down -v     # supprime aussi la base (réinitialisation complète)
```
