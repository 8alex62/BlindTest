# Blind test — application Web en Clean Architecture

Application Spring Boot de blind test : un visiteur s'inscrit, un participant connecté crée ou
rejoint un blind test, et dès que trois participants sont réunis la partie démarre sur une
séquence de sept morceaux. Le premier à cliquer « J'ai trouvé » met la lecture en pause et
réserve la réponse ; s'il a juste, il marque un point et le morceau suivant est joué, sinon la
lecture reprend pour tout le monde.

L'objet du projet est le **respect de la Clean Architecture**, pas la richesse fonctionnelle.

## Sommaire

1. [Démarrer](#1-démarrer)
2. [Les couches et la règle de dépendance](#2-les-couches-et-la-règle-de-dépendance)
3. [Comment chaque couche respecte la règle](#3-comment-chaque-couche-respecte-la-règle)
4. [Le domaine porte les règles métier](#4-le-domaine-porte-les-règles-métier)
5. [L'arbitrage du premier clic](#5-larbitrage-du-premier-clic)
6. [Champs ajoutés par rapport au diagramme](#6-champs-ajoutés-par-rapport-au-diagramme)
7. [Classes ajoutées et écarts assumés](#7-classes-ajoutées-et-écarts-assumés)
8. [Hypothèses retenues](#8-hypothèses-retenues)
9. [Tests](#9-tests)
10. [API REST](#10-api-rest)

---

## 1. Démarrer

Prérequis : JDK 25. Toutes les dépendances sont téléchargées par le wrapper Maven.

```bash
./mvnw clean install
```

```bash
./mvnw spring-boot:run
```

L'application écoute sur `http://localhost:8080`. La base H2 est en mémoire et sept morceaux
sont insérés au démarrage.

| Page | Rôle |
|---|---|
| `/inscription` | créer un compte |
| `/connexion` | se connecter (dépose le jeton JWT dans un cookie `HttpOnly`) |
| `/blindtests` | lister et créer des blind tests |
| `/blindtests/{id}` | salle de jeu : lecteur audio, bouton « J'ai trouvé », scores |

Pour jouer une partie complète il faut **trois comptes** : ouvrez trois fenêtres de navigation
privée, inscrivez et connectez un participant dans chacune, puis rejoignez le même blind test.
La partie démarre toute seule à l'arrivée du troisième.

La console H2 est disponible sur `/h2-console` (URL JDBC `jdbc:h2:mem:blindtest`).

---

## 2. Les couches et la règle de dépendance

Le projet suit le découpage du projet de référence `encheres_descendantes` : **un seul module
Maven**, découpé en packages sous `com.esgi.blindTest`, conformément au diagramme de packages
du modèle Visual Paradigm (`UML/projet_blind_test.vpp`).

| Couche | Package | Contenu | Dépend de |
|---|---|---|---|
| **Domaine** | `domain.model`, `domain.exception` | `Participant`, `BlindTest`, `Morceau`, `Participation` et les exceptions métier | rien |
| **Application** | `domain.usecase`, `domain.repository` | les use cases, leurs `OutputPort` et les ports de persistance | le domaine seul |
| **Adapters** | `adapter.usecase_adapter`, `adapter.mapper`, `adapter.repository` | implémentations des `OutputPort` et des ports, mappers MapStruct | domaine, application, infrastructure |
| **Infrastructure** | `infra.persistance.entity`, `.repository`, `.initialisation` | entités JPA, repositories Spring Data, données de démarrage | domaine |
| **Présentation** | `presentation.controller.rest`, `.web`, `presentation.request`, `.response` | contrôleurs REST, pages Thymeleaf, DTO | domaine, application, adapters |
| **Sécurité** | `security.configuration`, `.filter`, `.service`, `.util` | JWT, filtre, configuration Spring Security | domaine |

**La règle de dépendance pointe vers l'intérieur.** Le domaine est le noyau : il ne connaît ni
Spring Data, ni JPA, ni MapStruct, ni le Web, ni aucune classe des couches extérieures. Chaque
couche extérieure ne connaît que des couches plus internes, jamais l'inverse. Les inversions de
dépendance passent par des interfaces déclarées **dans** le domaine (`domain.repository` et les
`OutputPort` imbriqués dans chaque use case), implémentées **à l'extérieur**.

Les diagrammes Mermaid détaillés sont dans [docs/architecture.md](docs/architecture.md).

---

## 3. Comment chaque couche respecte la règle

### 3.1 Le domaine ne dépend de rien

`domain.model` et `domain.exception` ne contiennent que du Java, plus Lombok et
`jakarta.validation` (voir l'écart assumé au §7). Aucun import de `jakarta.persistence`,
`org.mapstruct`, `org.springframework.data` ou `org.springframework.web` — vérifié par ArchUnit.

### 3.2 Les use cases ne connaissent que leur port de sortie

Chaque use case est une classe à **une seule méthode publique `apply`**, avec une interface
imbriquée `OutputPort` :

```java
@Component
@AllArgsConstructor
public class MettreEnPauseBlindTestUseCase {

    public interface OutputPort {
        BlindTest findBlindTest(BlindTest blindTest);
        boolean reserverLaReponse(BlindTest blindTest);
    }

    private final OutputPort output;

    public void apply(Participant participant, BlindTest blindtest) {
        BlindTest courant = output.findBlindTest(blindtest);
        courant.reserverLaReponse(participant);          // la règle est dans le domaine
        if (!output.reserverLaReponse(courant)) {        // la base arbitre l'ordre
            courant.annulerLaReservation();
            throw new ReponseDejaReserveeException();
        }
    }
}
```

Le use case orchestre, il ne décide pas : la décision métier est prise par l'agrégat `BlindTest`.

### 3.3 Un adapter par use case porte les repositories

`adapter.usecase_adapter` contient une classe par use case, qui implémente son `OutputPort` et
dépend uniquement des **interfaces** de `domain.repository` :

| Adapter | Ports de persistance utilisés |
|---|---|
| `SInscrireAdapter`, `SeConnecterAdapter` | `ParticipantRepository` |
| `SeDeconnecterAdapter` | aucun |
| `ConsulterBlindTestAdapter`, `ConsulterEtatBlindTestAdapter`, `LancerBlindTestAdapter`, `MettreEnPauseBlindTestAdapter` | `BlindTestRepository` |
| `AjouterBlindTestAdapter` | `BlindTestRepository`, `MorceauRepository` |
| `RejoindreBlindTestAdapter`, `FaireUnePropositionAdapter` | `ParticipantRepository`, `ParticipationRepository`, `BlindTestRepository` |

### 3.4 Aucune entité JPA ne sort de l'infrastructure

`ParticipantEntity`, `BlindTestEntity`, `MorceauEntity` et `ParticipationEntity` vivent dans
`infra.persistance.entity`. Seuls `adapter.mapper` (MapStruct) et `adapter.repository` les
manipulent. Les `...RepositoryImpl` renvoient **toujours** des objets du domaine :

```java
@Override
@Transactional(readOnly = true)
public Optional<BlindTest> findById(Long id) {
    // Le mapping se fait dans la transaction : les collections paresseuses sont lisibles.
    return blindTestJpaRepository.findById(id).map(blindTestEntityMapper::toEntity);
}
```

### 3.5 Aucun DTO web n'entre dans l'application

`presentation.request` et `presentation.response` ne sont jamais importés par `domain`. Le
contrôleur convertit dans les deux sens : il lit un `record` de requête, appelle `apply(...)`
avec des types du domaine, et convertit le retour via `BlindTestMapper`.

### 3.6 Injection par constructeur

Toutes les dépendances sont des champs `final` injectés par constructeur, via Lombok
`@AllArgsConstructor` (convention du projet de référence). Aucun `@Autowired` sur un champ —
vérifié par ArchUnit.

---

## 4. Le domaine porte les règles métier

Toutes les règles sont des méthodes de `BlindTest`
([BlindTest.java](src/main/java/com/esgi/blindTest/domain/model/BlindTest.java)),
aucune n'est dans un service, un adapter ou un contrôleur.

| Règle | Méthode | Exception |
|---|---|---|
| Un blind test est une séquence de 7 morceaux | `ajouterLesMorceaux` | `NombreDeMorceauxInvalideException` |
| Au maximum 3 participants | `rejoindre` | `BlindTestCompletException` |
| Un participant ne rejoint qu'une fois | `rejoindre` | `ParticipantDejaPresentException` |
| Le blind test démarre à 3 participants | `rejoindre` → `demarrer` | — |
| Au démarrage, le premier morceau est joué | `demarrer` | `BlindTestIncompletException` |
| Le premier clic met en pause et réserve la réponse | `reserverLaReponse` | `BlindTestNonDemarreException`, `ParticipantHorsBlindTestException` |
| Un seul réservataire à la fois | `reserverLaReponse` | `ReponseDejaReserveeException` |
| Seul le réservataire peut répondre | `repondre` | `ReponseNonReserveeException` |
| Une bonne réponse rapporte un point | `repondre` → `Participation.ajouterUnPoint` | — |
| Une mauvaise réponse relance la lecture | `repondre` → `annulerLaReservation` | — |
| Après 7 morceaux : terminé, scores figés | `passerAuMorceauSuivant` | `BlindTestTermineException` |
| Comparaison sans casse, accents ni espaces superflus | `Morceau.correspondA` | — |

La normalisation des titres est du Java pur (`java.text.Normalizer`) : décomposition NFD,
suppression des diacritiques, réduction des espaces multiples, minuscules en `Locale.ROOT`,
et traitement des ligatures (`œ`, `æ`) et de l'apostrophe typographique.

---

## 5. L'arbitrage du premier clic

Le sujet impose que le serveur soit seul juge et que la réservation résiste aux accès
simultanés, sans mettre cette logique dans le contrôleur. Deux mécanismes se complètent.

**La règle est dans le domaine.** `BlindTest.reserverLaReponse(participant)` vérifie que le
blind test est en cours, que le participant y est inscrit et que personne n'a encore la main,
puis passe en pause et enregistre le réservataire. Cette règle se teste sans base de données :
deux appels successifs sur le même objet et le second lève `ReponseDejaReserveeException`.

**L'ordre d'arrivée est arbitré par la base.** Quand trois requêtes HTTP évaluent la règle en
parallèle sur trois copies mémoire toutes valides, il faut trancher qui est arrivé le premier.
`BlindTestJpaRepository.reserverLaReponse` réalise une **mise à jour conditionnelle** :

```sql
UPDATE blind_test
   SET id_participant_reservataire = :idParticipant,
       etat_lecture = 'PAUSE',
       version = version + 1
 WHERE id = :idBlindTest
   AND statut = 'EN_COURS'
   AND index_morceau_courant = :indexMorceau
   AND id_participant_reservataire IS NULL
```

Le premier `UPDATE` verrouille la ligne ; le second réévalue sa clause `WHERE` sur la version
validée, ne trouve plus `id_participant_reservataire IS NULL` et modifie zéro ligne. Le port du
domaine expose ce résultat en vocabulaire métier — `boolean reserverLaReponse(BlindTest)`,
« ai-je pris la main ? » — et le use case en tire la conséquence définie par la règle.

En résumé : **la base de données n'arbitre pas la règle, elle arbitre l'ordre.**

Les autres écritures (`rejoindre`, `demarrer`, `repondre`) sont protégées par un **verrou
optimiste** : `BlindTestEntity` porte un champ `@Version`, le domaine transporte cette version,
et `BlindTestRepositoryImpl.save` la compare à celle relue en base avant d'écrire. Deux
participants qui tentent d'être le troisième simultanément : le second reçoit un 409.

Vérification observée sur l'application réelle — trois `POST /pause` simultanés, un seul `204`,
deux `409` avec le message « Un autre participant a déjà pris la main sur ce morceau. »

---

## 6. Champs ajoutés par rapport au diagramme

Le diagramme de classes ne porte que `email` / `motDePasse` (`Participant`), `nom`
(`BlindTest`, `Morceau`) et `score` (`Participation`). Les champs techniques suivants ont été
ajoutés, au strict minimum :

| Champ | Classe | Justification |
|---|---|---|
| `id` | les quatre | identité persistante, nécessaire à JPA et aux URL REST |
| `statut` | `BlindTest` | distingue « en attente », « en cours » et « terminé », condition de presque toutes les règles |
| `indexMorceauCourant` | `BlindTest` | position dans la séquence des sept morceaux |
| `etatLecture` | `BlindTest` | indique au navigateur s'il doit jouer ou mettre en pause |
| `idParticipantReservataire` | `BlindTest` | identifie le gagnant du premier clic sur le morceau courant |
| `version` | `BlindTest` | jeton de verrou optimiste, transporté entre la base et le domaine |
| `urlAudio` | `Morceau` | adresse du fichier audio, exigée par la section « données de démarrage » |

Deux énumérations accompagnent ces champs : `StatutBlindTest` (`EN_ATTENTE`, `EN_COURS`,
`TERMINE`) et `EtatLecture` (`LECTURE`, `PAUSE`).

`ParticipationEntity` porte en plus une référence inverse vers `BlindTestEntity` : c'est une
contrainte de persistance qui reste **dans l'infrastructure**, sans équivalent dans le domaine.

---

## 7. Classes ajoutées et écarts assumés

### Classes ajoutées

| Classe | Pourquoi |
|---|---|
| `ConsulterEtatBlindTestUseCase` + son adapter | le navigateur interroge l'état toutes les secondes ; `ConsulterBlindTestUseCase` ne renvoie que les blind tests rejoignables et ne permettrait pas d'afficher les scores figés d'une partie terminée |
| `PageController` (`presentation.controller.web`) | les quatre pages Thymeleaf ; il ne porte aucune règle et n'appelle aucun use case |
| `GestionnaireDExceptions` | traduit les exceptions du domaine en codes HTTP, sans logique métier |

### Écarts assumés

**Spring et Lombok dans le domaine.** Comme dans le projet de référence, les use cases portent
`@Component` et les modèles utilisent Lombok (`@Data`, `@NonNull`) et `jakarta.validation`
(`@Email`, `@Size`). C'est un choix de cohérence avec le code du professeur ; ces deux points
sont donc volontairement **hors du périmètre** des règles ArchUnit, qui vérifient tout le reste.
Un domaine strictement pur exigerait d'instancier les use cases par `@Bean` dans une classe de
configuration de l'infrastructure.

**Mots de passe en clair.** `ParticipantRepository.findByEmailAndMotDePasse` reproduit la
signature du projet de référence. À ne pas reproduire en production : un hachage BCrypt
remplacerait cette méthode par `findByEmail` plus une comparaison dans `SeConnecterAdapter`.

**`SeDeconnecterAdapter` sans repository.** Le diagramme associe `ParticipantRepository` à
`SeDeconnecterUseCase`, mais le jeton JWT est sans état : la déconnexion se limite à vider le
contexte de sécurité et à effacer le cookie. Injecter un repository inutilisé serait du code mort.

**`FaireUnePropositionAdapter`.** Le diagramme de classes lui associe `MorceauRepository` ;
l'adapter ne l'utilise pas, car les sept morceaux appartiennent à l'agrégat `BlindTest` déjà
rechargé. En revanche il porte `BlindTestRepository`, comme le montre le diagramme de séquence.

**Composition `BlindTest` → `Morceau`.** Le diagramme la note comme une composition ; en base
c'est une `@ManyToMany` ordonnée (`@OrderColumn`), car le catalogue de morceaux est partagé
entre tous les blind tests. Chaque blind test conserve donc son propre ordre de passage.

**Spring Boot 4.1.1 plutôt que 3.x.** Le squelette déjà commité et le projet de référence
utilisent Spring Boot 4.1.1 avec Java 25, ainsi que les artefacts modulaires
(`spring-boot-starter-webmvc`, `spring-boot-h2console`). Conserver cette version évite un
dépaysement inutile par rapport au code du professeur.

---

## 8. Hypothèses retenues

- **« Blind tests en cours »** = statuts `EN_ATTENTE` et `EN_COURS`. Ne renvoyer que les parties
  déjà démarrées rendrait la liste inutile, puisque aucune ne serait rejoignable.
- **Recliquer après une mauvaise réponse est autorisé.** La main est libérée, la lecture reprend,
  et tout le monde — y compris l'auteur de l'erreur — peut cliquer à nouveau sur le même morceau.
- **`POST /lancer` sur un blind test déjà démarré renvoie 409.** Le démarrage nominal est
  automatique à l'arrivée du troisième participant ; `/lancer` est un déclencheur manuel soumis
  aux mêmes préconditions.
- **Les sept morceaux sont tirés au hasard** dans le catalogue inséré au démarrage, et figés à la
  création du blind test.
- **Un blind test naît sans participant.** `AjouterBlindTestUseCase.apply(String nom)` ne reçoit
  pas de créateur ; la cardinalité `1..3` du diagramme se lit « pendant la partie ».
- **Aucune minuterie serveur.** Sans clic, un morceau n'expire jamais. La lecture audio est
  pilotée par le navigateur, le serveur ne fait que publier un état.
- **Le titre du morceau courant n'est jamais publié** par `/etat` : c'est la réponse à trouver.
- **Déconnexion sans révocation.** Le cookie est effacé, mais le jeton reste valide jusqu'à son
  expiration (deux heures par défaut).
- **Les adresses audio sont des fichiers de démonstration libres** (SoundHelix), sans rapport
  avec les titres à deviner. Elles se remplacent par des fichiers locaux placés sous
  `src/main/resources/static/audio/`.
- **Pas de départage en cas d'égalité** de scores.

---

## 9. Tests

```bash
./mvnw clean install
```

50 tests, tous verts : 45 unitaires (surefire) et 5 d'intégration (failsafe, suffixe `IT`).

| Suite | Ce qu'elle vérifie |
|---|---|
| [`BlindTestTest`](src/test/java/com/esgi/blindTest/domain/model/BlindTestTest.java) | **Domaine sans Spring** : refus du 4ᵉ participant, démarrage automatique à 3, exigence des 7 morceaux, point gagné, refus du second clic, reprise après une mauvaise réponse, fin de partie et gel des scores, classement |
| [`MorceauTest`](src/test/java/com/esgi/blindTest/domain/model/MorceauTest.java) | normalisation des titres : casse, accents, espaces, ligatures |
| [`*UseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase) | **use cases avec Mockito** : `@Mock` sur l'`OutputPort`, `@InjectMocks` sur le use case — dont le cas où la base refuse la réservation |
| [`ArchitectureTest`](src/test/java/com/esgi/blindTest/architecture/ArchitectureTest.java) | **ArchUnit**, 10 règles de dépendances entre couches |
| [`BlindTestRestControllerIT`](src/test/java/com/esgi/blindTest/presentation/controller/rest/BlindTestRestControllerIT.java) | **intégration** : contexte Spring démarré, use cases mockés — 401 sans jeton, liste, création, 409 sur le second clic, 400 sur nom vide |

Les règles ArchUnit vérifiées :

1. le domaine ne dépend d'aucune couche extérieure ;
2. le domaine ignore JPA, MapStruct, Spring Data, le Web et JJWT ;
3. aucune entité JPA hors de l'infrastructure ;
4. aucun DTO web dans le domaine ;
5. la présentation ne dépend pas de l'infrastructure ;
6. aucune injection par champ (`@Autowired`) ;
7. un use case n'expose que `apply` ;
8. les `...RepositoryImpl` sont dans `adapter.repository` ;
9. les `...Adapter` sont dans `adapter.usecase_adapter` ;
10. les dépendances injectées sont `final`.

---

## 10. API REST

Toutes les routes sauf l'inscription et la connexion exigent le cookie JWT.

| Méthode | Chemin | Effet |
|---|---|---|
| `POST` | `/api/participants/inscription` | crée un compte — `201`, `409` si l'email existe, `400` si le mot de passe fait moins de 8 caractères |
| `POST` | `/api/participants/connexion` | dépose le cookie `HttpOnly` — `200`, `401` si les identifiants sont faux |
| `POST` | `/api/participants/deconnexion` | efface le cookie — `204` |
| `GET` | `/api/blindtests` | liste les blind tests rejoignables |
| `POST` | `/api/blindtests` | crée un blind test de 7 morceaux — `201` |
| `POST` | `/api/blindtests/{id}/rejoindre` | rejoint — `204`, `409` si complet ou déjà inscrit |
| `POST` | `/api/blindtests/{id}/lancer` | démarre — `204`, `409` si déjà démarré ou incomplet |
| `POST` | `/api/blindtests/{id}/pause` | clic « J'ai trouvé » — `204` pour le premier, `409` pour les suivants |
| `POST` | `/api/blindtests/{id}/proposition` | propose un titre — `200 {"juste": …}`, `409` si vous n'avez pas la main |
| `GET` | `/api/blindtests/{id}/etat` | état complet, interrogé chaque seconde par la salle de jeu |
