package com.samsung.ciam.common.core.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 1. FileName	: JsonNodeConverter.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: JsonNode 타입을 데이터베이스 문자열 컬럼에 저장하고 다시 JsonNode로 변환하기 위한 JPA Converter
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

@Converter(autoApply = true)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * 1. 메소드명: convertToDatabaseColumn
     * 2. 클래스명: JsonNodeConverter
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    JsonNode 객체를 데이터베이스에 저장할 수 있도록 JSON 문자열로 변환
     * 2. 사용법
     *    convertToDatabaseColumn(JsonNode attribute)
     * </PRE>
     * @param attribute JsonNode 타입의 객체
     * @return String 변환된 JSON 문자열
     */
    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert JsonNode to String", e);
        }
    }

    /*
     * 1. 메소드명: convertToEntityAttribute
     * 2. 클래스명: JsonNodeConverter
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    데이터베이스의 JSON 문자열을 JsonNode 객체로 변환하여 엔티티에 저장
     * 2. 사용법
     *    convertToEntityAttribute(String dbData)
     * </PRE>
     * @param dbData 데이터베이스에 저장된 JSON 문자열
     * @return JsonNode 변환된 JsonNode 객체
     */
    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readTree(dbData);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert String to JsonNode", e);
        }
    }
}