function showLoginPageResponseMessages(errorCode) {
  document.querySelectorAll('.message').forEach(el => el.classList.remove('show'));

  const errorElement = document.getElementById(`message-${errorCode}`);

  if (errorElement) {
    errorElement.classList.add('show');
  } else {
    throw new Error('Response message is not exist in Messages wrapper element');
  }
}

function hideLoginPageResponseMessage() {
  return document.querySelectorAll('.message').forEach(el => el.classList.remove('show'));
}