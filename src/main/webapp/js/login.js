// Validation côté client (confort d'utilisation).
// ⚠️ La vraie vérification doit rester dans la servlet (exigence du projet).
document.addEventListener('DOMContentLoaded', function () {
  var form  = document.getElementById('loginForm');
  var email = document.getElementById('email');
  var pwd   = document.getElementById('password');
  var btn   = document.getElementById('togglePwd');

  btn.addEventListener('click', function () {
    var hidden = pwd.type === 'password';
    pwd.type = hidden ? 'text' : 'password';
    btn.textContent = hidden ? 'Masquer' : 'Afficher';
  });

  function setInvalid(input, invalid) {
    input.closest('.field').classList.toggle('invalid', invalid);
  }

  form.addEventListener('submit', function (e) {
    var okEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim());
    var okPwd   = pwd.value.length > 0;
    setInvalid(email, !okEmail);
    setInvalid(pwd, !okPwd);
    if (!okEmail || !okPwd) e.preventDefault();
  });

  [email, pwd].forEach(function (i) {
    i.addEventListener('input', function () { setInvalid(i, false); });
  });
});