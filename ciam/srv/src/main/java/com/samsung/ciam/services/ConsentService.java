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

    public List<Map<String, Object>> getConsentTypeList(Map<String, String> payload) {
        String query = "select distinct c.type_id, (select name_en from consent_types where id = c.type_id) as name from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 and c.coverage = :coverage and c.type_id is not null";

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

    public List<String> getLanguageList(Map<String, String> payload) {
        return consentRepository.getLanguageList(payload.get("typeId"), payload.get("coverage"), payload.get("countries"));
    }

    public List<Map<String, Object>> getVersionList(Map<String, String> payload) {
        String query = "select distinct cc.version, cc.id from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 " +
                       "and c.type_id = :typeId and c.coverage = :coverage and c.countries LIKE '%'||:countries||'%' and cc.language_id = :language ORDER BY cc.version";

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

    public String getConsentContent(Map<String, String> payload) {
        return consentContentRepository.findContentById(Long.parseLong(payload.get("id")));
    }
}