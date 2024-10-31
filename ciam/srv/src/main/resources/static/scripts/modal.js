function appendTextToComponent(component, text) {
    let newLiComponent = document.createElement('li');
    newLiComponent.innerHTML = `${text}`;
    component.append(newLiComponent);
}

function openModal(type, onClickFunction, text, textArray, params, title, confirmBtnText, cancelBtnText) {
    const currentModalId = `${type}Modal`;
    const currentModal = document.getElementById(currentModalId);
    const currentModalText = document.getElementById(`${currentModalId}Text`);
    const currentModalCancelBtn = document.getElementById(`${currentModalId}CancelBtn`);
    const currentModalConfirmBtn = document.getElementById(`${currentModalId}ConfirmBtn`);
    const currentModalTitle = document.getElementById(`${currentModalId}Title`);

    const defaultTitle = (type.charAt(0).toUpperCase() + type.slice(1));

    
    // Set Title
    currentModalTitle.innerHTML = title || defaultTitle || '';

    // Set Text
    currentModalText.innerHTML = '';
    if (!!text) {
        appendTextToComponent(currentModalText, text);
    }
    if (!!textArray) {
        for (let value of textArray) {
            appendTextToComponent(currentModalText, value);
        }
    }

    // set function on 'OK' btn
    if (!!onClickFunction) {
        if (!!confirmBtnText) {
            currentModalConfirmBtn.innerText = confirmBtnText;
        }
        currentModalConfirmBtn.onclick = function() { onClickFunction(params); ModalOpenClose(`#${currentModalId}`); };
        // if (!!params) {
        //     currentModalConfirmBtn.onclick = function() { onClickFunction(params); ModalOpenClose(`#${currentModalId}`); };
        // } else {
        // }
        currentModalConfirmBtn.style.display = "";
    } else {
        currentModalConfirmBtn.onclick = "";
        currentModalConfirmBtn.style.display = "none";
    }
    // if (!!currentModalConfirmBtn) {
    // }

    if (!!cancelBtnText) {
        currentModalCancelBtn.innerText = cancelBtnText;
    }

    ModalOpen(`#${currentModalId}`);
}

function initializeScroll() {
    $(".modal.modal_dimmed .modal_content").scrollTop(0);
}