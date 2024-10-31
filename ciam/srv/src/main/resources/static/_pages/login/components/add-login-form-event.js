function addLoginFormEvent({form, submitButton, validity, enterKeyFunc, inputFunc}) {
  const checkValidity = ({validity, submitButton}) => {
    const isAllValid = Object.values(validity).every(Boolean);

    (function updateButtonState({isAllValid}) {
      const buttonTheme = isAllValid ? 'blue' : 'white';

      submitButton.className = `button button-size--m button-theme--${buttonTheme}`;
      submitButton.disabled = !isAllValid;
    })({isAllValid});
  };

  form.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' || e.keyCode === 13) {
      e.preventDefault();
      return submitButton.disabled ? showLoginPageResponseMessages('MISSING_REQUIRED_PARAMETER') : enterKeyFunc();
    }
  });

  form.addEventListener('input', (e) => {
    inputFunc(e);

    checkValidity({
      validity: validity,
      submitButton: submitButton
    });
  })
}
