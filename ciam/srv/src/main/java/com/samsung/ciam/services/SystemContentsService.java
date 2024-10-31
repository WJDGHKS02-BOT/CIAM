package com.samsung.ciam.services;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.samsung.ciam.models.ConsentJConsentContents;
import com.samsung.ciam.repositories.ConsentContentRepository;
import com.samsung.ciam.repositories.ConsentJConsentContentsRepository;
import com.samsung.ciam.repositories.ConsentRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class SystemContentsService {
    
    @Autowired
    public ConsentJConsentContentsRepository consentJConsentContentsRepository;

    @Autowired
    public UserProfileService userProfileService;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    public List<Map<String,Object>> searchConsentManagement(Map<String, String> payload, HttpSession session) {
        return userProfileService.getConsentManagerList(payload.get("channel"), payload.get("consentType"), payload.get("location"));
    }

    public ModelAndView insertConsentManagement(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("myPage");
        String language = payload.get("language");
        String content = payload.get("content");
        String statusId = payload.get("statusId");
        String uid = (String) session.getAttribute("cdc_uid");
        String purpose = payload.get("purpose");
        int consentId = Integer.parseInt(payload.get("consentId"));
        int version = Integer.parseInt(payload.get("version"));
        
        java.sql.Timestamp releasedAt = null;
        if (!statusId.equals("draft") && payload.get("releasedAt") != null && !payload.get("releasedAt").equals("undefined")) {
           String dateStr = payload.get("releasedAt");
           SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
           Date parsedDate = dateFormat.parse(dateStr);
           releasedAt = new java.sql.Timestamp(parsedDate.getTime());
        }

        String groupId = payload.get("consentGroup");
        // 그룹명 있으면 관련 그룹 데이터 insert
        if (groupId != null && !groupId.equals("")) {
            consentContentRepository.insertConsentContentManagement(version, statusId, purpose, content, uid, groupId, releasedAt);
        } else {
            // 그룹명 없으면 해당 데이터 1건 insert (add New Language도 동일)
            consentContentRepository.insertConsentContentManagement(language, version, statusId, purpose, content, uid, consentId, releasedAt);
        }

        modelAndView.addObject("content", "fragments/myPage/consentManager");
        return modelAndView;
    }

    public void updateConsentManagement(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request) throws ParseException {
        // ModelAndView modelAndView = new ModelAndView("myPage");
        String language = payload.get("language");
        String content = payload.get("content");
        String statusId = payload.get("statusId");
        String uid = (String) session.getAttribute("cdc_uid");
        String groupId = payload.get("consentGroup");
        int id = Integer.parseInt(payload.get("id"));
        int consentId = Integer.parseInt(payload.get("consentId"));
        int version = Integer.parseInt(payload.get("version"));
        int currentVersion;
        if (payload.get("currentVersion") != null && !payload.get("currentVersion").equals("undefined")) {
            currentVersion = Integer.parseInt(payload.get("currentVersion"));
        } else {
            currentVersion = version; // published인 데이터를 Modify 하지 않는 경우에는 else로 정상작동
        }

        log.warn("statusId ::: {}", statusId);

        java.sql.Timestamp releasedAt = null;
        if (statusId.equals("scheduled")) {
            log.warn("scheduled consentId : {}", consentId);

            String dateStr = payload.get("releasedAt");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(dateStr);
            releasedAt = new java.sql.Timestamp(parsedDate.getTime());
        }
        // 그룹명 있으면 관련 그룹 내용 update 및 상태값 draft로 변경
        if (groupId != null && !groupId.equals("")) {
            consentContentRepository.updateConsentContentStatusIdByGroupId(groupId, statusId, content, version, releasedAt, currentVersion);
        } else {
            // 그룹명 없으면 해당 데이터 1건 update 및 상태값 draft로 변경
            // consents 저장은 이미 이전 팝업화면에서 처리됨
            // consent_contents 저장 (language, version, content 변경된 insert);
            consentContentRepository.updateConsentContentStatusIdById(id, statusId, content, version, releasedAt);
        }
    }

    public Map<String, Object> duplicationConsentCheck(Map<String, String> payload, HttpSession session,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, Object> result = new HashMap<>();
        
        String consentGroup = payload.get("consentGroup");
        String type = payload.get("consentType");
        String channel = payload.get("channel");
        String country = payload.get("location");
        String subsidiary = payload.get("subsidiary");
        String uid = (String) session.getAttribute("cdc_uid");	
        // String duplicationConsentCheck = consentRepository.duplicationConsentCheck(channel, type, country, subsidiary);

        // regionId는 최대 5글자
        // 1. 데이터 중복 체크
        // 2. 데이터 있으면 "duplication", 없으면 데이터 추가하면서 "ok" 문구 리턴
        if(consentGroup != null && !consentGroup.equals("")) {
            // groupId 있을때는 groupId 기준으로 체크
            if (consentContentRepository.duplicationConsentContentCheck(consentGroup).equals("y")) {
                // consentGroup 기준으로 데이터가 이미 있으니까 중복 메시지를 화면에서 출력
                result.put("status", "consentGroupDuplication");
            } else {
                result.put("status", "ok");
            }
        } else {
            // groupId 없을때는 나머지 4개로 체크
            if(consentRepository.duplicationConsentCheck(channel, type, country, subsidiary).equals("n")){
                // ok이면 데이터 생성 이후에 다음 페이지로 이동
                // 1. 데이터 생성
                consentRepository.insertConsentsManagement(type, uid, payload.get("cdcConsentId"), payload.get("regionId"), channel, null, country, "en", null, subsidiary);
                // 2. Create 페이지로 이동
                result.put("status", "ok");
            } else if(consentRepository.duplicationConsentCheck(channel, type, country, subsidiary).equals("y") 
                        && consentContentRepository.duplicationConsentContentCheck(channel, type, country, subsidiary).equals("n")){
                // consents 데이터는 있지만 consent_contnets 데이터는 없으면 Create 페이지로 이동
                result.put("status", "ok");
            } else {
                // 중복 메시지를 화면에서 출력
                result.put("status", "duplication");
            }
        }
        return result;
    }

    private ZonedDateTime getTimeInKorea(){
        // 현재 UTC 시간 가져오기
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);

        // 9시간 더하기
        ZonedDateTime timeInKorea = utcNow.plus(Duration.ofHours(9));

        return timeInKorea;
    }
}
