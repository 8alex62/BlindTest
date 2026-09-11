async function rafraichir() {
    const reponse = await fetch('/api/blindtests');
    if (reponse.status === 401) {
        window.location.href = '/connexion';
        return;
    }
    const blindTests = await reponse.json();
    const lignes = blindTests.map(function (blindTest) {
        return '<tr>'
            + '<td>' + blindTest.nom + '</td>'
            + '<td>' + blindTest.statut + '</td>'
            + '<td>' + blindTest.nombreDeParticipants + ' / '
            + blindTest.nombreMaximumDeParticipants + '</td>'
            + '<td><a href="/blindtests/' + encodeURIComponent(blindTest.nom)
            + '">Ouvrir</a></td>'
            + '</tr>';
    });
    document.getElementById('liste').innerHTML =
        lignes.join('') || '<tr><td colspan="4">Aucun blind test pour le moment.</td></tr>';
}

document.getElementById('formulaire').addEventListener('submit', async function (evenement) {
    evenement.preventDefault();
    const resultat = await envoyer('/api/blindtests', {nom: document.getElementById('nom').value});
    if (resultat.ok) {
        document.getElementById('nom').value = '';
        afficherMessage('Blind test cree.', false);
        rafraichir();
    } else {
        afficherMessage(resultat.donnees ? resultat.donnees.message : 'Creation refusee.', true);
    }
});

document.getElementById('deconnexion').addEventListener('click', async function (evenement) {
    evenement.preventDefault();
    await envoyer('/api/participants/deconnexion');
    window.location.href = '/connexion';
});

rafraichir();
