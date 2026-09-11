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
4. [Où vivent les règles métier](#4-où-vivent-les-règles-métier)
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

L'application écoute sur `http://localhost:8080`. La base H2 étant en mémoire, un jeu de
données de démonstration est inséré à **chaque démarrage**, dans cet ordre :

| Ordre | Classe | Contenu |
|---|---|---|
| 1 | `AjoutMorceaux` | sept morceaux, titres et extraits de trente secondes venant de Deezer |
| 2 | `AjoutParticipants` | deux comptes prêts à l'emploi : `user1@test.fr` et `user2@test.fr`, mot de passe `12345678` |
| 3 | `AjoutBlindTest` | un blind test « Blind Test n°1 », en attente, déjà pourvu de ses sept morceaux |

L'ordre est explicite (`@Order`) parce que `AjoutBlindTest` consomme le catalogue produit par
`AjoutMorceaux` : sans cela, l'ordre d'appel des écouteurs d'événement ne serait pas garanti.

| Page | Rôle |
|---|---|
| `/inscription` | créer un compte |
| `/connexion` | se connecter (dépose le jeton JWT dans un cookie `HttpOnly`) |
| `/blindtests` | lister et créer des blind tests |
| `/blindtests/{nom}` | salle de jeu : lecteur audio, boutons « Rejoindre », « Lancer le blind test » et « J'ai trouvé », scores |

Pour jouer une partie complète il faut **trois comptes**. Deux sont déjà créés
(`user1@test.fr` et `user2@test.fr`, mot de passe `12345678`) : il ne reste qu'à en inscrire un
troisième. Ouvrez trois fenêtres de navigation privée, connectez un participant dans chacune,
puis rejoignez « Blind Test n°1 », qui vous attend déjà dans la liste.
Une fois les trois réunis, l'un d'eux clique sur « Lancer le blind test » : **le démarrage n'est
jamais automatique**. Le bouton « Rejoindre » disparaît dès que vous participez, et
« Lancer le blind test » reste désactivé tant que les trois participants ne sont pas là.

La console H2 est disponible sur `/h2-console` (URL JDBC `jdbc:h2:mem:blindtest`).

---

## 2. Les couches et la règle de dépendance

Le projet suit le découpage du projet de référence `encheres_descendantes` : **un seul module
Maven**, découpé en packages sous `com.esgi.blindTest`, conformément au diagramme de packages
du modèle Visual Paradigm (`UML/projet_blind_test.vpp`).

| Couche | Package | Contenu | Dépend de |
|---|---|---|---|
| **Domaine** | `domain.model`, `domain.exception` | `Participant`, `BlindTest`, `Morceau`, `Participation` — de simples porteurs de données — et les exceptions métier | rien |
| **Application** | `domain.usecase`, `domain.repository` | les use cases, qui **appliquent les règles**, leurs `OutputPort` et les ports de persistance | le domaine seul |
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

Les quatre modèles sont des **porteurs de données**, sans identifiant technique ni méthode,
exactement comme `Enchere`, `Participant`, `Offre` et `Article` dans le projet de référence.
L'identité passe par des clés naturelles : le `nom` pour un blind test ou un morceau, l'`email`
pour un participant.

### 3.2 Les use cases appliquent les règles et ne connaissent que leur port de sortie

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

        if (courant.getStatut() != StatutBlindTest.EN_COURS) {
            throw new BlindTestNonDemarreException();
        }
        if (!estInscrit(courant, participant)) {
            throw new ParticipantHorsBlindTestException();
        }
        // Règle métier : un seul réservataire de réponse à la fois.
        if (courant.getReservataire() != null) {
            throw new ReponseDejaReserveeException();
        }

        courant.setEtatLecture(EtatLecture.PAUSE);
        courant.setReservataire(participant);

        // La base n'arbitre pas la règle, elle arbitre l'ordre d'arrivée.
        if (!output.reserverLaReponse(courant)) {
            courant.setReservataire(null);
            courant.setEtatLecture(EtatLecture.LECTURE);
            throw new ReponseDejaReserveeException();
        }
    }
}
```

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
`infra.persistance.entity` — et ce sont **elles seules** qui portent un `@Id Long id`.
L'identifiant technique redevient ce qu'il doit être : une préoccupation de persistance qui ne
franchit pas la frontière. Seuls `adapter.mapper` (MapStruct) et `adapter.repository` les
manipulent, et les `...RepositoryImpl` renvoient **toujours** des objets du domaine :

```java
@Override
@Transactional(readOnly = true)
public Optional<BlindTest> findByNom(String nom) {
    // Le mapping se fait dans la transaction : les collections paresseuses sont lisibles.
    return blindTestJpaRepository.findByNom(nom).map(blindTestEntityMapper::toEntity);
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

## 4. Où vivent les règles métier

Les modèles étant de simples porteurs de données, **les règles sont appliquées par les use
cases**, dans le package `domain` : jamais par un service, un adapter ou un contrôleur.

| Règle | Appliquée par | Exception |
|---|---|---|
| Un blind test est une séquence de 7 morceaux | `AjouterBlindTestUseCase` | `NombreDeMorceauxInvalideException` |
| Au maximum 3 participants | `RejoindreBlindTestUseCase` | `BlindTestCompletException` |
| Un participant ne rejoint qu'une fois | `RejoindreBlindTestUseCase` | `ParticipantDejaPresentException` |
| Le démarrage résulte d'une action, jamais du remplissage | `LancerBlindTestUseCase` | `BlindTestDejaDemarreException` |
| Seul un participant inscrit peut lancer la partie | `LancerBlindTestUseCase` | `ParticipantHorsBlindTestException` |
| Le démarrage exige 3 participants et 7 morceaux | `LancerBlindTestUseCase` | `BlindTestIncompletException`, `NombreDeMorceauxInvalideException` |
| Au démarrage, le premier morceau est joué | `LancerBlindTestUseCase` | — |
| Le premier clic met en pause et réserve la réponse | `MettreEnPauseBlindTestUseCase` | `BlindTestNonDemarreException`, `ParticipantHorsBlindTestException` |
| Un seul réservataire à la fois | `MettreEnPauseBlindTestUseCase` | `ReponseDejaReserveeException` |
| Seul le réservataire peut répondre | `FaireUnePropositionUseCase` | `ReponseNonReserveeException` |
| Une bonne réponse rapporte un point | `FaireUnePropositionUseCase` | — |
| Une mauvaise réponse relance la lecture | `FaireUnePropositionUseCase` | — |
| Après 7 morceaux : terminé, scores figés | `FaireUnePropositionUseCase` | `BlindTestTermineException` |
| Comparaison sans casse, accents ni espaces superflus | `FaireUnePropositionUseCase` | — |

La normalisation des titres est du Java pur (`java.text.Normalizer`) : décomposition NFD,
suppression des diacritiques, réduction des espaces multiples, minuscules en `Locale.ROOT`,
et traitement des ligatures (`œ`, `æ`) et de l'apostrophe typographique.

---

## 5. L'arbitrage du premier clic

Le sujet impose que le serveur soit seul juge et que la réservation résiste aux accès
simultanés, sans mettre cette logique dans le contrôleur. Deux mécanismes se complètent.

**La règle est dans le domaine.** `MettreEnPauseBlindTestUseCase.apply` vérifie que le blind
test est en cours, que le participant y est inscrit et que personne n'a encore la main, puis
passe en pause et enregistre le réservataire. Cette règle se teste sans base de données : deux
appels successifs et le second lève `ReponseDejaReserveeException`.

**L'ordre d'arrivée est arbitré par la base.** Quand trois requêtes HTTP évaluent la règle en
parallèle sur trois copies mémoire toutes valides, il faut trancher qui est arrivé le premier.
`BlindTestJpaRepository.reserverLaReponse` réalise une **mise à jour conditionnelle** :

```sql
UPDATE blind_test
   SET reservataire_id = :idParticipant,
       etat_lecture = 'PAUSE',
       version = version + 1
 WHERE nom = :nom
   AND statut = 'EN_COURS'
   AND index_morceau_courant = :indexMorceau
   AND reservataire_id IS NULL
```

Le premier `UPDATE` verrouille la ligne ; le second réévalue sa clause `WHERE` sur la version
validée, ne trouve plus `reservataire_id IS NULL` et modifie zéro ligne. Le port du domaine
expose ce résultat en vocabulaire métier — `boolean reserverLaReponse(BlindTest)`,
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

Le diagramme de classes porte `email` / `motDePasse` (`Participant`), `nom` (`BlindTest`,
`Morceau`) et `score` (`Participation`). Les modèles n'ont **aucun identifiant technique** :
l'`id` reste dans les entités JPA. Les seuls champs ajoutés sont ceux sans lesquels la partie
ne pourrait pas se dérouler :

| Champ | Classe | Justification |
|---|---|---|
| `statut` | `BlindTest` | distingue « en attente », « en cours » et « terminé », condition de presque toutes les règles |
| `indexMorceauCourant` | `BlindTest` | position dans la séquence des sept morceaux |
| `etatLecture` | `BlindTest` | indique au navigateur s'il doit jouer ou mettre en pause |
| `reservataire` | `BlindTest` | participant qui a gagné le premier clic sur le morceau courant |
| `version` | `BlindTest` | jeton de verrou optimiste, transporté entre la base et le domaine |
| `urlAudio` | `Morceau` | adresse du fichier audio, exigée par la section « données de démarrage » |

Deux énumérations accompagnent ces champs : `StatutBlindTest` (`EN_ATTENTE`, `EN_COURS`,
`TERMINE`) et `EtatLecture` (`LECTURE`, `PAUSE`). `BlindTest` porte aussi les deux constantes
`NOMBRE_MAXIMUM_DE_PARTICIPANTS` et `NOMBRE_DE_MORCEAUX` : ce sont des données du modèle, et
les disperser sèmerait des nombres magiques dans trois use cases.

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

**Modèles anémiques.** Les quatre modèles n'ont ni identifiant ni méthode, comme dans le projet
de référence. Conséquence directe : un comportement partagé par deux use cases ne peut plus être
factorisé sur le modèle, et deux courtes duplications subsistent — la libération de la main dans
`MettreEnPauseBlindTestUseCase` et `FaireUnePropositionUseCase`, et la recherche d'une
participation par email dans trois use cases. C'est le prix assumé de l'alignement. La troisième,
la transition « démarrer », a disparu en même temps que le démarrage automatique : elle n'existe
plus que dans `LancerBlindTestUseCase`.

**`LancerBlindTestUseCase.apply(Participant, BlindTest)`.** Le diagramme de classes impose
`apply(BlindTest)`. Contrôler que seul un participant inscrit peut lancer la partie exige de
connaître l'appelant : le principal issu du `JwtFilter` porte l'email, et la comparaison se fait
dessus. Aucun appel supplémentaire au `ParticipantRepository`, les dépendances de l'adapter
restent donc celles du diagramme.

**Identité par clé naturelle.** Un blind test est désigné par son `nom` (unique en base), comme
l'enchère l'est par le sien dans la référence (`findByNomEnchere`). Les routes deviennent donc
`/api/blindtests/{nom}/…` au lieu des `/{id}` de l'énoncé. Les noms contenant des espaces ou des
accents sont encodés dans l'URL (`Soirée du jeudi` → `Soir%C3%A9e%20du%20jeudi`) ; le parcours a
été vérifié avec de tels noms.

**Spring et Lombok dans le domaine.** Comme dans le projet de référence, les use cases portent
`@Component` et les modèles utilisent Lombok (`@Data`, `@NonNull`) et `jakarta.validation`
(`@Email`, `@Size`). Ces deux points sont volontairement **hors du périmètre** des règles
ArchUnit, qui vérifient tout le reste.

**Mots de passe en clair.** `ParticipantRepository.findByEmailAndMotDePasse` reproduit la
signature du projet de référence. À ne pas reproduire en production.

**`SeDeconnecterAdapter` sans repository.** Le diagramme associe `ParticipantRepository` à
`SeDeconnecterUseCase`, mais le jeton JWT est sans état : la déconnexion se limite à vider le
contexte de sécurité et à effacer le cookie.

**`FaireUnePropositionAdapter`.** Le diagramme de classes lui associe `MorceauRepository` ;
l'adapter ne l'utilise pas, car les sept morceaux appartiennent à l'agrégat `BlindTest` déjà
rechargé. En revanche il porte `BlindTestRepository`, comme le montre le diagramme de séquence.

**Composition `BlindTest` → `Morceau`.** Le diagramme la note comme une composition ; en base
c'est une `@ManyToMany` ordonnée (`@OrderColumn`), car le catalogue de morceaux est partagé
entre tous les blind tests.

**Spring Boot 4.1.1 plutôt que 3.x.** Le squelette déjà commité et le projet de référence
utilisent Spring Boot 4.1.1 avec Java 25, ainsi que les artefacts modulaires
(`spring-boot-starter-webmvc`, `spring-boot-h2console`).

---

## 8. Hypothèses retenues

- **« Blind tests en cours »** = statuts `EN_ATTENTE` et `EN_COURS`. Ne renvoyer que les parties
  déjà démarrées rendrait la liste inutile, puisque aucune ne serait rejoignable.
- **Recliquer après une mauvaise réponse est autorisé.** La main est libérée, la lecture reprend,
  et tout le monde — y compris l'auteur de l'erreur — peut cliquer à nouveau sur le même morceau.
- **Le blind test ne démarre jamais tout seul.** Réunir trois participants le rend seulement
  lançable ; il faut qu'un **participant inscrit** clique sur « Lancer le blind test ». Un tiers
  qui tente `POST /lancer` reçoit un 403, et un second lancement un 409.
- **Le nom d'un blind test est unique** et ne peut pas être modifié : c'est sa clé.
- **Les sept morceaux sont tirés au hasard** dans le catalogue inséré au démarrage, et figés à la
  création du blind test.
- **Un blind test naît sans participant.** `AjouterBlindTestUseCase.apply(String nom)` ne reçoit
  pas de créateur ; la cardinalité `1..3` du diagramme se lit « pendant la partie ».
- **Aucune minuterie serveur.** Sans clic, un morceau n'expire jamais. La lecture audio est
  pilotée par le navigateur, le serveur ne fait que publier un état.
- **Le titre du morceau courant n'est jamais publié** par `/etat` : c'est la réponse à trouver.
- **Déconnexion sans révocation.** Le cookie est effacé, mais le jeton reste valide jusqu'à son
  expiration (deux heures par défaut).
- **Les morceaux viennent de l'API publique de Deezer**, qui expose pour chaque piste un extrait
  de trente secondes au format MP3 : le blind test porte donc sur de vrais titres connus. Si
  l'API est injoignable, ou si `app.catalogue.deezer.actif=false`, le catalogue retombe sur des
  fichiers de démonstration (SoundHelix) sans rapport avec les titres — il compte dans tous les
  cas sept morceaux.
- **Les liens d'extrait Deezer sont signés et ne valent que quinze minutes** ; l'URL privée de sa
  signature renvoie 403. Comme la base H2 est en mémoire, le catalogue est reconstruit à chaque
  démarrage : il suffit de **relancer l'application avant une démonstration**. Au-delà d'un quart
  d'heure de fonctionnement, l'audio peut cesser de se charger — limite assumée, le contournement
  (résoudre le lien à la demande derrière un endpoint de redirection) n'a pas été retenu.
- **YouTube a été écarté comme source.** La seule voie sanctionnée est l'API IFrame Player, or le
  lecteur affiche le titre de la vidéo — c'est-à-dire la réponse — et les conditions
  d'utilisation interdisent aussi bien de masquer le lecteur que de séparer l'audio de la vidéo.
- **Pas de départage en cas d'égalité** de scores.

---

## 9. Tests

```bash
./mvnw clean install
```

51 tests, tous verts : 45 unitaires (surefire) et 6 d'intégration (failsafe, suffixe `IT`).

| Suite | Ce qu'elle vérifie |
|---|---|
| [`RejoindreBlindTestUseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase/RejoindreBlindTestUseCaseTest.java) | refus du 4ᵉ participant, refus du doublon, et **absence de démarrage** à l'arrivée du 3ᵉ |
| [`LancerBlindTestUseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase/LancerBlindTestUseCaseTest.java) | démarrage sur le premier morceau, refus à moins de 3 participants, refus d'un participant qui ne joue pas, refus d'un second démarrage |
| [`AjouterBlindTestUseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase/AjouterBlindTestUseCaseTest.java) | exigence des 7 morceaux |
| [`MettreEnPauseBlindTestUseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase/MettreEnPauseBlindTestUseCaseTest.java) | premier clic accepté, second refusé, course perdue en base, participant hors partie |
| [`FaireUnePropositionUseCaseTest`](src/test/java/com/esgi/blindTest/domain/usecase/FaireUnePropositionUseCaseTest.java) | point gagné et morceau suivant, mauvaise réponse qui relance, seul le réservataire répond, fin après le 7ᵉ morceau, et des tests paramétrés sur casse / accents / espaces / ligatures |
| [`AjoutMorceauxTest`](src/test/java/com/esgi/blindTest/infra/persistance/initialisation/AjoutMorceauxTest.java) | le catalogue compte toujours 7 morceaux, que Deezer réponde ou non, et n'est pas réinséré s'il existe déjà |
| [`ArchitectureTest`](src/test/java/com/esgi/blindTest/architecture/ArchitectureTest.java) | **ArchUnit**, 10 règles de dépendances entre couches |
| [`BlindTestRestControllerIT`](src/test/java/com/esgi/blindTest/presentation/controller/rest/BlindTestRestControllerIT.java) | **intégration** : contexte Spring démarré, use cases mockés — 401 sans jeton, liste, création, 409 sur le second clic, 403 sur un lancement par un non-participant, 400 sur nom vide |

Tous les tests de règles métier sont des **tests unitaires sans Spring**, avec un `OutputPort`
mocké par Mockito.

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

Toutes les routes sauf l'inscription et la connexion exigent le cookie JWT. `{nom}` est le nom
du blind test, encodé pour l'URL.

| Méthode | Chemin | Effet |
|---|---|---|
| `POST` | `/api/participants/inscription` | crée un compte — `201`, `409` si l'email existe, `400` si le mot de passe fait moins de 8 caractères |
| `POST` | `/api/participants/connexion` | dépose le cookie `HttpOnly` — `200`, `401` si les identifiants sont faux |
| `POST` | `/api/participants/deconnexion` | efface le cookie — `204` |
| `GET` | `/api/blindtests` | liste les blind tests rejoignables |
| `POST` | `/api/blindtests` | crée un blind test de 7 morceaux — `201` |
| `POST` | `/api/blindtests/{nom}/rejoindre` | rejoint — `204`, `409` si complet ou déjà inscrit |
| `POST` | `/api/blindtests/{nom}/lancer` | démarre la partie — `204`, `403` si vous n'y participez pas, `409` si déjà démarrée ou incomplète |
| `POST` | `/api/blindtests/{nom}/pause` | clic « J'ai trouvé » — `204` pour le premier, `409` pour les suivants |
| `POST` | `/api/blindtests/{nom}/proposition` | propose un titre — `200 {"juste": …}`, `409` si vous n'avez pas la main |
| `GET` | `/api/blindtests/{nom}/etat` | état complet, interrogé chaque seconde par la salle de jeu |
