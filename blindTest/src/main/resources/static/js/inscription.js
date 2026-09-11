document.getElementById('formulaire').addEventListener('submit', async function (evenement) {
    evenement.preventDefault();
    const resultat = await envoyer('/api/participants/inscription', {
        email: document.getElementById('email').value,
        motDePasse: document.getElementById('motDePasse').value
    });
    if (resultat.ok) {
        afficherMessage('Compte cree, vous pouvez vous connecter.', false);
        setTimeout(function () { window.location.href = '/connexion'; }, 800);
    } else {
        afficherMessage(resultat.donnees ? resultat.donnees.message : 'Inscription refusee.', true);
    }
});
