function afficherMessage(texte, estUneErreur) {
    const message = document.getElementById('message');
    message.textContent = texte || '';
    message.className = 'message ' + (estUneErreur ? 'erreur' : 'succes');
}

async function envoyer(url, corps) {
    const options = {method: 'POST', headers: {'Content-Type': 'application/json'}};
    if (corps !== undefined) {
        options.body = JSON.stringify(corps);
    }
    const reponse = await fetch(url, options);
    const texte = await reponse.text();
    const donnees = texte ? JSON.parse(texte) : null;
    return {ok: reponse.ok, statut: reponse.status, donnees: donnees};
}
