package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import com.samsung.ciam.utils.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CdcCallService {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ConsentContentRepository consentContentRepository;
    
    @Value("${server}")
    private String serverName;
 
    public String updateConsentContentPublished(String testDate) {
        log.warn("serverName : {}", serverName);
        
        String msg;
        // consent manager > published 상태값 변경 예정
        try {
            // 호스트 이름을 기반으로 IP 주소를 가져옴
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            List<ConsentContent> consentContentList;
            if(testDate != null) {
                // 1. 해당 날짜(yyyy-mm-dd)에 (배포대기 상태인) scheduled 데이터 개수만큼 반복
                consentContentList = consentContentRepository.listConsentContentStatusIdPublished(testDate);
            } else {
                // 1. 오늘 날짜에 (배포대기 상태인) scheduled 데이터 개수만큼 반복
                consentContentList = consentContentRepository.listConsentContentStatusIdPublished();
            }
            
            int cnt = consentContentList.size();
            for (int i=0;i<consentContentList.size();i++) {
                Long id = consentContentList.get(i).getId();
                Long consentId = consentContentList.get(i).getConsentId();
                String cdcConsentId = consentContentRepository.getCdcConsentId(consentId);
                String language_id = consentContentList.get(i).getLanguageId();
                Integer version = (int) Double.parseDouble(consentContentList.get(i).getVersion().toString());
                String purpose = consentContentList.get(i).getPurpose();


                // 2-1. 신규 여부 확인
                String newYn = consentContentRepository.newConsentContentStatusIdPublished(consentId);
                // 2-2. 기존 consentId의 published는 historic으로 변경
                consentContentRepository.updateConsentContentStatusIdHistoric(consentId);
                // 2-3. published로 변경
                consentContentRepository.updateConsentContentStatusIdPublished(id);

                // log.warn("temporary cdc don't call");
                if(!serverName.equals("local")) {
                    log.warn("cdc call");
                    if(newYn.equals("y")) {
                        // 3-1-1. accounts.setConsentsStatements : langs, isMandatory, writeAccess
                        setConsentsStatements(cdcConsentId, language_id);
                        // 3-1-2. accounts.setConsentDefaultLang : consentId, lang
                        setConsentDefaultLang(cdcConsentId, language_id);
                        // 3-1-3. accounts.setLegalStatements : consentId, lang, legalStatements
                        setLegalStatements(cdcConsentId, language_id, version, purpose);
                    } else {
                        // 3-2-1. accounts.setConsentsStatements : langs, isMandatory, writeAccess
                        setConsentsStatements(cdcConsentId, language_id);
                        // 3-2-2. accounts.setLegalStatements : consentId, lang, legalStatements
                        setLegalStatements(cdcConsentId, language_id, version, purpose);
                    }
                } else {
                    log.warn("cdc don't call");
                }
            }     
            
            if (cnt > 0) {
                msg = "published count : "+cnt;
            } else {
                msg = "There is no data available for publish today";
            }
        } catch (Exception e) {
            msg = "An error occurred.\nError message : "+e.toString();
        }
        
        return msg;
    }

    public void setConsentsStatements(String cdcConsentId, String language_id) {
        // String loginUID = "47835cb8b053490294937e333ad5ceb3";
        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        // cdcParams.put("UID", loginUID);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 데이터 필드 설정
            Map<String, Object> dataFields = Map.of(
                cdcConsentId, Map.of(
                    "langs", new String[] { language_id },
                    "isMandatory", "TRUE",
                    "writeAccess", "serverOnly"
                )
            );
            String dataJson = objectMapper.writeValueAsString(dataFields);
            cdcParams.put("preferences", dataJson);

            log.info("Setting account ID in CDC: {}", dataJson);

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.setConsentsStatements", cdcParams);

            log.info("Response from CDC: {}", response.getResponseText());

            if (response.getErrorCode() == 0) {
                log.info("Set account ID response: {}", response.getResponseText());
            } else {
                String msg = "Could not set account ID. Response: " + response.getResponseText();
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("Error updating account ID in CDC: {}", e.getMessage());
            throw new RuntimeException("Error updating account ID in CDC", e);
        }
    }

    // 작업완료
    public void setConsentDefaultLang(String cdcConsentId, String language_id) {
        // String loginUID = "47835cb8b053490294937e333ad5ceb3";
        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        // cdcParams.put("UID", loginUID);
        
        // 데이터 필드 설정
        Map<String, String> dataFields = new HashMap<>();
        // dataFields.put("accountID", accountId);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String dataJson = objectMapper.writeValueAsString(dataFields);
            cdcParams.put("consentId", cdcConsentId);
            cdcParams.put("lang", language_id);

            log.info("Setting account ID in CDC: {}", dataJson);

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.setConsentDefaultLang", cdcParams);

            log.info("Response from CDC: {}", response.getResponseText());

            if (response.getErrorCode() == 0) {
                log.info("Set account ID response: {}", response.getResponseText());
            } else {
                String msg = "Could not set account ID. Response: " + response.getResponseText();
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("Error updating account ID in CDC: {}", e.getMessage());
            throw new RuntimeException("Error updating account ID in CDC", e);
        }
    }

    // 작업중
    public void setLegalStatements(String cdcConsentId, String language_id, Integer version, String purpose) {
        // String loginUID = "47835cb8b053490294937e333ad5ceb3";
        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        // cdcParams.put("UID", loginUID);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            cdcParams.put("consentId", cdcConsentId);
            cdcParams.put("lang", language_id);
            Map<String, Object> dataFields = Map.of(
                "currentDocVersion", version,
                "mindocVersion", version,
                "versions", Map.of(
                    version, Map.of(
                        "purpose", purpose
                    )
                )
            );
            String dataJson = objectMapper.writeValueAsString(dataFields);
            cdcParams.put("legalStatements", dataJson);

            log.info("Setting account ID in CDC: {}", dataJson);

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.setLegalStatements", cdcParams);

            log.info("Response from CDC: {}", response.getResponseText());

            if (response.getErrorCode() == 0) {
                log.info("Set account ID response: {}", response.getResponseText());
            } else {
                String msg = "Could not set account ID. Response: " + response.getResponseText();
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("Error updating account ID in CDC: {}", e.getMessage());
            throw new RuntimeException("Error updating account ID in CDC", e);
        }
    }
}