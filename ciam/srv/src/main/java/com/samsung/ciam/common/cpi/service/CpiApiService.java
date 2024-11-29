package com.samsung.ciam.common.cpi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.ciam.common.cpi.enums.CpiRequestFieldMapping;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1. FileName	: CpiApiService.java
 * 2. Package	: com.samsung.ciam.common.cpi.service
 * 3. Comments	: CPI를 통한 CMDM 회사 정보를 조회하고, 응답 데이터를 변환 및 매핑하여 제공하는 서비스 클래스
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@Slf4j
@Service
public class CpiApiService {

    @Autowired
    private RestTemplate restTemplate; // HTTP 요청을 보내기 위한 RestTemplate 객체

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    /*
     * 1. 메소드명: accountSearch
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CMDM 회사 계정을 CPI API를 통해 조회하고, 응답 데이터를 매핑하여 반환.
     * 2. 사용법
     *    파라미터로 회사 ID인 "acctid"를 받아, 회사 정보를 조회함.
     * 3. 예시 데이터
     *    - Input: paramArray = { "acctid": "A020147124" }
     *    - Output: 회사 정보 리스트, 예) [{ "name": "Samsung Electronics", "country": "KR", "acctid": "A020147124" }]
     * </PRE>
     *
     * @param paramArray 검색 파라미터를 포함한 맵
     * @param url API 엔드포인트 URL
     * @param responseFieldMapping 응답 필드 매핑 객체
     * @param session 현재 HTTP 세션
     * @return 회사 정보 리스트
     */
    public List<Map<String, Object>> accountSearch(Map<String, Object> paramArray, String url, CpiResponseFieldMapping responseFieldMapping, HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 파라미터를 JSON 문자열로 변환
            String jsonData = objectMapper.writeValueAsString(paramArray);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 기본 인증 헤더 추가
            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            // 요청 본문과 헤더를 포함한 HttpEntity 객체 생성
            HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

            // CPI API에 POST 요청 보내고 응답 받음
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("api response: {}", responseEntity.getBody()); // 응답 내용 로깅

            // 응답 JSON을 Map 형식으로 변환
            Map<String, Object> originalResponse = objectMapper.readValue(responseEntity.getBody(), Map.class);

            // 응답 데이터를 매핑하여 반환
            return mapResponseFields(originalResponse, responseFieldMapping.getFieldMap(),"account");
        } catch (Exception e) {
            log.error("Error sending api", e);
            throw new RuntimeException("Error sending api", e);
        }
    }

    /*
     * 1. 메소드명: mapResponseFields
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CPI 응답 데이터의 각 필드를 매핑하여 변환된 회사 정보 리스트를 반환
     * 2. 사용법
     *    CPI 응답 데이터를 전달하여 매핑된 필드를 적용한 회사 정보 반환.
     * 3. 예시 데이터
     *    - Input: originalResponse = { "name": "Samsung Electronics", "vatno": "1234567890" }, fieldMap = { "name": "orgName" }
     *    - Output: [{ "name": "Samsung Electronics", "vatno": "1234567890" }]
     * </PRE>
     *
     * @param originalResponse CPI 원본 응답 데이터
     * @param fieldMap 매핑할 필드의 맵
     * @param dataSource 데이터 출처 필드 ("account", "duplist" 등)
     * @return 매핑된 응답 데이터 리스트
     */
    public List<Map<String, Object>> mapResponseFields(Map<String, Object> originalResponse, Map<String, String> fieldMap,String dataSource) {
        List<Map<String, Object>> mappedResponseList = new ArrayList<>();
        Object accountDataObject = originalResponse.get(dataSource);

        // 데이터가 List 형식일 경우, 각 Map을 매핑하여 추가
        if (accountDataObject instanceof List) {
            List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;
            for (Map<String, Object> accountData : accountDataList) {
                mappedResponseList.add(mapSingleAccount(accountData, fieldMap));
            }
        } else if (accountDataObject instanceof Map) { // 단일 Map 형식일 경우
            // account 필드가 단일 Map인 경우
            Map<String, Object> accountData = (Map<String, Object>) accountDataObject;
            mappedResponseList.add(mapSingleAccount(accountData, fieldMap));
        }

        return mappedResponseList;
    }

    /*
     * 1. 메소드명: mapSingleAccount
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    개별 계정 데이터를 필드 매핑하여 변환된 맵을 반환.
     *    CPI 응답에서 제공된 필드를 내부 시스템 필드 이름으로 변환함.
     * 2. 사용법
     *    CPI 응답 데이터의 개별 계정 데이터를 매핑할 필드 정보와 함께 전달하여 변환된 계정 데이터 반환.
     * 3. 예시 데이터
     *    - Input: accountData = { "name": "Samsung", "vatno": "123456" }, fieldMap = { "name": "orgName", "vatno": "taxId" }
     *    - Output: { "orgName": "Samsung", "taxId": "123456" }
     * </PRE>
     *
     * @param accountData 응답으로부터 매핑할 개별 계정 데이터
     * @param fieldMap 매핑할 필드 이름의 맵
     * @return 매핑된 계정 데이터
     */
    public Map<String, Object> mapSingleAccount(Map<String, Object> accountData, Map<String, String> fieldMap) {
        Map<String, Object> mappedResponse = new HashMap<>();
        // 응답 데이터의 필드를 매핑하여 결과 맵에 저장
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String targetField = entry.getKey();
            String sourceField = entry.getValue();
            mappedResponse.put(targetField, accountData.get(sourceField)); // 필드 매핑
        }
        return mappedResponse;
    }

    /*
     * 1. 메소드명: findSimilar
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CPI API를 통해 유사 회사 목록을 조회하고, duplist와 dnblist를 추출하여 반환.
     * 2. 사용법
     *    회사 정보를 포함한 파라미터 맵과 요청/응답 필드 매핑 객체를 전달하여 CPI의 유사 회사 리스트를 얻음.
     * 3. 예시 데이터
     *    - Input: paramArray = { "name": "Samsung" }, requestFieldMapping, responseFieldMapping
     *    - Output: [{ "companyName": "Samsung Electronics", "country": "KR" }]
     * </PRE>
     *
     * @param paramArray 검색 파라미터 맵
     * @param url API 엔드포인트 URL
     * @param requestFieldMapping 요청 필드 매핑 객체
     * @param responseFieldMapping 응답 필드 매핑 객체
     * @param sessions HTTP 세션 정보
     * @return 유사 회사 정보 리스트
     */
    public List<Map<String, Object>> findSimilar(Map<String, Object> paramArray, String url, CpiRequestFieldMapping requestFieldMapping,CpiResponseFieldMapping responseFieldMapping,HttpSession sessions) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> fieldMap = requestFieldMapping.getFieldMap();

            // 요청 파라미터 매핑
            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                String requestKey = entry.getKey();
                String apiKey = entry.getValue();
                requestBody.put(apiKey, paramArray.getOrDefault(requestKey, ""));
            }

            // 공통 필드 설정 및 세션 정보 확인하여 추가
            requestBody.put("name_gl", paramArray.getOrDefault("name", "")); // Assume "filter_Name" is used for both name_lo and name_gl
            requestBody.put("showcmdmlist", true);
            requestBody.put("getmaxrank_record", false); // Fixed value field
            // convertYn 세션 값 확인
            if (sessions.getAttribute("convertYn") != null && "Y".equals(sessions.getAttribute("convertYn").toString())) {
                requestBody.put("showdnblist", false);
            } else {
                requestBody.put("showdnblist", true);
            }
            requestBody.put("country", sessions.getAttribute("secCountry") != null ? sessions.getAttribute("secCountry").toString() : paramArray.getOrDefault("country", ""));
            if(paramArray.get("isNewCompany")!=null && "true".equals(paramArray.get("isNewCompany"))) {
                requestBody.put("isNewCompany","true");
            } else {
                requestBody.put("isNewCompany","false");
            }
            String jsonData = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("api response: {}", responseEntity.getBody());
            Map<String, Object> originalResponse = objectMapper.readValue(responseEntity.getBody(), Map.class);

            // 응답의 msgcode가 1040이면 검증 오류 메시지 추가
            if ("1040".equals(originalResponse.get("msgcode"))) {
                Map<String, Object> validationErrorMap = new HashMap<>();
                validationErrorMap.put("validationError", true);
                validationErrorMap.put("msg", "bizregno1 validation error");
                resultList.add(validationErrorMap);
            } else {
                List<Map<String, Object>> duplist = mapResponseFields(originalResponse, responseFieldMapping.getFieldMap(), "duplist");
                resultList.addAll(duplist);

                // dnblist 필드가 존재하는 경우 추가 처리
                if("false".equals(requestBody.get("isNewCompany"))) {
                    List<Map<String, Object>> dnblist = (List<Map<String, Object>>) originalResponse.get("dnblist");
                    if (dnblist != null && !dnblist.isEmpty()) {
                        for (Map<String, Object> dnbItem : dnblist) {
                            Map<String, Object> dnbAccountData = getDnbAccountData(dnbItem);
                            resultList.add(dnbAccountData);
                        }
                    }
                }
            }

            return resultList;
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("HTTP Request failed with status {} and response: {}", e.getStatusCode(), responseBody);
            throw e;
        } catch (Exception e) {
            log.error("Error sending api", e);
            throw new RuntimeException("Error sending api", e);
        }
    }

    /*
     * 1. 메소드명: getDnbAccountData
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    DnB 응답 데이터에서 계정 정보를 추출하여 매핑된 계정 데이터 맵을 반환.
     *    전화번호, 사업자 번호 등 주요 필드를 매핑함.
     * 2. 사용법
     *    DnB 응답 데이터의 각 필드와 매핑하여 반환할 정보를 포함한 Map 반환.
     * 3. 예시 데이터
     *    - Input: responseData = { "telephone": [{"telephoneNumber": "010-1234-5678"}], "registrationNumbers": [{"registrationNumber": "123456"}] }
     *    - Output: { "phonenumber1": "010-1234-5678", "bizregno1": "123456" }
     * </PRE>
     *
     * @param responseData DnB 원본 응답 데이터
     * @return 매핑된 DnB 계정 데이터
     */
    public Map<String, Object> getDnbAccountData(Map<String, Object> responseData) {
        // 전화번호와 사업자 등록번호 추출 및 변환
        List<String> telephones = ((List<Map<String, String>>) responseData.getOrDefault("telephone", new ArrayList<>()))
                .stream()
                .map(phone -> phone.getOrDefault("telephoneNumber", ""))
                .collect(Collectors.toList());

        // Extract registration numbers
        List<String> regNumbers = ((List<Map<String, String>>) responseData.getOrDefault("registrationNumbers", new ArrayList<>()))
                .stream()
                .map(reg -> reg.getOrDefault("registrationNumber", ""))
                .collect(Collectors.toList());

        // 기본 주소 정보 추출
        Map<String, Object> primaryAddress = (Map<String, Object>) responseData.getOrDefault("primaryAddress", new HashMap<>());

        // Initialize the result map
        Map<String, Object> dnbAccountData = new HashMap<>();

        // Populate the result map with info fields
        dnbAccountData.put("country", ((Map<String, String>) primaryAddress.getOrDefault("addressCountry", new HashMap<>())).getOrDefault("isoAlpha2Code", ""));
        dnbAccountData.put("city", ((Map<String, String>) primaryAddress.getOrDefault("addressLocality", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("state", ((Map<String, String>) primaryAddress.getOrDefault("addressRegion", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("street_address", ((Map<String, String>) primaryAddress.getOrDefault("streetAddress", new HashMap<>())).getOrDefault("line1", ""));

        // Handle postalCode as a string
        String postalCode = (String) primaryAddress.getOrDefault("postalCode", "");
        dnbAccountData.put("zip_code", postalCode);

        dnbAccountData.put("region", ((Map<String, String>) primaryAddress.getOrDefault("addressRegion", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("dunsno", responseData.getOrDefault("duns", ""));
        dnbAccountData.put("phonenumber1", telephones.size() > 0 ? telephones.get(0) : "");
        dnbAccountData.put("bizregno1", regNumbers.size() > 0 ? regNumbers.get(0) : "");
        dnbAccountData.put("phonenumber2", telephones.size() > 1 ? telephones.get(1) : "");

        // Add additional fields
        dnbAccountData.put("bpid", "");
        dnbAccountData.put("orgId", "");
        dnbAccountData.put("name", responseData.getOrDefault("primaryName", ""));
        dnbAccountData.put("orgName", responseData.getOrDefault("primaryName", ""));
        dnbAccountData.put("type", "");
        dnbAccountData.put("dnbListYn", true); // dnbList 항목임을 표시

        return dnbAccountData;
    }

    /*
     * 1. 메소드명: createContact
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CMDM Contact를 CPI API를 통해 생성하고, 생성된 정보를 반환.
     * 2. 사용법
     *    context와 사용자 데이터를 전달하여 CMDM 시스템에 Contact을 생성함.
     * 3. 예시 데이터
     *    - Input: context = "B2B", userData = { "lastName": "Kim", "firstName": "Lee", "email": "example@example.com" }
     *    - Output: 생성된 연락처 정보
     * </PRE>
     *
     * @param context 연관된 요청의 컨텍스트
     * @param userData 사용자 정보가 담긴 Map 객체
     * @param session 현재 HTTP 세션
     * @param channelName 채널 이름
     * @return 생성된 연락처 정보
     */
    public Map<String, Object> createContact(String context, Map<String, Object> userData, HttpSession session,String channelName) {
        // 필드 초기화 및 데이터 추출
        ObjectMapper objectMapper = new ObjectMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime seoulTime = ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul"));

        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.createContactUrl");

        Map<String, Object> queryParams = Map.of("context", context);

        // 사용자 전화번호 정보
        String CMDMMobilePhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "mobile_phone");
        String CMDMWorkPhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "work_phone");
        String CMDMFaxPhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "fax");

        log.info("createContact: {}", userData);

        // 채널 정보 설정
        String usedChannelName = "";
        String origin = "";
        String subsidiary = getDataValue(userData, "subsidiary");
        String companyCode=Optional.ofNullable(newSubsidiaryRepository.selectCompanyCode(subsidiary))
                .orElse(""); // 값이 null이면 빈 문자열로 대체
        String regSource = Optional.ofNullable((String) userData.get("regSource")).orElse("").toLowerCase();

        Optional<Channels> channelInfo = channelRepository.findByChannelName(regSource);

        Channels channel = channelRepository.findByChannelName(regSource)
                .orElseGet(() -> channelRepository.findByRegSouce(regSource).orElse(null));

        if (channel != null) {
            usedChannelName = channel.getCmdmRegch();
            origin = channel.getSfdcRegch();
        } else {
            origin = usedChannelName = regSource;
        }

        // 마케팅 동의 및 국가 설정
        String userCountry = getProfileValue(userData, "country");
        Map<String, Object> marketingConsent = getMarketingConsent(userData);

        String Mktg_gbValue = "ALL";
        String optIn = "N";
        String optOut = "Y";

        if (Boolean.parseBoolean(Optional.ofNullable(marketingConsent)
                .map(mc -> String.valueOf(mc.get("isConsentGranted")))  // null일 경우 "null" 문자열이 반환되지 않도록 처리
                .orElse("false"))) {
            Mktg_gbValue = "ALL";
            optIn = "Y";
            optOut = "N";
        }

        if ("FR".equals(userCountry) && marketingConsent == null) {
            optIn = "B";
            optOut = "B";
        }

        Map<String, String> SFDC_TO_CMDM_DEPT_MAP = commonCodeRepository.findByHeader("BIZ_WITH_DEPT").stream()
                .collect(Collectors.toMap(
                        CommonCode::getName,
                        CommonCode::getValue,
                        (existingValue, newValue) -> existingValue // 중복된 키가 있을 경우 기존 값을 유지
                ));

        String CMDMDepartment = SFDC_TO_CMDM_DEPT_MAP.getOrDefault(getDataValue(userData, "userDepartment"), "");

        Map<String, Object> bodyParams = new HashMap<>();
        // 기본 정보 매핑 -> 2024.11.12 다시 정의된 필드로 수정
        bodyParams.put("lastname_lo", getProfileValue(userData, "lastName"));
        bodyParams.put("firstname_lo", getProfileValue(userData, "firstName"));
        bodyParams.put("jobtitle", getDataValue(userData, "jobtitle"));
        bodyParams.put("deptname", CMDMDepartment);
        bodyParams.put("telno", CMDMWorkPhoneString);
        bodyParams.put("email", getProfileValue(userData, "email"));
        bodyParams.put("countrycode", userCountry);
        bodyParams.put("regch", "CIAM");
        bodyParams.put("origin", origin); // 하드코딩
        bodyParams.put("acctid", getDataValue(userData, "accountID"));
        bodyParams.put("userid", getProfileValue(userData, "username"));
        bodyParams.put("companycode", companyCode);
        bodyParams.put("mktggb", Mktg_gbValue);
        bodyParams.put("opt_in", optIn);
        bodyParams.put("opt_indate", seoulTime.format(formatter));
        bodyParams.put("opt_out", optOut);
        bodyParams.put("opt_outdate", seoulTime.format(formatter));
        bodyParams.put("creatorid", "CIAM");
        bodyParams.put("privateconsent_yn", "Y");

//        bodyParams.put("middlename_lo", getDataValue(userData, "middleName"));
//        bodyParams.put("title", getDataValue(userData, "salutation"));
//        bodyParams.put("mobileno", CMDMMobilePhoneString);
//        bodyParams.put("faxno", CMDMFaxPhoneString);
//        bodyParams.put("street_lo", getProfileValue(userData, "address"));
//        bodyParams.put("city_lo", getProfileValue(userData, "city"));
//        bodyParams.put("district_lo", getProfileValue(userData, "state"));
//        bodyParams.put("postalcode", getProfileValue(userData, "zip"));
//        bodyParams.put("contactowner", getDataValue(userData, "ownerEmployeeID"));
//        bodyParams.put("partner_accountid", "");
//        bodyParams.put("thirdpartyuse_yn", "");
//        bodyParams.put("contactid", getDataValue(userData, "contactID"));

        log.info("Contact body params: {}", bodyParams);

        try {
            // API 요청 전송 및 응답 처리
            ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
            Map<String, Object> responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);

            if (!responseData.containsKey("contact")) {
                log.error("No contact created. " + responseEntity.getBody());
                throw new Exception("No contact created. " + responseEntity.getBody());
            }

            log.info("New CMDM contact created: {}", responseData);
            return responseData;
        } catch (Exception e) {
            log.error("Error creating contact: {}", e.getMessage());
            throw new RuntimeException("Error creating contact", e);
        }
    }

    /*
     * 1. 메소드명: getProfileValue
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    userData에서 프로필 키 값을 읽어옴.
     * 2. 사용법
     *    사용자 프로필 정보를 포함한 userData에서 지정된 키의 값을 반환함.
     * 3. 예시 데이터
     *    - Input: userData = { "profile": { "country": "KR" } }, key = "country"
     *    - Output: "KR"
     * </PRE>
     *
     * @param userData 사용자 데이터
     * @param key 가져올 프로필 데이터의 키
     * @return 프로필 키에 해당하는 값
     */
    public String getProfileValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (String) profile.get(key))
                .orElse("");
    }

    /*
     * 1. 메소드명: getDataValue
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    userData에서 data 키 값을 읽어옴.
     * 2. 사용법
     *    사용자 데이터를 포함한 userData에서 지정된 키의 값을 반환함.
     * 3. 예시 데이터
     *    - Input: userData = { "data": { "accountID": "12345" } }, key = "accountID"
     *    - Output: "12345"
     * </PRE>
     *
     * @param userData 사용자 데이터
     * @param key 가져올 데이터의 키
     * @return 키에 해당하는 값
     */
    public String getDataValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("data"))
                .map(data -> (String) data.get(key))
                .orElse("");
    }

    /*
     * 1. 메소드명: getWorkValue
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    userData에서 work 키 값을 읽어옴.
     * 2. 사용법
     *    userData의 프로필 내부에서 work 영역의 특정 키 값을 가져옴.
     * 3. 예시 데이터
     *    - Input: userData = { "profile": { "work": { "company": "Samsung" } } }, key = "position"
     *    - Output: "Samsung"
     * </PRE>
     *
     * @param userData 사용자 데이터
     * @param key 가져올 work 프로필의 키
     * @return work 프로필 키에 해당하는 값
     */
    public String getWorkValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (Map<String, Object>) profile.get("work"))
                .map(work -> (String) work.get(key))
                .orElse("");
    }

    /*
     * 1. 메소드명: extractDataFromObject
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    organizationInfo에서 List 형식의 값을 각 요소의 문자열로 변환하여 결합된 문자열로 대체.
     * 2. 사용법
     *    데이터 내의 List 필드를 검사하여 문자열 결합으로 변환.
     * 3. 예시 데이터
     *    - Input: organizationInfo = { "addresses": [ "Seoul", "Busan" ] }
     *    - Output: { "addresses": "Seoul, Busan" }
     * </PRE>
     *
     * @param organizationInfo 데이터를 포함한 맵
     */
    public void extractDataFromObject(Map<String, Object> organizationInfo) {
        organizationInfo.replaceAll((key, value) -> {
            if (value instanceof List<?>) {
                // List를 검사하고, 만약 List<String>이 아닌 경우, 각 요소를 문자열로 변환 후 결합
                List<?> listValue = (List<?>) value;
                return listValue.stream()
                        .map(Object::toString) // 요소를 문자열로 변환
                        .collect(Collectors.joining(", "));
            }
            return value; // List가 아닌 경우 원래 값을 유지
        });
    }

    /*
     * 1. 메소드명: parseIndustryType
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    industryType 문자열을 파싱하여 List 형식으로 반환.
     * 2. 사용법
     *    industryType의 문자열을 분할하여 리스트에 추가.
     * 3. 예시 데이터
     *    - Input: industryType = "[\"Tech\", \"Finance\"]"
     *    - Output: [ "Tech", "Finance" ]
     * </PRE>
     *
     * @param industryType 유형을 나타내는 문자열
     * @return 분할된 리스트
     */
    public static List<String> parseIndustryType(String industryType) {
        List<String> industryArr = new ArrayList<>();
        try {
            if (industryType.startsWith("[") && industryType.endsWith("]")) {
                // 문자열에서 대괄호를 제거하고 내부 문자열을 리스트로 변환
                String jsonArrayString = industryType.substring(1, industryType.length() - 1);

                // 콤마로 구분된 값들을 리스트로 분할
                industryArr = Arrays.asList(jsonArrayString.split(",\\s*"));

                // 문자열 요소를 처리하여 각 요소의 양쪽 공백이나 따옴표 제거
                industryArr = industryArr.stream()
                        .map(String::trim) // 양쪽 공백 제거
                        .map(s -> s.replaceAll("^\"|\"$", "")) // 양쪽의 따옴표 제거
                        .collect(Collectors.toList());
            } else {
                // industry_type이 단순한 문자열인 경우
                industryArr.add(industryType);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return industryArr;
    }

    /*
     * 1. 메소드명: createAccount
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CMDM 회사 계정을 CPI API를 통해 생성하고 응답 데이터를 반환.
     * 2. 사용법
     *    계정 데이터, 채널 이름, context를 받아 새로운 계정을 생성.
     * 3. 예시 데이터
     *    - Input: accountData = { "orgName": "Samsung", "industry_type": "[\"Tech\"]" }
     *    - Output: 생성된 계정 정보
     * </PRE>
     *
     * @param channelName 채널 이름
     * @param context 요청의 컨텍스트
     * @param accountData 계정 정보가 담긴 Map 객체
     * @param session 현재 HTTP 세션
     * @param newCompany 새로운 회사 정보 객체
     * @return 생성된 계정 정보
     */
    public Map<String, Object> createAccount(String channelName, String context, Map<String, Object> accountData, HttpSession session, NewCompany newCompany) {
        // 관련 변수 및 값 초기화
        ObjectMapper objectMapper = new ObjectMapper();
        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.createAccountUrl");

        Map<String, Object> queryParams = Map.of("context", context);
        Map<String, Object> infoData = (Map<String, Object>) accountData.get("info");

        extractDataFromObject(infoData);

        log.info("createAccount: {}", accountData);

        String regSource = Optional.ofNullable(infoData.get("regch")).orElse("").toString();
        String companyCode="";
        if(newCompany != null) {
            companyCode = Optional.ofNullable(newSubsidiaryRepository.selectCompanyCode(newCompany.getSubsidiary()))
                    .orElse(""); // 값이 null이면 빈 문자열로 대체
        }
        Channels channel = channelRepository.findByChannelName(channelName)
                .orElseGet(() -> channelRepository.findByRegSouce(regSource).orElse(null));

        String usedChannelName;
        String origin;
        if (channel != null) {
            usedChannelName = channel.getCmdmRegch();
            origin = channel.getSfdcRegch();
        } else {
            origin = usedChannelName = regSource;
        }

        List<String> industryArr = new ArrayList<>();
        String industryType = infoData.get("industry_type").toString();
        industryArr = parseIndustryType(industryType);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("name_lo", accountData.getOrDefault("orgName",""));
        bodyParams.put("country", infoData.getOrDefault("country",""));
        bodyParams.put("regch", "CIAM");
        bodyParams.put("origin", origin);
        //bodyParams.put("origin", "");
        bodyParams.put("requesttype", "SELF");
        bodyParams.put("creatorid", "CIAM");

        bodyParams.put("bizregno1", infoData.get("bizregno1"));
        bodyParams.put("vatno", infoData.getOrDefault("vatno",""));
        // newCompany가 null이 아닐 때만 처리
        if (newCompany != null) {
            bodyParams.put("dunsno", Optional.ofNullable(newCompany.getDunsNo()).orElse(""));
        } else {
            bodyParams.put("dunsno", "");  // newCompany가 null일 때 빈 값 설정
        }
        bodyParams.put("ciscode", infoData.getOrDefault("ciscode",""));
        bodyParams.put("name_gl", accountData.getOrDefault("orgName",""));
        bodyParams.put("accountwebsite", infoData.getOrDefault("accountwebsite",""));
        bodyParams.put("accountowner", "");
        bodyParams.put("street_lo", infoData.getOrDefault("street_address",""));
        bodyParams.put("city_lo", infoData.getOrDefault("city",""));
        bodyParams.put("district_lo", infoData.getOrDefault("state",""));
        bodyParams.put("street_gl", infoData.getOrDefault("street_address",""));
        bodyParams.put("city_gl", infoData.getOrDefault("city",""));
        bodyParams.put("district_gl", infoData.getOrDefault("district_gl",""));
        bodyParams.put("postalcode", infoData.getOrDefault("zip_code",""));
        bodyParams.put("email", infoData.getOrDefault("email",""));
        bodyParams.put("phonenumber1", infoData.getOrDefault("phonenumber1",""));
        bodyParams.put("phonenumber2", infoData.getOrDefault("phonenumber2",""));
        bodyParams.put("faxno", infoData.getOrDefault("faxno",""));
        bodyParams.put("industrytype2", industryArr.isEmpty() ? "" : industryArr.get(0));
        bodyParams.put("companycode", companyCode);
        log.info("Account body params: {}", bodyParams);

        try {
            boolean isAccount = false;
            Map<String, Object> responseData = new HashMap<>();

            if (accountData.containsKey("bpid")) {
                Map<String, Object> paramArray = new HashMap<>();
                paramArray.put("acctid", accountData.get("bpid"));

                String accountSerachUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString(channelName);
                List<Map<String, Object>> searchResults = accountSearch(paramArray, accountSerachUrl, responseFieldMapping, session);
                isAccount = !searchResults.isEmpty();

                if (isAccount) {
                    responseData = searchResults.get(0);
                    log.info("Existing CMDM account found: {}", responseData);
                }
            }

            if (!isAccount) {
                ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
                responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);
                log.info("New CMDM account created: {}", responseData);
            }

            if (!responseData.containsKey("account")) {
                throw new Exception("No account created. " + objectMapper.writeValueAsString(responseData));
            }

            return responseData;
        } catch (Exception e) {
            log.error("Error creating account: {}", e.getMessage());
            String accountId = extractAccountIdFromError(e.getMessage());
            if (!accountId.isEmpty()) {
                Map<String, Object> paramArray = new HashMap<>();
                paramArray.put("accountId", accountId);
                paramArray.put("context", "Context1");

                List<Map<String, Object>> existingAccount = accountSearch(paramArray, apiUrl, null, session);
                if (!existingAccount.isEmpty()) {
                    return existingAccount.get(0);
                }
            }
            throw new RuntimeException("Error creating account", e);
        }
    }

    /*
     * 1. 메소드명: extractAccountIdFromError
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    에러 메시지에서 account ID를 추출.
     * 2. 사용법
     *    계정 관련 에러 메시지에서 ID를 가져옴.
     * 3. 예시 데이터
     *    - Input: errorMessage = "Account already exists (ID12345)"
     *    - Output: "ID12345"
     * </PRE>
     *
     * @param errorMessage 에러 메시지
     * @return 추출된 account ID
     */
    public String extractAccountIdFromError(String errorMessage) {
        try {
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.error("Error extracting account ID: {}", e.getMessage());
        }
        return "";
    }

    /*
     * 1. 메소드명: sendRequest
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    HTTP 요청을 보내고 응답을 반환.
     * 2. 사용법
     *    본문과 URL을 전달하여 요청을 수행.
     * 3. 예시 데이터
     *    - Input: bodyParams = { "name": "Samsung" }, apiUrl = "http://api.example.com"
     *    - Output: 응답 결과
     * </PRE>
     *
     * @param bodyParams 요청 본문 파라미터
     * @param apiUrl API 엔드포인트 URL
     * @param queryParams 요청 쿼리 파라미터
     * @return API 응답 결과
     */
    public ResponseEntity<String> sendRequest(Map<String, Object> bodyParams, String apiUrl, Map<String, Object> queryParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyParams, headers);

        return restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class, queryParams);
    }

    /*
     * 1. 메소드명: getPhoneNumber
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 전화 타입에 따라 번호를 반환.
     * 2. 사용법
     *    사용자 프로필에서 전화번호를 검색.
     * 3. 예시 데이터
     *    - Input: profile = { "phones": [ { "type": "work", "number": "12345" } ] }, phoneType = "work"
     *    - Output: "12345"
     * </PRE>
     *
     * @param profile 사용자 프로필 데이터
     * @param phoneType 전화 번호의 유형 (work, mobile 등)
     * @return 해당 유형의 전화 번호
     */
    public String getPhoneNumber(Map<String, Object> profile, String phoneType) {
        List<Map<String, Object>> phones = (List<Map<String, Object>>) profile.get("phones");

        if (phones == null) {
            return "";  // phones 리스트가 null인 경우 빈 문자열 반환
        }

        return phones.stream()
                .filter(phone -> phoneType.equals(phone.get("type")))
                .map(phone -> (String) phone.get("number"))
                .findFirst()
                .orElse("");
    }

    /*
     * 1. 메소드명: getMarketingConsent
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자의 마케팅 동의 상태를 반환.
     * 2. 사용법
     *    country 정보에 따라 적절한 마케팅 동의 정보를 가져옴.
     * 3. 예시 데이터
     *    - Input: userData = { "profile": { "country": "KR" } }
     *    - Output: 마케팅 동의 객체
     * </PRE>
     *
     * @param userData 사용자 데이터
     * @return 마케팅 동의 데이터 객체
     */
    public Map<String, Object> getMarketingConsent(Map<String, Object> userData) {
        boolean isProd = "prod".equals(BeansUtil.getApplicationProperty("server"));
        String userCountry = Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (String) profile.get("country"))
                .orElse("");
        Map<String, Object> marketingConsent = null;

        Map<String, Object> preferences = (Map<String, Object>) userData.get("preferences");
        Map<String, Object> b2b = preferences != null ? (Map<String, Object>) preferences.get("b2b") : null;
        Map<String, Object> common = b2b != null ? (Map<String, Object>) b2b.get("common") : null;

        switch (userCountry) {
            case "KR":
                if (isProd) {
                    Map<String, Object> noneu = common != null ? (Map<String, Object>) common.get("noneu") : null;
                    if (noneu != null) {
                        marketingConsent = (Map<String, Object>) noneu.get("kr");
                    }
                } else {
                    Map<String, Object> noneu = common != null ? (Map<String, Object>) common.get("noneu") : null;
                    if (common != null) {
                        marketingConsent = (Map<String, Object>) common.get("kr");
                    }
                }
                break;
            default:
                if (isProd) {
                    Map<String, Object> all = common != null ? (Map<String, Object>) common.get("all") : null;
                    if (all != null) {
                        marketingConsent = (Map<String, Object>) all.get("af");
                    }
                } else {
                    if (common != null) {
                        marketingConsent = (Map<String, Object>) common.get("all");
                    }
                }
        }

        return marketingConsent;
    }

    /*
     * 1. 메소드명: sendUidProvisioningNoConfigChecking
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    UID 프로비저닝 요청을 비동기 방식으로 수행하고, 요청 파라미터 및 채널 정보를 설정하여 호출.
     * 2. 사용법
     *    파라미터로 채널 이름, 요청 타입, UID를 받아서 UID 프로비저닝 요청을 수행.
     * 3. 예시 데이터
     *    - Input: channel = "sba", requestType = "I", uid = "12345"
     *    - Output: Future<ResponseEntity<String>> 객체
     * </PRE>
     *
     * @param channel 채널 이름
     * @param requestType 요청 타입
     * @param uid 사용자 UID
     * @return 비동기 응답 객체
     */
    @Async
    public Future<ResponseEntity<String>> sendUidProvisioningNoConfigChecking(String channel,String requestType,String uid) {
        // channel을 통해 채널 정보 조회
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 매핑을 위한 ObjectMapper 객체 생성
        Map<String, Object> bodyParams = new HashMap<>(); // API 본문 파라미터 맵 생성
        Map<String, Object> queryParams = new HashMap<>(); // 요청 쿼리 파라미터 맵 생성
        String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.provisioningUrl");
        Optional<Channels> optionalChannel = channelRepository.findByChannelName(channel);

        // 채널 정보를 조회하여 설정 정보 가져오기
        if (optionalChannel.isPresent()) {
            Channels channelData = optionalChannel.get();
            Map<String, Object> configMap = channelData.getConfigMap();
            Map<String, Object> javaProvisioningMap = (Map<String, Object>) configMap.get("java_provisioning");

            // 가변 필드인 channel, requestType, UID 설정
            bodyParams.put("channel", channel);  // 전달받은 channel 값
            bodyParams.put("requestType", requestType);  // 전달받은 requestType 값
            bodyParams.put("UID", uid);  // 전달받은 UID 값
            bodyParams.put("type", channelData.getChannelType().toLowerCase());

            // 제외할 필드를 정의하여 javaProvisioningMap에서 필요하지 않은 필드는 추가하지 않음
            Set<String> excludedKeys = Set.of("channel", "requestType", "UID","type","provisioning_type");

            // javaProvisioningMap에서 제외할 필드 외의 나머지 필드 추가
            javaProvisioningMap.forEach((key, value) -> {
                if (!excludedKeys.contains(key)) {
                    bodyParams.put(key, value);  // 제외할 필드가 아닌 경우 bodyParams에 추가
                }
            });

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            // API 요청 본문 및 헤더를 포함한 HttpEntity 생성
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyParams, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class, queryParams);

            // 비동기 응답 반환
            return new AsyncResult<>(responseEntity);
        } else {
            log.error("Channel not found: {}", channel);
            throw new RuntimeException("Channel not found: " + channel);
        }
    }

    /*
     * 1. 메소드명: partnerCompanyDuplicateCheck
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CMDM에서 비슷한 회사가 있는지 확인하고 중복 여부 결과 반환.
     * 2. 사용법
     *    회사 검색 파라미터를 전달하여 유사한 회사 중복 여부를 확인.
     * 3. 예시 데이터
     *    - Input: allParams = { "channel": "salesforce", "country": "KR" }
     *    - Output: 중복 체크 결과 맵
     * </PRE>
     *
     * @param allParams 검색 파라미터
     * @param session 현재 HTTP 세션
     * @return 중복 체크 결과 맵
     */
    public Map<String, Object> partnerCompanyDuplicateCheck(Map<String, Object> allParams, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.findSimilarUrl");
            CpiRequestFieldMapping requestFieldMapping = CpiRequestFieldMapping.fromString((String) allParams.get("channel"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));

            // API 요청에서 사용할 기본 값 설정
            allParams.put("isNewCompany",allParams.get("isNewCompany"));
            allParams.put("country",allParams.get("country"));

            // 중복 여부 확인 API 호출
            List<Map<String, Object>> cpiResult = findSimilar(allParams, url, requestFieldMapping, responseFieldMapping, session);

            // 유효성 검사 오류 처리
            if (!cpiResult.isEmpty() && cpiResult.get(0).containsKey("validationError")) {
                result.put("result", "validationError");
                result.put("msg", cpiResult.get(0).get("msg"));
                return result;
            }

            // rank가 100인 항목이 있는지 확인하여 중복 처리
            for (Map<String, Object> item : cpiResult) {
                if (item.get("rank") != null && item.get("rank").equals(100)) {
                    String accountId = (String) item.get("bpid");
                    result.put("result", "duplicate");
                    result.put("msg", "Duplicate company found. The account ID : " + accountId);
                    result.put("accountId", accountId);
                    return result;
                }
            }

            // 중복 항목이 없는 경우
            result.put("result", "success");
            result.put("msg", "No duplicate with rank 100 found");
        } catch (Exception e) {
            result.put("result", "error");
            result.put("msg", "An error occurred: " + e.getMessage());
        }

        return result;
    }

    /*
     * 1. 메소드명: saveSubsidiaries
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 정보에서 회사및 법인 정보 목록을 저장하고, DB에 반영.
     * 2. 사용법
     *    회사 데이터를 전달하여 DB에 회사 정보 저장.
     * 3. 예시 데이터
     *    - Input: requestData = { "company": [ { "company_name": "Samsung" } ] }
     * </PRE>
     *
     * @param requestData 자회사 데이터를 포함한 맵
     */
    public void saveSubsidiaries(Map<String, Object> requestData) {
        // 모든 데이터를 먼저 삭제
        newSubsidiaryRepository.deleteAll();

        // company 리스트 추출
        List<Map<String, Object>> companies = (List<Map<String, Object>>) requestData.get("company");

        if (companies != null && !companies.isEmpty()) {
            List<NewSubsidiary> subsidiaries = new ArrayList<>();

            for (Map<String, Object> company : companies) {
                if ("Y".equals(company.get("cmdm_useyn"))) {
                    NewSubsidiary subsidiary = new NewSubsidiary();
                    subsidiary.setCountryCode((String) company.get("country_code"));
                    subsidiary.setRequestChannel((String) company.get("request_channel"));
                    subsidiary.setErpUseYn((String) company.get("erp_useyn"));
                    subsidiary.setChangerId((String) company.get("changer_id"));

                    // change_date 변환 처리 (한국 시간 기준)
                    String changeDateStr = (String) company.get("change_date");
                    if (changeDateStr != null && !changeDateStr.isEmpty()) {
                        LocalDateTime changeDate = LocalDateTime.parse(changeDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        Timestamp changeDateTimestamp = Timestamp.valueOf(changeDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
                        subsidiary.setChangeDate(changeDateTimestamp.toLocalDateTime());
                    }

                    subsidiary.setCompanyName((String) company.get("company_name"));
                    subsidiary.setCompanyAbbreviation((String) company.get("company_abbreviation"));
                    subsidiary.setCreatorId((String) company.get("creator_id"));
                    subsidiary.setCmdmUseYn((String) company.get("cmdm_useyn"));
                    subsidiary.setCompanyCode((String) company.get("company_code"));

                    // CreateDate는 빈 값일 수 있으므로 예외 처리
                    String createDateStr = (String) company.get("create_date");
                    if (createDateStr != null && !createDateStr.isEmpty()) {
                        LocalDateTime createDate = LocalDateTime.parse(createDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        Timestamp createDateTimestamp = Timestamp.valueOf(createDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
                        subsidiary.setCreateDate(createDateTimestamp.toLocalDateTime());
                    }

                    subsidiaries.add(subsidiary);
                }
            }

            // 데이터 저장
            newSubsidiaryRepository.saveAll(subsidiaries);
        }
    }

    /*
     * 1. 메소드명: invitationCompanyMerge
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    초대된 회사의 중복을 통합하고 DB에 업데이트.
     * 2. 사용법
     *    초대 회사의 bpid와 병합할 대상 ID를 전달하여 업데이트 수행.
     * 3. 예시 데이터
     *    - Input: requestData = { "bpid": "B", "mergeInto": "A" }
     * </PRE>
     *
     * @param requestData bpid 및 병합할 대상 ID가 포함된 요청 데이터
     */
    public void invitationCompanyMerge(Map<String, Object> requestData) {
        // 파라미터로 들어온 bpid와 mergeinto 값 추출
        String bpid = (String) requestData.get("bpid");
        String mergeInto = (String) requestData.get("mergeInto");

        // status가 pending이고 bpid가 B인 데이터를 찾음
        List<ChannelInvitation> invitations = channelInvitationRepository.findByBpidAndStatus(bpid, "pending");

        // 해당 데이터의 bpid 값을 A로 업데이트
        for (ChannelInvitation invitation : invitations) {
            invitation.setBpid(mergeInto);
        }

        // 변경된 데이터를 DB에 저장
        channelInvitationRepository.saveAll(invitations);
    }

    /*
     * 1. 메소드명: updateContact
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CMDM Contact 정보를 업데이트하고 마케팅 동의 상태를 설정.
     * 2. 사용법
     *    업데이트할 Contact의 데이터와 마케팅 동의 정보를 전달하여 API 호출.
     * 3. 예시 데이터
     *    - Input: userData = { "profile": { "country": "FR" } }
     *    - Output: 업데이트된 Contact 데이터
     * </PRE>
     *
     * @param context 현재 실행 컨텍스트
     * @param userData 업데이트할 사용자 데이터
     * @param channelName 채널 이름
     * @return 업데이트된 Contact 정보
     */
    public Map<String, Object> updateContact(String context, Map<String, Object> userData,String channelName) {
        ObjectMapper objectMapper = new ObjectMapper();
        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.updateContactUrl");

        Map<String, Object> queryParams = Map.of("context", context);

        log.info("updateContact: {}", userData);

        String userCountry = getProfileValue(userData, "country");
        Map<String, Object> marketingConsent = getMarketingConsent(userData);

        String Mktg_gbValue = "";
        String optIn = "N";
        String optOut = "Y";

        if (Boolean.parseBoolean(Optional.ofNullable(marketingConsent)
                .map(mc -> String.valueOf(mc.get("isConsentGranted")))  // null일 경우 "null" 문자열이 반환되지 않도록 처리
                .orElse("false"))) {
            Mktg_gbValue = "ALL";
            optIn = "Y";
            optOut = "N";
        }

        if ("FR".equals(userCountry) && marketingConsent == null) {
            optIn = "B";
            optOut = "B";
        }

        Map<String, String> SFDC_TO_CMDM_DEPT_MAP = commonCodeRepository.findByHeader("BIZ_WITH_DEPT").stream()
                .collect(Collectors.toMap(
                        CommonCode::getName,
                        CommonCode::getValue,
                        (existingValue, newValue) -> existingValue // 중복된 키가 있을 경우 기존 값을 유지
                ));

        String CMDMDepartment = SFDC_TO_CMDM_DEPT_MAP.getOrDefault(getDataValue(userData, "userDepartment"), "");

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("opt_in", optIn);
        bodyParams.put("opt_indate", java.time.LocalDate.now().toString());
        bodyParams.put("mktggb", Mktg_gbValue);
        bodyParams.put("opt_out", optOut);
        bodyParams.put("opt_outdate", java.time.LocalDate.now().toString());
        bodyParams.put("contactid", getDataValue(userData, "contactID"));

        log.info("Contact body params: {}", bodyParams);

        try {
            ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
            Map<String, Object> responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);

            if (!responseData.containsKey("contact")) {
                log.error("No contact created. " + responseEntity.getBody());
                throw new Exception("No contact created. " + responseEntity.getBody());
            }

            log.info("New CMDM contact created: {}", responseData);
            return responseData;
        } catch (Exception e) {
            log.error("Error creating contact: {}", e.getMessage());
            throw new RuntimeException("Error creating contact", e);
        }
    }

    /*
     * 1. 메소드명: updateCpiContact
     * 2. 클래스명: CpiApiService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CONTACT 수정을 CPI로 호출하여 실행
     * 2. 사용법
     *    파라미터로 요청 타입, UID를 받아서 UID CONTACT업데이트를 수행
     * 3. 예시 데이터
     *    - Input: requestType = "I", uid = "12345"
     *    - Output: Future<ResponseEntity<String>> 객체
     * </PRE>
     *
     * @param requestType 요청 타입
     * @param uid 사용자 UID
     * @return 비동기 응답 객체
     */
    public Future<ResponseEntity<String>> updateCpiContact(String requestType, String uid) {
        Map<String, Object> bodyParams = new HashMap<>();
        String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.updateContactUrl");

        bodyParams.put("requestType", requestType);
        bodyParams.put("UID", uid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyParams, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                // 성공 로직
                return new AsyncResult<>(responseEntity);
            } else {
                // 실패 시 에러 처리
                log.error("update Contact요청 실패:" + responseEntity);
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            log.error("API 요청 실패", e);
            //return new AsyncResult<>(new ResponseEntity<>("API 요청 실패", HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return null; // 실패 시 null 반환 또는 적절한 처리
    }

}
