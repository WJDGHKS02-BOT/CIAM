let currentConsentGridElementId,
    currentConsentGridPaginationSizeElementId,
    currentConsentGridSearchElementId = '';

let currentConsentGridResultElement,
    currentConsentGrid,
    currentConsentGridData = null;

let currentDimEditor = null;

let currentDimGrid = {};

const defaultConsentGridOptions = {
    loading: true,  // Grid Data 로딩 액션 활성화 여부
    rowSelection: 'single', // 'single' or 'multiple'
    columnDefs: [],
    // rowMultiSelectWithClick: true,  // multiple 선택일 때 주석 해제
    pagination: true,
    paginationPageSize: 3,
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
function initializeConsentGrid(gridElementIds, detailConsents){
    // Intitialize
    currentConsentGridElementId,
    currentConsentGridPaginationSizeElementId,
    currentConsentGridSearchElementId = '';

    currentConsentGridResultElement,
    currentConsentGridData = null;

    if(!!currentConsentGrid){
        currentConsentGrid.destroy();
    }

    // Set Default Value
    currentConsentGridElementId = gridElementIds['gridElementId'];
    currentConsentGridPaginationSizeElementId = gridElementIds['gridPaginationSizeElementId'];
    currentConsentGridSearchElementId = gridElementIds['gridSearchElementId'];

    currentConsentGridResultElement = document.querySelector(`#${currentConsentGridElementId}`);
    currentConsentGrid = agGrid.createGrid(currentConsentGridResultElement, defaultConsentGridOptions);

    if (!!currentConsentGridPaginationSizeElementId) {
        setConsentGridPaginationSizeSelectOnChange(currentConsentGridPaginationSizeElementId);
    }

    consentDimPopupRenderData(detailConsents);
    // searchCompany(); // 테스트용
}

// 테이블 상세 설정
function renderConsentResultTable(oResult) {
    if (!oResult) {
        
    }

    let columnDefs = [
        
    ];

    // 테스트용
    columnDefs = oResult.header ? oResult.header : [];
    currentConsentGridData = oResult.results ? oResult.results : [];

    // setting new options
    currentConsentGrid.setGridOption("rowData", currentConsentGridData);         // rowData
    currentConsentGrid.setGridOption("columnDefs", columnDefs);           // columnDefs
    currentConsentGrid.sizeColumnsToFit();                                // reSize
    currentConsentGrid.setGridOption("loading", false);                   // loading - false

    if (!!currentConsentGridSearchElementId) {
        setConsentGridDataSearchOnInput(currentConsentGridSearchElementId);
    }
}

// pagination size selectbox 이벤트 설정
function setConsentGridPaginationSizeSelectOnChange(elementId) {
    const elementIdWithSharp = `#${elementId}`;

    if (!!currentConsentGrid) {
        $(document).on("change", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;
            currentConsentGrid.setGridOption("paginationPageSize", value);
        });
    }
}

// grid search filter Inputbox 이벤트 설정
function setConsentGridDataSearchOnInput(elementId) {
    if (!elementId) {
        return;
    }
    const elementIdWithSharp = `#${elementId}`;

    if (!!currentConsentGrid) {
        $(document).on("input", elementIdWithSharp, (event) => {
            const target = event.target;
            const value = target.value;

            currentConsentGrid.setGridOption("quickFilterText", value);
        });
    }
}

function renderConsentDimResultTable(grid, oResult) {
    if (!oResult) {
        return;
    } else {
        let columnDefs = [
            // 채우면 됨
        ];
    
        // 테스트용
        columnDefs = oResult.header ? oResult.header : [];
        let gridData = oResult.results ? oResult.results : [];
    
        // setting new options
        grid.setGridOption("rowData", gridData);         // rowData
        grid.setGridOption("columnDefs", columnDefs);           // columnDefs
    }
    grid.sizeColumnsToFit();                                // reSize
    grid.setGridOption("loading", false);                   // loading - false
}

function getCurrentVersion(data) {
    let maxNum = 0;
    if (data.length > 0) {
        for (let value of data) {
            const curVersion = value.version;
            if (curVersion > maxNum) {
                maxNum = curVersion;
            }
        }
    } else {
        maxNum = data.version;
    }
    return maxNum;
}

function renderConsentDimGridData(type, consentData, pageType) {
    /* Version setting - only use consent detail preview */
    const currentVersionElement = document.getElementById(`currentVersion_${type}`);
    const nextVersionElement = document.getElementById(`nextVersion_${type}`);

    if (!!currentDimGrid[type]) {
        currentDimGrid[type].setGridOption("loading", true);
    }

    let oTableData = '';

    // search 클릭 후 render 시에 동작
    if (!!consentData && consentData.length > 0) {
        oTableData = consentData;
    }

    if (!!currentVersionElement && !!nextVersionElement) {
        let maxNum = getCurrentVersion(consentData);
        currentVersionElement.value = maxNum;
        nextVersionElement.value = maxNum + 1;
    }
    /* Version setting End */

    /* Table data setting */
    const columnDefs = [
        {
            headerName: 'Consent Group',
            field: 'consentGroup', // consentGroup,
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
            hide: true,
        },
        {
            headerName: 'Channel',
            field: 'channelNm', // channel,
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
            hide: true,
        },
        {
            headerName: 'Consent Type',
            field: 'consentTypeNm', // consentType
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
        },
        {
            headerName: 'Location',
            field: 'locationNm', // location
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
        },
        {
            headerName: 'Subsidiary',
            field: 'subsidiary',
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
        },
        {
            headerName: 'Language',
            field: 'languageNm',
            unSortIcon: true,
            cellStyle: { 
                textAlign: "center"
            },
            tooltipValueGetter: (params) => {
                return params.value;
            },
        },
    ];

    // Table Header and Data Setting
    const gridData = {
        'header': columnDefs,   // header
        'results': consentData,  // data
    };
    renderConsentDimResultTable(currentDimGrid[type], gridData);
}

function initializeConsentDimGrid(type, consentData, pageType) {
    const currentDimGridId = `consentAgGrid_${type}`;
    const currentDImGridElement = document.querySelector(`#${currentDimGridId}`);
    currentDimGrid[type] = agGrid.createGrid(currentDImGridElement, defaultConsentGridOptions);


    renderConsentDimGridData(type, consentData, pageType);
}

function openConsentDim(title, content, type, saveBtnOnClick, publishingBtnOnClick, cancelBtnOnClick) {
    const consentDimId = `consentDimmed_${type}`;
    const consentTitleElement = document.getElementById(`${consentDimId}Title`);
    const consentContentElement = document.getElementById(`${consentDimId}Content`);
    const consentSaveBtnElement = document.getElementById(`${consentDimId}SaveBtn`);
    const consentPublishingBtnElement = document.getElementById(`${consentDimId}PublishingBtn`);
    const consentCancelBtnElement = document.getElementById(`${consentDimId}CancelBtn`);
    const consentCloseBtnElement = document.getElementById(`${consentDimId}CloseBtn`);

    consentTitleElement.innerText = title || '';
    if (type != 'modify' && type != 'publishing' && type != 'add') {
        consentContentElement.innerHTML = content || '';
    }

    // Save btn onclick
    if (!!saveBtnOnClick) {
        consentSaveBtnElement.onclick = function () {
            if (type == 'add') {
                const curLanguage = $("[name=curLanguage]").val();
                if (!curLanguage) {
                    openModal('alert', '', "Please select language");
                    return;
                }
            }
            saveBtnOnClick(type);
        }
    }

    // Publishing btn onclick
    if (!!publishingBtnOnClick) {
        consentPublishingBtnElement.onclick = function () {
            publishingBtnOnClick(type);
        }
    }

    // cancel btn onclick
    if (!!cancelBtnOnClick) {
        consentCancelBtnElement.onclick = () => {
            cancelBtnOnClick(type);
            ModalOpenClose(`#${consentDimId}`);
        }
        consentCloseBtnElement.onclick = () => {
            cancelBtnOnClick(type);
            ModalOpenClose(`#${consentDimId}`);
        }
    }

    ModalOpen(`#${consentDimId}`);
}

function openCreateNewConsentConditionPopup() {
    // Consent Groupd Selectbox 설정

    // Subsidiary Selectbox 설정
    // Channel Selectbox 설정
    // Type Selectbox 설정
    // Location Selectbox 설정
    console.log(`Current Popup File Name: 'createNewConsentConditionPopup.html'`);
    ModalOpen(`#createNewConsentCondition`);
}

function clickedPublishingBtn(type, callbackFunc, data, pageType) {
    if (type == 'add') {
        const curLanguage = $("[name=curLanguage]").val();
        if (!curLanguage) {
            openModal('alert', '', "Please select language");
            return;
        }
    }
    console.log('clickedPublic:', data);
    initializePublishingPopup();    // initialize publishing popup

    const curDimTitle = 'Consent Publishing';   // title
    let cancelBtnFunc = '';                     // cancelBtn onclick function
    
    let currentPublishingFunc = () => { callbackFunc(type, 'publishing', data) };

    if (pageType == 'history') {
        currentPublishingFunc = () => {
            callbackFunc(data);
        }
    }

    if (pageType == 'create') {
        currentPublishingFunc = () => {
            callbackFunc(type);
        }
    }
    // if (!!data) {
    // }

    if (!!type) {
        ModalOpenClose(`#consentDimmed_${type}`);
        if (type == 'modify' || type == 'add') {
            cancelBtnFunc = () => {
                callbackFunc(type, 'cancel');
            }
        }
    }
    openConsentDim(curDimTitle, '', 'publishing', '', currentPublishingFunc, cancelBtnFunc);
}

function setConsentType(typeList, role, selectboxElement) {
    let allowType = [
        "etc",
        "terms",
        "privacy",
        "b2b",
        "internal",
        "marketing",
    ];
    switch(role) {
        case 'Channel Admin':
            allowType = [
                "terms",
                "privacy"
            ];
            break;
        case 'CIAM Admin':
        default:
            break;
    }

    if (!!typeList && typeList.length > 0) {
        for (let typeData of typeList) {
            if (allowType.includes(typeData.id)) {
                let optionElement = document.createElement('option');
                optionElement.value = typeData.id;
                optionElement.text = typeData.nameEn;
                selectboxElement.appendChild(optionElement);
            } 
        }
    }
}