let successText = '';
let successFunction;

function executeReAuthentication(text, func) {
    successText = text;
    successFunction = func;

    ModalOpen('#reAuthenticationModal');
}

// Open verifying account Popup
function executeVerifyingAccount(){
    ModalOpenClose('#reAuthenticationModal');

    // open modal
    setResendTime();
    ModalOpen('#verifyingAccountModal');
}

function executeSamsungVerifyingAccount(){
    // open modal
    samsungSetResendLink();
    setResendTime();
    ModalOpen('#samsungVerifyingAccountModal');
}

// Close myPage Popup
function closeMyPagePopup(id){
    ModalOpenClose(id);
}

function verifySuccess() {
    closeMyPagePopup('#verifyingAccountModal');
    successFunction();
}

function samsungVerifySuccess() {
    closeMyPagePopup('#samsungVerifyingAccountModal');
    successFunction();
}

function openUserConsentPreviewModal(consent, content, version, releaseDate) {

    const previewTitle = document.getElementById("userConsentPreviewTitle");
    const previewVersion = document.getElementById("userConsentPreviewVersion");
    const previewContent = document.getElementById("userConsentPreviewContent");

    let previewVersionText = '';
    previewVersionText = `Version ${version} | ${releaseDate}`;

    previewTitle.innerText = consent;
    previewVersion.innerText = previewVersionText;
    previewContent.innerHTML = content;

    ModalOpen('#userConsentPreviewModal');
}