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

- `back/` — API REST Spring Boot (Java 25)
- `front/` — application Vue (TypeScript)
- `docs/` — documents de conception (voir ci-dessous)

## Documents de conception (livrables)

Tout se trouve dans le dossier `docs/` :

- **MCD** (Modèle Conceptuel de Données) : [`docs/mcd.png`](docs/mcd.png) (image) et [`docs/mcd.dbml`](docs/mcd.dbml) (source, ré-importable sur dbdiagram.io)
- **Maquettes** : [`docs/maquettes.pdf`](docs/maquettes.pdf)

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
- **Device barmaker** : ouvrir http://localhost:8080/barmaker/login puis se connecter. La page **Tables** affiche les QR codes et permet d'en **ajouter de nouvelles** (le QR est généré automatiquement à partir du nom).

> **Scanner les QR codes avec un téléphone** : chaque QR encode l'adresse par laquelle la page barmaker a été ouverte. Pour qu'un téléphone puisse les scanner, ouvrir la page barmaker via l'**IP réseau** de la machine (ex `http://192.168.x.x:8080/barmaker/login`), et **non** `localhost` (que le téléphone ne peut pas joindre). Le téléphone doit être sur le même réseau Wi-Fi.

### Identifiants barmaker

- Email : `barmaker@lebarapp.fr`
- Mot de passe : `barmaker123`

### Parcours de test (bout en bout)

Pour valider l'application après le démarrage :

1. **Client** — ouvrir http://localhost:8080 (redirige vers une table). Parcourir la carte, ouvrir un cocktail, l'ajouter au panier, puis **commander** (le prénom est optionnel).
2. **Barmaker** — se connecter sur `/barmaker/login`. La commande apparaît dans la file **Commandes** ; l'ouvrir et faire avancer chaque cocktail étape par étape (Ingrédients → Assemblage → Dressage → Terminé).
3. **Client** — sur la page de suivi de la commande, l'avancement se met à jour automatiquement (interrogation régulière du serveur).
4. **Gestion de la carte** — côté barmaker, page **Ma carte** : créer, modifier ou supprimer un cocktail (préremplissage possible depuis TheCocktailDB). Les changements sont aussitôt visibles côté client.
5. **Tables & QR** — page **Tables** : imprimer les QR ou **ajouter une nouvelle table** (son QR est généré automatiquement). Pour scanner au téléphone, voir la note ci-dessus (ouvrir via l'IP réseau).

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
