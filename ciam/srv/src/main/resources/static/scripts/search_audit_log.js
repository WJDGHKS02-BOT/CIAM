// ag-grid object
let oSearchAuditLogResultGrid = null;

// reset filters
function resetFilters(){
  $('[id*=filter_]').val('');

  /**
   * Channel
   */
  $('#filter_channel').val(defaultChannel);

  /**
   * Date
   */
  let offset = 7;  // 7일 전
  let today = new Date();
  let startDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() - offset);
  $('#filter_created_at').daterangepicker({
      locale: {
          format: 'YYYY-MM-DD',
          separator: ' ~ '
      },
      startDate: startDate,
      maxDate: today,
      endDate: today
  });

  // clearAuditLogGrid();
}

function clearAuditLogGrid(){
  if (!!currentGrid) {
    currentGrid.destroy();
  }
}

// clear grid data
function initializeAuditLogGrid() {
  resetFilters();

  doSearchAuditLog();
}

function processStart() {
  bEventProcessing = true;
}

function processEnd() {
  bEventProcessing = false;
}

// Company Search Action
let bEventProcessing = false;  // flag to avoid nested event
function doSearchAuditLog(event) {
  if (!!currentGrid) {
    currentGrid.setGridOption("loading", true);
  }
  if (event && event.keyCode != 13) {
    return;
  }

  // check nested event
  if (bEventProcessing) {
    return;
  }

  processStart();

  let params = {};
  let aInputObjs = $('[id*=filter_]');
  for (let idx = 0; idx < aInputObjs.length; idx++) {
    if (aInputObjs[idx].value != '') {
      params[aInputObjs[idx].id.substring(7)] = aInputObjs[idx].value;
    }
  }

  // clear grid data
  // clearAuditLogGrid();

  $.ajax({
    url: "/search/auditLog",
    type: "POST",
    data: JSON.stringify(params),
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function (oResult) {
      renderAuditLog(oResult);
      // renderResultTable(oResult);
      processEnd();
    },
    error: function (e) {
      console.error("Error .... : ", e);
      // const message = "데이터 통신중 에러발생";
      // setErrorModalMsg(message);
      // ModalAlertOpen('#errorDialogBox');
      processEnd();
    }
  });

}

// Generate Result Grid
let aHeader, aResultsData, mobileDisplaySize = 4;
let startIdx = 0, endIdx = mobileDisplaySize;
class RequesterUidRenderer{
  oRequesterUid;

  // Optional: Params for rendering. The same params that are passed to the cellRenderer function.
  init(params) {
    this.oRequesterUid = document.createElement('a');
    this.oRequesterUid.href = "javascript:void(0)";
    this.oRequesterUid.innerText = params.value;
    this.oRequesterUid.setAttribute("style", "text-decoration: underline;");
    this.oRequesterUid.setAttribute("onclick", "popupDetail(this)");
  }

  // Required: Return the DOM element of the component, this is what the grid puts into the cell
  getGui() {
    return this.oRequesterUid;
  }

  // Required: Get the cell to refresh.
  refresh(params) {
    return false
  }
}

function popupDetail(obj){
  let rowIdx = obj.parentElement.parentElement.parentElement.parentElement.getAttribute("row-index");
  let oSelectedData = currentGrid.getRowNode(rowIdx).data;

  $('#details').val(oSelectedData.details);
  $('#reason').val(oSelectedData.reason);

  // show popup
  ModalOpen('#auditLogDetailPopup');
}

function renderAuditLog(oResult) {
  aHeader = oResult.header ? oResult.header : [];
  // cellRenderer replace
  aHeader.forEach(function(headerInfo){
    if(headerInfo.cellRenderer){
      headerInfo.cellRenderer = RequesterUidRenderer;
    }
  });

  aResultsData = oResult.results ? oResult.results : [];

  let newResult = {
    header: aHeader,
    results: aResultsData,
  };

  renderResultTable(newResult);
}

// function renderResultTable(oResult) {
//   aHeader = oResult.header ? oResult.header : [];
//   // cellRenderer replace
//   aHeader.forEach(function(headerInfo){
//     if(headerInfo.cellRenderer){
//       headerInfo.cellRenderer = RequesterUidRenderer;
//     }
//   });

//   aResultsData = oResult.results ? oResult.results : [];

//   // Grid Options: Contains all of the Data Grid configurations
//   const gridOptions = {
//         rowData: aResultsData,
//         columnDefs: aHeader,
//         rowSelection: 'single',
//         pagination: true,
//         paginationPageSize: 10,
//         paginationPageSizeSelector: [10, 50, 100],
//         localeText: {
//           noRowsToShow: 'No Match Found'  // 'No Rows To Show' 메시지를 'No Match Found'로 변경
//         }
//       };

//   // Your Javascript code to create the Data Grid
//   const searchAuditLogResultElement = document.querySelector('#searchAuditLogResult');

//   // create grid
//   oSearchAuditLogResultGrid = agGrid.createGrid(searchAuditLogResultElement, gridOptions);

//   processEnd();
// }

/**
 * Add Menu Type Options
 */
function setMenuType(obj){
  let menuVal = obj.value;
  let oMenuType = document.getElementById("filter_menu_type");
  let oMenuTypeOptions = oMenuType.getElementsByTagName("option");
  if(oMenuTypeOptions.length>1){
    let firstIdx = oMenuTypeOptions.length-1;
    for(let idx=firstIdx; idx>0; idx--){
      oMenuType.remove(idx);
    }
  }

  let aMenuTypes;
  switch(menuVal){
    case "Approval_Request":
      aMenuTypes = [
        { value : "Company_Domain", desc : "Company Domain" },
        { value : "Role_Management", desc : "Role Management" }
      ];
      break;

    case "Approval_List":
      aMenuTypes = [
        { value : "New Registration", desc : "New Registration" },
        { value : "Conversion Registration", desc : "Conversion Registration" },
        { value : "Invite Registration", desc : "Invite Registration" },
        { value : "AD Registration", desc : "AD Registration" },
        { value : "SSO Acess", desc : "SSO Acess" },
        { value : "Role Management", desc : "Role Management" },
        { value : "Company Domain", desc : "Company Domain" }
      ];
      break;

    default:
      aMenuTypes = [];
  }

  aMenuTypes.forEach(function(menuType){
    let oOption = document.createElement("option");
    oOption.value = menuType.value;
    oOption.innerText = menuType.desc;
    oMenuType.appendChild(oOption);
  });
}
