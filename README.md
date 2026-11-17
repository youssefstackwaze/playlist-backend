# Music Playlist Management System

API REST Spring Boot 3.5 (Java 17+, H2 en mémoire) : gestion de playlists, shuffle multi-stratégies, recommandations, export multi-formats, lecteur global thread-safe.

## Prérequis

JDK 17 ou 21, Maven 3.9+. Aucune base externe ni Docker : H2 en mémoire.

Dépendances : Spring Web, Spring Data JPA + H2, Validation, Actuator, Springdoc OpenAPI (Swagger UI), Spring Boot Test (JUnit 5, Mockito, MockMvc).
Lombok n'est pas utilisé : les DTO sont des `record` Java.

## Lancer

```bash
mvn spring-boot:run      # http://localhost:8080
mvn test                 # tests unitaires + intégration
```

| Outil | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON, importable dans Postman/Insomnia) | http://localhost:8080/v3/api-docs |
| Actuator | http://localhost:8080/actuator/health |
| Console H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:musicdb`, user `sa`) |

Un catalogue de 30 chansons est chargé au démarrage (`src/main/resources/data.sql`).

## Endpoints

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/songs?genre=ROCK&artist=Queen` | Catalogue (filtres optionnels) |
| GET / POST | `/api/playlists` | Lister / créer |
| GET / PUT / DELETE | `/api/playlists/{id}` | Lire / modifier / supprimer |
| POST | `/api/playlists/{id}/songs` | Ajouter `{songId, position?}` |
| DELETE | `/api/playlists/{id}/songs/{songId}` | Retirer une chanson |
| PUT | `/api/playlists/{id}/songs/order` | Déplacer `{from, to}` |
| GET | `/api/playlists/shuffle-strategies` | Stratégies disponibles |
| GET | `/api/playlists/{id}/shuffle?strategy=random\|smart\|genre-balanced` | Ordre mélangé |
| GET | `/api/playlists/{id}/recommendations?limit=5` | Recommandations |
| GET | `/api/playlists/export-formats` | Formats disponibles |
| GET | `/api/playlists/{id}/export?format=json\|m3u\|pls\|csv\|xspf` | Export (fichier) |
| GET | `/api/playlists/{id}/analysis` | Durée, genres, diversité des artistes |
| POST | `/api/playlists/smart` | **Créer + recommander + mélanger + jouer** |
| GET | `/api/player` | État du lecteur |
| POST | `/api/player/load/{playlistId}?strategy=smart&autoPlay=true` | Charger une playlist |
| POST | `/api/player/play`, `/pause`, `/stop`, `/next`, `/previous` | Contrôles |
| PUT | `/api/player/repeat?enabled=true`, `/api/player/volume` `{volume}` | Réglages |

### Exemples

```bash
curl -X POST localhost:8080/api/playlists -H "Content-Type: application/json" \
  -d '{"name":"Rock","owner":"alice","songIds":[1,4,6,7]}'

curl "localhost:8080/api/playlists/1/shuffle?strategy=smart"
curl "localhost:8080/api/playlists/1/export?format=m3u"

curl -X POST localhost:8080/api/playlists/smart -H "Content-Type: application/json" \
  -d '{"name":"Jazz night","seedSongIds":[13,14],"recommendationCount":5,"shuffleStrategy":"genre-balanced"}'

curl -X POST localhost:8080/api/player/next
```

## Conception

| Défi | Solution | Code |
|---|---|---|
| Algorithmes sélectionnables à l'exécution | **Strategy** + registre alimenté par Spring (`List<ShuffleStrategy>`) | `shuffle/` |
| Shuffle « smart » | Glouton : à chaque pas, l'artiste ≠ précédent ayant le plus de titres restants → aucune répétition dès que c'est possible | `SmartShuffleStrategy` |
| Shuffle « genre-balanced » | Chaque genre est réparti proportionnellement sur toute la playlist (slots + jitter) | `GenreBalancedShuffleStrategy` |
| Recommandations | Critères pondérés pluggables (compatibilité de genre 0.7, artiste 0.3), exclusion des titres déjà présents | `recommendation/` |
| Export / analyse extensibles | Interfaces `PlaylistExporter` / `PlaylistAnalyzer` + registre : ajouter un format = ajouter un `@Component` | `export/`, `analysis/` |
| État global thread-safe | `MusicPlayer` singleton Spring, état protégé par `ReentrantReadWriteLock`, file immuable (`List.copyOf`), réponses = snapshots cohérents | `player/MusicPlayer` |
| Opération multi-étapes | **Facade** : création + recommandations dans une seule méthode `@Transactional` (`PlaylistService.createWithRecommendations`, rollback complet en cas d'erreur), puis chargement du lecteur seulement après commit ; stratégie validée avant toute écriture | `facade/MusicFacade` |

Erreurs uniformes (`404`, `400`) via `GlobalExceptionHandler`.

## Tests

| Type | Classe | Couvre |
|---|---|---|
| Unitaire | `ShuffleStrategiesTest` | 3 stratégies, absence de répétition d'artiste, registre |
| Unitaire | `ExportersTest` | M3U, PLS, CSV (échappement), XSPF |
| Unitaire (Mockito) | `RecommendationServiceTest` | Classement par genre/artiste, exclusions |
| Unitaire (Mockito) | `MusicFacadeTest` | Orchestration, lecteur intact si la transaction échoue |
| Concurrence | `MusicPlayerConcurrencyTest` | 100 threads, aucune mise à jour perdue |
| Intégration (MockMvc) | `PlaylistApiIntegrationTest` | CRUD, shuffle, export, analyse, `/smart`, rollback, Swagger, Actuator |

## Frontend

L'interface Angular est dans un **dépôt séparé** (frontend). En développement, lancez ce backend (`mvn spring-boot:run`, port 8080) puis `npm start` dans le dépôt frontend (port 4200, proxy `/api` → 8080).

Le frontend est hébergé sur un autre domaine : `CorsConfig` autorise les origines définies par `app.cors.allowed-origins` (variable `CORS_ALLOWED_ORIGINS`) — par défaut `http://localhost:4200` en dev et `https://*.onrender.com` en prod.

## Déploiement gratuit (Render)

`render.yaml` déclare un **Web Service Docker** (`Dockerfile` multi-stage Maven → JRE, profil `prod` : console H2 désactivée) :

1. Pousser ce dépôt sur GitHub.
2. Render → **New → Blueprint** → choisir ce dépôt.
3. L'API est disponible sur `https://<nom>.onrender.com` (Swagger : `/swagger-ui.html`, santé : `/actuator/health`). Donner `https://<nom>.onrender.com/api` comme `API_BASE_URL` au frontend.

Limites de l'offre gratuite : mise en veille après ~15 min d'inactivité (premier appel ensuite ~30–60 s), 512 Mo de RAM, base H2 en mémoire remise à zéro à chaque redémarrage.
