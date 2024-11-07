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

/**
 * 1. FileName   : SystemContentsService.java
 * 2. Package    : com.samsung.ciam.services
 * 3. Comments   : 약관 관련된 데이터 관리 서비스로서, 약관 조회, 삽입, 수정, 중복 확인 등을 수행함.
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
public class SystemContentsService {
    
    @Autowired
    public ConsentJConsentContentsRepository consentJConsentContentsRepository;

    @Autowired
    public UserProfileService userProfileService;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    /*
     * 1. 메소드명: searchConsentManagement
     * 2. 클래스명: SystemContentsService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 관리 데이터를 조회하는 메소드로, 사용자 프로필 정보를 통해 약관 목록을 반환.
     * 2. 사용법
     *    payload에서 채널, 약관 정보를 전달하여 해당 동의 목록을 조회.
     * 3. 예시 데이터
     *    - Input: payload = { "channel": "samsung", "consentType": "general", "location": "KR" }
     *    - Output: 해당 조건에 맞는 약관 목록
     * </PRE>
     *
     * @param payload 검색에 필요한 파라미터 맵
     * @param session 현재 HTTP 세션
     * @return 약관 목록을 포함하는 리스트
     */
    public List<Map<String,Object>> searchConsentManagement(Map<String, String> payload, HttpSession session) {
        return userProfileService.getConsentManagerList(payload.get("channel"), payload.get("consentType"), payload.get("location"));
    }

    /*
     * 1. 메소드명: insertConsentManagement
     * 2. 클래스명: SystemContentsService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 관리 데이터 삽입 메소드로, 약관 내용을 데이터베이스에 저장.
     * 2. 사용법
     *    payload로 약관 정보를 전달받아 그룹 데이터가 있을 경우 관련 그룹 데이터를 삽입하며,
     *    그룹이 없을 경우 새로운 동의 데이터를 삽입.
     * 3. 예시 데이터
     *    - Input: payload = { "language": "ko", "content": "내용", "statusId": "active", ... }
     *    - Output: 약관 목록이 포함된 ModelAndView 객체
     * </PRE>
     *
     * @param payload 삽입할 약관 데이터
     * @param session 현재 HTTP 세션
     * @param request HTTP 요청 객체
     * @return ModelAndView 삽입 후 이동할 페이지 정보
     * @throws ParseException 날짜 파싱 오류 발생 시
     */
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

    /*
     * 1. 메소드명: updateConsentManagement
     * 2. 클래스명: SystemContentsService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 데이터를 수정하는 메소드로, 주어진 데이터에 따라 상태 및 내용 업데이트.
     * 2. 사용법
     *    payload에 있는 데이터를 통해 약관내용을 갱신하며, 그룹 ID가 있을 경우 그룹 관련 데이터를 모두 업데이트.
     * 3. 예시 데이터
     *    - Input: payload = { "language": "en", "content": "updated content", "statusId": "active", ... }
     *    - Output: 약관 데이터 업데이트
     * </PRE>
     *
     * @param payload 업데이트할 약관 데이터
     * @param session 현재 HTTP 세션
     * @param request HTTP 요청 객체
     * @throws ParseException 날짜 파싱 오류 발생 시
     */
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

    /*
     * 1. 메소드명: duplicationConsentCheck
     * 2. 클래스명: SystemContentsService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    약관 데이터의 중복 여부를 확인하여 결과를 반환하는 메소드.
     * 2. 사용법
     *    payload를 통해 전달된 데이터를 바탕으로 중복 체크 후, 상태 메시지를 반환.
     * 3. 예시 데이터
     *    - Input: payload = { "consentGroup": "group1", "consentType": "type1", ... }
     *    - Output: { "status": "duplication" } (중복 시) 또는 { "status": "ok" } (중복 아님)
     * </PRE>
     *
     * @param payload 중복 확인할 약관 데이터
     * @param session 현재 HTTP 세션
     * @param request HTTP 요청 객체
     * @param redirectAttributes 리다이렉트 시 사용할 속성 객체
     * @return 중복 여부를 나타내는 상태 메시지
     */
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

    /*
     * 1. 메소드명: getTimeInKorea
     * 2. 클래스명: SystemContentsService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    현재 한국 시간 (KST)을 반환하는 메소드.
     * 2. 사용법
     *    한국 시간대(KST)를 기준으로 현재 시간을 가져옴.
     * 3. 예시 데이터
     *    - Output: ZonedDateTime.now(ZoneOffset.UTC).plus(Duration.ofHours(9))
     * </PRE>
     *
     * @return 현재 한국 시간 (KST)
     */
    private ZonedDateTime getTimeInKorea(){
        // 현재 UTC 시간 가져오기
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);

        // 9시간 더하기
        ZonedDateTime timeInKorea = utcNow.plus(Duration.ofHours(9));

        return timeInKorea;
    }
}
