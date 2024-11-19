package com.samsung.ciam.services;

import com.samsung.ciam.models.Consent;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import java.util.*;

/**
 * 1. FileName   : ConsentService.java
 * 2. Package    : com.samsung.ciam.services
 * 3. Comments   : 사용자의 약관 데이터를 조회, 관리하는 서비스로, 동의 타입, 국가, 언어, 버전 목록을 포함하여 관리.
 * 4. Author     : 서정환
 * 5. DateTime   : 2024. 11. 04.
 * 6. History    :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date         | Name          | Comment
 * <p>
 * -------------  -------------   ------------------------------
 * <p>
 * 2024. 11. 04.   | 서정환        | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@Slf4j
@Service
public class ConsentService {
    
    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private ConsentTypesRepository consentTypesRepository;

    @Autowired
    private ConsentLanguagesRepository consentLanguagesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * 1. 메소드명: consentsView
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 관련 데이터들을 ModelAndView로 가져오는 메소드로, 역할, 채널, 타입 등의 데이터 설정.
     * 2. 사용법
     *    사용자가 로그인 후 약관 화면으로 접근 시 자동으로 호출되며, 세션 데이터에서 역할 정보와 채널 정보를 가져옴.
     * 3. 예시 데이터
     *    - Output: ModelAndView 객체에 role, channel, type 약관 정보를 포함하여 반환
     * </PRE>
     *
     * @param session 현재 HTTP 세션
     * @return 약관 화면에 필요한 데이터가 담긴 ModelAndView 객체
     */
    public ModelAndView consentsView(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("fragments/consentView");

        // common code
        // channel (channels 테이블 > value : channel_name / name : channel_display_name)
        modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

        boolean isChannelAdmin = Optional.ofNullable(session.getAttribute("cdc_channeladmin"))
        .map(attr -> (Boolean) attr)
        .orElse(false);

        boolean isCiamAdmin = Optional.ofNullable(session.getAttribute("cdc_ciamadmin"))
            .map(attr -> (Boolean) attr)
            .orElse(false);

        if (isChannelAdmin) {
            modelAndView.addObject("role", "Channel Admin");
        } else if(isCiamAdmin) {
            modelAndView.addObject("role", "CIAM Admin");
        } else {
            // role (테스트 데이터 포함 임시로 지정)
            modelAndView.addObject("role", session.getAttribute("btp_myrole"));
        }

        // type (consent_types 테이블 > value : id / name : name_en)
        modelAndView.addObject("type", consentTypesRepository.findAll());
        // 3가지는 다른 api에서 조회
        // modelAndView.addObject("country", cisCountryRepository.findAllOrderedByNameEn());
        // log.warn("country ::: {}", cisCountryRepository.findAllOrderedByNameEn());
        // modelAndView.addObject("language", consentLanguagesRepository.findAll());
        // log.warn("language ::: {}", consentLanguagesRepository.findAll());
        // modelAndView.addObject("version", consentContentRepository.selectDistinctVersion());

        return modelAndView;
    }

    /*
     * 1. 메소드명: getConsentTypeList
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 타입 목록을 조회하는 메소드로, coverage에 따라 타입 목록을 반환.
     * 2. 사용법
     *    payload에 담긴 coverage를 기준으로 조건에 맞는 타입 목록을 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "coverage": "samsung" }
     *    - Output: { "typeCode": "001", "typeName": "서비스 약관" } 형태의 목록
     * </PRE>
     *
     * @param payload coverage 정보를 포함한 검색 파라미터 맵
     * @return 약관 타입 목록을 포함한 리스트
     */
    public List<Map<String, Object>> getConsentTypeList(Map<String, String> payload) {
        String query = "select distinct c.type_id, (select name_en from consent_types where id = c.type_id) as name from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 and c.coverage = :coverage and c.type_id is not null and cc.status_id ='published'";

        List<Object[]> results = entityManager.createNativeQuery(query)
                                .setParameter("coverage", payload.get("coverage"))
                                .getResultList();
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("typeCode", result[0]);
            termsMap.put("typeName", result[1]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
        // return consentRepository.getConsentTypeList(payload.get("coverage"));
    }

    /*
     * 1. 메소드명: getCountryList
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관에 적용 가능한 국가 목록을 조회하는 메소드로, common 또는 특정 채널에 맞는 국가 리스트를 반환.
     * 2. 사용법
     *    payload에 coverage 정보를 전달받아 해당 국가 리스트를 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "coverage": "samsung" }
     *    - Output: { "countryCode": "KR", "countryName": "Korea" } 형태의 국가 목록
     * </PRE>
     *
     * @param payload coverage 정보를 포함한 검색 파라미터 맵
     * @return 국가 목록을 포함한 리스트
     */
    public List<Map<String, Object>> getCountryList(Map<String, String> payload) {
        String query = "";

        String coverage = payload.get("coverage");
        List<Object[]> results = new ArrayList<Object[]>();

        if("common".equals(coverage)) {
            query= "select country_code ,name_en from cis_countries ";
            results = entityManager.createNativeQuery(query)
                    .getResultList();
        } else {
            query = "select country_code ,country from sec_serving_countries " +
                    "   where channel = :coverage ";

            results = entityManager.createNativeQuery(query)
                    .setParameter("coverage", payload.get("coverage"))
                    .getResultList();
        }


        List<Map<String, Object>> mappedResults = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("countryCode", result[0]);
            termsMap.put("countryName", result[1]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
        // return consentRepository.getCountryList(payload.get("typeId"), payload.get("coverage"));
    }

    /*
     * 1. 메소드명: getLanguageList
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관에 사용 가능한 언어 목록을 조회하는 메소드로, 타입과 coverage 정보에 따라 언어 리스트를 반환.
     * 2. 사용법
     *    payload에 타입 ID, coverage, 약관 목록을 전달하여 언어 목록을 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "typeId": "001", "coverage": "samsung", "countries": "KR" }
     *    - Output: ["en", "ko"] 형태의 언어 목록
     * </PRE>
     *
     * @param payload 타입 ID, coverage, 국가 목록이 포함된 검색 파라미터 맵
     * @return 언어 목록을 포함한 리스트
     */
    public List<String> getLanguageList(Map<String, String> payload) {
        return consentRepository.getLanguageList(payload.get("typeId"), payload.get("coverage"), payload.get("countries"));
    }

    /*
     * 1. 메소드명: getVersionList
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 약관 타입과 국가에 대한 버전 목록을 조회하는 메소드로, 언어, coverage, 타입 ID에 따라 버전 목록을 반환.
     * 2. 사용법
     *    payload에 타입 ID, coverage, 국가, 언어를 전달하여 해당 버전 목록을 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "typeId": "001", "coverage": "samsung", "countries": "KR", "language": "en" }
     *    - Output: { "version": "1.0", "id": "123" } 형태의 버전 목록
     * </PRE>
     *
     * @param payload 타입 ID, coverage, 국가, 언어 정보를 포함한 검색 파라미터 맵
     * @return 버전 목록을 포함한 리스트
     */
    public List<Map<String, Object>> getVersionList(Map<String, String> payload) {
        String query = "select distinct cc.version, cc.id from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 " +
                       "and c.type_id = :typeId and c.coverage = :coverage and c.countries LIKE '%'||:countries||'%' and cc.language_id = :language and cc.status_id ='published' ORDER BY cc.version";

        List<Object[]> results = entityManager.createNativeQuery(query)
                                .setParameter("typeId", payload.get("typeId"))
                                .setParameter("coverage", payload.get("coverage"))
                                .setParameter("countries", payload.get("countries"))
                                .setParameter("language", payload.get("language"))
                                .getResultList();
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("version", result[0]);
            termsMap.put("id", result[1]);
            mappedResults.add(termsMap);
        }

        return mappedResults;
    }

    /*
     * 1. 메소드명: getConsentContent
     * 2. 클래스명: ConsentService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관의 특정 콘텐츠를 조회하는 메소드로, 주어진 ID에 따라 콘텐츠를 반환.
     * 2. 사용법
     *    payload에 약관 콘텐츠 ID를 전달하여 해당 콘텐츠를 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "id": "123" }
     *    - Output: "약관 내용 텍스트" 형태의 문자열
     * </PRE>
     *
     * @param payload 약관 콘텐츠 ID가 포함된 검색 파라미터 맵
     * @return 약관 콘텐츠 문자열
     */
    public String getConsentContent(Map<String, String> payload) {
        return consentContentRepository.findContentById(Long.parseLong(payload.get("id")));
    }
}