package com.samsung.ciam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 1. FileName	: StringUtil.java
 * 2. Package	: com.samsung.ciam.utils
 * 3. Comments	: 문자열 및 JSON 처리를 위한 유틸리티 클래스
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
public class StringUtil {

    /*
     * 1. 메소드명: isValidEmail
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    이메일 유효성을 정규식을 사용하여 검사
     * 2. 사용법
     *    isValidEmail("email@example.com") 와 같이 사용하여 이메일 유효성을 확인
     * </PRE>
     * @param email 검사할 이메일 주소
     * @return boolean 이메일 유효성 여부
     */
    public static boolean isValidEmail(String email) {
        // 이메일 유효성 검사를 위한 정규 표현식
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    /*
     * 1. 메소드명: capitalizeFirstLetter
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    문자열의 첫 글자를 대문자로 변환하고 나머지는 소문자로 변환하여 반환
     * 2. 사용법
     *    capitalizeFirstLetter("example") 와 같이 사용
     * </PRE>
     * @param value 변환할 문자열
     * @return String 첫 글자만 대문자로 변환된 문자열
     */
    public static String capitalizeFirstLetter(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    /*
     * 1. 메소드명: getSafeString
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    JSON 노드 내에서 안전하게 문자열 값을 가져오는 메서드, 경로에 존재하지 않는 노드는 빈 문자열 반환
     * 2. 사용법
     *    getSafeString(rootNode, "path", "to", "field") 와 같이 JSON 경로를 지정하여 호출
     * </PRE>
     * @param rootNode JSON의 루트 노드
     * @param path 경로를 지정하는 문자열 배열
     * @return String JSON 노드의 문자열 값 또는 빈 문자열
     */
    public static String getSafeString(JsonNode rootNode, String... path) {
        JsonNode node = rootNode;
        for (String field : path) {
            node = node.path(field);
            if (node.isMissingNode()) {
                return "";
            }
        }
        return node.asText("");
    }

    /*
     * 1. 메소드명: isEmpty
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    객체가 null이거나 빈 문자열인지 확인
     * 2. 사용법
     *    isEmpty(object) 와 같이 호출
     * </PRE>
     * @param objectVal 검사할 객체
     * @return boolean 객체가 null이거나 빈 문자열일 경우 true 반환
     */
    public static boolean isEmpty(Object objectVal){
        return objectVal==null || "".equals(objectVal);
    }

    /*
     * 1. 메소드명: getStringValue
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    객체가 null이거나 빈 문자열일 경우 기본 문자열을 반환하고, 그렇지 않으면 객체의 문자열 값을 반환
     * 2. 사용법
     *    getStringValue(object, "default") 와 같이 사용
     * </PRE>
     * @param objectVal 검사할 객체
     * @param defaultString 기본 문자열
     * @return String 객체의 문자열 값 또는 기본 문자열
     */
    public static String getStringValue(Object objectVal, String defaultString){
        if(isEmpty(objectVal)){
            return defaultString;
        }else{
            return (String)objectVal;
        }
    }

    /*
     * 1. 메소드명: getNonEmptyValue
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    JSON 값에서 빈 문자열이 아닌 값을 반환
     * 2. 사용법
     *    getNonEmptyValue(jsonNode) 와 같이 호출
     * </PRE>
     * @param jsonNode JSON 노드
     * @return String 빈 문자열이 아닌 JSON 값
     */
    // JSON 값에서 빈 문자열이 아닌 값을 추출하는 헬퍼 메서드
    public static String getNonEmptyValue(JsonNode jsonNode) {
        String value = jsonNode.asText();
        return value.isEmpty() ? "" : value;
    }

    /*
     * 1. 메소드명: convertListToJsonArray
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    문자열 리스트를 JSON 배열의 문자열로 변환
     * 2. 사용법
     *    convertListToJsonArray(List.of("value1", "value2")) 와 같이 호출
     * </PRE>
     * @param valueList 문자열 리스트
     * @return String JSON 배열 문자열
     */
    public static String convertListToJsonArray(List<String> valueList) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (valueList != null && !valueList.isEmpty()) {
                return objectMapper.writeValueAsString(valueList);
            }
        } catch (JsonProcessingException e) {
            log.error("Error convertListToJsonArray processing failed", e);
        }
        return "[]"; // 빈 배열로 처리
    }

    /*
     * 1. 메소드명: extractValueAsList
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    요청 매개변수 Map에서 특정 키의 값을 리스트로 변환하여 반환
     * 2. 사용법
     *    extractValueAsList(requestParams, "key") 와 같이 호출
     * </PRE>
     * @param requestParams 요청 매개변수 Map
     * @param key 값을 추출할 키
     * @return List<String> 변환된 문자열 리스트
     */
    public static List<String> extractValueAsList(Map<String, String> requestParams, String key) {
        Object value = requestParams.getOrDefault(key, "");
        if (value instanceof List) {
            return (List<String>) value;
        } else if (!value.toString().isEmpty()) {
            return new ArrayList<>(Collections.singletonList(value.toString()));
        }
        return new ArrayList<>();
    }

    /*
     * 1. 메소드명: createOrganizationJson
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 정보 배열과 요청 매개변수를 사용하여 조직 정보를 포함하는 JSON 문자열 생성
     * 2. 사용법
     *    createOrganizationJson(industryArray, requestParams) 와 같이 호출
     * </PRE>
     * @param industryArray 산업 정보 배열
     * @param requestParams 요청 매개변수 Map
     * @return String 조직 정보를 포함하는 JSON 문자열
     */
    public static String createOrganizationJson(List<String> industryArray, Map<String, String> requestParams) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> organization = new HashMap<>();
            String industryArrayString = convertIndustryArrayToJsonArray(industryArray);

            organization.put("industry_type", industryArrayString);
            organization.put("products", requestParams.getOrDefault("products", ""));
            organization.put("channeltype", requestParams.getOrDefault("channelType", ""));

            return objectMapper.writeValueAsString(organization);
        } catch (JsonProcessingException e) {
            log.error("Error createOrganizationJson processing failed", e);
            return "{}"; // 예외가 발생할 경우 빈 JSON 반환
        }
    }

    /*
     * 1. 메소드명: convertIndustryArrayToJsonArray
     * 2. 클래스명: StringUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사 정보 리스트를 JSON 배열 문자열로 변환
     * 2. 사용법
     *    convertIndustryArrayToJsonArray(industryArray) 와 같이 호출
     * </PRE>
     * @param industryArray 산업 정보 리스트
     * @return String JSON 배열 문자열로 변환된 값
     */
    public static String convertIndustryArrayToJsonArray(List<String> industryArray) {
        return industryArray.stream()
                .map(item -> "\"" + item + "\"") // 각 요소를 따옴표로 감싸기
                .collect(Collectors.joining(", ", "[", "]")); // 배열 형식으로 변환
    }
}
