package com.samsung.ciam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.MenuAccessControlRepository;
import com.samsung.ciam.services.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.repositories.BtpAccountsRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 1. 파일명   : UserProfileController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 마이페이지 관련 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@RequestMapping("/myPage")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private CompanyProfileService companyProfileService;

    @Autowired
    private EmpVerificationService empVerificationService;

    @Autowired
    private ApprovalListService approvalListService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private BtpAccountsRepository btpAccountsRepository;

    @Autowired
    private ConsentProfileService consentProfileService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MenuAccessControlRepository menuAccessControlRepository;


    /*
     * 1. 메소드명: channelHeader
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		공통적으로 필요한 로그인한 채널 설정 모델 저장
     * 2. 사용법
     *
     * </PRE>
     *   @param session
     *   @return Map<String,String>
     */
    @ModelAttribute("channelHeader")
    public Map<String,String> channelHeader(HttpSession session) {
        Map<String,String> channelHeader = new HashMap<String,String>();
        channelHeader.put("channelName",(String) session.getAttribute("session_channel"));
        channelHeader.put("channelDisplayName",(String) session.getAttribute("session_display_channel"));
        // channelHeader.put("myRole",(String) session.getAttribute("btp_myrole"));         // 실제 유저의 권한
        channelHeader.put("myRole", (String) session.getAttribute("btp_myrole"));              // 개발 및 테스트를 위해 'CIAM Admin'으로 고정
        channelHeader.put("loginUserId",(String) session.getAttribute("cdc_email"));
        channelHeader.put("samsungAdYn",(String) session.getAttribute("samsungAdYn"));

//        if(session.getAttribute("isMultiChannel")!=null && "Y".equals(session.getAttribute("isMultiChannel"))) {
//            channelHeader.put("isMultiChannel",(String) session.getAttribute("isMultiChannel"));
//        } else {
//            channelHeader.put("isMultiChannel","N");
//        }

        return channelHeader;
    }

    /*
     * 1. 메소드명: accessibleMenus
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		메뉴권한 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param session
     *   @return List<MenuAccessControl>
     */
    @ModelAttribute("accessibleMenus")
    public List<MenuAccessControl> accessibleMenus(HttpSession session) {
        // 세션에서 역할을 가져옵니다.
        String role = (String) session.getAttribute("btp_myrole");
        String channel = (String) session.getAttribute("session_channel");

        // 역할에 맞는 메뉴 권한을 DB에서 조회
        return menuAccessControlRepository.selectByRole(role,channel);
    }

    /*
     * 1. 메소드명: personalInformation
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-personalInformation 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    // 24.07.22 홍정인 추가 - 마이페이지 Direct 접근 url
    @GetMapping("/personalInformation")
    public ModelAndView personalInformation(HttpServletRequest request, HttpSession session) {
        return userProfileService.personalInformation(request,session );
    }

    /*
     * 1. 메소드명: companyProfile
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-companyProfile(회사정보)메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request , HttpSession session
     *   @return ModelAndView
     */
    // 24.07.22 홍정인 추가 - 마이페이지 Direct 접근 url
    @GetMapping("/companyProfile")
    public ModelAndView companyProfile(HttpServletRequest request, HttpSession session) {
        return companyProfileService.companyInformation(request,session);
    }

    /*
     * 1. 메소드명: passwordReset
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-passwordReset(패스워드 변경)메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    // 24.07.25 홍정인 추가 - 마이페이지 - 패스워드 초기화 Direct 접근 url
    @GetMapping("/passwordReset")
    public ModelAndView passwordReset(HttpServletRequest request, HttpSession session) {
        return companyProfileService.passwordReset(request,session);
    }

    /*
     * 1. 메소드명: checkOldPassword
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		패스워드 변경 시 기존 패스워드 값 알맞은지 확인
     * 2. 사용법
     *
     * </PRE>
     *   @param  payload, HttpSession session
     *   @return Map<String, Object>
     */
    //24.07.29 kimjy PW 확인
    @ResponseBody
    @PostMapping("/checkOldPassword")
    public Map<String, Object> checkOldPassword(@RequestBody Map<String, String> payload,  HttpSession session) {
        return companyProfileService.checkOldPassword(payload, session);
    }

    /*
     * 1. 메소드명: changePasswordSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		패스워드 변경
     * 2. 사용법
     *
     * </PRE>
     *   @param payload, HttpSession session,HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
     //24.07.29 kimjy PW 변경
    @PostMapping("/changePasswordSubmit")
    public RedirectView changePasswordSubmit(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return companyProfileService.changePasswordSubmit(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: mfaSetting
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-MfaSetting 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    // 24.07.25 홍정인 추가 - 마이페이지 - MFA Setting Direct 접근 url
    @GetMapping("/mfaSetting")
    public ModelAndView mfaSetting(HttpServletRequest request, HttpSession session) {
        return companyProfileService.mfaSetting(request,session);
    }

    /*
     * 1. 메소드명: mfaSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		mfa 인증방식 변경
     * 2. 사용법
     *
     * </PRE>
     *   @param payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/mfaSubmit")
    public RedirectView mfaSubmit(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return companyProfileService.mfaSubmit(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: withdrawUser
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 홍정인
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-withdrawUser(회원탈퇴) 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    // 24.07.25 홍정인 추가 - 마이페이지 - 회원탈퇴 Direct 접근 url
    @GetMapping("/withdrawUser")
    public ModelAndView withdrawUser(HttpServletRequest request, HttpSession session) {
        return companyProfileService.withdrawUser(request,session);
    }

    /*
     * 1. 메소드명: savePersonalInformation
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지 personalInformation 메뉴 회원정보 수정
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    // 24.07.29 한국민 추가
    @PostMapping("/savePersonalInformation")
    public RedirectView savePersonalInformation(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return userProfileService.savePersonalInformation(payload,session,request,redirectAttributes);
    }

    /*
     * 1. 메소드명: ssoAccess
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-ssoAccess 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    @GetMapping("/ssoAccess")
    public ModelAndView ssoAccess(HttpServletRequest request, HttpSession session) {

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/ssoAccess";
        String menu = "ssoAccess";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("channels", session.getAttribute("session_display_channel"));
        modelAndView.addObject("channel_displayName", session.getAttribute("session_display_channel"));

        modelAndView.addObject("addChannels", "");
        modelAndView.addObject("deleteChannels", "");

        return modelAndView;
    }

    /*
     * 1. 메소드명: getAccessRedirect
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		SSOAccess-채널 acsURL로 ACCESS 이동
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam String channelName
     *   @return Map<String, String>
     */
    @GetMapping("/getAccessRedirect")
    @ResponseBody
    public Map<String, String> getAccessRedirect(@RequestParam String channelName) {
        // 필요한 로직으로 URL 생성 또는 조회
        String specificUrl = BeansUtil.getApplicationProperty("acsUrl.channels." + channelName+".url");

        Map<String, String> response = new HashMap<>();
        response.put("url", specificUrl);
        return response;
    }

    /*
     * 1. 메소드명: ssoAccessSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		SSOAccess-채널 확장
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam("requestType") String requestType,
     *                                         @RequestParam("targetChannel") String targetChannel,
     *                                         @RequestParam("channels") String channels, HttpSession session
     *   @return RedirectView
     */
    @PostMapping("/ssoAccessSubmit")
    public RedirectView ssoAccessSubmit(@RequestParam("requestType") String requestType,
                                        @RequestParam("targetChannel") String targetChannel,
                                        @RequestParam("channels") String channels, HttpSession session) {
        // 로직 처리 및 리다이렉트
        String uid = (String) session.getAttribute("cdc_uid");

        JsonNode CDCUserProfile = cdcTraitService.getCdcUser(uid, 0);
        String country = CDCUserProfile.path("profile").path("country").asText(null);
        String loginUserId = CDCUserProfile.path("profile").path("email").asText(null);
        String bpId = CDCUserProfile.path("data").path("accountID").asText(null);
        String vendorcode = CDCUserProfile.path("data").path("vendorCode").asText(null);

        String salutation = CDCUserProfile.path("salutation").asText(null);
        String language = CDCUserProfile.path("profile").path("locale").asText(null);
        language = "en".equals(language) ? "en_US" : "ko".equals(language) ? "ko_KR" : language;
        String tcpp_language = CDCUserProfile.path("profile").path("languages").asText("");
        tcpp_language = "en_US".equals(tcpp_language) ? "en" : "ko_KR".equals(tcpp_language) ? "ko" : tcpp_language;
        String firstName = CDCUserProfile.path("profile").path("firstName").asText(null);
        String lastName = CDCUserProfile.path("profile").path("lastName").asText(null);
        JsonNode phonesNode = CDCUserProfile.path("profile").path("phones");
        String workPhone = null;
        if (phonesNode.isArray()) {
            for (JsonNode phoneNode : phonesNode) {
                if ("work_phone".equals(phoneNode.path("type").asText())) {
                    workPhone = phoneNode.path("number").asText(null);
                    break;
                }
            }
        }

        // 첫 번째 부분에서 "+" 기호를 제거하고 숫자만 남김
        String countryCode = "";
        String phoneNumber = "";
        if (workPhone != null) {
            String[] parts = workPhone.split(" ");

            // 국가 코드와 전화번호 분리
            for (String part : parts) {
                if (part.startsWith("+")) {
                    countryCode = part.replace("+", "").replaceAll("\\D", "");
                } else if (!part.isEmpty()) {
                    phoneNumber = part;
                }
            }
        }

        String secDept = CDCUserProfile.path("data").path("userDepartment").asText(null);
        String job_title = CDCUserProfile.path("data").path("jobtitle").asText(null);

        session.setAttribute("country",country);
        session.setAttribute("loginUserId",loginUserId);
        session.setAttribute("ssoAccessYn","Y");

        JsonNode companyNode = cdcTraitService.getB2bOrg(bpId);
        JsonNode infoNode = companyNode.path("info");

        // companyObject 설정
        Map<String, String> companyObject = new HashMap<>();
        companyObject.put("bpid", companyNode.path("bpid").asText(""));
        companyObject.put("source", companyNode.path("source").asText(""));
        companyObject.put("type", companyNode.path("type").asText(""));
        companyObject.put("validStatus", companyNode.path("status").asText(""));
        companyObject.put("zip_code", infoNode.path("zip_code").asText(""));
        companyObject.put("vendorcode", companyNode.path("bpid").asText(""));  // vendorcode와 bpid가 동일

        Map<String, String> registerCompany = new HashMap<>();


        registerCompany.put("name", companyNode.path("orgName").asText(""));
        String extractedCountry = infoNode.has("country") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("country")) : "";
        registerCompany.put("country", !extractedCountry.isEmpty() ? extractedCountry : country);
        registerCompany.put("state", infoNode.has("state") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("state")) : "");
        registerCompany.put("city", infoNode.has("city") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("city")) : "");
        registerCompany.put("street_address", infoNode.has("street_address") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("street_address")) : "");
        registerCompany.put("phonenumber1", infoNode.has("phonenumber1") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("phonenumber1")) : "");
        registerCompany.put("fax", infoNode.has("fax") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("fax")) : "");
        registerCompany.put("email", companyNode.path("email").asText(""));
        registerCompany.put("bizregno1", infoNode.has("bizregno1") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("bizregno1")) : "");
        registerCompany.put("representative", infoNode.has("representative") ? cdcTraitService.extractFirstNonEmptyValue(infoNode.get("representative")) : "");

        // session에 설정된 데이터를 추가
        session.setAttribute("registerCompany", registerCompany);
        session.setAttribute("companyObject", companyObject);

        //companyData Setting
        Map<String,String> accountObject = new HashMap<String,String>();

        accountObject.put("salutation",salutation);
        accountObject.put("language",language);
        accountObject.put("firstName",firstName);
        accountObject.put("lastName",lastName);

        accountObject.put("country_code_work",countryCode);
        accountObject.put("work_phone",phoneNumber);
        accountObject.put("secDept",secDept);
        accountObject.put("job_title",job_title);
        session.setAttribute("accountObject", accountObject);
        session.setAttribute("tcpp_language",tcpp_language);

        String redirectUrl = "/registration/"+ targetChannel;
        return new RedirectView(redirectUrl);
    }

    /*
     * 1. 메소드명: ssoAccessDelete
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		SSOAccess-채널 삭제
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestBody Map<String, String> payload, HttpSession session
     *   @return String
     */
    @ResponseBody
    @PostMapping("/ssoAccessDelete")
    public String ssoAccessDelete(@RequestBody Map<String, String> payload, HttpSession session) {
        String targetChannel = payload.get("targetChannel");
        return userProfileService.deleteSsoAccess(targetChannel,session);
    }

    /*
     * 1. 메소드명: selectChannel
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지 채널 다중 보유시 채널선택 화면 VIEW
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session
     *   @return ModelAndView
     */
    // 24.08.01 홍정인 추가 - 마이페이지 - Select Channel Direct 접근 url
    @GetMapping("/selectChannel")
    public ModelAndView selectChannel(HttpServletRequest request, HttpSession session) {
        return companyProfileService.selectChannel(request,session);
    }

    /*
     * 1. 메소드명: deleteWithdrawUser
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		CDC 회원탈퇴 진행
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    // 24.08.01 한국민 추가
    @PostMapping("/deleteWithdrawUser")
    public RedirectView deleteWithdrawUser(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return userProfileService.deleteWithdrawUser(payload,session,request,redirectAttributes);
    }

    /*
     * 1. 메소드명: login
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		CDC 로그인
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestBody Map<String, String> payload,HttpSession session
     *   @return Map<String,String>
     */
    @ResponseBody
    @PostMapping("/login")
    public Map<String,String> login(@RequestBody Map<String, String> payload,HttpSession session) {
        return userProfileService.login(payload,session);
    }

    /*
     * 1. 메소드명: sendEmailCode
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		이메일 인증 발송
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestBody Map<String, String> payload,HttpSession session,RedirectAttributes redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/sendEmailCode")
    public String sendEmailCode(@RequestBody Map<String, String> payload,HttpSession session,RedirectAttributes redirectAttributes) {
        return userProfileService.sendEmailCode(payload,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: signupVerified
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		이메일 인증
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/emailVerified")
    public String signupVerified(@RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return userProfileService.emailVerified(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: selectedChannel
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		다중 채널 화면에서 채널 선택
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam String channel, @RequestParam String selectedText, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/selectedChannel")
    public RedirectView selectedChannel(@RequestParam String channel, @RequestParam String selectedText, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return userProfileService.selectedChannel(channel,selectedText,session,request,redirectAttributes);
    }

    /*
     * 1. 메소드명: consent
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-약관 동의이력 조회 VIEW 및 동의이력 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param session, Model model
     *   @return ModelAndView
     */
    @GetMapping("/consent")
    public ModelAndView consent(HttpSession session, Model model) {
        return consentProfileService.index(session,model);
    }

    /*
     * 1. 메소드명: marketingUpdate
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-마케팅 약관동의 변경
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, Object> payload,HttpSession session,RedirectAttributes redirectAttributes,HttpServletRequest request
     *   @return RedirectView
     */
    @PostMapping("/marketingUpdate")
    public RedirectView marketingUpdate(@RequestParam Map<String, Object> payload,HttpSession session,RedirectAttributes redirectAttributes,HttpServletRequest request) {
        return consentProfileService.marketingUpdate(payload,session,redirectAttributes,request);
    }

    /*
     * 1. 메소드명: approvalList
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-approvalList(승인리스트) 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session, Model model
     *   @return ModelAndView
     */
    @GetMapping("/approvalList")
    public ModelAndView approvalList(HttpServletRequest request, HttpSession session, Model model) {
        return approvalListService.approvalList(request, session, model);
    }

    /*
     * 1. 메소드명: consentManager
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-approvalList(승인리스트) 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session, Model model
     *   @return ModelAndView
     */
    @GetMapping("/consentManager")
    public ModelAndView consentManager(HttpServletRequest request, HttpSession session, Model model) {
        String channel = (String) session.getAttribute("session_channel");
        List<Map<String, Object>> resultList = userProfileService.getConsentManagerList(channel, "", "");
        
        //Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "Consent_Manager");
        param.put("action", "ListView"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", channel);
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);

        return userProfileService.consentManager(request, session, model);
    }

    /*
     * 1. 메소드명: approvalRequest
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-approvalRequest(회사 도메인 등록) 메뉴 VIEW 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request, HttpSession session, Model model
     *   @return ModelAndView
     */
    @GetMapping("/approvalRequest")
    public ModelAndView approvalRequest(HttpServletRequest request, HttpSession session, Model model) {
        return empVerificationService.approvalRequest(request, session, model);
    }

    /*
     * 1. 메소드명: requestDomainSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-approvalRequest 도메인 등록
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/requestDomainSubmit")
    public RedirectView requestDomainSubmit(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
         return empVerificationService.requestDomainSubmit(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: requestRoleSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *		마이페이지-Role Management 등록
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/requestRoleSubmit")
    public RedirectView requestRoleSubmit(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
         return empVerificationService.requestRoleSubmit(payload,session, request,redirectAttributes);
    }

    /*
     * 1. 메소드명: consentDetailSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 김준용
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *
     * 2. 사용법
     *
     * </PRE>
     *   @param @RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/consentDetailSubmit")
    public RedirectView consentDetail(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return userProfileService.consentDetail(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: consentDetail
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      ConsentManager 상세 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session,model
     *   @return ModelAndView
     */
    @GetMapping("/consentDetail")
    public ModelAndView consentDetail(HttpServletRequest request, HttpSession session, Model model) {
        // 먼저 세션에서 데이터를 확인
        Map<String, String> payload = (Map<String, String>) session.getAttribute("consentDetail");

        // 세션에도 payloadData가 없을 경우 빈 맵으로 초기화
        if (payload == null || payload.isEmpty()) {
            payload = new HashMap<>();
        }

        return userProfileService.consentDetail(payload, request, session, model);
    }

    /*
     * 1. 메소드명: consentHistory (POST)
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      ConsentManager 약관 히스토리 저장
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session,redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/consentHistorySubmit")
    public RedirectView consentHistory(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return userProfileService.consentHistory(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: consentHistory (GET)
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      ConsentManager 약관 히스토리 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session,model
     *   @return ModelAndView
     */
    @GetMapping("/consentHistory")
    public ModelAndView consentHistory(HttpServletRequest request, HttpSession session, Model model) {
        // 먼저 세션에서 데이터를 확인
        Map<String, String> payload = (Map<String, String>) session.getAttribute("payloadData");

        // 세션에도 payloadData가 없을 경우 빈 맵으로 초기화
        if (payload == null || payload.isEmpty()) {
            payload = new HashMap<>();
        }

        return userProfileService.consentHistory(payload, request, session, model);
    }

    /*
     * 1. 메소드명: createNewConsent
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 한국민
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      ConsentManager 새 약관 등록 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session,model
     *   @return ModelAndView
     */
    @GetMapping("/createNewConsent")
    public ModelAndView createNewConsent(HttpServletRequest request, HttpSession session, Model model) {
        return userProfileService.createNewConsent(request, session, model);
    }

    /*
     * 1. 메소드명: inviteUser
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이피이지-inviteUser(초대) 페이지 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param session,model
     *   @return ModelAndView
     */
    @GetMapping("/inviteUser")
    public ModelAndView inviteUser(HttpSession session, Model model) {

        return invitationService.inviteUser(session, model);
    }

    /*
     * 1. 메소드명: ssoAccessListSerach
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-inviteUser(초대) 채널 초대 목록 그리드 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param allParams,session,model
     *   @return Map<String, Object>
     */
    @ResponseBody
    @PostMapping("/inviteList")
    public Map<String, Object> ssoAccessListSerach(@RequestBody Map<String, String> allParams, HttpSession session, Model model) {
        return invitationService.inviteList(allParams,session);
    }

    /*
     * 1. 메소드명: resend
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-채널 초대 재발송
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session,redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/resend")
    public String resend(@RequestBody Map<String, String> payload,HttpSession session, RedirectAttributes redirectAttributes) {
        return invitationService.resend(payload,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: cancel
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-채널 초대건 삭제
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session,redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/cancel")
    public String cancel(@RequestBody Map<String, String> payload,HttpSession session, RedirectAttributes redirectAttributes) {
        return invitationService.cancel(payload,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: auditLogList
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      AuditLog저장
     * 2. 사용법
     *
     * </PRE>
     *   @param status,channel,session,model
     *   @return ModelAndView
     */
    @GetMapping("/auditLog")
    public ModelAndView auditLogList(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(required = false) String channel,
            HttpSession session,
            Model model) {
        return auditLogService.auditLogList(session, model);
    }

    /*
     * 1. 메소드명: inviteinfomationCreate
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-채널 초대 생성
     * 2. 사용법
     *
     * </PRE>
     *   @param param,session
     *   @return ModelAndView
     */
    @GetMapping("/inviteinfomationCreate")
    public ModelAndView inviteinfomationCreate(@RequestParam("param") String param, HttpSession session) {
        return invitationService.inviteInfomation(param, session);
    }

    /*
     * 1. 메소드명: inviteinfomationCreate
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-employmentVerification 메뉴 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session,model
     *   @return ModelAndView
     */
    @GetMapping("/employmentVerification")
    public ModelAndView employmentVerification(HttpServletRequest request, HttpSession session, Model model) {
        return empVerificationService.employVerification( request, session, model);
    }

    /*
     * 1. 메소드명: empVerificationList
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-empVerificationList 리스트 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session
     *   @return List<Map<String, Object>>
     */
    @ResponseBody
    @PostMapping("/empVerificationList")
    public List<Map<String, Object>> empVerificationList(@RequestBody Map<String, String> payload, HttpSession session) {
        List<Map<String, Object>> resultList = empVerificationService.searchempVerification(payload, session);
 
        // Audit Log
        Map<String,String> param = new HashMap<String,String>();
        param.put("type", "User_Profile");
        param.put("action", "Search"); // 6가지 : ListView, DetailedView, View, Search, Creation, Modification
        param.put("condition", String.valueOf(payload));
        // 조회된 데이터의 길이를 result_count로, 데이터를 items로 추가
        param.put("result_count", String.valueOf(resultList.size()));
        auditLogService.addAuditLog(session, param);
        return resultList;
    }

    /*
     * 1. 메소드명: createNewConsentContainGroup
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-새 약관 등록
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session,model
     *   @return RedirectView
     */
    @ResponseBody
    @PostMapping("/createNewConsentSubmit")
    public RedirectView createNewConsentContainGroup(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return userProfileService.createNewConsentContainGroup(payload, request, session, model);
    }

    /*
     * 1. 메소드명: inviteCompanyAndAccountCreated
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-채널 초대시 CDC계정 생성
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session,request,redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/inviteCompanyAndAccountCreated")
    // @ResponseBody
    public String inviteCompanyAndAccountCreated(@RequestParam Map<String, String> payload, HttpSession session, HttpServletRequest request,RedirectAttributes redirectAttributes) {
        return invitationService.inviteCompanyAndAccountCreated(payload,session,redirectAttributes,request);
    }

    /*
     * 1. 메소드명: userManagment
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-userManagment 메뉴 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session
     *   @return ModelAndView
     */
    // 24.07.25 홍정인 추가 - 마이페이지 - 회원탈퇴 Direct 접근 url
    @GetMapping("/userManagment")
    public ModelAndView userManagment(HttpServletRequest request, HttpSession session) {
        return systemService.userManagment(request,session);
    }

    /*
     * 1. 메소드명: searchUserManagment
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-UserManagment 리스트 조회(CDC조회)
     * 2. 사용법
     *
     * </PRE>
     *   @param allParams,session.model
     *   @return Map<String, Object>
     */
    @ResponseBody
    @PostMapping("/searchUserManagment")
    public Map<String, Object> searchUserManagment(@RequestBody Map<String, String> allParams, HttpSession session, Model model) {
        return systemService.searchUserManagment(allParams,session);
    }

    /*
     * 1. 메소드명: userManagmentDetail
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-UserManagment 상세 조회 및 데이터 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session.payload
     *   @return String
     */
    @PostMapping("/userManagmentDetail")
    public ModelAndView userManagmentDetail(HttpServletRequest request,HttpSession session,@RequestParam Map<String, String> payload,
                                       Model model) {
        return systemService.userManagmentDetail(request,session,payload);
    }

    /*
     * 1. 메소드명: adminTypeEdit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-UserManagment 권한 부여
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session.redirectAttributes
     *   @return String
     */
    @ResponseBody
    @PostMapping("/adminTypeEdit")
    public String adminTypeEdit(@RequestBody Map<String, String> payload,HttpSession session, RedirectAttributes redirectAttributes) {
        return systemService.adminTypeEdit(payload,session,redirectAttributes);
    }

    /*
     * 1. 메소드명: employmentVerificationSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-employmentVerification 등록
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/employmentVerificationSubmit")
    public RedirectView employmentVerificationSubmit(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return empVerificationService.employmentVerificationSubmit(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: deleteDomain
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-Approval Request 도메인 삭제
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/deleteDomain")
    public RedirectView deleteDomain(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return approvalListService.deleteDomain(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: approvalConfiguration
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-승인 룰 조회 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.model
     *   @return ModelAndView
     */
    @GetMapping("/approvalConfiguration")
    public ModelAndView approvalConfiguration(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return userProfileService.approvalConfiguration(payload, request, session, model);
    }

    /*
     * 1. 메소드명: approvalListPendingSubmit
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-승인 진행
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/approvalListPendingSubmit")
    public RedirectView approvalListPendingSubmit(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return approvalListService.approvalListPendingSubmit(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: userListDetail
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-승인 VIEW 상세 데이터 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/userListDetail")
    public RedirectView userListDetail(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return approvalListService.userListDetail(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: approvalInformation
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-승인 상세 조회 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session.model
     *   @return ModelAndView
     */
    @GetMapping("/approvalInformation")
    public ModelAndView approvalInformation(@ModelAttribute("payloadData") Map<String, String> payload, HttpServletRequest request, HttpSession session, Model model) {
        return approvalListService.approvalInformation(payload, request, session, model);
    }

    /*
     * 1. 메소드명: approvalListSearch
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-approvalList 승인 리스트 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,session
     *   @return List<Map<String,Object>>
     */
    @ResponseBody
    @PostMapping("/approvalListSearch")
    public List<Map<String,Object>> approvalListSearch(@RequestBody Map<String, String> payload, HttpSession session) {
        String requestType = (String) payload.get("requestType");

        switch(requestType) {
            case "registrationApproval":
                return approvalListService.searchW01(payload, session);
            case "W01":
                return approvalListService.searchW01(payload, session);
            case "invitationApproval":
                // return approvalListService.searchW03(payload, session);
                return approvalListService.searchW01(payload, session);
            case "W05":
                 return approvalListService.searchW05(payload, session);
                //return approvalListService.searchW07(payload, session);
            case "roleManagement":
                return approvalListService.searchW05(payload, session);
               //return approvalListService.searchW07(payload, session);
            case "W02":
                return approvalListService.searchW01(payload, session);
            case "W03":
                return approvalListService.searchW01(payload, session);
            case "W06":
                return approvalListService.searchW01(payload, session);
            case "companyDomain":
                return approvalListService.searchW07(payload, session);
            case "W07":
                return approvalListService.searchW07(payload, session);

            default:
                return approvalListService.searchW01(payload, session);
        }
    }

    /*
     * 1. 메소드명: searchBusinessLocationList
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-ApprovalRequest
     * 2. 사용법
     *
     * </PRE>
     *   @param allParams,session,model
     *   @return List<SecServingCountry>
     */
    @ResponseBody
    @PostMapping("/searchBusinessLocationList")
    public List<SecServingCountry> searchBusinessLocationList(@RequestBody Map<String, String> allParams, HttpSession session, Model model) {
        return empVerificationService.searchBusinessLocationList(allParams, session);
    }

    /*
     * 1. 메소드명: ciamApprovalList
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-ciamApprovalList(관리자용 승인리스트 조회) 페이지 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session
     *   @return ModelAndView
     */
    @GetMapping("/ciamApprovalList")
    public ModelAndView ciamApprovalList(HttpServletRequest request, HttpSession session) {
        return approvalListService.ciamApprovalList(request,session);
    }

    /*
     * 1. 메소드명: ciamApprovalListSearch
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-CiamApprovalList(ADMIN 조회용 승인리스트 조회) 그리드 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param allParams,session
     *   @return Map<String, Object>
     */
    @ResponseBody
    @PostMapping("/ciamApprovalListSearch")
    public Map<String, Object> ciamApprovalListSearch(@RequestBody Map<String, String> allParams, HttpSession session) {
        return approvalListService.getApprovalList(allParams,session);
    }

    /*
     * 1. 메소드명: ciamApprovalDetail
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-CiamApprovalList 승인 상세 데이터 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param payload,request,session,redirectAttributes
     *   @return RedirectView
     */
    @PostMapping("/ciamApprovalDetail")
    public RedirectView ciamApprovalDetail(@RequestParam Map<String, String> payload, HttpServletRequest request, HttpSession session,RedirectAttributes redirectAttributes) {
        return approvalListService.ciamApprovalDetail(payload, request, session, redirectAttributes);
    }

    /*
     * 1. 메소드명: ciamApprovalDetail
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-CiamApprovalList 승인 상세 VIEW 랜더링
     * 2. 사용법
     *
     * </PRE>
     *   @param request,session,model
     *   @return ModelAndView
     */
    @GetMapping("/ciamApprovalDetail")
    public ModelAndView ciamApprovalDetail(HttpServletRequest request, HttpSession session, Model model) {
        Map<String, String> payload = (Map<String, String>) session.getAttribute("ciamApprovalDetail");

        return approvalListService.ciamApprovalDetail(payload, request, session, model);
    }

    /*
     * 1. 메소드명: CiamApprovalDetailListSerach
     * 2. 클래스명: UserProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 09. 10.
     */
    /**
     * <PRE>
     * 1. 설명
     *      마이페이지-CiamApprovalList상세->승인 리스트 조회
     * 2. 사용법
     *
     * </PRE>
     *   @param allParams,session
     *   @return Map<String, Object>
     */
    @ResponseBody
    @PostMapping("/CiamApprovalDetailListSerach")
    public Map<String, Object> CiamApprovalDetailListSerach(@RequestBody Map<String, String> allParams, HttpSession session) {
        return approvalListService.getApprovalDetailList(allParams,session);
    }
}