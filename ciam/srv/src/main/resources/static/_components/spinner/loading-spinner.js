/*
  관련 파일 : /_scripts/states/process-manager
*/
function showLoadingSpinner() {
  const loadingSpinnerElement = document.querySelector('.loading-spinner');
  loadingSpinnerElement.className = "loading-spinner show"
}

function hideLoadingSpinner() {
  const loadingSpinnerElement = document.querySelector('.loading-spinner');
  loadingSpinnerElement.className = "loading-spinner hidden"
}