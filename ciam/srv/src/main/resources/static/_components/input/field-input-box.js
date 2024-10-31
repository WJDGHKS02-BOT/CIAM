document.addEventListener('DOMContentLoaded', function () {
  const inputs = document.querySelectorAll('.field-input-box');

  inputs.forEach(input => {
    const fieldWrapper = input.closest('.field-wrapper');
    const clearButton = fieldWrapper.querySelector('.field-input-box-clear-button');

    // 초기 상태에서 clear 버튼 숨기기
    clearButton.style.display = 'none';

    // input에 focus 되었을 때 clear 버튼 보이기
    input.addEventListener('focus', () => {
      if (input.value) {
        clearButton.style.display = 'block';
      }
    });

    // input에서 focus가 벗어났을 때 clear 버튼 숨기기
    input.addEventListener('blur', () => {
      // setTimeout을 사용하여 클릭 이벤트가 발생할 시간을 줍니다.
      setTimeout(() => {
        clearButton.style.display = 'none';
      }, 100);
    });

    // input 값이 변경될 때마다 clear 버튼 표시 여부 결정
    input.addEventListener('input', () => {
      clearButton.style.display = input.value ? 'block' : 'none';
    });

    // clear 버튼 클릭 시 input 값 초기화
    clearButton.addEventListener('click', () => {
      input.value = '';
      clearButton.style.display = 'none';
      input.focus(); // 값을 지운 후 다시 input에 focus
    });
  });
});

function togglePasswordVisibility(button) {
  const wrapper = button.closest('.field-wrapper');
  const input = wrapper.querySelector('input');
  const img = button.querySelector('img');

  if (input.type === 'password') {
    input.type = 'text';
    img.src = '/theme/assets/image/common/ico_pwd_hidden.png';
    img.alt = 'password-visible';
  } else {
    input.type = 'password';
    img.src = '/theme/assets/image/common/ico_pwd.png';
    img.alt = 'password-hidden';
  }
}

function resetFieldInputBox({inputElement}) {
  inputElement.parentNode.classList.remove('field-state--error')
  inputElement.classList.remove('field-input-box-state--error')
  inputElement.parentNode.classList.remove('field-state--success')
  inputElement.classList.remove('field-input-box-state--success')
  inputElement.parentNode.children[3].textContent = '';
}

function setMessageFieldInputBox({type, inputElement, message}) {
  if (type === 'error') {
    inputElement.parentNode.classList.add('field-state--error')
    inputElement.classList.add('field-input-box-state--error')
    inputElement.parentNode.children[3].textContent = message;
  } else if (type === 'success') {
    inputElement.parentNode.classList.add('field-state--success')
    inputElement.classList.add('field-input-box-state--success')
    inputElement.parentNode.children[3].textContent = message;
  }
}