function getUserLetter(pt) {
    if (pt._ruLetter) {
        return pt._ruLetter.toUpperCase();
    }
}


function setLetter(value) {
    var $session = $jsapi.context().session;
    $session.currentLetter = value;
}

function getUserWord(pt) {
    if (pt.randomWord) {
        return pt._randomWord.toUpperCase();
    }
}




// function getUserWord(pt) {
//     if (!pt || !pt._words) return "";
//     return pt._words.trim().toUpperCase();
// }