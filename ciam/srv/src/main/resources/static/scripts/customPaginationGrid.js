let currentGridElementId,
    currentGridPaginationSizeElementId,
    currentGridSearchElementId = '';

let currentGridResultElement,
    currentGrid,
    currentGridData = null;

const defaultGridOptions = {
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
    enableCellTextSelection: true,
    // copyHeadersToClipboard: true,
    // cellSelection: true,
    // defaultColDef: {
    //     flex: 1,
    //     minWidth: 100,
    // },
    // selection: { mode: "cell" },
    // defaultColDef: {
    //     editable: true,
    // },
    // readOnlyEdit: true,
}

// 초기 설정
function initializeGrid(gridElementIds){
    // Intitialize
    currentGridElementId,
    currentGridPaginationSizeElementId,
    currentGridSearchElementId = '';

    currentGridResultElement,
    currentGridData = null;

    if(!!currentGrid){
        currentGrid.destroy();
    }

    // Set Default Value
    currentGridElementId = gridElementIds['gridElementId'];
    currentGridPaginationSizeElementId = gridElementIds['gridPaginationSizeElementId'];
    currentGridSearchElementId = gridElementIds['gridSearchElementId'];

    currentGridResultElement = document.querySelector(`#${currentGridElementId}`);
    currentGrid = agGrid.createGrid(currentGridResultElement, defaultGridOptions);

    if (!!currentGridPaginationSizeElementId) {
        setGridPaginationSizeSelectOnChange(currentGridPaginationSizeElementId);
    }
}

// 테이블 상세 설정
function renderResultTable(oResult) {
    if (!oResult) {
        // oResult = [[${gridData}]] || '';
    }

    // approvalListGridData = oResult ? oResult.approvalListGridData : [];

    let columnDefs = [
        // 채우면 됨
    ];

    // 테스트용
    columnDefs = oResult.header ? oResult.header : [];
    currentGridData = oResult.results ? oResult.results : [];

    // setting new options
    currentGrid.setGridOption("rowData", currentGridData);         // rowData
    currentGrid.setGridOption("columnDefs", columnDefs);           // columnDefs
    currentGrid.sizeColumnsToFit();                                // reSize
    currentGrid.setGridOption("loading", false);                   // loading - false

    if (!!currentGridSearchElementId) {
        setGridDataSearchOnInput(currentGridSearchElementId);
    }
}

// pagination size selectbox 이벤트 설정
function setGridPaginationSizeSelectOnChange(elementId) {
    const elementIdWithSharp = `#${elementId}`;

    if (!!currentGrid) {
        $(document).on("change", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;
            currentGrid.setGridOption("paginationPageSize", value);
        });
    }
}

// grid search filter Inputbox 이벤트 설정
function setGridDataSearchOnInput(elementId) {
    if (!elementId) {
        return;
    }
    const elementIdWithSharp = `#${elementId}`;

    if (!!currentGrid) {
        $(document).on("input", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;

            currentGrid.setGridOption("quickFilterText", value);
        });
    }
}

function getSelectedRowData() {
    return currentGrid.getSelectedRows();
}