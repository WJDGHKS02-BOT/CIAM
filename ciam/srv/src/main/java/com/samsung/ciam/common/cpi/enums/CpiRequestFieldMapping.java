package com.samsung.ciam.common.cpi.enums;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 1. FileName	: CpiRequestFieldMapping.java
 * 2. Package	: com.samsung.ciam.common.cpi.enums
 * 3. Comments	: CPI 요청 필드 매핑을 정의하는 열거형으로, 각 유형별로 필드 매핑을 설정
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

public enum CpiRequestFieldMapping {

    SBA(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("name", "name_lo"),
            new AbstractMap.SimpleEntry<>("filter_country", "country"),
            new AbstractMap.SimpleEntry<>("filter_bizregno1", "bizregno1"),
            new AbstractMap.SimpleEntry<>("filter_vatno", "vatno"),
            new AbstractMap.SimpleEntry<>("filter_dunsno", "dunsno"),
            new AbstractMap.SimpleEntry<>("filter_ciscode", "ciscode"),
            new AbstractMap.SimpleEntry<>("filter_accountwebsite", "accountwebsite"),
            new AbstractMap.SimpleEntry<>("filter_email", "email"),
            new AbstractMap.SimpleEntry<>("filter_phonenumber1", "phonenumber1"),
            new AbstractMap.SimpleEntry<>("filter_faxno", "faxno"),
            new AbstractMap.SimpleEntry<>("filter_street_address", "street_lo"),
            new AbstractMap.SimpleEntry<>("filter_city", "city_lo"),
            new AbstractMap.SimpleEntry<>("filter_state", "district_lo"),
            new AbstractMap.SimpleEntry<>("filter_zip_code", "postalcode")
    )),
    TYPE_B(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("filter_Name", "organizationName"),
            new AbstractMap.SimpleEntry<>("filter_country", "countryCode"),
            new AbstractMap.SimpleEntry<>("filter_bizregno1", "businessRegNumber1"),
            new AbstractMap.SimpleEntry<>("filter_vatno", "vatNumber"),
            new AbstractMap.SimpleEntry<>("filter_dunsno", "dunsNumber"),
            new AbstractMap.SimpleEntry<>("filter_ciscode", "cisCode"),
            new AbstractMap.SimpleEntry<>("filter_accountwebsite", "website"),
            new AbstractMap.SimpleEntry<>("filter_email", "emailAddress"),
            new AbstractMap.SimpleEntry<>("filter_phonenumber1", "phone1"),
            new AbstractMap.SimpleEntry<>("filter_faxno", "faxNumber"),
            new AbstractMap.SimpleEntry<>("filter_street_address", "street"),
            new AbstractMap.SimpleEntry<>("filter_city", "city"),
            new AbstractMap.SimpleEntry<>("filter_state", "state"),
            new AbstractMap.SimpleEntry<>("filter_zip_code", "postalCode")
    ));

    private final Map<String, String> fieldMap;

    /*
     * 1. 생성자명: CpiRequestFieldMapping
     * 2. 클래스명: CpiRequestFieldMapping
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CPI 요청 필드 매핑을 초기화하는 생성자
     * 2. 사용법
     *    각 열거형에 지정된 필드 매핑을 설정
     * </PRE>
     * @param fieldMap 필드 매핑을 위한 Map 객체
     */
    CpiRequestFieldMapping(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    /*
     * 1. 메소드명: getFieldMap
     * 2. 클래스명: CpiRequestFieldMapping
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CPI 필드 매핑 정보를 반환
     * 2. 사용법
     *    각 열거형에서 필드 매핑 정보를 가져올 때 사용
     * </PRE>
     * @return Map<String, String> 필드 매핑 정보
     */
    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    /*
     * 1. 메소드명: fromString
     * 2. 클래스명: CpiRequestFieldMapping
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    문자열 값을 기반으로 해당하는 CpiRequestFieldMapping 열거형을 반환
     * 2. 사용법
     *    fromString("SBA") 와 같이 문자열을 통해 열거형을 가져올 때 사용
     * </PRE>
     * @param value 문자열로 입력된 열거형 이름
     * @return CpiRequestFieldMapping 문자열에 해당하는 열거형 객체
     */
    public static CpiRequestFieldMapping fromString(String value) {
        return CpiRequestFieldMapping.valueOf(value.toUpperCase());
    }

    /*
     * 1. 메소드명: getDefaultValues
     * 2. 클래스명: CpiRequestFieldMapping
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    열거형 타입에 따라 기본 값을 반환
     * 2. 사용법
     *    getDefaultValues()를 호출하여 기본 값이 필요한 필드를 얻을 때 사용
     * </PRE>
     * @return Map<String, Object> 기본 값을 포함하는 Map 객체
     */
    public Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<>();
        switch (this) {
            case SBA:
                defaultValues.put("name_gl", "");
                defaultValues.put("getmaxrank_recode", false);
                break;
            case TYPE_B:
                // Add any default values specific to TYPE_B if needed
                break;
        }
        return defaultValues;
    }
}
