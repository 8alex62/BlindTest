# Architecture

## 1. Diagramme de packages

La flèche se lit « dépend de ». Toutes les flèches pointent vers `domain` : c'est la règle
de dépendance vers l'intérieur.

```mermaid
flowchart BT
    subgraph presentation["presentation"]
        direction TB
        rest["controller.rest<br/>ParticipantRestController<br/>BlindTestRestController<br/>GestionnaireDExceptions"]
        web["controller.web<br/>PageController"]
        requete["request<br/>InscriptionRequest, ConnexionRequest<br/>AjoutBlindTestRequest, PropositionRequest"]
        reponse["response<br/>ConnexionResponse, BlindTestResponse<br/>EtatBlindTestResponse, ScoreParticipant"]
    end

    subgraph securite["security"]
        direction TB
        config["configuration<br/>SecurityConfiguration"]
        filtre["filter<br/>JwtFilter"]
        service["service<br/>AuthenticationService"]
        util["util<br/>JwtUtil"]
    end

    subgraph infra["infra.persistance"]
        direction TB
        entity["entity<br/>ParticipantEntity, BlindTestEntity<br/>MorceauEntity, ParticipationEntity<br/>— seules classes porteuses d'un id"]
        jpa["repository<br/>ParticipantJpaRepository, BlindTestJpaRepository<br/>MorceauJpaRepository, ParticipationJpaRepository"]
        init["initialisation<br/>AjoutMorceaux"]
    end

    subgraph adapter["adapter"]
        direction TB
        ucAdapter["usecase_adapter<br/>SInscrireAdapter … MettreEnPauseBlindTestAdapter"]
        mapper["mapper<br/>ParticipantEntityMapper, BlindTestEntityMapper<br/>MorceauEntityMapper, ParticipationEntityMapper, BlindTestMapper"]
        repoImpl["repository<br/>ParticipantRepositoryImpl, BlindTestRepositoryImpl<br/>MorceauRepositoryImpl, ParticipationRepositoryImpl"]
    end

    subgraph domain["domain"]
        direction TB
        model["model<br/>Participant, BlindTest, Morceau, Participation<br/>— porteurs de données, sans id ni méthode"]
        usecase["usecase<br/>les 10 use cases : ils appliquent les règles"]
        port["repository<br/>ParticipantRepository, BlindTestRepository<br/>MorceauRepository, ParticipationRepository"]
        exception["exception<br/>les exceptions métier"]
    end

    usecase --> model
    usecase --> exception
    usecase --> port
    port --> model

    ucAdapter --> usecase
    ucAdapter --> port
    repoImpl --> port
    repoImpl --> jpa
    repoImpl --> mapper
    mapper --> model
    mapper --> entity
    mapper --> reponse

    entity --> model
    jpa --> entity
    init --> entity
    init --> jpa

    rest --> usecase
    rest --> requete
    rest --> reponse
    rest --> mapper
    rest --> service
    web --> rest

    filtre --> util
    filtre --> model
    config --> filtre
    service --> util
    service --> model
```

Lecture des trois règles à retenir :

- `domain` n'a **aucune** flèche sortante vers `adapter`, `infra`, `presentation` ou `security`.
- `entity` (les entités JPA) n'est atteint que depuis `infra` et `adapter.mapper` : aucune entité
  de persistance ne remonte vers la présentation ni vers le domaine, et **l'identifiant technique
  ne sort jamais de cette couche**.
- `presentation` n'a **aucune** flèche vers `infra` : elle passe par les use cases et leurs ports.

Ces trois propriétés sont vérifiées automatiquement par
[ArchitectureTest](../src/test/java/com/esgi/blindTest/architecture/ArchitectureTest.java).

## 2. Diagramme de séquence — faire une proposition

Le scénario reprend l'interaction modélisée dans Visual Paradigm
(`UML/projet_blind_test.vpp`, diagramme « Mettre en pause + Faire une proposition »),
depuis l'envoi de la proposition jusqu'à la réponse. Les modèles étant de simples porteurs de
données, les règles sont appliquées par le use case lui-même.

```mermaid
sequenceDiagram
    autonumber
    actor P as Participant
    participant JF as JwtFilter
    participant RC as BlindTestRestController
    participant UC as FaireUnePropositionUseCase
    participant AD as FaireUnePropositionAdapter
    participant BRI as BlindTestRepositoryImpl
    participant PRI as ParticipationRepositoryImpl
    participant JPA as BlindTestJpaRepository
    participant DB as Base de données

    P->>JF: POST /api/blindtests/{nom}/proposition
    JF->>JF: lit le cookie HttpOnly, valide le jeton
    JF->>RC: requête authentifiée (principal = Participant)

    RC->>UC: apply(blindtest, participant, proposition)

    UC->>AD: findParticipant(participant)
    AD-->>UC: Participant

    UC->>AD: findBlindTest(blindtest)
    AD->>BRI: findByNom(nom)
    BRI->>JPA: findByNom(nom)
    JPA->>DB: SELECT blind_test
    DB-->>JPA: row selected
    JPA-->>BRI: BlindTestEntity
    BRI-->>AD: BlindTest (mappé par MapStruct)
    AD-->>UC: BlindTest

    Note over UC: Règles appliquées ici :<br/>seul le réservataire répond,<br/>comparaison sans casse ni accents,<br/>un point par bonne réponse.

    alt proposition juste
        UC->>UC: score + 1, puis morceau suivant<br/>ou fin après le 7e
        UC->>AD: ajouterUnPoint(blindTest, participation)
        AD->>PRI: ajouterUnPoint(nom, participation)
        PRI->>DB: UPDATE participation
        DB-->>PRI: row updated
        PRI-->>AD: Participation
    else proposition fausse
        UC->>UC: libère la main, la lecture reprend
    end

    UC->>AD: save(blindTest)
    AD->>BRI: save(blindTest)
    BRI->>BRI: contrôle de version (verrou optimiste)
    BRI->>JPA: save(entity)
    JPA->>DB: UPDATE blind_test
    DB-->>JPA: row updated
    JPA-->>BRI: BlindTestEntity
    BRI-->>AD: BlindTest
    AD-->>UC: BlindTest

    UC-->>RC: résultat : juste ou faux
    RC-->>P: 200 {"juste": true|false}
```

## 3. Diagramme de séquence — arbitrage du premier clic

Le point délicat : trois participants peuvent cliquer « J'ai trouvé » en même temps.
La **règle** est appliquée par le use case, l'**ordre d'arrivée** est arbitré par la base.

```mermaid
sequenceDiagram
    autonumber
    actor A as Alice
    actor B as Bob
    participant RC as BlindTestRestController
    participant UC as MettreEnPauseBlindTestUseCase
    participant BRI as BlindTestRepositoryImpl
    participant DB as Base de données

    par Alice clique
        A->>RC: POST /api/blindtests/{nom}/pause
        RC->>UC: apply(alice, blindtest)
        Note over UC: Règle : statut EN_COURS,<br/>participant inscrit,<br/>aucun réservataire.
        UC->>UC: passe en PAUSE, réservataire = alice
        UC->>BRI: reserverLaReponse(blindTest)
        BRI->>DB: UPDATE blind_test SET reservataire_id = alice<br/>WHERE reservataire_id IS NULL AND index = n
        DB-->>BRI: 1 ligne modifiée
        BRI-->>UC: true
        UC-->>RC: succès
        RC-->>A: 204
    and Bob clique au même instant
        B->>RC: POST /api/blindtests/{nom}/pause
        RC->>UC: apply(bob, blindtest)
        UC->>UC: passe en PAUSE, réservataire = bob
        UC->>BRI: reserverLaReponse(blindTest)
        BRI->>DB: même UPDATE conditionnel
        DB-->>BRI: 0 ligne modifiée
        BRI-->>UC: false
        UC->>UC: libère la main
        UC-->>RC: ReponseDejaReserveeException
        RC-->>B: 409 « Un autre participant a déjà pris la main sur ce morceau. »
    end
```

La clause `WHERE ... AND reservataire_id IS NULL AND index_morceau_courant = :n`
est évaluée par la base après le verrou de ligne du premier `UPDATE` : il ne peut donc pas y
avoir deux gagnants, quel que soit l'entrelacement des requêtes.
