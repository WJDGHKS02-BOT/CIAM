let currentParamsData = '';

let approverListGridElementId,
    approverListGridPaginationSizeElementId,
    approverListGridSearchElementId = '';

let approverListGridResultElement,
    approverListGrid,
    approverListGridData = null;

const approverListDefaultGridOptions = {
    loading: true,  // Grid Data 로딩 액션 활성화 여부
    rowSelection: 'single', // 'single' or 'multiple'
    columnDefs: [],
    // rowMultiSelectWithClick: true,  // multiple 선택일 때 주석 해제
    pagination: true,
    paginationPageSize: 10,
    suppressHorizontalScroll: true, // 수평 스크롤을 억제
    domLayout: 'autoHeight', // 자동 높이 조정
    cellClass: "cell-centered",
    localeText: {
        noRowsToShow: ""  // If no rowdata, this message will show
    },
    overlayLoadingTemplate: `<img style="width: 100px;" src="/theme/assets/image/common/loading.svg" alt="loading">`,    // Grid Data Loading Image
    tooltipShowDelay: 500,
}

// 초기 설정
function initializeApproverListGrid(gridElementIds){
    // Intitialize
    approverListGridElementId,
    approverListGridPaginationSizeElementId,
    approverListGridSearchElementId = '';

    approverListGridResultElement,
    approverListGridData = null;

    if(!!approverListGrid){
        approverListGrid.destroy();
    }

    // Set Default Value
    approverListGridElementId = gridElementIds['gridElementId'];
    approverListGridPaginationSizeElementId = gridElementIds['gridPaginationSizeElementId'];
    approverListGridSearchElementId = gridElementIds['gridSearchElementId'];

    approverListGridResultElement = document.querySelector(`#${approverListGridElementId}`);
    approverListGrid = agGrid.createGrid(approverListGridResultElement, approverListDefaultGridOptions);

    if (!!approverListGridPaginationSizeElementId) {
        setGridPaginationSizeSelectOnChange(approverListGridPaginationSizeElementId);
    }
}

// 테이블 상세 설정
function renderApproverListGridResultTable(oResult) {
    let columnDefs = [
        // 채우면 됨
    ];

    // 테스트용
    columnDefs = oResult.header ? oResult.header : [];
    approverListGridData = oResult.results ? oResult.results : [];

    // setting new options
    approverListGrid.setGridOption("rowData", approverListGridData);         // rowData
    approverListGrid.setGridOption("columnDefs", columnDefs);           // columnDefs
    approverListGrid.sizeColumnsToFit();                                // reSize
    approverListGrid.setGridOption("loading", false);                   // loading - false

    if (!!approverListGridSearchElementId) {
        setApproverListGridDataSearchOnInput(approverListGridSearchElementId);
    }
}

// pagination size selectbox 이벤트 설정
function setGridPaginationSizeSelectOnChange(elementId) {
    const elementIdWithSharp = `#${elementId}`;

    if (!!approverListGrid) {
        $(document).on("change", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;
            approverListGrid.setGridOption("paginationPageSize", value);
        });
    }
}

// grid search filter Inputbox 이벤트 설정
function setApproverListGridDataSearchOnInput(elementId) {
    if (!elementId) {
        return;
    }
    const elementIdWithSharp = `#${elementId}`;

    if (!!approverListGrid) {
        $(document).on("input", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;

            approverListGrid.setGridOption("quickFilterText", value);
        });
    }
}

function getApproverListGridSelectedRowData() {
    return approverListGrid.getSelectedRows();
}

function deleteApprover(role, paramsData, selectedRowData) {
    let params = {
        id: paramsData.id,
        type: paramsData.type,
        ruleMasterId: selectedRowData.ruleMasterId,
        ruleLevel: selectedRowData.ruleLevel,
        creatorId: paramsData.creatorId
    }

    $.ajax({
        url: "/approvalConfiguration/deleteApprovalAdmin",    // setting delete approver url
        type: "POST",
        data: JSON.stringify(params),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: (oResult) => {
            // 테이블 다시 로딩
            console.log('oResult: ', oResult);
            openModal('success', '', 'Success Delete');
            setApproverListGrid(role, selectedRowData);
        },
        error: (e) => {
            console.error("Error: ", e);
            openModal('error', '', 'Failed to Delete');
            setApproverListGrid(role, selectedRowData);
        }
    });
}

function getApproverListButton(btnType, paramsData, selectedRowData, role) {
    let newButton = document.createElement('button');
    let buttonText = '';
    let buttonOnclick = '';
    let buttonStyleList = [];
    let buttonType = 'button';

    switch(btnType) {
        case 'view':
            break;
        case 'modify':
            break;
        case 'delete':
            buttonText = 'Delete';
            buttonStyleList.push("btn_table_s", "red");
            let deleteCallbackFunc = () => {
                deleteApprover(role, paramsData, selectedRowData);
            }
            buttonOnclick = () => {
                openModal('alert', deleteCallbackFunc, "Really delete this approver?");
            }
            break;
        default:
            break;
    }

    newButton.type = buttonType;
    newButton.innerText = buttonText;

    for (let btnStyle of buttonStyleList) {
        newButton.classList.add(btnStyle);
    }

    if (!!buttonOnclick) {
        newButton.onclick = buttonOnclick;
    }

    return newButton;
}

function getApproverListColumn(columnName, columnFieldName, btnType, selectedRowData, role, unSortIcon, textAlign, customFieldName) {
    let currentResultColumn = {
        headerName: columnName,
        unSortIcon: unSortIcon || 'true',
        cellStyle: {
            textAlign: textAlign || "center",
        },
        tooltipValueGetter: (params) => {
            return params.value;
        },
    };

    if (!!columnFieldName) {
        currentResultColumn.field = columnFieldName;
    }

    if (!!btnType) {
        let cell = (params) => {
            const paramsData = params.data;
            return getApproverListButton(btnType, paramsData, selectedRowData, role);
        }
        currentResultColumn.cellRenderer = cell;
    }

    if (!!customFieldName) {
        let cell = () => {
            return customFieldName;
        }
        currentResultColumn.cellRenderer = cell;
    }

    return currentResultColumn;
}

function getApproverListColumnDefs(role, params) {
    let columnDefs = [];

    columnDefs.push(getApproverListColumn("Login ID", "email"));
    columnDefs.push(getApproverListColumn("Role", "typeNm"));
    columnDefs.push(getApproverListColumn("Company Name", "companyName"));
    columnDefs.push(getApproverListColumn("Last Access Time", "lastAccessTime"));

    if (role == 'CIAM Admin') {
        columnDefs.push(getApproverListColumn("Creation Date", "createdDate"));
        columnDefs.push(getApproverListColumn("Creator ID", "creatorId"));
        columnDefs.push(getApproverListColumn("Delete", '', 'delete', params, role));
    }

    return columnDefs;
}

function renderApproverListTableData(columnDefs, oResult) {
    let oTableData = [];

    if(!!oResult) {
        oTableData = oResult;
    }
    
    const gridData = {
        'header': columnDefs,   // header
        'results': oTableData,  // data
    };

    renderApproverListGridResultTable(gridData);
}

function setApproverListGrid(role, params) {
    /* set loading - grid */
    console.log(role);
    // initializeApproverListModal(role);
    let approverListColumnDefs = getApproverListColumnDefs(role, params);
    currentParamsData = params;

    if (!!approverListGrid) {
        approverListGrid.setGridOption('loading', true);
    }

    const approverListPromise = new Promise((resolve, reject) => {
        $.ajax({
            url: "/approvalConfiguration/selectApprovalAdminList",    // setting get approver list url
            type: "POST",
            data: JSON.stringify(params),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: (oResult) => {
                resolve(oResult);
            },
            error: (e) => {
                reject();
            }
        });
    });

    Promise.all([approverListPromise]).then((data)=> {
        const result = data[0];

        /* table Rendering */
        renderApproverListTableData(approverListColumnDefs, result);
    });
}

// function initializeApproverListModal(role) {
//     const approverListCancelBtnDiv = document.getElementById('approverListCancelBtnDiv');
//     const approverListAddBtnDiv = document.getElementById('approverListAddBtnDiv');

//     if (role == 'CIAM Admin') {
//         approverListCancelBtnDiv.style.display = 'none';
//         approverListAddBtnDiv.style.display = '';
//     } else {
//         approverListCancelBtnDiv.style.display = '';
//         approverListAddBtnDiv.style.display = 'none';
//     }
// }


function closeApproverListModal(modalId) {
    const closeApproverListGridElement = document.getElementById(approverListGridSearchElementId);
    const closeApproverListGridPaginationSizeElement = document.getElementById(approverListGridPaginationSizeElementId);
    if (!!closeApproverListGridElement) {
        closeApproverListGridElement.value = '';
        closeApproverListGridElement.dispatchEvent(new Event('input', {bubbles: true}));
    }
    if (!!closeApproverListGridPaginationSizeElement) {
        closeApproverListGridPaginationSizeElement.selectedIndex = "0";
        closeApproverListGridPaginationSizeElement.dispatchEvent(new Event('change', {bubbles: true}));
    }

    ModalOpenClose(modalId);
}