// Le blind test est designe par son nom : il faut encoder les espaces et accents.
const base = '/api/blindtests/' + encodeURIComponent(window.NOM_BLIND_TEST);
const lecteur = document.getElementById('lecteur');
const boutonRejoindre = document.getElementById('rejoindre');
const boutonLancer = document.getElementById('lancer');
const boutonTrouve = document.getElementById('trouve');
let urlCourante = null;

async function rafraichir() {
    const reponse = await fetch(base + '/etat');
    if (reponse.status === 401) {
        window.location.href = '/connexion';
        return;
    }
    if (!reponse.ok) {
        return;
    }
    const etat = await reponse.json();

    document.getElementById('nom').textContent = etat.nom;
    document.getElementById('statut').textContent = etat.statut;
    document.getElementById('morceau').textContent =
        etat.numeroDuMorceau + ' / ' + etat.nombreDeMorceaux;
    document.getElementById('lecture').textContent = etat.etatLecture;
    document.getElementById('scores').innerHTML = etat.scores.map(function (score) {
        return '<li>' + score.email + ' : ' + score.score + '</li>';
    }).join('');

    if (etat.urlAudio && etat.urlAudio !== urlCourante) {
        urlCourante = etat.urlAudio;
        lecteur.src = etat.urlAudio;
    }
    if (etat.etatLecture === 'LECTURE') {
        // Le navigateur peut refuser la lecture automatique avant une action de l'utilisateur.
        lecteur.play().catch(function () { });
    } else {
        lecteur.pause();
    }

    const enAttente = etat.statut === 'EN_ATTENTE';
    const participants = etat.scores.length;
    const complet = participants >= etat.nombreMaximumDeParticipants;

    // On ne propose de rejoindre qu'a qui ne participe pas encore.
    boutonRejoindre.hidden = !enAttente || etat.vousParticipez;

    // Le blind test ne demarre jamais tout seul : un participant doit le lancer.
    boutonLancer.hidden = !enAttente || !etat.vousParticipez;
    boutonLancer.disabled = !complet;
    boutonLancer.textContent = complet
        ? 'Lancer le blind test'
        : 'Lancer (' + participants + ' / ' + etat.nombreMaximumDeParticipants + ')';

    boutonTrouve.disabled = etat.statut !== 'EN_COURS' || etat.reponseReservee;
    document.getElementById('proposition-form').hidden = !etat.vousAvezLaMain;
}

boutonRejoindre.addEventListener('click', async function () {
    const resultat = await envoyer(base + '/rejoindre');
    afficherMessage(resultat.ok ? 'Vous avez rejoint ce blind test.'
        : (resultat.donnees ? resultat.donnees.message : 'Impossible de rejoindre.'), !resultat.ok);
    rafraichir();
});

boutonLancer.addEventListener('click', async function () {
    const resultat = await envoyer(base + '/lancer');
    afficherMessage(resultat.ok ? 'Le blind test est lance.'
        : (resultat.donnees ? resultat.donnees.message : 'Impossible de lancer.'), !resultat.ok);
    rafraichir();
});

boutonTrouve.addEventListener('click', async function () {
    const resultat = await envoyer(base + '/pause');
    afficherMessage(resultat.ok ? 'Vous avez la main, proposez un titre.'
        : (resultat.donnees ? resultat.donnees.message : 'Trop tard.'), !resultat.ok);
    rafraichir();
});

document.getElementById('proposition-form').addEventListener('submit', async function (evenement) {
    evenement.preventDefault();
    const champ = document.getElementById('proposition');
    const resultat = await envoyer(base + '/proposition', {proposition: champ.value});
    if (resultat.ok) {
        afficherMessage(resultat.donnees.juste ? 'Bonne reponse, un point de plus.'
            : 'Raté, la lecture reprend pour tout le monde.', !resultat.donnees.juste);
    } else {
        afficherMessage(resultat.donnees ? resultat.donnees.message : 'Proposition refusee.', true);
    }
    champ.value = '';
    rafraichir();
});

setInterval(rafraichir, 1000);
rafraichir();
