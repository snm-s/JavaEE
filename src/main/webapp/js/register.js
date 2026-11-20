// Validation côté client du formulaire d'inscription (confort d'utilisation).
// La vérification qui fait foi reste celle de AuthService, côté serveur.
document.addEventListener('DOMContentLoaded', function () {
  var form = document.getElementById('registerForm');
  if (!form) return;

  var firstName = document.getElementById('firstName');
  var lastName = document.getElementById('lastName');
  var email = document.getElementById('email');
  var password = document.getElementById('password');
  var confirmPassword = document.getElementById('confirmPassword');

  function setInvalid(input, invalid) {
    input.closest('.field').classList.toggle('invalid', invalid);
  }

  function validPassword(value) {
    return value.length >= 8
      && value.length <= 72
      && /[A-Za-z]/.test(value)
      && /\d/.test(value);
  }

  form.addEventListener('submit', function (event) {
    var okFirstName = firstName.value.trim().length > 0;
    var okLastName = lastName.value.trim().length > 0;
    var okEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim());
    var okPassword = validPassword(password.value);
    var okConfirm = confirmPassword.value === password.value && okPassword;

    setInvalid(firstName, !okFirstName);
    setInvalid(lastName, !okLastName);
    setInvalid(email, !okEmail);
    setInvalid(password, !okPassword);
    setInvalid(confirmPassword, !okConfirm);

    if (!okFirstName || !okLastName || !okEmail || !okPassword || !okConfirm) {
      event.preventDefault();
    }
  });

  [firstName, lastName, email, password, confirmPassword].forEach(function (input) {
    input.addEventListener('input', function () {
      setInvalid(input, false);
    });
  });
});
