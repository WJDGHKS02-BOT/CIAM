package com.samsung.ciam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class StringUtil {

    public static boolean isValidEmail(String email) {
        // 이메일 유효성 검사를 위한 정규 표현식
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static String capitalizeFirstLetter(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

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

    public static boolean isEmpty(Object objectVal){
        return objectVal==null || "".equals(objectVal);
    }
    
    public static String getStringValue(Object objectVal, String defaultString){
        if(isEmpty(objectVal)){
            return defaultString;
        }else{
            return (String)objectVal;
        }
    }

    // JSON 값에서 빈 문자열이 아닌 값을 추출하는 헬퍼 메서드
    public static String getNonEmptyValue(JsonNode jsonNode) {
        String value = jsonNode.asText();
        return value.isEmpty() ? "" : value;
    }

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

    public static List<String> extractValueAsList(Map<String, String> requestParams, String key) {
        Object value = requestParams.getOrDefault(key, "");
        if (value instanceof List) {
            return (List<String>) value;
        } else if (!value.toString().isEmpty()) {
            return new ArrayList<>(Collections.singletonList(value.toString()));
        }
        return new ArrayList<>();
    }

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

    public static String convertIndustryArrayToJsonArray(List<String> industryArray) {
        return industryArray.stream()
                .map(item -> "\"" + item + "\"") // 각 요소를 따옴표로 감싸기
                .collect(Collectors.joining(", ", "[", "]")); // 배열 형식으로 변환
    }
}
