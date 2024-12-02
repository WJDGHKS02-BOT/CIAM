// ag-grid object
let oSearchRevisionNoticeResultGrid = null;

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

    // clearRevisionNoticeGrid();
}

function clearRevisionNoticeGrid(){
    if (!!currentGrid) {
        currentGrid.destroy();
    }
}

// clear grid data
function initializeRevisionNoticeGrid() {
    resetFilters();

    doSearchRevisionNotice();
}

function processStart() {
    bEventProcessing = true;
}

function processEnd() {
    bEventProcessing = false;
}

// Company Search Action
let bEventProcessing = false;  // flag to avoid nested event
function doSearchRevisionNotice(event) {
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

    $.ajax({
        url: "/mypage/revisionNoticeSearch",
        type: "POST",
        data: JSON.stringify(params),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (oResult) {
            renderRevisionNotice(oResult);
            processEnd();
        },
        error: function (e) {
            console.error("Error .... : ", e);
            processEnd();
        }
    });
}

function renderRevisionNotice(oResult) {
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


    aMenuTypes.forEach(function(menuType){
        let oOption = document.createElement("option");
        oOption.value = menuType.value;
        oOption.innerText = menuType.desc;
        oMenuType.appendChild(oOption);
    });
}
