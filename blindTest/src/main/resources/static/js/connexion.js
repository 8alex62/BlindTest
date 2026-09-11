document.getElementById('formulaire').addEventListener('submit', async function (evenement) {
    evenement.preventDefault();
    const resultat = await envoyer('/api/participants/connexion', {
        email: document.getElementById('email').value,
        motDePasse: document.getElementById('motDePasse').value
    });
    if (resultat.ok) {
        window.location.href = '/blindtests';
    } else {
        afficherMessage(resultat.donnees ? resultat.donnees.message : 'Connexion refusee.', true);
    }
});
