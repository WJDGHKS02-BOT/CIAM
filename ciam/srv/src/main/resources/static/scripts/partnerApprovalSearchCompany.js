// ag-grid object
let oSearchCompanyResultGrid = null;

// function showCreateCompanyButton() {
//     const createCompanyButton = document.querySelector("#createCompanyButton");
//     createCompanyButton.className = "btn_m isActive";
// }
//
// function hideCreateCompanyButton() {
//     const createCompanyButton = document.querySelector("#createCompanyButton");
//     createCompanyButton.className = "btn_m isVisible";
// }

// clear grid data
function initializeGrid() {
    startIdx = 0;
    endIdx = mobileDisplaySize;

    // focus to company name filter
    setTimeout(function () {
        $("#filter_name").focus();
    }, 300);

    // for PC
    if (oSearchCompanyResultGrid) {
        oSearchCompanyResultGrid.destroy();
    }

    // for Mobile
    $("#searchCompanyResultMobile").empty();
}

// Open Company Search Popup
function executeSearchCompany() {
    // initialize filters
    resetSearchCompanyFilters("name");
    switchDisplay("selectCompanyBtn", false);
    switchDisplay("noDataResult", false);

    const filter_name = document.querySelector("#filter_name");
    const filter_name_error = document.querySelector('#filterNameError');
    filter_name.classList.remove('inp_error');
    filter_name_error.classList.remove('txt_inp_error');
    filter_name_error.innerText = null;

    ModalOpen("#searchCompanyModal");
    if ($("#filter_name").val()) {
        doSearchCompany();
    } else {
        initializeGrid();
    }
}

// Close Company Search Popup
function closeDialog() {
    ModalOpenClose("#searchCompanyModal");
}

// switch status 'Select Company' button & no data message for mobile
function switchDisplay(objId, displayOn) {
    if (objId == "noDataResult") {
        switchDisplay("searchCompanyResultMobile", !displayOn);
        if (displayOn) {
            switchDisplay("mobileMoreBtn", !displayOn);
        }
    }
    if (displayOn) {
        $("#" + objId).show();
    } else {
        $("#" + objId).hide();
    }
}

function processStart() {
    bEventProcessing = true;
}

function processEnd() {
    bEventProcessing = false;
}

// Company Search Action
let bEventProcessing = false; // flag to avoid nested event
function doSearchCompany(event) {
    if (event && event.keyCode != 13) {
        return;
    }

    // check nested event
    if (bEventProcessing) {
        return;
    }

    processStart();
    // if (typeof registrationType === 'undefined' || registrationType !== 'conversion') {
    //     hideCreateCompanyButton();
    // }
    // hide 'Select Company' button & no data message for mobile
    switchDisplay("selectCompanyBtn", false);
    switchDisplay("noDataResult", false);

    let params = {};
    let aInputObjs = $("[id*=filter_]");
    for (let idx = 0; idx < aInputObjs.length; idx++) {
        if (aInputObjs[idx].value != "") {
            params[aInputObjs[idx].id.substring(7)] = aInputObjs[idx].value;
        }
    }

    var countryValue = $("#country").val();
    params.channel = channel;
    params.country = countryValue;

    if (params.name === "") {
        const message = "please enter a company name";
        setErrorModalMsg(null);
        setErrorModalMsg(message);
        ModalAlertOpen("#errorDialogBox");
        processEnd();
    } else {
        // clear grid data
        initializeGrid();
        $.ajax({
            url: "/search/partnerCompany",
            type: "POST",
            data: JSON.stringify(params),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (oResult) {
                renderResultTable(oResult);
            },
            error: function (e) {
                console.error("Error .... : ", e);
                const message = "데이터 통신중 에러발생";
                setErrorModalMsg(message);
                ModalAlertOpen("#errorDialogBox");
                processEnd();
            },
        });
    }
}

// Generate Result Grid
let aHeader,
    aResultsData,
    mobileDisplaySize = 4;
let startIdx = 0,
    endIdx = mobileDisplaySize;

function renderResultTable(oResult) {
    aHeader = oResult.header ? oResult.header : [];
    aResultsData = oResult.results ? oResult.results : [];
    /**
     * for PC
     */
        // Grid Options: Contains all of the Data Grid configurations
    const gridOptions = {
            rowData: aResultsData,
            columnDefs: aHeader,
            rowSelection: "single",
            pagination: true,
            paginationPageSize: 10,
            paginationPageSizeSelector: [10, 50, 100],
            localeText: {
                noRowsToShow: "No Match Found", // 'No Rows To Show' 메시지를 'No Match Found'로 변경
            },
        };

    // Your Javascript code to create the Data Grid
    const searchCompanyResultElement = document.querySelector(
        "#searchCompanyResult"
    );

    // create grid
    oSearchCompanyResultGrid = agGrid.createGrid(
        searchCompanyResultElement,
        gridOptions
    );

    /**
     * for Mobile
     */
    if (aResultsData.length > mobileDisplaySize) {
        switchDisplay("mobileMoreBtn", true);
    } else {
        switchDisplay("mobileMoreBtn", false);
    }

    addElementsForMobile()

    // if (typeof registrationType === 'undefined' || registrationType !== 'conversion') {
    //     showCreateCompanyButton();
    // }

    // show 'Select Company' button
    if (aResultsData.length) {
        switchDisplay("selectCompanyBtn", true);
    } else {
        switchDisplay("noDataResult", true);
    }
    processEnd();
}

function addElementsForMobile(bAddIdx) {
    if (bAddIdx) {
        startIdx += mobileDisplaySize;
        endIdx += mobileDisplaySize;
    }

    // hide '+More' button when all data displayed
    if (endIdx >= aResultsData.length) {
        switchDisplay("mobileMoreBtn", false);
    }

    for (let idx = startIdx; idx < aResultsData.length && idx < endIdx; idx++) {
        let sElementId = "radio_" + idx;
        let mobileElement =
            "<dt role='button'>" +
            "<span class='radio'>" +
            "<input type='radio' name='companyElement' id='" +
            sElementId +
            "'>" +
            "<label for='" +
            sElementId +
            "'>" +
            "<span><em>Company Name</em>" +
            aResultsData[idx].name +
            "</span>" +
            "</span>" +
            "</dt>" +
            "<dd><ul>";
        for (let hIdx = 1; hIdx < aHeader.length; hIdx++) {
            mobileElement +=
                "<li><p>" +
                aHeader[hIdx].headerName +
                "</p>" +
                (aResultsData[idx][aHeader[hIdx].field]
                    ? aResultsData[idx][aHeader[hIdx].field]
                    : "-") +
                "</li>";
        }

        mobileElement += "</ul></dd>";

        $("#searchCompanyResultMobile").append(mobileElement);
    }

    // set event handler for the elements
    setAccodion();
}

// Select Company (click 'Select Company' button)
function onSelectCompany() {
    const fieldsElement = document.querySelectorAll(
        "#companyInfo input, #companyInfo select"
    );

    fieldsElement.forEach((element) => {
        resetFieldValue(element);
        element.readOnly = "readonly";
        if (element.id === "name") return element.removeAttribute("readonly");
    });

    let aSelectedDataMobile = $("[name=companyElement]:radio:checked");
    if (oSearchCompanyResultGrid || aSelectedDataMobile.length) {
        let aSelectedRows = oSearchCompanyResultGrid
            ? oSearchCompanyResultGrid.getSelectedRows()
            : [];
        if (aSelectedRows.length) {
            // set selected company information to parent page
            if (aSelectedRows[0].validstatus === "Blocked" || aSelectedRows[0].validstatus === "Expired") {
                const message = "To select a company in Expired or Blocked status, it is necessary to change the company's status.";
                setErrorModalMsg(message);
                ModalAlertOpen("#errorDialogBox");
            } else {
                // set selected company information to parent page
                setCompanyInformation(aSelectedRows[0]);

                closeDialog();
            }
        } else if (aSelectedDataMobile.length) {
            let targetIndex = aSelectedDataMobile[0].id.substring(6);
            if (aResultsData[targetIndex].validstatus === "Blocked" || aResultsData[targetIndex].validstatus === "Expired") {
                const message =  "To select a company in Expired or Blocked status, it is necessary to change the company's status.";
                setErrorModalMsg(message);
                ModalAlertOpen("#errorDialogBox");
            } else {
                // set selected company information to parent page
                setCompanyInformation(aResultsData[targetIndex]);

                closeDialog();
            }
        } else {
            const message = "Please select a company";
            setErrorModalMsg(message);
            ModalAlertOpen("#errorDialogBox");
        }
    }
}

// toggle for Company Search Advanced Filters Area
function toggleAdvancedSearchFilters() {
    if ($("#advancedSearchBtn").is(":visible")) {
        $("#advancedSearchBtn").hide();
        $("[id*=advancedFilters]").show();
        // 팝업 창 크기 조정
        $(".modal_dimmed  .modal_content").css({
            "max-height": "700px",
        });
    } else {
        $("#advancedSearchBtn").show();
        $("[id*=advancedFilters]").hide();
        // 팝업 창 크기 원래대로
        $(".modal_dimmed  .modal_content").css({
            "max-height": "",
        });
    }
}

// reset all Company Search Filters
function resetSearchCompanyFilters(sInitObjId) {
    $("[id*=filter_]").val("");
    if (sInitObjId) {
        $("#filter_name").val($("#" + sInitObjId).val());
    }
}

// function handleClickCreateCompanyButton() {
//     const filter_name = document.querySelector("#filter_name");
//     const filter_name_error = document.querySelector('#filterNameError');
//
//     if (!filter_name.value.replaceAll(' ', '')) {
//         filter_name.classList.add('inp_error');
//         filter_name_error.classList.add('txt_inp_error');
//         filter_name_error.innerText = 'Input company name';
//         return;
//     }
//
//     companyValidationFields["companySearch"] = true;
//     companyValidationFields['vendorcode'] = true;
//     companyValidationFields['name'] = true;
//
//     if (vendorCodeField) {
//         vendorCodeField.required = false;
//         const pElement = vendorCodeField.parentNode.querySelector('p');
//         pElement.classList.remove('requ');
//     }
//
//     ModalOpenClose("#searchCompanyModal");
//     switchDisplay("selectCompanyBtn", false);
//     switchDisplay("noDataResult", false);
//     hideCreateCompanyButton();
//     isNewCompanyElement.value = "true";
//
//     const fieldsElement = document.querySelectorAll(
//         "#companyInfo input, #companyInfo select"
//     );
//     // console.log(fieldsElement);
//     fieldsElement.forEach((element) => {
//         if (element.id === "name") {
//
//             element.readOnly = true;
//             element.value = filter_name.value;
//             return;
//         }
//         if (element.id !== "vendorcode") element.removeAttribute("readonly");
//         resetFieldValue(element);
//     });
// }
