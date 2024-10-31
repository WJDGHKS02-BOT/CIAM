// let currentParamsData = '';

let approvalConfigurationGridElementIds = {},
    approvalConfigurationGridPaginationSizeElementIds = {},
    approvalConfigurationGridSearchElementIds = {};

let approvalConfigurationGridResultElements = {},
    approvalConfigurationGrids = {},
    approvalConfigurationGridDatas = {};

const approvalConfigurationDefaultGridOptions = {
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
    rowClassRules: {
        "green_row": (params) => {
            return params.data.isNew;
        },
        "blue_row": (params) => {
            return params.data.isUpdate;
        }
    },
}

// 초기 설정
function initializeApprovalConfigurationGrid(gridElementIds, modalType) {
    // Intitialize
    approvalConfigurationGridElementIds[modalType] = null;
    approvalConfigurationGridPaginationSizeElementIds[modalType] = null;
    approvalConfigurationGridSearchElementIds[modalType] = null;

    approvalConfigurationGridResultElements[modalType],
    approvalConfigurationGridDatas[modalType] = null;

    if(!!approvalConfigurationGrids[modalType]){
        approvalConfigurationGrids[modalType].destroy();
    }

    // Set Default Value
    approvalConfigurationGridElementIds[modalType] = gridElementIds['gridElementId'];
    approvalConfigurationGridPaginationSizeElementIds[modalType] = gridElementIds['gridPaginationSizeElementId'];
    approvalConfigurationGridSearchElementIds[modalType] = gridElementIds['gridSearchElementId'];

    approvalConfigurationGridResultElements[modalType] = document.querySelector(`#${approvalConfigurationGridElementIds[modalType]}`);
    approvalConfigurationGrids[modalType] = agGrid.createGrid(approvalConfigurationGridResultElements[modalType], approvalConfigurationDefaultGridOptions);

    if (!!approvalConfigurationGridPaginationSizeElementIds[modalType]) {
        setGridPaginationSizeSelectOnChange(approvalConfigurationGridPaginationSizeElementIds[modalType]);
    }
}

// 테이블 상세 설정
function renderApprovalConfigurationGridsResultTable(oResult, modalType) {
    let columnDefs = [
        // 채우면 됨
    ];

    // 테스트용
    columnDefs = oResult.header ? oResult.header : [];
    approvalConfigurationGridDatas[modalType] = oResult.results ? oResult.results : [];

    // setting new options
    approvalConfigurationGrids[modalType].setGridOption("rowData", approvalConfigurationGridDatas[modalType]);         // rowData
    approvalConfigurationGrids[modalType].setGridOption("columnDefs", columnDefs);           // columnDefs
    approvalConfigurationGrids[modalType].sizeColumnsToFit();                                // reSize
    approvalConfigurationGrids[modalType].setGridOption("loading", false);                   // loading - false

    if (!!approvalConfigurationGridSearchElementIds[modalType]) {
        setApprovalConfigurationGridDataSearchOnInput(approvalConfigurationGridSearchElementIds[modalType], modalType);
    }
}

// pagination size selectbox 이벤트 설정
function setApprovalConfigurationGridPaginationSizeSelectOnChange(elementId, modalType) {
    const elementIdWithSharp = `#${elementId}`;

    if (!!approvalConfigurationGrids[modalType]) {
        $(document).on("change", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;
            approvalConfigurationGrids[modalType].setGridOption("paginationPageSize", value);
        });
    }
}

// grid search filter Inputbox 이벤트 설정
function setApprovalConfigurationGridDataSearchOnInput(elementId, modalType) {
    if (!elementId) {
        return;
    }
    const elementIdWithSharp = `#${elementId}`;

    if (!!approvalConfigurationGrids[modalType]) {
        $(document).on("input", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;

            approvalConfigurationGrids[modalType].setGridOption("quickFilterText", value);
        });
    }
}

function renderApprovalConfigurationTableData(columnDefs, oResult, modalType) {
    const popupRuleLevel = document.getElementById(`modalRuleLevel_${modalType}`);
    const popupRequstType = document.getElementById(`modalRequestType_${modalType}`);
    // const popupExecutionCondition = document.getElementById(`modalExecutionCondition_${modalType}`);
    const popupApprover = document.getElementById(`modalApprover_${modalType}`);
    // const popupApprovalFormat = document.getElementById(`modalApprovalFormat_${modalType}`);
    const popupApprovalCondition = document.getElementById(`modalApprovalCondition_${modalType}`);
    let currentPopupId = !!currentSelectedRowData ? currentSelectedRowData.id : '';

    let oTableData = [];

    if(!!oResult) {
        oTableData = oResult;
    }

    if (modalType == 'modify') {
        // oResult에서 id(RuleId) 같은 것에 데이터 덮어쓰기 변경
        for (let data of oTableData) {
            if (data.id == currentPopupId) {
                data.ruleLevel = popupRuleLevel.options[popupRuleLevel.selectedIndex].text;
                data.requestTypeNm = popupRequstType.options[popupRequstType.selectedIndex].text;
                // data.executionConditionsNm = popupExecutionCondition.options[popupExecutionCondition.selectedIndex].text;
                data.approverNm = popupApprover.options[popupApprover.selectedIndex].text;
                data.approveFormatNm = approveFormatElement.options[approveFormatElement.selectedIndex].text;
                data.approveConditionsNm = popupApprovalCondition.options[popupApprovalCondition.selectedIndex].text;
                data.isUpdate = true;
            }
        }
    } else if (modalType == 'add') {
        let originTableLength = oResult.length;
        let newRowIndex = originTableLength - 1;
        // oResult에 추가
        let addPopupRuleCondition = {
            ruleLevel: popupRuleLevel.options[popupRuleLevel.selectedIndex].text,
            requestTypeNm: popupRequstType.options[popupRequstType.selectedIndex].text,
            // executionConditionsNm: popupExecutionCondition.options[popupExecutionCondition.selectedIndex].text,
            approverNm: popupApprover.options[popupApprover.selectedIndex].text,
            approveFormatNm: approveFormatElement.options[approveFormatElement.selectedIndex].text,
            // approveFormatNm: popupApprovalFormat.options[popupApprovalFormat.selectedIndex].text,
            approveConditionsNm: popupApprovalCondition.options[popupApprovalCondition.selectedIndex].text,
            isNew: true,
        };

        oResult.splice(newRowIndex, 0, addPopupRuleCondition);
    }
    
    const gridData = {
        'header': columnDefs,   // header
        'results': oTableData,  // data
    };

    renderApprovalConfigurationGridsResultTable(gridData, modalType);
}

function getApprovalConfigurationColumn(columnName, columnFieldName, unSortIcon, textAlign, customFieldName) {
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

    if (!!customFieldName) {
        let cell = () => {
            return customFieldName;
        }
        currentResultColumn.cellRenderer = cell;
    }

    return currentResultColumn;
}

function getApprovalConfigurationColumnDefs() {
    let columnDefs = [];

    columnDefs.push(getApprovalConfigurationColumn("Rule Level", "ruleLevel"));
    columnDefs.push(getApprovalConfigurationColumn("Request Type", "requestTypeNm"));
    // columnDefs.push(getApprovalConfigurationColumn("Execution Conditions", "executionConditionsNm")); 9/4 이후 제거
    columnDefs.push(getApprovalConfigurationColumn("Approver", "approverNm"));
    columnDefs.push(getApprovalConfigurationColumn("Approve Format", "approveFormatNm"));
    columnDefs.push(getApprovalConfigurationColumn("Approve Conditions", "approveConditionsNm"));
    columnDefs.push(getApprovalConfigurationColumn("Last Modified Date", "lastModifiedDate"));

    return columnDefs;
}

function setApprovalConfigurationGrid(modalType) {
    /* set loading - grid */

    let approvalConfigurationColumnDefs = getApprovalConfigurationColumnDefs();

    if (!!approvalConfigurationGrids[modalType]) {
        approvalConfigurationGrids[modalType].setGridOption('loading', true);
    }

    const approvalConfigurationPromise = new Promise((resolve, reject) => {
        $.ajax({
            url: "/approvalConfiguration/approvalConfigurationList",    // setting get approver list url
            type: "POST",
            data: JSON.stringify(currentConditions),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: (oResult) => {
                console.log(oResult);
                resolve(oResult);
            },
            error: (e) => {
                reject();
            }
        });
    });

    Promise.all([approvalConfigurationPromise]).then((data)=> {
        const result = data[0];

        /* table Rendering */
        renderApprovalConfigurationTableData(approvalConfigurationColumnDefs, result, modalType);
    });
}

function getCurrentExceptInitialFields(currentModalType) {
    let exceptInitialFields = '';
    if (currentModalType == 'modify') {
        exceptInitialFields = [
            `modalRequestType_${currentModalType}`,
            // `modalExecutionCondition_${currentModalType}`,
            `modalApprovalCondition_${currentModalType}`,
            `modalStatus_${currentModalType}`,
        ];
    } else if (currentModalType == 'add') {
        exceptInitialFields = [
            `modalRequestType_${currentModalType}`,
            `modalApprovalCondition_${currentModalType}`,
            `modalStatus_${currentModalType}`,
        ];
    }
    return exceptInitialFields;
}

function clickedApprovalConfigurationModalBtn(btnType, currentModalType) {
    let isClose = (btnType == 'close' ? true : false);
    switch(btnType) {
        case 'preview':
            initializeModalFields('preview', currentModalType);
            break;
        case 'cancel':
        case 'close':
            initializeModalFields('initial', currentModalType);
            
            break;
    }
    if (isClose) {
        setSelectboxOptions(currentModalType, '');  // 해당 selectbox 값 초기화
        ModalOpenClose(`#approvalConfigurationModal_${currentModalType}`);
    }
}

function setFieldReadonly(elementType, value, exceptInitialFields, currentModalType) {
    let fields = document.querySelector(`#modalFields_${currentModalType}`).querySelectorAll(elementType);
    let readonlyOptionName = '';
    if (elementType == 'input') {
        readonlyOptionName = 'readOnly';
    } else if (elementType == 'select') {
        readonlyOptionName = 'ariaReadOnly';
    }

    if (!!fields) {
        const newFields = Object.entries(fields).filter(([key, component]) => {
            return !(exceptInitialFields.includes(component.id));
        });
    
        for (let curField of newFields) {
            curField[1][readonlyOptionName] = value;
        }
    }
}

function setSelectboxOptions(currentModalType, rowData) {
    if (!!rowData) {
        $(`select[id='modalRuleLevel_${currentModalType}']`).val(rowData.ruleLevel);
        $(`select[id='modalRequestType_${currentModalType}']`).val(rowData.requestType);
        // $(`select[id='modalExecutionCondition_${currentModalType}']`).val(rowData.executionConditions);
        $(`select[id='modalApprover_${currentModalType}']`).val(rowData.approver);
        // $(`select[id='modalApprovalFormat_${currentModalType}']`).val(rowData.approveFormat);
        $(`select[id='modalApprovalCondition_${currentModalType}']`).val(rowData.approveConditions);
        $(`select[id='modalStatus_${currentModalType}']`).val(rowData.status);
    } else {
        document.getElementById(`modalRuleLevel_${currentModalType}`).selectedIndex = "0";
        document.getElementById(`modalRequestType_${currentModalType}`).selectedIndex = "0";
        // document.getElementById(`modalExecutionCondition_${currentModalType}`).selectedIndex = "0";
        document.getElementById(`modalApprover_${currentModalType}`).selectedIndex = "0";
        // document.getElementById(`modalApprovalFormat_${currentModalType}`).selectedIndex = "0";
        document.getElementById(`modalApprovalCondition_${currentModalType}`).selectedIndex = "0";
        document.getElementById(`modalStatus_${currentModalType}`).selectedIndex = "0";
    }
}

function initializeModalFields(initialType, currentModalType) {
    const approvalConfigurationModalPreviewGridElement = document.getElementById(`previewGrid_${currentModalType}`);
    const approvalConfigurationModalModifyBtnWrapWrapElement = document.getElementById(`modifyBtnWrap_${currentModalType}`);
    const approvalConfigurationModalPreviewBtnWrapElement = document.getElementById(`previewBtnWrap_${currentModalType}`);
    const approvalConfigurationModalAddBtnWrapWrapElement = document.getElementById(`addBtnWrap_${currentModalType}`);

    let currentModalElement = currentModalType == 'modify' ? approvalConfigurationModalModifyBtnWrapWrapElement : approvalConfigurationModalAddBtnWrapWrapElement ;

    let curModifyBtnDisplayType = '';
    let curPreviewBtnDisplayType = '';
    let curReadonlyType = '';

    let exceptInitialFields = getCurrentExceptInitialFields(currentModalType);

    if (initialType == 'initial') {
        curModifyBtnDisplayType = 'none';
        curReadonlyType = false;
    } else if (initialType == 'preview') {
        setApprovalConfigurationGrid(currentModalType);
        curPreviewBtnDisplayType = 'none';
        curReadonlyType = true;
    }

    // 8/4 에러 예외처리 (예외처리해도 다른 에러 발생)
    if(currentModalElement == null) { return; }
    currentModalElement.style.display = curModifyBtnDisplayType;
    approvalConfigurationModalPreviewGridElement.style.display = curModifyBtnDisplayType;
    approvalConfigurationModalPreviewBtnWrapElement.style.display = curPreviewBtnDisplayType;

    // setFieldReadonly('input', curReadonlyType, exceptInitialFields, currentModalType);
    setFieldReadonly('select', curReadonlyType, exceptInitialFields, currentModalType);
}

function approvalConfigurationModalUpdate(type) {
    let updateFormAction = '';
    let currentPopup = `#approvalConfigurationModal_${type}`;
    let paramsData = {};
    let modalType = type;

    const modalRuleLevelElement = document.getElementById(`modalRuleLevel_${type}`);
    const modalRequestTypeElement = document.getElementById(`modalRequestType_${type}`);
    // const modalExecutionConditionElement = document.getElementById(`modalExecutionCondition_${type}`);
    const modalApproverElement = document.getElementById(`modalApprover_${type}`);
    // const modalApprovalFormatElement = document.getElementById(`modalApprovalFormat_${type}`);
    const modalApprovalConditionElement = document.getElementById(`modalApprovalCondition_${type}`);

    switch(type) {
        case 'add':
            updateFormAction = 'insertApprovalRule';
            paramsData.channel = currentConditions.selectedChannel;
            paramsData.workflowCode = modalRequestTypeElement.value;
            paramsData.country = currentConditions.selectedLocation;
            paramsData.subsidiary = currentConditions.selectedSubsidiary;
            paramsData.division = currentConditions.selectedDivision;
            paramsData.ruleLevel = modalRuleLevelElement.value;
            // paramsData.executionCondition = modalExecutionConditionElement.value;
            // paramsData.approveFormat = modalApprovalFormatElement.value;
            paramsData.approveCondition = modalApprovalConditionElement.value;
            paramsData.role = modalApproverElement.value;
            break;
        case 'modify':
            updateFormAction = 'updateApprovalRule';
            paramsData.id = currentSelectedRowData.id;
            paramsData.ruleLevel = modalRuleLevelElement.value;
            paramsData.role = modalApproverElement.value;
            // paramsData.approveFormat = modalApprovalFormatElement.value;
            break;
        case 'delete':
            updateFormAction = 'deleteApprovalRule';
            paramsData.id = currentSelectedRowData.id;
            currentPopup = '';
            modalType = '';
            break;
        default:
            break;
    }

    const updateApprovalConfiguration = new Promise((resolve, reject) => {
        if ((updateFormAction == 'insertApprovalRule' || updateFormAction == 'updateApprovalRule') && paramsData.ruleLevel == '') {
            alert("Rule Level is Nothing");
            return;
        }

        $.ajax({
            url: `/approvalConfiguration/${updateFormAction}`,
            type: "POST",
            data: JSON.stringify(paramsData),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: (oResult) => {
                console.log(oResult);
                resolve(oResult);
            },
            error: (e) => {
                reject();
            }
        });
    });

    Promise.all([updateApprovalConfiguration]).then((data) => {
        const result = data[0];

        if (!!result) {
            if (!!currentPopup) {
                ModalOpenClose(currentPopup);   // close popup
            }
            clickedApprovalConfigurationSearchBtn();    // refer to approvalConfiguration.html
            openModal('success', '', `Success ${type} approval rule`);
        } else {
            openModal('error', '', "Failed to ${type} approval rule");
        }

        clickedApprovalConfigurationModalBtn('close', modalType);
    });
}