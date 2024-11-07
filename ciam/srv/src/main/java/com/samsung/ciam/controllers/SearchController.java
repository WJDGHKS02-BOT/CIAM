package com.samsung.ciam.controllers;

import java.util.*;

import com.samsung.ciam.common.cpi.enums.CpiRequestFieldMapping;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.samsung.ciam.utils.StringUtil;
import com.samsung.ciam.services.SearchService;
import com.samsung.ciam.models.BtpAccounts;

/**
 * 1. 파일명   : SearchController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : NERP,CMDM 회사 관련 검색 기능을 제공하는 컨트롤러 (VENDOR,CUSTOMER 유형의 채널 지원)
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private CpiApiService cpiApiService;

    /*
     * 1. 메소드명: searchCompany
     * 2. 클래스명: SearchController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 정보를 검색하고 결과 및 헤더 정보를 반환
     * 2. 사용법
     *    검색 조건을 요청 바디에 포함하여 호출
     * 3. 예시 데이터
     *    - Input: 나라 정보와 검색 파라미터 맵
     *    - Output: 검색된 회사 목록 및 헤더 정보
     * </PRE>
     * @param allParams 요청 파라미터 맵
     * @param session 사용자 세션
     * @return 회사 검색 결과 및 헤더 정보
     */
    @RequestMapping("/company")
    public Map<String, Object> searchCompany(@RequestBody Map<String, String> allParams,HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 국가 정보가 비어있는 경우 세션에서 기본 나라 정보를 설정 -> 마이페이지 Approval Request에서는 국가정보가 들어옴
        if (allParams.get("country") == null || "".equals(allParams.get("country"))) {
            allParams.put("country",(String) session.getAttribute("secCountry"));
        }

        // 회사 검색 결과를 가져오기
        List<BtpAccounts> searchCompany = searchService.searchCompany(allParams);
        result.put("results", searchCompany);

        // 헤더 정의 키 설정
        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon"};

        Object[][] columnList = searchService.getColumnList();
        List<Map<String,Object>> headerInfo = new ArrayList<>();

        // 헤더 정보 가져오기
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }

        // 결과에 헤더 정보 추가
        result.put("header", headerInfo);

        return result;
    }

    /*
     * 1. 메소드명: ssoAccessListSerach
     * 2. 클래스명: SearchController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    SSO 접근 목록을 검색하고 결과를 반환
     * 2. 사용법
     *    검색 조건을 요청 바디에 포함하여 호출
     * 3. 예시 데이터
     *    - Input: 검색 파라미터 맵
     *    - Output: SSO 접근 목록 검색 결과
     * </PRE>
     * @param allParams 요청 파라미터 맵
     * @param session 사용자 세션
     * @param model 모델 객체
     * @return SSO 접근 목록 검색 결과
     */
    @RequestMapping("/ssoAccessListSerach")
    public Map<String, Object> ssoAccessListSerach(@RequestBody Map<String, String> allParams, HttpSession session, Model model) {
        Map<String, Object> result = new HashMap<>();

        // SSO 채널 목록 조회
        result = searchService.searchSsoAccess(allParams,session);

        // 결과에서 추가된 채널 및 삭제된 채널 리스트 추출
        List<String> addChannels = (List<String>) result.get("addChannels");
        List<String> deleteChannels = (List<String>) result.get("deleteChannels");

        // 모델에 추가 및 삭제된 채널 리스트 추가
        model.addAttribute("addChannels", addChannels);
        model.addAttribute("deleteChannels", deleteChannels);

        return result;
    }

    /*
     * 1. 메소드명: partnerCompany
     * 2. 클래스명: SearchController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    파트너(CUSTOMER) -> CMDM 회사 정보를 검색하여 CPI로부터 데이터를 가져옴
     * 2. 사용법
     *    회사 ID 또는 유사 검색 파라미터를 포함하여 호출
     * 3. 예시 데이터
     *    - Input: 검색 파라미터 맵
     *    - Output: 파트너 회사 정보 및 헤더 정보
     * </PRE>
     * @param allParams 요청 파라미터 맵
     * @param session 사용자 세션
     * @return 파트너 회사 정보 및 헤더 정보
     */
    @RequestMapping("/partnerCompany")
    public Map<String, Object> partnerCompany(@RequestBody Map<String, Object> allParams,HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> cpiResut = new ArrayList<>();

        // accountId가 존재하는 경우 CMDM 회사 검색
        if(!StringUtil.isEmpty(allParams.get("accountId"))){
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");
            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("acctid",allParams.get("accountId"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));
            cpiResut = cpiApiService.accountSearch(dataMap, url, responseFieldMapping,session);
        } else {
            // 유사검색으로 CMDM 회사 유사검색
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.findSimilarUrl");
            CpiRequestFieldMapping requestFieldMapping = CpiRequestFieldMapping.fromString((String) allParams.get("channel"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));
            cpiResut = cpiApiService.findSimilar(allParams, url, requestFieldMapping ,responseFieldMapping ,session);
        }

        result.put("results", cpiResut);

        // 헤더 정의 키 설정
        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon"};

        // 헤더 정보 가져오기
        Object[][] columnList = searchService.getPartnerColumnList();
        List<Map<String,Object>> headerInfo = new ArrayList<>();

        // 컬럼 정보를 헤더에 추가
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }

        // 결과에 헤더 정보 추가
        result.put("header", headerInfo);

        return result;
    }

    /*
     * 1. 메소드명: searchAuditLog
     * 2. 클래스명: SearchController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Audit 로그 목록을 검색하고 결과 및 헤더 정보를 반환
     * 2. 사용법
     *    검색 조건을 요청 바디에 포함하여 호출
     * 3. 예시 데이터
     *    - Input: 감사 로그 검색 조건
     *    - Output: 감사 로그 검색 결과 및 헤더 정보
     * </PRE>
     * @param allParams 요청 파라미터 맵
     * @return 감사 로그 검색 결과 및 헤더 정보
     */
    @RequestMapping("/auditLog")
    public Map<String, Object> searchAuditLog(@RequestBody Map<String, String> allParams) {
        Map<String, Object> result = new HashMap<>();

        // Audit 로그 헤더 정보 정의
        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon", "cellRenderer" };
        Object[][] columnList = {
            {"requester_uid", "Requester UID", null, true, true},
            {"email", "Login ID", null, true, null},
            {"channel", "Channel", null, true, null},
            {"type", "Menu", null, true, null},
            {"menu_type", "Menu Type", null, true, null},
            {"action", "Action", null, true, null},
            {"created_at", "Date", null, true, null}
        };

        // 커스텀 컬럼 리스트 설정
        searchService.setCustomColumnDefList(columnList);

        // Audit 로그 검색 수행 및 결과 설정
        List<Map<String,Object>> searchAuditLogList = searchService.searchAuditLogList(allParams);
        result.put("results", searchAuditLogList);

        // 헤더 정보 매핑 및 추가
        List<Map<String,Object>> headerInfo = new ArrayList<>();
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }
        result.put("header", headerInfo);

        return result;
    }

    /**
     * 1. 메소드명: checkCompanyDuplicate
     * 2. 클래스명: SearchController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 중복 여부를 확인하는 메소드. 회사 이름, 사업자 번호, DUNS 번호 등 필수 정보를 기반으로
     *    회사가 중복되는지 확인하고 그 결과를 반환한다.
     * 2. 사용법
     *    POST 요청으로 회사 정보를 전달하여 중복 여부를 확인
     * 3. 예시 데이터
     *    - Input: 사업자 번호, 회사 이름, 국가 등
     *    - Output: 중복 상태(중복/오류/중복 아님)에 따른 메시지와 상태 코드
     * </PRE>
     * @param requestParams 회사 중복 여부 확인을 위한 요청 파라미터 맵
     * @param session       사용자 세션 객체
     * @return ResponseEntity<Map<String, Object>> 중복 여부 결과가 포함된 응답 엔터티
     */
    @ResponseBody
    @PostMapping("/checkCompanyDuplicate")
    public ResponseEntity<Map<String, Object>> checkCompanyDuplicate(@RequestBody Map<String, String> requestParams,
                                                                     HttpSession session) {
        // 중복 체크를 위한 요청 파라미터 생성
        Map<String, Object> companyDupCheckReq = new HashMap<>();

        // 사업자 번호, DUNS 번호, 회사 이름 등 필터 설정
        companyDupCheckReq.put("filter_bizregno1", requestParams.getOrDefault("bizregno1", ""));
        companyDupCheckReq.put("filter_dunsno", requestParams.getOrDefault("dunsno", ""));
        companyDupCheckReq.put("name", requestParams.getOrDefault("name", ""));
        companyDupCheckReq.put("channel", requestParams.getOrDefault("channel", ""));
        companyDupCheckReq.put("isNewCompany", requestParams.getOrDefault("isNewCompany", ""));
        companyDupCheckReq.put("country", requestParams.getOrDefault("country", ""));

        // 중복 체크 로직 수행 -> CPI 서비스 호출
        Map<String, Object> response = cpiApiService.partnerCompanyDuplicateCheck(companyDupCheckReq, session);

        // 중복 상태에 따른 응답 반환
        if ("duplicate".equals(response.get("result"))) {
            // 중복이 발견된 경우, OK 상태로 중복 메시지 반환
            return ResponseEntity.ok(response);
        } else if ("error".equals(response.get("result"))) {
            // 오류가 발생한 경우, 서버 에러 상태로 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            // 중복이 없을 경우 OK 상태로 반환
            return ResponseEntity.ok(response);
        }
    }


}
