const identifiant = window.ID_BLIND_TEST;
const lecteur = document.getElementById('lecteur');
let urlCourante = null;

async function rafraichir() {
    const reponse = await fetch('/api/blindtests/' + identifiant + '/etat');
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

    document.getElementById('trouve').disabled =
        etat.statut !== 'EN_COURS' || etat.reponseReservee;
    document.getElementById('rejoindre').hidden = etat.statut !== 'EN_ATTENTE';
    document.getElementById('proposition-form').hidden = !etat.vousAvezLaMain;
}

document.getElementById('rejoindre').addEventListener('click', async function () {
    const resultat = await envoyer('/api/blindtests/' + identifiant + '/rejoindre');
    afficherMessage(resultat.ok ? 'Vous avez rejoint ce blind test.'
        : (resultat.donnees ? resultat.donnees.message : 'Impossible de rejoindre.'), !resultat.ok);
    rafraichir();
});

document.getElementById('trouve').addEventListener('click', async function () {
    const resultat = await envoyer('/api/blindtests/' + identifiant + '/pause');
    afficherMessage(resultat.ok ? 'Vous avez la main, proposez un titre.'
        : (resultat.donnees ? resultat.donnees.message : 'Trop tard.'), !resultat.ok);
    rafraichir();
});

document.getElementById('proposition-form').addEventListener('submit', async function (evenement) {
    evenement.preventDefault();
    const champ = document.getElementById('proposition');
    const resultat = await envoyer('/api/blindtests/' + identifiant + '/proposition',
        {proposition: champ.value});
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
