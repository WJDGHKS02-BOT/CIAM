// ag-grid object
let oSearchCompanyResultGrid = null;

// target name field
let targetNameField = null;

// clear grid data
function initializeGrid() {
    startIdx = 0;
    endIdx = mobileDisplaySize;

    // focus to company name filter
    setTimeout(function () {
        $('#filter_name').focus();
    }, 300);

    // for PC
    if (oSearchCompanyResultGrid) {
        oSearchCompanyResultGrid.destroy();
    }

    // for Mobile
    $('#searchCompanyResultMobile').empty();
}

// Open Company Search Popup
function executeSearchCompany() {
    // initialize filters
    resetSearchCompanyFilters('name');

    ModalOpen('#searchCompanyModal');
    if ($('#filter_name').val()) {
        doSearchCompany();
    } else {
        initializeGrid();
    }
}

// Open Company Search Popup with company name
function executeSearchCompanyWithName(nameField, isMyPage) {
    targetNameField = nameField;
    ModalOpen('#searchCompanyModal');
    if ($('#filter_name').val()) {
        doSearchCompany('', isMyPage);
    } else {
        initializeGrid();
    }
}

// Close Company Search Popup
function closeDialog(isSelect) {
    ModalOpenClose('#searchCompanyModal');
    if (!isSelect && !!targetNameField) {
        targetNameField.value = '';
        targetNameField = null;
    }
    switchDisplay("selectCompanyBtn", false);
    resetSearchCompanyFilters('name');
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
        $('#' + objId).show();
    } else {
        $('#' + objId).hide();
    }
}

function processStart() {
    bEventProcessing = true;
}

function processEnd() {
    bEventProcessing = false;
}

// Company Search Action
let bEventProcessing = false;  // flag to avoid nested event
function doSearchCompany(event, isMyPage) {
    if (event && event.keyCode != 13) {
        return;
    }

    // check nested event
    if (bEventProcessing) {
        return;
    }

    processStart();

    // hide 'Select Company' button & no data message for mobile
    switchDisplay("selectCompanyBtn", false);
    switchDisplay("noDataResult", false);

    let params = {};
    let aInputObjs = $('[id*=filter_]');
    for (let idx = 0; idx < aInputObjs.length; idx++) {
        if (aInputObjs[idx].value != '') {
            params[aInputObjs[idx].id.substring(7)] = aInputObjs[idx].value;
        }
    }
    params.country = country;

    if (Object.keys(params).length && params.name != '') {
        // clear grid data
        initializeGrid();

        console.log('ppp', params, isMyPage);

        $.ajax({
            url: "/search/company",
            type: "POST",
            data: JSON.stringify(params),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (oResult) {
                processEnd();
                renderResultTable(oResult);
            },
            error: function (e) {
                console.error("Error .... : ", e);
                const message = "데이터 통신중 에러발생";
                setErrorModalMsg(message);
                ModalAlertOpen('#errorDialogBox');
                processEnd();
            }
        });
    } else {
        const message = "please enter a company name";
        setErrorModalMsg(message);
        ModalAlertOpen('#errorDialogBox');
        processEnd();
    }
}

// Generate Result Grid
let aHeader, aResultsData, mobileDisplaySize = 4;
let startIdx = 0, endIdx = mobileDisplaySize;

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
            rowSelection: 'single',
            pagination: true,
            paginationPageSize: 10,
            paginationPageSizeSelector: [10, 50, 100],
            localeText: {
                noRowsToShow: 'No Match Found'  // 'No Rows To Show' 메시지를 'No Match Found'로 변경
            }
        };

    // Your Javascript code to create the Data Grid
    const searchCompanyResultElement = document.querySelector('#searchCompanyResult');

    // create grid
    oSearchCompanyResultGrid = agGrid.createGrid(searchCompanyResultElement, gridOptions);


    /**
     * for Mobile
     */
    if (aResultsData.length > mobileDisplaySize) {
        switchDisplay("mobileMoreBtn", true);
    } else {
        switchDisplay("mobileMoreBtn", false);
    }

    addElementsForMobile();

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
        let mobileElement = "<dt role='button'>"
            + "<span class='radio'>"
            + "<input type='radio' name='companyElement' id='" + sElementId + "'>"
            + "<label for='" + sElementId + "'>"
            + "<span><em>Company Name</em>" + (aResultsData[idx].name) + "</span>"
            + "</span>"
            + "</dt>"
            + "<dd><ul>";
        for (let hIdx = 1; hIdx < aHeader.length; hIdx++) {
            mobileElement += "<li><p>" + aHeader[hIdx].headerName + "</p>"
                + (aResultsData[idx][aHeader[hIdx].field] ? aResultsData[idx][aHeader[hIdx].field] : "-") + "</li>";
        }

        mobileElement += "</ul></dd>";

        $('#searchCompanyResultMobile').append(mobileElement);
    }

    // set event handler for the elements
    setAccodion();
}

// Select Company (click 'Select Company' button)
function onSelectCompany() {
    let aSelectedDataMobile = $('[name=companyElement]:radio:checked');
    if (oSearchCompanyResultGrid || aSelectedDataMobile.length) {
        let aSelectedRows = oSearchCompanyResultGrid ? oSearchCompanyResultGrid.getSelectedRows() : [];
        if (aSelectedRows.length) {
            // set selected company information to parent page
            setCompanyInformation(aSelectedRows[0]);

            closeDialog(true);
        } else if (aSelectedDataMobile.length) {
            let targetIndex = aSelectedDataMobile[0].id.substring(6);
            // set selected company information to parent page
            setCompanyInformation(aResultsData[targetIndex]);

            closeDialog(true);
        } else {
            const message = "Please select a company";
            setErrorModalMsg(message);
            ModalAlertOpen('#errorDialogBox');
        }
    }
}

// toggle for Company Search Advanced Filters Area
function toggleAdvancedSearchFilters() {
    if ($('#advancedSearchBtn').is(':visible')) {
        $('#advancedSearchBtn').hide();
        $('[id*=advancedFilters]').show();
        // 팝업 창 크기 조정
        $('.modal_dimmed  .modal_content').css({
            'max-height': '700px'
        });
    } else {
        $('#advancedSearchBtn').show();
        $('[id*=advancedFilters]').hide();
        // 팝업 창 크기 원래대로
        $('.modal_dimmed  .modal_content').css({
            'max-height': ''
        });
    }
}

// reset all Company Search Filters
function resetSearchCompanyFilters(sInitObjId) {
    $('[id*=filter_]').val('');
    if (sInitObjId) {
        $('#filter_name').val($('#' + sInitObjId).val());
    }
}
