package com.samsung.ciam.controllers;

import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.services.TempWFCreateService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.SecureRandom;
import java.util.*;

/**
 * 1. 파일명   : ApprovalTestController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 승인 테스트 페이지에 대한 컨트롤러로, 승인 테스트 페이지를 보여주고 워크플로우를 생성하여 승인 테스트 결과를 반환
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜        | 이름          | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@Profile({"local", "dev","prod"})
public class ApprovalTestController {

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @Autowired
    private CisCountryRepository cisCountryRepository;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    @Autowired
    private TempWFCreateService wfCreateService;

    @Autowired
    private TempWfMasterRepository wfMasterRepository;

    @Autowired
    private TempWfListRepository wfListRepository;

    @Autowired
    private ApprovalRuleRepository approvalRuleRepository;

    @Autowired
    private ApprovalRuleMasterRepository approvalRuleMasterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * 1. 메소드명: getApprovalTest
     * 2. 클래스명: ApprovalTestController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인 테스트 페이지를 반환
     * 2. 사용법
     *    GET 요청으로 승인 테스트 페이지를 로드
     * 3. 예시 데이터
     *    - Input: /approvalTest
     *    - Output: 승인 테스트 페이지
     * </PRE>
     * @param servletRequest 서블릿 요청 객체
     * @param request HTTP 요청 객체
     * @param model 모델 객체
     * @return 승인 테스트 페이지를 위한 ModelAndView 객체
     */
    @GetMapping("/approvalTest")
    public ModelAndView getApprovalTest(
            ServletRequest servletRequest,
            HttpServletRequest request,
            Model model
    ) {

        List<CisCountry> location = cisCountryRepository.findAllOrderedByNameEn();
        List<Channels> channels = channelRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("approvalTest");
        modelAndView.addObject("channels", channels);
        modelAndView.addObject("requestTypes", commonCodeRepository.findByHeaderOrderBySortOrder("REQUEST_TYPE_CODE"));
        modelAndView.addObject("location", location);
        modelAndView.addObject("subsidiary", newSubsidiaryRepository.findByCompanyAbbreviationOrderByIdAsc());
        modelAndView.addObject("divisions", commonCodeRepository.findByHeader("DIVISION_CODE"));

        return modelAndView;
    }

    /*
     * 1. 메소드명: postApprovalTest
     * 2. 클래스명: ApprovalTestController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    워크플로우를 생성하고 승인 테스트 결과 페이지를 반환
     * 2. 사용법
     *    POST 요청으로 워크플로우 생성
     * 3. 예시 데이터
     *    - Input: 승인 요청 데이터 (Map)
     *    - Output: 승인 테스트 결과 페이지
     * </PRE>
     * @param servletRequest 서블릿 요청 객체
     * @param request HTTP 요청 객체
     * @param payload 승인 요청에 필요한 파라미터 맵
     * @param model 모델 객체
     * @return 승인 테스트 결과 페이지를 위한 ModelAndView 객체
     */
    @PostMapping("/approvalTest")
    public ModelAndView postApprovalTest(
            ServletRequest servletRequest,
            HttpServletRequest request,
            @RequestParam Map<String, String> payload,
            Model model
    ) {
        String wf_id = "";
        Map<String,String> wf_param = new HashMap<String,String>();

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder randomUid = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(characters.length());
            randomUid.append(characters.charAt(index));
        }

        // requestor_email이 없거나 빈 값일 경우 이메일 생성 로직
        String email = payload.getOrDefault("email", "");
        if (email.isEmpty()) {
            int randomNum = random.nextInt(900000) + 100000; // 6자리 랜덤 숫자 생성
            email = "test" + randomNum + "@yopmail.com";
        }

        // bpId가 없거나 빈 값일 경우 기본값 설정
        String bpId = payload.getOrDefault("bpId", "");
        if (bpId.isEmpty()) {
            bpId = "A020147124";  // 기본 bpId 설정
        }

        String wf_code = payload.get("requestType");

        wf_param.put("workflow_code",wf_code);
        wf_param.put("channel",payload.get("channel"));
        wf_param.put("country",payload.get("location"));
        wf_param.put("subsidiary",payload.get("subsidiary"));
        wf_param.put("division",payload.get("division"));
        wf_param.put("requestor_email",email);
        wf_param.put("bpid", bpId);  // 조건에 따른 bpId 설정
        wf_param.put("requestor_uid", randomUid.toString());
        wf_param.put("bp_name","test");

        if("W06".equals(wf_code)) {
            String reg_channel = "SBA";
            String target_channel =payload.get("channel");
            String target_channeltype = "CUSTOMER";

            wf_param.put("reg_channel",reg_channel);
            wf_param.put("target_channel",target_channel);
            wf_param.put("target_channeltype",target_channeltype);
        }

        if("W03".equals(wf_code)) {
            String invitaion_sender_id = "";
            String invitaion_sender_email = "";
            Object invitationIdObj = "test";

            wf_param.put("invitaion_sender_id","testUID");
            wf_param.put("invitaion_sender_email","test@yopmail.com");
        }

        wf_id = wfCreateService.wfCreate(wf_param) ;

        TempWfMaster wmaster = wfMasterRepository.selectWfMaster(wf_id);
        List<TempWfList> wfList = wfListRepository.selectWfList(wf_id);

        List<ApprovalRuleMaster> approvalRuleMasterList = approvalRuleMasterRepository.selectApprovalRuleMasterList(wmaster.getRuleMasterId());

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT id, rule_master_id, rule_level, approve_format, approve_condition, status, role, created_at, updated_at ")
                .append("FROM approval_rule ")
                .append("WHERE rule_master_id = :ruleMasterId");

        // 네이티브 쿼리 실행 및 결과 리스트로 받기
        List<Object[]> results = entityManager.createNativeQuery(queryBuilder.toString())
                .setParameter("ruleMasterId", wmaster.getRuleMasterId())
                .getResultList();

        // 결과 처리 및 객체 리스트에 추가
        List<Map<String, Object>> approvalRuleList = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", result[0]);
            resultMap.put("ruleMasterId", result[1]);
            resultMap.put("ruleLevel", result[2]);
            resultMap.put("approveFormat", result[3]);
            resultMap.put("approveCondition", result[4]);
            resultMap.put("status", result[5]);
            resultMap.put("role", result[6]);
            resultMap.put("createdAt", result[7]);
            resultMap.put("updatedAt", result[8]);
            approvalRuleList.add(resultMap);
        }

        // ModelAndView 객체 생성
        ModelAndView modelAndView = new ModelAndView("approvalTestResult");

        // 조회된 데이터를 Model에 추가하여 Thymeleaf에서 사용 가능하게 설정
        modelAndView.addObject("wmaster", wmaster); // TempWfMaster 객체를 전달
        modelAndView.addObject("wfList", wfList);   // WfList 목록을 전달
        modelAndView.addObject("approvalRuleMasterList", approvalRuleMasterList);
        modelAndView.addObject("approvalRuleList", approvalRuleList);

        return modelAndView;
    }
}
