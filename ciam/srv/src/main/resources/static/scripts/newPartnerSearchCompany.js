// ag-grid object
// const isNewCompanyElement = document.querySelector("#isNewCompany");
// let newPartnerSearchCompanyResultGrid = null;
let newPartnerSearchCompanyResultGrid = null;

let newPartnerSearchCompanyTargetNameField = null;

function showCreateCompanyButton() {
  const createCompanyButton = document.querySelector("#createCompanyButton");
  createCompanyButton.className = "btn_m isActive";
}

function hideCreateCompanyButton() {
  const createCompanyButton = document.querySelector("#createCompanyButton");
  createCompanyButton.className = "btn_m isVisible";
}

// clear grid data
function initializeNewPartnerSearchCompanyGrid() {
  startIdx = 0;
  endIdx = newPartnerSearchCompanyMobileDisplaySize;

  // focus to company name filter
  setTimeout(function () {
    $("#newPartnerSearchCompanyModalnewPartnerSearchCompanyModalFilter_name").focus();
  }, 300);

  // for PC
  if (newPartnerSearchCompanyResultGrid) {
    newPartnerSearchCompanyResultGrid.destroy();
  }
}

// Open Company Search Popup
function executeNewPartnerSearchCompany(nameField) {
// function executeSearchCompany() {
  // initialize filters
  // newPartnerSearchCompanyModalResetSearchCompanyFilters("name");
//   switchNewPartnerSearchCompanyDisplay("selectCompanyBtn", false);
//   switchNewPartnerSearchCompanyDisplay("noDataResult", false);

  newPartnerSearchCompanyTargetNameField = nameField;
  ModalOpen("#newPartnerSearchCompanyModal");
  if ($("#newPartnerSearchCompanyModalFilter_name").val()) {
    newPartnerSearchCompanyModalSearchCompany();
  } else {
    initializeNewPartnerSearchCompanyGrid();
  }
}

// Close Company Search Popup
function closeNewPartnerSearchCompanyDialog(isSelect) {
  ModalOpenClose("#newPartnerSearchCompanyModal");
  if (!isSelect && !!newPartnerSearchCompanyTargetNameField) {
    newPartnerSearchCompanyTargetNameField.value = '';
    newPartnerSearchCompanyTargetNameField = null;
  }
  switchNewPartnerSearchCompanyDisplay("newPartnerSearchCompanyModalSelectCompanyBtn", false);
  newPartnerSearchCompanyModalResetSearchCompanyFilters("name");
}

// switch status 'Select Company' button & no data message for mobile
function switchNewPartnerSearchCompanyDisplay(objId, displayOn) {
  if (objId == "noDataResult") {
    switchNewPartnerSearchCompanyDisplay("searchCompanyResultMobile", !displayOn);
    if (displayOn) {
      switchNewPartnerSearchCompanyDisplay("mobileMoreBtn", !displayOn);
    }
  }
  if (displayOn) {
    $("#" + objId).show();
  } else {
    $("#" + objId).hide();
  }
}

function newPartnerSearchCompanyProcessStart() {
  bNewPartnerSearchCompanyEventProcessing = true;
}

function newPartnerSearchCompanyProcessEnd() {
  bNewPartnerSearchCompanyEventProcessing = false;
}

// Company Search Action
let bNewPartnerSearchCompanyEventProcessing = false; // flag to avoid nested event
function newPartnerSearchCompanyModalSearchCompany(event) {
  if (event && event.keyCode != 13) {
    return;
  }

  // check nested event
  if (bNewPartnerSearchCompanyEventProcessing) {
    return;
  }

  const businessLocationValue = $("#businessLocation").val();
  if (!businessLocationValue) {
    openModal('alert', '', "Please Select Business Location");
    return;
  }

  newPartnerSearchCompanyProcessStart();
//   hideCreateCompanyButton();
  // hide 'Select Company' button & no data message for mobile
  switchNewPartnerSearchCompanyDisplay("newPartnerSearchCompanyModalSelectCompanyBtn", false);
//   switchNewPartnerSearchCompanyDisplay("noDataResult", false);

  let params = {};
  let aInputObjs = $("[id*=newPartnerSearchCompanyModalFilter_]");
  for (let idx = 0; idx < aInputObjs.length; idx++) {
    if (aInputObjs[idx].value != "") {
      params[aInputObjs[idx].id.substring(35)] = aInputObjs[idx].value;
    }
  }

  if (params.name === "") {
    const message = "please enter a company name";
    openModal('alert', '', message);
    newPartnerSearchCompanyProcessEnd();
  } else {
    // clear grid data
    initializeNewPartnerSearchCompanyGrid();

    params.channel = channelElement.value;
    params.country = businessLocationValue;

    $.ajax({
      url: "/search/partnerCompany",
      type: "POST",
      data: JSON.stringify(params),
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      success: function (oResult) {
        renderNewPartnerSearchCompanyResultTable(oResult);
      },
      error: function (e) {
        console.error("Error .... : ", e);
        const message = "데이터 통신중 에러발생";
        openModal('alert', '', message);
        newPartnerSearchCompanyProcessEnd();
      },
    });
  }
}

// Generate Result Grid
let aNewPartnerSearchCompanyHeader,
    aNewPartnerSearchCompanyResultsData,
    newPartnerSearchCompanyMobileDisplaySize = 4;
let newPartnerSearchCompanyStartIdx = 0,
    newPartnerSearchCompanyEndIdx = newPartnerSearchCompanyMobileDisplaySize;

function renderNewPartnerSearchCompanyResultTable(oResult) {
  aNewPartnerSearchCompanyHeader = oResult.header ? oResult.header : [];
  aNewPartnerSearchCompanyResultsData = oResult.results ? oResult.results : [];

  /**
   * for PC
   */
      // Grid Options: Contains all of the Data Grid configurations
  const gridOptions = {
        rowData: aNewPartnerSearchCompanyResultsData,
        columnDefs: aNewPartnerSearchCompanyHeader,
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
      "#newPartnerSearchCompanyModalSearchCompanyResult"
  );

  // create grid
  newPartnerSearchCompanyResultGrid = agGrid.createGrid(
      searchCompanyResultElement,
      gridOptions
  );

  /**
   * for Mobile
   */
//   if (aNewPartnerSearchCompanyResultsData.length > newPartnerSearchCompanyMobileDisplaySize) {
//     switchNewPartnerSearchCompanyDisplay("mobileMoreBtn", true);
//   } else {
//     switchNewPartnerSearchCompanyDisplay("mobileMoreBtn", false);
//   }

//   addElementsForMobile();
//   showCreateCompanyButton();

  // show 'Select Company' button
  if (aNewPartnerSearchCompanyResultsData.length) {
    switchNewPartnerSearchCompanyDisplay("newPartnerSearchCompanyModalSelectCompanyBtn", true);
  } else {
    switchNewPartnerSearchCompanyDisplay("noDataResult", true);
  }
  newPartnerSearchCompanyProcessEnd();
}

// function addElementsForMobile(bAddIdx) {
//   if (bAddIdx) {
//     startIdx += newPartnerSearchCompanyMobileDisplaySize;
//     endIdx += newPartnerSearchCompanyMobileDisplaySize;
//   }

//   // hide '+More' button when all data displayed
//   if (endIdx >= aNewPartnerSearchCompanyResultsData.length) {
//     switchNewPartnerSearchCompanyDisplay("mobileMoreBtn", false);
//   }

//   for (let idx = startIdx; idx < aNewPartnerSearchCompanyResultsData.length && idx < endIdx; idx++) {
//     let sElementId = "radio_" + idx;
//     let mobileElement =
//         "<dt role='button'>" +
//         "<span class='radio'>" +
//         "<input type='radio' name='companyElement' id='" +
//         sElementId +
//         "'>" +
//         "<label for='" +
//         sElementId +
//         "'>" +
//         "<span><em>Company Name</em>" +
//         aNewPartnerSearchCompanyResultsData[idx].name +
//         "</span>" +
//         "</span>" +
//         "</dt>" +
//         "<dd><ul>";
//     for (let hIdx = 1; hIdx < aNewPartnerSearchCompanyHeader.length; hIdx++) {
//       mobileElement +=
//           "<li><p>" +
//           aNewPartnerSearchCompanyHeader[hIdx].headerName +
//           "</p>" +
//           (aNewPartnerSearchCompanyResultsData[idx][aNewPartnerSearchCompanyHeader[hIdx].field]
//               ? aNewPartnerSearchCompanyResultsData[idx][aNewPartnerSearchCompanyHeader[hIdx].field]
//               : "-") +
//           "</li>";
//     }

//     mobileElement += "</ul></dd>";

//     $("#searchCompanyResultMobile").append(mobileElement);
//   }

//   // set event handler for the elements
//   setAccodion();
// }

// Select Company (click 'Select Company' button)
function onNewPartnerSearchCompanyModalSelectCompany() {
//   const fieldsElement = document.querySelectorAll(
//       "#companyInfo input, #companyInfo select"
//   );
//   const isNewCompanyElement = document.querySelector("#isNewCompany");

//   fieldsElement.forEach((element) => {
//     resetFieldValue(element);
//     element.readOnly = "readonly";
//     if (element.id === "name") return element.removeAttribute("readonly");
//   });

//   isNewCompanyElement.value = false;

//   let aSelectedDataMobile = $("[name=companyElement]:radio:checked");
  if (newPartnerSearchCompanyResultGrid) {
    let aSelectedRows = newPartnerSearchCompanyResultGrid
        ? newPartnerSearchCompanyResultGrid.getSelectedRows()
        : [];
    if (aSelectedRows.length) {
      // set selected company information to parent page
      setCompanyInformation(aSelectedRows[0]);

      closeNewPartnerSearchCompanyDialog(true);
    }
    // else if (aSelectedDataMobile.length) {
    //   let targetIndex = aSelectedDataMobile[0].id.substring(6);
    //   // set selected company information to parent page
    //   setCompanyInformation(aNewPartnerSearchCompanyResultsData[targetIndex]);

    //   closeDialog();
    // }
    else {
      const message = "Please select a company";
      openModal('alert', '', message);
    //   setErrorModalMsg(message);
    //   ModalAlertOpen("#errorDialogBox");
    }
  }
}

// toggle for Company Search Advanced Filters Area
// function toggleAdvancedSearchFilters() {
//   if ($("#advancedSearchBtn").is(":visible")) {
//     $("#advancedSearchBtn").hide();
//     $("[id*=advancedFilters]").show();
//     // 팝업 창 크기 조정
//     $(".modal_dimmed  .modal_content").css({
//       "max-height": "700px",
//     });
//   } else {
//     $("#advancedSearchBtn").show();
//     $("[id*=advancedFilters]").hide();
//     // 팝업 창 크기 원래대로
//     $(".modal_dimmed  .modal_content").css({
//       "max-height": "",
//     });
//   }
// }

// reset all Company Search Filters
function newPartnerSearchCompanyModalResetSearchCompanyFilters(sInitObjId) {
  $("[id*=newPartnerSearchCompanyModalFilter_]").val("");
  if (sInitObjId) {
    $("#newPartnerSearchCompanyModalFilter_name").val($("#" + sInitObjId).val());
  }
}

// function handleClickCreateCompanyButton() {
//   const newPartnerSearchCompanyModalFilter_name = document.querySelector("#newPartnerSearchCompanyModalFilter_name");

//   if (!newPartnerSearchCompanyModalFilter_name.value.replaceAll(' ', '')) return;


//   companyValidationFields["companySearch"] = true;
//   companyValidationFields['vendorcode'] = true;
//   companyValidationFields['name'] = true;

//   if (vendorCodeField) {
//     vendorCodeField.required = false;
//     const pElement = vendorCodeField.parentNode.querySelector('p');
//     pElement.classList.remove('requ');
//   }

//   ModalOpenClose("#newPartnerSearchCompanyModal");
//   switchNewPartnerSearchCompanyDisplay("selectCompanyBtn", false);
//   switchNewPartnerSearchCompanyDisplay("noDataResult", false);
//   hideCreateCompanyButton();
//   isNewCompanyElement.value = "true";

//   const fieldsElement = document.querySelectorAll(
//       "#companyInfo input, #companyInfo select"
//   );
//   // console.log(fieldsElement);
//   fieldsElement.forEach((element) => {
//     if (element.id === "name") {

//       element.readOnly = true;
//       element.value = newPartnerSearchCompanyModalFilter_name.value;
//       return;
//     }
//     if (element.id !== "vendorcode") element.removeAttribute("readonly");
//     resetFieldValue(element);
//   });
// }