package com.samsung.ciam.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import com.samsung.ciam.utils.EncryptUtil;
import com.samsung.ciam.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 1. FileName	: CdcTraitService.java
 * 2. Package	: <insert package name here if applicable>
 * 3. Comments	: CDC 통신과 관련된 메소드를 수행
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 | Name			| Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.	 | 서정환		| 최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@Slf4j
@Service
public class CdcTraitService {

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    @Autowired
    private ChannelApproversRepository channelApproversRepository;

    @Autowired
    private ChannelApprovalStatusesRepository channelApprovalStatusesRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private NewCompanyRepository newCompanyRepository;

    @Autowired
    private BtpAccountsRepository btpAccountsRepository;

    @Lazy
    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CpiApiService cpiApiService;

    @Autowired
    private ChannelAddFieldRepository channelAddFieldRepository;

    @Autowired
    private ApprovalAdminRepository approvalAdminRepository;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @Autowired
    private UserAgreedConsentsRepository userAgreedConsentsRepository;

    /*
     * 1. 메소드명: getCdcUser
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CDC 사용자 정보 조회->프로필,데이터,약관동의내용
     * 2. 사용법
     *    조회하고싶은 사용자 UID 전달하여 사용
     * 3. 예시 데이터
     *    - Input: uid = "21312312"
     *    - Output: CDC 사용자 정보 값
     * </PRE>
     * @param  uid 사용자 UID
     * @param  retryCnt 재시도횟수
     * @return JSON 형태의 CDC 사용자 정보
     */
    public JsonNode getCdcUser(String uid, int retryCnt) {
        Map<String, Object> cdcParams = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        //CDC 요청 파라미터 설정 -> 조회하고싶은 필드들 설정
        cdcParams.put("UID", uid);
        cdcParams.put("include", "identities-active,identities-all,identities-global, loginIDs, emails, profile, data, password, lastLoginLocation, regSource, irank, rba, subscriptions, userInfo, preferences");
        cdcParams.put("extraProfileFields", "languages,address,phones, education, honors, publications, patents, certifications, professionalHeadline, bio, industry, specialties, work, skills, religion, politicalView, interestedIn, relationshipStatus, hometown, favorites, followersCount, followingCount, username, locale, verified, timezone, likes, samlData");

        //GIGYA활용하여 Account정보 조회 API 호출
        GSResponse response = gigyaService.executeRequest("default", "accounts.getAccountInfo", cdcParams);

        try {
            JsonNode responseData = objectMapper.readTree(response.getResponseText());
            if (responseData.get("profile") == null || responseData.get("profile").isEmpty()) {
                if (response.getErrorMessage() != null && !response.getErrorMessage().isEmpty()) {
                    log.warn("getCdcUser Failed! uid: {}, response: {}", uid, response.getErrorMessage());
                    return responseData;
                }

                if (retryCnt == 2) {
                    log.warn("getCdcUser Failed! uid: {}, response: {}", uid, response.getErrorMessage());
                    return responseData;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return getCdcUser(uid, retryCnt + 1);
            }

            return responseData;
        } catch (Exception e) {
            log.error("Error processing CDC response", e);
            return null;
        }
    }

    /*
     * 1. 메소드명: consentSelector
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    BTP 약관 정보 조회
     * 2. 사용법
     *
     * 3. 예시 데이터
     *    - Input: String uid, String channel, String language, String subsidiary
     *    - Output: 동의해야할 약관 정보
     * </PRE>
     * @param uid 사용자 UID
     * @param channel 약관 조회 채널
     * @param language 약관 조회 언어
     * @param subsidiary 약관 조회 법인
     * @return 약관 정보가 담긴 MAP
     */
    public Map<String, Object> consentSelector(String uid, String channel, String language, String subsidiary) {
        //UID에 해당하는 CDC정보조회
        JsonNode accUser = getCdcUser(uid, 0);
        if (accUser == null || !accUser.has("profile") || !accUser.path("profile").has("country")) {
            log.error("User does not have country!");
        }

        //CDC계정의 국자정보 조회
        String secCountry = accUser.path("profile").path("country").asText();
        String termsCommon = "";
        String termsChannel = "";
        String privacyCommon = "";
        String privacyChannel = "";
        String marketingCommon = "b2b.marketing";

        // Get common terms
        String commonTermConsentId = "";
        String commonTermContentId = "";
        String termsCommonText = "";

        //약관 COMMON TERMS 유형 법인조건으로 조회 -> 미존재시 법인정보 ALL조회
        Consent commonTerm = Optional.ofNullable(consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary("common", secCountry, subsidiary))
                .orElseGet(() -> consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary("common", secCountry, "ALL"));

        //약관 COMMON TERMS 유형 국가 조건으로만 조회
        commonTerm = Optional.ofNullable(commonTerm)
                .orElseGet(() -> consentRepository.selectLatestTermsByCoverageAndCountry("common", secCountry));

        if (commonTerm != null) {
            commonTermConsentId = String.valueOf(commonTerm.getId());
            termsCommon = commonTerm.getCdcConsentId();
            //약관 컨텐츠 조회 -> 게시된 published만 조회
            ConsentContent termsCommonContent = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(commonTerm.getId(), language, "published");

            if (termsCommonContent != null) {
                commonTermContentId = String.valueOf(termsCommonContent.getId());
                termsCommonText = termsCommonContent.getContent();
            } else {
                //약관 컨텐츠 미존재시 기본언어로 세팅된 언어로 조회
                termsCommonContent = consentContentRepository.selectLatestByConsentIdAndLanguagePublishedId(commonTerm.getId(), commonTerm.getDefaultLanguage());
                if (termsCommonContent != null) {
                    commonTermContentId = String.valueOf(termsCommonContent.getId());
                    termsCommonText = termsCommonContent.getContent();
                } else {
                    termsCommonText = "not found";
                }
                ;
            }
        } else {
            //약관 COMMON TERMS 우형 미존재시 기본 TERMS 조회
            commonTerm = consentRepository.selectFirstByCoverageAndType("common", "terms");
            if (commonTerm != null) {
                commonTermConsentId = String.valueOf(commonTerm.getId());
                termsCommon = commonTerm.getCdcConsentId();
                //약관 컨텐츠 조회 -> 기본언어로 세팅된 언어로 조회
                ConsentContent termsCommonContentDef = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(commonTerm.getId(), commonTerm.getDefaultLanguage(), "published");

                if (termsCommonContentDef != null) {
                    commonTermContentId = String.valueOf(termsCommonContentDef.getId());
                    termsCommonText = termsCommonContentDef.getContent();
                } else {
                    termsCommonText = "not found";
                }
            } else {
                termsCommonText = "not found";
            }
        }

        String channelTermsConsentId = "";
        String channelTermsContentId = "";
        String termsChannelText = "";

        //약관 CHANNEL TERMS 유형 법인조건으로 조회 -> 미존재시 법인정보 ALL조회
        Consent channelTerms = Optional.ofNullable(
                        consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary(channel, secCountry, subsidiary))
                .orElseGet(() -> consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary(channel, secCountry, "ALL"));

        //약관 CHANNEL TERMS 유형 국가 조건으로만 조회
        channelTerms = Optional.ofNullable(channelTerms)
                .orElseGet(() -> consentRepository.selectByCoverageAndCountryAndType(channel, secCountry));

        if (channelTerms != null) {
            channelTermsConsentId = String.valueOf(channelTerms.getId());
            termsChannel = channelTerms.getCdcConsentId();
            //약관 컨텐츠 조회 -> 게시된 published만 조회
            ConsentContent termsChannelContent = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(channelTerms.getId(), language, "published");

            if (termsChannelContent != null) {
                channelTermsContentId = String.valueOf(termsChannelContent.getId());
                termsChannelText = termsChannelContent.getContent();
            } else {
                //약관 컨텐츠 미존재시 기본언어로 세팅된 언어로 조회
                termsChannelContent = consentContentRepository.selectLatestByConsentIdPublishedId(channelTerms.getId());
                if (termsChannelContent != null) {
                    channelTermsContentId = String.valueOf(termsChannelContent.getId());
                    termsChannelText = termsChannelContent.getContent();
                } else {
                    termsChannelText = "not found";
                }
            }
        } else {
            termsChannelText = "not found";

//            channelTerms = consentRepository.selectFirstByCoverageAndType(channel, "terms");
//
//            if (channelTerms != null) {
//                channelTermsConsentId = String.valueOf(channelTerms.getId());
//                termsChannel = channelTerms.getCdcConsentId();
//
//                ConsentContent termsChannelContentDef = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(channelTerms.getId(), channelTerms.getDefaultLanguage(), "published");
//
//                if (termsChannelContentDef != null) {
//                    channelTermsContentId = String.valueOf(termsChannelContentDef.getId());
//                    termsChannelText = termsChannelContentDef.getContent();
//                } else {
//                    termsChannelText = "not found";
//                }
//            }
        }

        // Get common privacy
        String commonPrivacyConsentId = "";
        String commonPrivacyContentId = "";
        String privacyCommonText = "";

        Consent commonPrivacy = Optional.ofNullable(
                        consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary("common", secCountry, subsidiary))
                .orElseGet(() -> consentRepository.selectLatestTermsByCoverageAndCountrySubsidiary("common", secCountry, "ALL"));

        commonPrivacy = Optional.ofNullable(commonPrivacy)
                .orElseGet(() -> consentRepository.selectFirstByCoverageAndCountryAndPrivacy("common", secCountry));

        if (commonPrivacy != null) {
            commonPrivacyConsentId = String.valueOf(commonPrivacy.getId());
            privacyCommon = commonPrivacy.getCdcConsentId();
            ConsentContent privacyCommonContent = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(commonPrivacy.getId(), language, "published");

            if (privacyCommonContent != null) {
                commonPrivacyContentId = String.valueOf(privacyCommonContent.getId());
                privacyCommonText = privacyCommonContent.getContent();
            } else {
                privacyCommonContent = consentContentRepository.selectLatestByConsentIdAndLanguagePublishedId(commonPrivacy.getId(), commonPrivacy.getDefaultLanguage());
                if (privacyCommonContent != null) {
                    commonPrivacyContentId = String.valueOf(privacyCommonContent.getId());
                    privacyCommonText = privacyCommonContent.getContent();
                } else {
                    privacyCommonText = "not found";
                }
            }
        } else {
            commonPrivacy = consentRepository.selectFirstByCoverageAndType("common", "privacy");

            if (commonPrivacy != null) {
                commonPrivacyConsentId = String.valueOf(commonPrivacy.getId());
                privacyCommon = commonPrivacy.getCdcConsentId();

                ConsentContent privacyCommonContentDef = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(commonPrivacy.getId(), commonPrivacy.getDefaultLanguage(), "published");

                if (privacyCommonContentDef != null) {
                    commonPrivacyContentId = String.valueOf(privacyCommonContentDef.getId());
                    privacyCommonText = privacyCommonContentDef.getContent();
                } else {
                    privacyCommonText = "not found";
                }
            }
        }

        // Get channel privacy
        Consent channelPrivacy = Optional.ofNullable(
                        consentRepository.selectLatestTermsByCoverageAndCountrySubsidiaryPrivacy(channel, secCountry, subsidiary))
                .orElseGet(() -> consentRepository.selectLatestTermsByCoverageAndCountrySubsidiaryPrivacy(channel, secCountry, "ALL"));

        channelPrivacy = Optional.ofNullable(channelPrivacy)
                .orElseGet(() -> consentRepository.selectByCoverageAndCountryAndPrivacy(channel, secCountry));


        String channelPrivacyConsentId = "";
        String channelPrivacyContentId = "";
        String privacyChannelText = "";

        if (channelPrivacy != null) {
            channelPrivacyConsentId = String.valueOf(channelPrivacy.getId());
            privacyChannel = channelPrivacy.getCdcConsentId();
            ConsentContent privacyChannelContent = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(channelPrivacy.getId(), language, "published");

            if (privacyChannelContent != null) {
                channelPrivacyContentId = String.valueOf(privacyChannelContent.getId());
                privacyChannelText = privacyChannelContent.getContent();
            } else {
                privacyChannelContent = consentContentRepository.selectLatestByConsentIdPublishedId(channelPrivacy.getId());
                if (privacyChannelContent != null) {
                    channelPrivacyContentId = String.valueOf(privacyChannelContent.getId());
                    privacyChannelText = privacyChannelContent.getContent();
                } else {
                    privacyChannelText = "not found";
                }
            }
        } else {
            privacyChannelText = "not found";

//            channelPrivacy = consentRepository.selectFirstByCoverageAndType(channel);
//
//            if (channelPrivacy != null) {
//                channelPrivacyConsentId = String.valueOf(channelPrivacy.getId());
//                privacyChannel = channelPrivacy.getCdcConsentId();
//
//                ConsentContent privacyChannelContentDef = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(channelPrivacy.getId(), channelPrivacy.getDefaultLanguage(), "published");
//
//                if (privacyChannelContentDef != null) {
//                    channelPrivacyContentId = String.valueOf(privacyChannelContentDef.getId());
//                    privacyChannelText = privacyChannelContentDef.getContent();
//                } else {
//                    privacyChannelText = "not found";
//                }
//            }
        }

        // Get marketing consent
        Consent marketing;
        Long marketingId;
        if ("KR".equals(secCountry)) {
            marketingId = Long.parseLong(BeansUtil.getApplicationProperty("marketing.kr.id"));
            marketing = consentRepository.selectCommonMarketingKR(marketingId);
        } else {
            marketingId = Long.parseLong(BeansUtil.getApplicationProperty("marketing.other.id"));
            marketing = consentRepository.selectCommonMarketingOther(marketingId);
        }

        String marketingConsentId = "";
        String marketingContentId = "";
        String marketingCommonText = "";
        String mktLang = "KR".equals(secCountry) ? "ko" : "en";

        if (marketing != null) {
            marketingConsentId = String.valueOf(marketing.getId());
            marketingCommon = marketing.getCdcConsentId();

            ConsentContent marketingContent = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(marketing.getId(), mktLang, "published");

            if (marketingContent != null) {
                marketingContentId = String.valueOf(marketingContent.getId());
                marketingCommonText = marketingContent.getContent();
            } else {
                marketingContent = consentContentRepository.selectLatestByConsentIdAndLanguagePublishedId(marketing.getId(), mktLang);
                if (marketingContent != null) {
                    marketingContentId = String.valueOf(marketingContent.getId());
                    marketingCommonText = marketingContent.getContent();
                } else {
                    marketingCommonText = "not found";
                }
            }
        } else {
            marketingId = Long.parseLong(BeansUtil.getApplicationProperty("marketing.other.id"));
            marketing = consentRepository.selectCommonMarketingOther(marketingId);
            if (marketing != null) {
                marketingConsentId = String.valueOf(marketing.getId());
                marketingCommon = marketing.getCdcConsentId();

                ConsentContent marketingCommonContentDefault;
                if (secCountry.equals("KR")) {
                    marketingCommonContentDefault = consentContentRepository.selectLatestByConsentIdAndLanguagePublishedId(marketing.getId(), "ko");
                } else {
                    marketingCommonContentDefault = consentContentRepository.selectLatestByConsentIdAndLanguagePublishedId(marketing.getId(), "en");
                }

                if (marketingCommonContentDefault != null) {
                    marketingContentId = String.valueOf(marketingCommonContentDefault.getId());
                    marketingCommonText = marketingCommonContentDefault.getContent();
                }
            }
        }

        //약관정보 셋팅
        Map<String, Object> returnArray = new HashMap<>();
        returnArray.put("marketingCommon", marketingCommon);
        returnArray.put("marketingCommonText", marketingCommonText);
        returnArray.put("marketingConsentid", marketingConsentId);
        returnArray.put("marketingcontentid", marketingContentId);
        returnArray.put("secCountry", secCountry);
        returnArray.put("privacyCommon", privacyCommon);
        returnArray.put("privacyCommonText", privacyCommonText);
        returnArray.put("commonPrivacyConsentId", commonPrivacyConsentId);
        returnArray.put("commonPrivacyContentId", commonPrivacyContentId);
        returnArray.put("privacyChannel", privacyChannel);
        returnArray.put("privacyChannelText", privacyChannelText);
        returnArray.put("channelPrivacyConsentId", channelPrivacyConsentId);
        returnArray.put("channelPrivacyContentId", channelPrivacyContentId);
        returnArray.put("termsCommon", termsCommon);
        returnArray.put("termsCommonText", termsCommonText);
        returnArray.put("commonTermConsentId", commonTermConsentId);
        returnArray.put("commonTermContentId", commonTermContentId);
        returnArray.put("termsChannel", termsChannel);
        returnArray.put("termsChannelText", termsChannelText);
        returnArray.put("channelTermsConsentId", channelTermsConsentId);
        returnArray.put("channelTermsContentId", channelTermsContentId);

        return returnArray;
    }

    public int getApprovalFlow(String param, String uid, String approvalType, String channel, boolean isSamsungEmployee) {
        String mailtemplate = ("invitation".equals(approvalType)) ? "TEMPLET-009" : "TEMPLET-002";
        if (approvalService.approvalConfig(param, approvalType, uid, false)) {
            return 0;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        String emailSerUrl = BeansUtil.getApplicationProperty("email.service.url");
        String emailservLogin = BeansUtil.getApplicationProperty("email.service.login");
        String emailservPassword = BeansUtil.getApplicationProperty("email.service.password");

        JsonNode cdcUser = getCdcUser(uid, 0);

        log.info("Processing normal approval flow for channel " + param);

        ChannelApprovers ifAutoApprove = channelApproversRepository.selectAutoApprove(param).orElse(null);

        if (ifAutoApprove != null) {
            String userDept = "";
            if (cdcUser.get("data").get("accountID") != null) {
                log.error("missing account ID");
            }
            //approveUser(uid, cdcUser.get("data").get("accountID").asText(), param, userDept, approvalType);
        } else {
            List<ChannelApprovers> approvers = channelApproversRepository.selectLevel1Approvers(param);
            int approversCount = approvers.size();

            boolean matchFound = false;
            String matchType = "catchAll";
            int counter = 1;

            for (ChannelApprovers approver : approvers) {
                log.info("approver rule check: " + approver.getId());

                if (approver.getCountry().equals(cdcUser.get("profile").get("country").asText())) {
                    matchType = "group";
                    matchFound = true;
                    log.info("approver rule match group: " + approver.getId());
                }

                if (matchFound || approversCount == counter) {
                    log.info("approver send to approvers: " + approver.getId());

                    try {
                        JsonNode approverEmails = objectMapper.readTree(approver.getApproverEmail());
                        for (JsonNode approverEmail : approverEmails) {
                            ChannelApprovalStatuses newStatus = new ChannelApprovalStatuses();
                            newStatus.setLoginId(
                                    Optional.ofNullable(cdcUser.get("profile").get("username"))
                                            .map(JsonNode::asText)
                                            .orElse("")
                            );
                            newStatus.setLoginUid(uid);
                            newStatus.setChannel(param);
                            newStatus.setChannelApproverId(approver.getId());
                            newStatus.setApprovalLine(approver.getApprovalLine());
                            newStatus.setMatchType(matchType);
                            newStatus.setRequestType(approvalType);
                            newStatus.setStatus("pending");
                            String approverEmailsStr = approverEmail.asText();
                            JsonNode approverEmailNode = objectMapper.readTree(approverEmailsStr);
                            newStatus.setApproverName(StringUtil.getSafeString(approverEmailNode, "name"));
                            newStatus.setApproverEmail(StringUtil.getSafeString(approverEmailNode, "email"));
                            //newStatus.setApproverEmail("jeonghwan_seo002@yopmail.com");
                            newStatus.setApproverUid(StringUtil.getSafeString(approverEmailNode, "uid"));
                            newStatus.setSubsidiary(
                                    Optional.ofNullable(cdcUser.get("data").get("subsidiary"))
                                            .map(JsonNode::asText)
                                            .orElse("")
                            );
                            channelApprovalStatusesRepository.save(newStatus);
                            log.info("sent approval notice to channel admin at " + StringUtil.getSafeString(approverEmailNode, "email"));

                            JsonNode approverCdc = getCdcUser(approver.getUid(), 0);

                            String url = emailSerUrl + "/mail";

                            ObjectNode dataNode = objectMapper.createObjectNode();
                            dataNode.put("template", mailtemplate);
                            dataNode.put("language", approverCdc.get("profile").get("locale").asText());
                            dataNode.put("channel", channel);

                            ArrayNode fieldsNode = objectMapper.createArrayNode();
                            ObjectNode field = objectMapper.createObjectNode();
                            field.put("$Channel Admin", StringUtil.getSafeString(approverEmailNode, "name"));
                            field.put("$Channel", StringUtil.capitalizeFirstLetter(param));
                            field.put("$CIAM Admin", "CIAM ADMIN");
                            fieldsNode.add(field);

                            dataNode.set("fields", fieldsNode);

                            ArrayNode toNode = objectMapper.createArrayNode();
                            ObjectNode to = objectMapper.createObjectNode();
                            to.put("name", StringUtil.getSafeString(approverEmailNode, "name"));
                            to.put("mail", StringUtil.getSafeString(approverEmailNode, "email"));
                            //to.put("mail", "jeonghwan_seo002@yopmail.com");
                            toNode.add(to);

                            dataNode.set("to", toNode);

                            String data = objectMapper.writeValueAsString(dataNode);

                            URL emailUrl = new URL(url);
                            HttpURLConnection connection = (HttpURLConnection) emailUrl.openConnection();
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("Authorization", "Basic " + EncryptUtil.encodeBase64(emailservLogin + ":" + emailservPassword));

                            try (OutputStream os = connection.getOutputStream()) {
                                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                                os.write(input, 0, input.length);
                            }

                            int responseCode = connection.getResponseCode();
                            String responseMessage = connection.getResponseMessage();
                            log.info("sent approval notice to channel admin result " + responseMessage);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse approver emails: " + e.getMessage());
                    }

                    break;
                }
                counter++;
            }
        }

        return 0;
    }

    public String approveUser(String uid, String bpid, String channel, String department, String requestType) {
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
        HttpServletRequest requestServlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = requestServlet.getSession();

        if (!optionalChannelObj.isPresent()) {
            log.error("Channel not found: {}", channel);
            return "failed";
        }

        Channels channelObj = optionalChannelObj.get();

        // config를 JSON으로 파싱
        JsonNode configNode = null;
        try {
            configNode = objectMapper.readTree((String) channelObj.getConfig());
        } catch (Exception e) {
            log.error("Error parsing channel config", e);
            return "failed";
        }

        boolean useBtpAccount = configNode.path("use_btp_account").asBoolean();

        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("siteUID", uid);
        GSResponse response = gigyaService.executeRequest("default", "accounts.notifyLogin", cdcParams);

        log.info("notifyLogin: {}", response.getResponseText());

        try {
            JsonNode responseData = objectMapper.readTree((String) response.getResponseText());
            if (responseData.has("regToken")) {
                String regToken = responseData.get("regToken").asText();
                cdcParams.clear();
                cdcParams.put("regToken", regToken);
                GSResponse finalizeResponse = gigyaService.executeRequest("default", "accounts.finalizeRegistration", cdcParams);
                log.info("finalizeRegistration: {}", finalizeResponse.getResponseText());
            }
        } catch (Exception e) {
            log.error("Error processing notifyLogin response", e);
        }

        JsonNode account = getCdcUser(uid, 0);
        log.info("user for approval: {}", account.toString());

        String companyVendorCode = "";

        if (account.has("data") && account.get("data").has("accountID")) {
            JsonNode company = getB2bOrg(bpid);

            if (company.has("errorCode") && company.get("errorCode").asInt() == 0) {
                if(company.has("type") && !"VENDOR".equals(company.get("type").asText())) {
                    companyVendorCode = company.get("bpid").asText("");
                } else {
                    companyVendorCode = company.get("info").get("vendorcode").get(0).asText();
                }
                log.info("existing company with bpid {} no new creation required", bpid);
            } else {
                NewCompany newCompany = null;
                BtpAccounts btpAccount = null;
                if (!useBtpAccount) {
                    newCompany = newCompanyRepository.selectByBpid(account.get("data").get("accountID").asText());
                } else {
                    btpAccount = btpAccountsRepository.selectByBpid(account.get("data").get("accountID").asText());
                }

                if (newCompany != null || btpAccount != null) {
                    try {

                        // bizCheck 및 usedBizRegNo 처리
                        String bpidValue = (newCompany != null) ? newCompany.getBpid() : btpAccount.getBpid();
                        String[] bizCheck = bpidValue.split("-");
                        log.info("bizCheck: {}", Arrays.toString(bizCheck));
                        String usedBizRegNo = (bizCheck.length > 1 && "NOBIZREG".equals(bizCheck[1])) ? "" : ((newCompany != null) ? newCompany.getBizRegNo1() : btpAccount.getBizregno1());
                        companyVendorCode = (newCompany != null) ? newCompany.getVendorCode() : btpAccount.getVendorcode();;

                        // Organization 정보를 수동으로 Map에 추가
                        Map<String, Object> organizationMap = new HashMap<>();
                        organizationMap.put("name", (newCompany != null) ? newCompany.getName() : btpAccount.getName());
                        organizationMap.put("name_search", (newCompany != null) ? newCompany.getName() : btpAccount.getName());
                        organizationMap.put("bpid", bpidValue);
                        organizationMap.put("bizregno1", usedBizRegNo);
                        organizationMap.put("phonenumber1", (newCompany != null) ? newCompany.getPhoneNumber1() : btpAccount.getPhonenumber1());
                        organizationMap.put("country", (newCompany != null) ? newCompany.getCountry() : btpAccount.getCountry());
                        organizationMap.put("street_address", ((newCompany != null) ? newCompany.getStreetAddress() : btpAccount.getStreetAddress()).toLowerCase());
                        organizationMap.put("city", (newCompany != null) ? newCompany.getCity() : btpAccount.getCity());
                        organizationMap.put("state", (newCompany != null) ? newCompany.getState() : btpAccount.getState());
                        organizationMap.put("zip_code", (newCompany != null) ? newCompany.getZipCode() : btpAccount.getZipCode());
                        organizationMap.put("type", (newCompany != null) ? newCompany.getType() : btpAccount.getType());
                        organizationMap.put("regch", (newCompany != null) ? newCompany.getRegCh() : "");
                        organizationMap.put("vendorcode", (newCompany != null) ? newCompany.getVendorCode() : btpAccount.getVendorcode());
                        organizationMap.put("industry_type", (newCompany != null) ? newCompany.getIndustryType() : "");
                        organizationMap.put("products", (newCompany != null) ? newCompany.getProducts() : "");
                        organizationMap.put("channeltype", (newCompany != null) ? newCompany.getChannelType() : "");

                        // Organization 정보 JSON 문자열로 변환
                        String organizationJson = objectMapper.writeValueAsString(organizationMap);

                        // Requester 정보 JSON 문자열로 변환
                        String requesterJson = objectMapper.writeValueAsString(Map.of(
                                "firstName", account.get("profile").get("firstName").asText().toUpperCase(),
                                "lastName", account.get("profile").get("lastName").asText().toUpperCase(),
                                "email", account.get("profile").get("email").asText()
                        ));

                        // CDC 파라미터 설정
                        cdcParams.put("organization", organizationJson);
                        cdcParams.put("requester", requesterJson);
                        cdcParams.put("status", "approved");

                        // Gigya 서비스 요청 실행
                        GSResponse registerOrgResponse = gigyaService.executeRequest("default", "accounts.b2b.registerOrganization", cdcParams);
                        log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

                        company = objectMapper.readTree((String) registerOrgResponse.getResponseText());

                        if (newCompany != null) {
                            bpid = newCompany.getBpid();
                        } else {
                            bpid = btpAccount.getBpid();
                        }

                        if (registerOrgResponse.getErrorCode() == 0) {
                            log.info("new company with bpid {} created", bpid);
                        } else {
                            log.error("new company with bpid {} create failed with error: {}", bpid, registerOrgResponse.getResponseText());
                        }
                    } catch (Exception e) {
                        log.error("Error processing registerOrganization request", e);
                    }
                } else {
                    //CMDM관련 로직 X
                    log.info("new company with bpid {} not found, checking in CMDM", bpid);

                    // CMDM에서 회사 데이터를 검색하기 위한 파라미터 설정
                    Map<String, Object> searchParams = new HashMap<>();
                    searchParams.put("accountId", account.get("data").get("accountID").asText());  // CDC 계정 ID를 사용하여 CMDM에서 검색
                    CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((channel));

                    // CMDM에서 데이터 검색
                    List<Map<String, Object>> companyDataList = cpiApiService.accountSearch(
                            Collections.singletonMap("acctid", account.get("data").get("accountID").asText()),
                            BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl"),
                            responseFieldMapping,
                            session
                    );

                    if (!companyDataList.isEmpty()) {
                        // CMDM에서 검색된 첫 번째 회사 데이터를 사용
                        Map<String, Object> companyData = companyDataList.get(0);

                        Map<String, Object> organization = new HashMap<>();
                        organization.put("name", companyData.getOrDefault("orgName", ""));
                        organization.put("name_search", companyData.getOrDefault("orgName", ""));
                        organization.put("bpid", companyData.getOrDefault("bpid", ""));
                        organization.put("bizregno1", companyData.getOrDefault("bizregno1", ""));
                        organization.put("phonenumber1", companyData.getOrDefault("phonenumber1", ""));
                        organization.put("country", companyData.getOrDefault("country", ""));
                        organization.put("street_address", companyData.getOrDefault("street_address", "").toString().toLowerCase());
                        organization.put("city", companyData.getOrDefault("city", ""));
                        organization.put("state", companyData.getOrDefault("state", ""));
                        organization.put("zip_code", companyData.getOrDefault("zip_code", ""));
                        organization.put("type", "CMDM");
                        organization.put("regch", companyData.getOrDefault("regch", ""));
                        //organization.put("vendorcode", companyData.getOrDefault("vendorcode", ""));
                        organization.put("industry_type", companyData.getOrDefault("industry_type", ""));
                        organization.put("channeltype", companyData.getOrDefault("channeltype", ""));

                        Map<String, Object> companyParams = (Map<String, Object>) session.getAttribute("companyParams");
                        if(companyParams!=null && companyParams.size() > 0) {
                            extractDynamicCompanyCdcDataFields(companyParams, organization, channel);
                        }

                        try {
                            String organizationJson = objectMapper.writeValueAsString(organization);

                            Map<String, Object> requester = new HashMap<>();
                            requester.put("firstName", account.get("profile").get("firstName").asText().toUpperCase());
                            requester.put("lastName", account.get("profile").get("lastName").asText().toUpperCase());
                            requester.put("email", account.get("profile").get("email").asText());

                            String requesterJson = objectMapper.writeValueAsString(requester);

                            // CDC 파라미터 설정
                            cdcParams.clear();
                            cdcParams.put("organization", organizationJson);
                            cdcParams.put("requester", requesterJson);
                            cdcParams.put("status", "approved");

                            // CDC에 조직 등록 요청
                            GSResponse registerOrgResponse = gigyaService.executeRequest("default", "accounts.b2b.registerOrganization", cdcParams);
                            log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

                            bpid = (String) companyData.getOrDefault("bpid", "");

                            if (registerOrgResponse.getErrorCode() == 0) {
                                log.info("new CMDM company with bpid {} created in CDC", bpid);
                            } else {
                                log.error("new CMDM company with bpid {} create in CDC failed with error: {}", bpid, registerOrgResponse.getResponseText());
                            }
                        } catch (Exception e) {
                            log.error("Error processing registerOrganization request", e);
                        }
                    } else {
                        log.error("Company not found in CMDM for accountID: {}", account.get("data").get("accountID").asText());
                    }
                }

            }

        }

        cdcParams.clear();
        // Approve user in channel field
        boolean isNewCompany = Boolean.TRUE.equals(session.getAttribute("isNewCompany"));
        String contactId = "";
        String accountId = "";

        JsonNode accountNode = getCdcUser(uid, 0);
        JsonNode companyNode = getB2bOrg(bpid);

        if("CUSTOMER".equals(channelObj.getChannelType())) {

            if (companyNode.has("type") && !"VENDOR".equals(companyNode.get("type").asText()) && accountNode.path("data").has("accountID")) {
                log.info("user with no contactID detected, creating new contact in CMDM");

                if(isNewCompany) {
                    NewCompany newCompany = newCompanyRepository.selectByBpid(bpid);
                    Map<String, Object> createdAccount = cpiApiService.createAccount(channel, "Context1", objectMapper.convertValue(companyNode, Map.class), session,newCompany);

                    Map<String, Object> accountMap = (Map<String, Object>) createdAccount.get("account");
                    Map<String, Object> wfobj = (Map<String, Object>) createdAccount.get("wfobj");  // 단일 Map으로 처리

                    if (accountMap != null && !accountMap.isEmpty()) {
                        accountId = (String) accountMap.get("acctid");
                        newCompany.setBpidInCdc(accountId);
                        newCompanyRepository.save(newCompany);

                        updateAccountID(uid, newCompany.getBpidInCdc());
                        accountNode = getCdcUser(uid, 0);

                        Map<String, Object> orgInfo = new HashMap<>();
                        orgInfo.put("bpid", accountId);
                        orgInfo.put("wfstate", wfobj.get("wfstate"));  // 단일 Map으로 처리
                        orgInfo.put("identifystatus", account.get("identifystatus"));
//                        orgInfo.put("industry_type", newCompany.getIndustryType());
//                        orgInfo.put("products", newCompany.getProducts());
//                        orgInfo.put("channeltype", newCompany.getChannelType());

                        // Update organization in CDC
                        updateOrganization(bpid, orgInfo);

                        //approvalAdminRepository.updateApprovalCompanyCode(uid,bpid);

                    }
                }

                // createContact 호출하여 새로운 Contact 생성
                Map<String, Object> createdContact = cpiApiService.createContact("Context1", objectMapper.convertValue(accountNode, Map.class), session,channel);

                if (createdContact != null && createdContact.get("contact") instanceof List) {
                    List<Map<String, Object>> contacts = (List<Map<String, Object>>) createdContact.get("contact");
                    if (!contacts.isEmpty()) {
                        contactId = contacts.get(contacts.size() - 1).get("contactid").toString();
                    }
                } else if (createdContact != null && createdContact.get("contact") instanceof Map) {
                    Map<String, Object> cmdmContact = (Map<String, Object>) createdContact.get("contact");
                    contactId = cmdmContact.getOrDefault("contactid", "").toString();
                }

                log.info("creating new contact in CMDM done, new contactID: {}", contactId);

            }
        }

        cdcParams.put("UID", uid);
        String approvalStatus = "request to join".equals(requestType) ? "inviteSent" : "approved";
        String userStatus = "request to join".equals(requestType) ? "inviteSent" : "active";

        Map<String, Object> dataFields = new HashMap<>();

        dataFields.put("lastTenureCheck",ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dataFields.put("channels", Map.of(
                channel, Map.of(
                        "adminType", 0,
                        "approvalStatus", approvalStatus,
                        "approvalStatusDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
        ));
        dataFields.put("mLoginID", maskEmail(account.get("profile").get("email").asText()));
        if("VENDOR".equals(channelObj.getChannelType())) {
            dataFields.put("vendorCode", companyVendorCode);
        }
        dataFields.put("userStatus", userStatus);
        if("CUSTOMER".equals(channelObj.getChannelType())) {
            dataFields.put("contactID", contactId);
        }

        try {
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
            cdcParams.put("preferences", objectMapper.writeValueAsString(Map.of(
                    "internal." + channel, Map.of("isConsentGranted", true),
                    "internal.mypage", Map.of("isConsentGranted", true)
            )));
        } catch (Exception e) {
            log.error("Error processing data fields", e);
        }

        GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
        log.info("setAccountInfo: {}", setAccountResponse.getResponseText());

        // 비동기 메서드 sendProvisioning2 호출
        //프로비저닝 호출 -> ad가입 제외
        if (!"adRegistration".equals(requestType)) {
            // Optional에서 Channels 객체를 가져옴
            Map<String, Object> configMap = channelObj.getConfigMap();

            Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                    ? (Boolean) configMap.get("java_useprovisioning")
                    : false;

            // userProvisioning이 true일 때만 프로비저닝 실행
            if (Boolean.TRUE.equals(userProvisioning)) {
                String operationType = getOperationType(requestType);

                if (operationType != null) {
                    cpiApiService.sendUidProvisioningNoConfigChecking(channel, operationType, uid);
                    log.info("Provisioning send end for requestType: {}", requestType);
                }
            }
        }

        removeUserRoleFromB2B(uid);
        account = getCdcUser(uid, 0);

        if (setAccountResponse.getErrorCode() == 0) {

            ChannelApprovalStatuses approverData = channelApprovalStatusesRepository
                    .selectFirstByLoginUidAndChannelAndStatus(uid, channel, "approved")
                    .orElse(null);

            String approvalDate = Optional.ofNullable(approverData)
                    .map(ChannelApprovalStatuses::getApprovalDate)
                    .map(LocalDateTime::toString)
                    .orElse(LocalDateTime.now().toString());
            String approvalAdmin = approverData != null ? approverData.getApproverName() : "Channel Admin";

            String channelDisplayName = channelRepository.selectChannelDisplayName(channel);

            if ("conversion".equals(requestType)) {
                Map<String, Object> emailDataArray = new HashMap<>();
                emailDataArray.put("template", "TEMPLET-005");
                emailDataArray.put("language", determineLanguage(account.get("profile").get("languages").asText()));
                emailDataArray.put("to", List.of(Map.of(
                        "name", account.get("profile").get("firstName").asText() + " " + account.get("profile").get("lastName").asText(),
                        "mail", account.get("profile").get("email").asText()
                )));
                emailDataArray.put("fields", Map.of(
                        "$UserName", account.get("profile").get("email").asText(),
                        "$RegDate", approvalDate,
                        "$CIAM Admin", approvalAdmin
                ));

                try {
                    String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

                    HttpHeaders headers = createHeaders(
                            BeansUtil.getApplicationProperty("email.service.login"),
                            BeansUtil.getApplicationProperty("email.service.password")
                    );
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                            HttpMethod.POST,
                            request,
                            String.class
                    );

                    log.info("Email (conversion) sent successfully. Response: {}", responseEntity.getBody());
                } catch (HttpClientErrorException e) {
                    log.error("HTTP Request failed: " + e.getStatusCode() + " " + e.getStatusText());
                } catch (JsonProcessingException e) {
                    log.error("Error processing JSON", e);
                }
            }

            if (!List.of("request to join", "conversion").contains(requestType)) {
                String approverPhoneNumber = "";
                String approverEmail = "";
                String approverName = "";

                if (approverData != null) {
                    approverPhoneNumber = ""; // Optional.ofNullable(approverData.getApproverPhone()).orElse("");
                    approverEmail = Optional.ofNullable(approverData.getApproverEmail()).orElse("");
                    approverName = Optional.ofNullable(approverData.getApproverName()).orElse("");
                }

                Map<String, Object> emailDataArray = new HashMap<>();
                emailDataArray.put("template", "TEMPLET-NEW-001");
                //emailDataArray.put("template", "TEMPLET-001");
                emailDataArray.put("language", determineLanguage(account.get("profile").get("locale").asText()));
                emailDataArray.put("to", List.of(Map.of(
                        "name", account.get("profile").get("firstName").asText() + " " + account.get("profile").get("lastName").asText(),
                        "mail", account.get("profile").get("email").asText()
                )));
                emailDataArray.put("fields", Map.of(
                        "$ChannelName", channelDisplayName,
                        "$firstName", account.get("profile").get("firstName").asText(),
                        "$lastName", account.get("profile").get("lastName").asText()
                ));

                try {
                    String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

                    HttpHeaders headers = createHeaders(
                            BeansUtil.getApplicationProperty("email.service.login"),
                            BeansUtil.getApplicationProperty("email.service.password")
                    );
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                            HttpMethod.POST,
                            request,
                            String.class
                    );

                    log.info("Email sent successfully. Response: {}", responseEntity.getBody());
                } catch (HttpClientErrorException e) {
                    log.error("HTTP Request failed: " + e.getStatusCode() + " " + e.getStatusText());
                } catch (JsonProcessingException e) {
                    log.error("Error processing JSON", e);
                }
            }

            return "ok";
        } else {
            return "failed";
        }
    }

    private HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    public JsonNode getB2bOrg(String param) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("bpid", param);

        GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.getOrganizationInfo", cdcParams);
        JsonNode responseData = null;

        try {
            responseData = objectMapper.readTree((String) response.getResponseText());
        } catch (Exception e) {
            log.error("Error processing getB2bOrg response", e);
        }

        return responseData;
    }

    public String maskEmail(String email) {
        String[] emailSections = email.split("@");
        String namePart = emailSections[0];
        String domainPart = emailSections[1];

        int nameLength = namePart.length();
        String maskedName;

        if (nameLength > 3) {
            maskedName = namePart.substring(0, nameLength - 3) + "***";
        } else {
            maskedName = "*".repeat(nameLength);
        }

        int domainLength = domainPart.length();
        StringBuilder maskedDomain = new StringBuilder(domainPart);

        if (domainLength >= 2) {
            maskedDomain.setCharAt(1, '*');
        }
        if (domainLength >= 5) {
            maskedDomain.setCharAt(4, '*');
        }
        if (domainLength >= 7) {
            maskedDomain.setCharAt(6, '*');
        }

        return maskedName + "@" + maskedDomain.toString();
    }

    public boolean removeUserRoleFromB2B(String uid) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode accUser = getCdcUser(uid,0);

        JsonNode channels = accUser.path("data").path("channels");
        String department = accUser.path("data").path("userDepartment").asText("");
        String bpid = accUser.path("data").path("accountID").asText(
                accUser.path("profile").path("work").path("companyID").asText("")
        );

        List<String> approvedChannels = new ArrayList<>();
        if (channels.isObject()) {
            channels.fields().forEachRemaining(entry -> {
                JsonNode channel = entry.getValue();
                if (channel.has("approvalStatus") && "approved".equals(channel.get("approvalStatus").asText())) {
                    approvedChannels.add(entry.getKey());
                }
            });
        }
        String approvedChannelsString = String.join(",", approvedChannels);

        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("UID", uid);
        cdcParams.put("bpid", bpid);
        cdcParams.put("department", department);
        cdcParams.put("roleNames", approvedChannelsString.isEmpty() ? null : approvedChannelsString);
        cdcParams.put("status", "active");

        try {
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.setAccountOrganizationInfo", cdcParams);
            JsonNode responseData = objectMapper.readTree((String) response.getResponseText());

            if (response.getErrorCode() == 0) {
                log.info("setAccountOrganizationInfo: {}", responseData);
                return true;
            } else {
                log.info("setAccountOrganizationInfo failed: {}", responseData);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to remove user from company. {}", e.getMessage());
            return false;
        }
    }

    private void sendEmail(JsonNode account, String approvalDate, String approvalAdmin, String template, boolean isConversion) {
        Map<String, Object> emailDataArray = new HashMap<>();
        emailDataArray.put("template", template);
        emailDataArray.put("language", determineLanguage(account.path("profile").path("languages").asText()));
        emailDataArray.put("to", List.of(
                Map.of("name", account.path("profile").path("firstName").asText() + " " + account.path("profile").path("lastName").asText(),
                        "mail", account.path("profile").path("email").asText())
        ));
        emailDataArray.put("fields", Map.of(
                "$UserName", account.path("profile").path("email").asText(),
                "$RegDate", approvalDate,
                "$CIAM Admin", approvalAdmin
        ));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                emailDataArray,
                String.class
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            log.info("Email sent successfully. Response: {}", responseEntity.getBody());
        } else {
            log.error("Failed to send email. Response: {}", responseEntity.getBody());
        }
    }

    public String determineLanguage(String locale) {
        if (locale.length() == 2) {
            return locale.toLowerCase();
        }

        if (locale.length() > 2 && locale.contains("_")) {
            String[] localeArr = locale.split("_");
            return localeArr[0].toLowerCase();
        }

        return "en";
    }

    public void setAdminSession(HttpSession session) {
        String myRole = "General User";

        String cdcUid = (String) session.getAttribute("cdc_uid");
        JsonNode accUser = getCdcUser(cdcUid,0);

        Boolean isCIAMAdmin = Optional.ofNullable(accUser.path("data").path("isCIAMAdmin").asBoolean(false))
                .orElse(false);

        if(Boolean.TRUE.equals(isCIAMAdmin)) {
            myRole = "CIAM Admin";
            session.setAttribute("cdc_ciamadmin",true);
        } else {
            if (accUser != null && accUser.has("data") && accUser.get("data").has("channels")) {
                JsonNode channelsNode = accUser.get("data").get("channels");
                Iterator<Map.Entry<String, JsonNode>> fields = channelsNode.fields();

                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String key = entry.getKey();
                    JsonNode channel = entry.getValue();

                    if (channel.has("approvalStatus") && "approved".equals(channel.get("approvalStatus").asText())
                            && session.getAttribute("session_channel").equals(key)
                            && channel.has("adminType")) {

                        int adminType = channel.get("adminType").asInt();

                        if (adminType == 1) {
                            myRole = "Channel Admin";
                            session.setAttribute("cdc_channeladmin",true);
                        } else if (adminType == 2) {
                            myRole = "Channel biz Admin";
                            session.setAttribute("cdc_channeladminType",true);
                        } else if (adminType == 3) {
                            myRole = "Partner Admin";
                            session.setAttribute("cdc_companyadmin",true);
                        } else if (adminType == 0) {
                            myRole = "General User";
                        } else if (adminType == 9) {
                            myRole = "Temp Approver";
                            session.setAttribute("cdc_tempApprover",true);
                        } else {
                            myRole = "General User";
                        }
                        break;
                    }
                }
            }
        }

        session.setAttribute("btp_myrole", myRole);
    }

    public void extractChannelAddFields(JsonNode infoNode, JsonNode CDCUserProfile, String param, Map<String, Object> accountObject, Map<String, Object> regCompanyData) {
        // channel_add_field 테이블의 cdc_data_field를 참조하여 추가 필드 추출
        List<ChannelAddField> addFields = channelAddFieldRepository.selectAddFieldList(param);

        for (ChannelAddField field : addFields) {
            String cdcDataField = field.getCdcDataField();
            String tabType = field.getTabType();
            String cdcType = field.getCdcType();

            Object fieldValue = ""; // 기본값은 빈 문자열로 설정
            JsonNode currentNode = null;

            // cdcType에 따라 적절한 JSON 노드를 선택
            if ("account".equals(cdcType)) {
                currentNode = CDCUserProfile;
                // account 타입일 경우, 경로 앞에 "data."를 붙임
                cdcDataField = "data." + cdcDataField;
                // cdc_data_field의 경로에서 $channelname을 실제 채널명으로 치환
                cdcDataField = cdcDataField.replace("$channelname", param);
            } else if ("company".equals(cdcType)) {
                currentNode = infoNode;
                // company 유형의 경우 $channelname 치환을 하지 않음
            }

            // 필드가 있는지 확인하고 값을 추출
            if (currentNode != null) {
                // JSON 경로에서 값 추출
                JsonNode valueNode = currentNode.at("/" + cdcDataField.replace(".", "/"));
                if (valueNode != null && !valueNode.isMissingNode()) {
                    if (valueNode.isArray()) {
                        // 배열인 경우 값을 콤마로 구분된 문자열로 변환
                        StringBuilder arrayValues = new StringBuilder();
                        for (JsonNode node : valueNode) {
                            if (arrayValues.length() > 0) {
                                arrayValues.append(", ");
                            }
                            arrayValues.append(node.asText());
                        }
                        fieldValue = arrayValues.toString();
                    } else {
                        fieldValue = valueNode.asText();
                    }
                }
            }

            // tabType에 따라 적절한 Map에 추가
            if ("user".equalsIgnoreCase(tabType)) {
                accountObject.put(field.getElementId(), fieldValue);
            } else if ("company".equalsIgnoreCase(tabType)) {
                regCompanyData.put(field.getElementId(), fieldValue);
            }
        }
    }

    public void populateAccountObject(Map<String, String> infoMap, Map<String, Object> mapObject, List<ChannelAddField> addFields, String param) {
        if (infoMap != null) {
            for (ChannelAddField field : addFields) {
                // 필드명 예: "channels.$channelname.contactPerson.name"에서 '$channelname'을 실제 채널명으로 치환
                String elementId = field.getElementId();

                // accountMap에서 필드명에 해당하는 값이 있는지 확인
                if (infoMap.containsKey(elementId)) {
                    String fieldValue = infoMap.get(elementId);

                    // accountObject에 값 추가
                    mapObject.put(elementId, fieldValue != null ? fieldValue : "");
                }
            }
        }
    }

    public List<ChannelAddField> setDisabledAndReadonly(List<ChannelAddField> fields) {
        for (ChannelAddField field : fields) {
            if ("select".equalsIgnoreCase(field.getType())) {
                field.setDisabled("Y");
            } else if ("text".equalsIgnoreCase(field.getType())) {
                field.setReadonly("Y");
            }
        }
        return fields;
    }

    public List<ChannelAddField> setCustomDisabledAndReadonly(List<ChannelAddField> fields, Map<String, String> paramsMap) {
        for (ChannelAddField field : fields) {
            String parameterId = field.getParameterId();
            String fieldValue = paramsMap.get(parameterId);

            // paramsMap에 값이 존재하고 그 값이 빈 문자열이 아닌 경우에만 Disabled 또는 Readonly 설정
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if ("select".equalsIgnoreCase(field.getType())) {
                    field.setDisabled("Y");
                } else if ("text".equalsIgnoreCase(field.getType())) {
                    field.setReadonly("Y");
                }
            }
        }
        return fields;
    }

    // paramsMap을 받지 않는 버전의 generateFieldMap 메서드
    public Map<String, List<?>> generateFieldMap(String param, ObjectMapper objectMapper, boolean includeCompany, boolean includeUser, boolean setReadonlyDisabled) {
        return generateFieldMap(param, objectMapper, includeCompany, includeUser, setReadonlyDisabled, null);
    }

    public Map<String, List<?>> generateFieldMap(String param, ObjectMapper objectMapper, boolean includeCompany, boolean includeUser, boolean setReadonlyDisabled, @Nullable Map<String, String> paramsMap) {
        Map<String, List<?>> fieldMap = new HashMap<>();

        if (includeCompany) {
            List<ChannelAddField> channelCompanySpecShortFieldList = channelAddFieldRepository.selectFieldList(param, "company", "spec", "short");
            List<ChannelAddField> channelCompanySpecLongFieldList = channelAddFieldRepository.selectFieldList(param, "company", "spec", "long");
            List<ChannelAddField> channelCompanyAdditionalShortFieldList = channelAddFieldRepository.selectFieldList(param, "company", "additional", "short");
            List<ChannelAddField> channelCompanyAdditionalLongFieldList = channelAddFieldRepository.selectFieldList(param, "company", "additional", "long");

            if (setReadonlyDisabled) {
                channelCompanySpecShortFieldList = setDisabledAndReadonly(channelCompanySpecShortFieldList);
                channelCompanySpecLongFieldList = setDisabledAndReadonly(channelCompanySpecLongFieldList);
                channelCompanyAdditionalShortFieldList = setDisabledAndReadonly(channelCompanyAdditionalShortFieldList);
                channelCompanyAdditionalLongFieldList = setDisabledAndReadonly(channelCompanyAdditionalLongFieldList);
            } else {
                boolean useDefaultReadonlyDisabled = (paramsMap == null || paramsMap.isEmpty());
                if(!useDefaultReadonlyDisabled) {
                    channelCompanySpecShortFieldList = setCustomDisabledAndReadonly(channelCompanySpecShortFieldList, paramsMap);
                    channelCompanySpecLongFieldList = setCustomDisabledAndReadonly(channelCompanySpecLongFieldList, paramsMap);
                    channelCompanyAdditionalShortFieldList = setCustomDisabledAndReadonly(channelCompanyAdditionalShortFieldList, paramsMap);
                    channelCompanyAdditionalLongFieldList = setCustomDisabledAndReadonly(channelCompanyAdditionalLongFieldList, paramsMap);
                }
            }

            fieldMap.put("channelCompanySpecShortFieldList", groupFieldsInPairs(parseOptions(channelCompanySpecShortFieldList, objectMapper)));
            fieldMap.put("channelCompanySpecLongFieldList", parseOptions(channelCompanySpecLongFieldList, objectMapper));
            fieldMap.put("channelCompanyAdditionalShortFieldList", groupFieldsInPairs(parseOptions(channelCompanyAdditionalShortFieldList, objectMapper)));
            fieldMap.put("channelCompanyAdditionalLongFieldList", parseOptions(channelCompanyAdditionalLongFieldList, objectMapper));
        }

        if (includeUser) {
            List<ChannelAddField> channelUserSpecShortFieldList = channelAddFieldRepository.selectFieldList(param, "user", "spec", "short");
            List<ChannelAddField> channelUserSpecLongFieldList = channelAddFieldRepository.selectFieldList(param, "user", "spec", "long");
            List<ChannelAddField> channelUserAdditionalShortFieldList = channelAddFieldRepository.selectFieldList(param, "user", "additional", "short");
            List<ChannelAddField> channelUserAdditionalLongFieldList = channelAddFieldRepository.selectFieldList(param, "user", "additional", "long");

            if (setReadonlyDisabled) {
                channelUserSpecShortFieldList = setDisabledAndReadonly(channelUserSpecShortFieldList);
                channelUserSpecLongFieldList = setDisabledAndReadonly(channelUserSpecLongFieldList);
                channelUserAdditionalShortFieldList = setDisabledAndReadonly(channelUserAdditionalShortFieldList);
                channelUserAdditionalLongFieldList = setDisabledAndReadonly(channelUserAdditionalLongFieldList);
            } else {
                boolean useDefaultReadonlyDisabled = (paramsMap == null || paramsMap.isEmpty());
                if(!useDefaultReadonlyDisabled) {
                    channelUserSpecShortFieldList = setCustomDisabledAndReadonly(channelUserSpecShortFieldList, paramsMap);
                    channelUserSpecLongFieldList = setCustomDisabledAndReadonly(channelUserSpecLongFieldList, paramsMap);
                    channelUserAdditionalShortFieldList = setCustomDisabledAndReadonly(channelUserAdditionalShortFieldList, paramsMap);
                    channelUserAdditionalLongFieldList = setCustomDisabledAndReadonly(channelUserAdditionalLongFieldList, paramsMap);
                }
            }

            fieldMap.put("channelUserSpecShortFieldList", groupFieldsInPairs(parseOptions(channelUserSpecShortFieldList, objectMapper)));
            fieldMap.put("channelUserSpecLongFieldList", parseOptions(channelUserSpecLongFieldList, objectMapper));
            fieldMap.put("channelUserAdditionalShortFieldList", groupFieldsInPairs(parseOptions(channelUserAdditionalShortFieldList, objectMapper)));
            fieldMap.put("channelUserAdditionalLongFieldList", parseOptions(channelUserAdditionalLongFieldList, objectMapper));
        }

        return fieldMap;
    }

    public List<ChannelAddField> parseOptions(List<ChannelAddField> fields, ObjectMapper objectMapper) {
        for (ChannelAddField field : fields) {
            // options 필드가 null이 아니고 비어 있지 않은 경우에만 파싱
            if (field.getOption() != null && !field.getOption().isEmpty()) {
                try {
                    // options 필드를 JSON 배열로 파싱
                    List<Map<String, String>> options = objectMapper.readValue(field.getOption(), new TypeReference<List<Map<String, String>>>(){});
                    field.setOptions(options);  // 파싱된 값을 새로운 필드에 설정
                } catch (Exception e) {
                    // 예외 처리: 파싱 실패 시 로깅
                    log.error(e.getMessage());
                }
            }
        }
        return fields;
    }

    public List<List<ChannelAddField>> groupFieldsInPairs(List<ChannelAddField> fields) {
        List<List<ChannelAddField>> groupedFields = new ArrayList<>();
        for (int i = 0; i < fields.size(); i += 2) {
            List<ChannelAddField> pair = fields.subList(i, Math.min(i + 2, fields.size()));
            groupedFields.add(pair);
        }
        return groupedFields;
    }

    public void processFields(List<ChannelAddField> fields, Map<String, String> requestParams, Map<String, String> params, String param, boolean replaceChannelName) {
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String elementId = entry.getKey();
            String value = entry.getValue();

            ChannelAddField matchingField = fields.stream()
                    .filter(field -> field.getElementId().equals(elementId))
                    .findFirst()
                    .orElse(null);

            if (matchingField != null) {
                String cdcDataField = matchingField.getCdcDataField();
                if (replaceChannelName) {
                    cdcDataField = cdcDataField.replace("$channelname", param);
                }
                params.put(cdcDataField, value);
            }
        }
    }

    public void updateAccountID(String loginUID, String accountId) {
        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("UID", loginUID);

        // 데이터 필드 설정
        Map<String, String> dataFields = new HashMap<>();
        dataFields.put("accountID", accountId);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String dataJson = objectMapper.writeValueAsString(dataFields);
            cdcParams.put("data", dataJson);

            log.info("Setting account ID in CDC: {}", dataJson);

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);

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

    public void updateOrganization(String accountId, Map<String, Object> accountData) {
        // bpid 업데이트
        updateOrganizationID(accountId, (String) accountData.getOrDefault("bpid", ""));

        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("bpid", accountData.get("bpid"));
        accountData.remove("bpid");  // bpid는 다른 곳에서 이미 사용되었으므로 제거
        cdcParams.put("info", convertMapToJsonString(accountData));
        cdcParams.put("source", "CMDM");

        try {
            log.info("Updating organization in CDC with CMDM data and source to CMDM: {}", convertMapToJsonString(accountData));

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationInfo", cdcParams);

            if (response.getErrorCode() == 0) {
                log.info("Update organization info response: {}", response.getResponseText());
            } else {
                String msg = "Could not update organization info: " + response.getResponseText();
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("updateOrganization error: {}", e.getMessage());
            throw new RuntimeException("Error updating organization in CDC", e);
        }
    }

    public void updateOrganizationID(String accountId, String newAccountId) {
        // CDC 요청 파라미터 설정
        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("bpid", accountId);
        cdcParams.put("newBpid", newAccountId);

        try {
            log.info("Changing account ID in CDC to [{}].", newAccountId);

            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.b2b.setOrganizationBpid", cdcParams);

            if (response.getErrorCode() == 0) {
                log.info("Update organization ID response: {}", response.getResponseText());
            } else {
                String msg = "Could not update organization ID: " + response.getResponseText();
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("Error changing account ID in CDC: {}", e.getMessage());
            throw new RuntimeException("Error changing account ID in CDC", e);
        }
    }

    public String convertMapToJsonString(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON string: {}", e.getMessage());
            return "{}";  // 빈 JSON 객체 반환
        }
    }

    public void mapFieldsToDataFields(List<ChannelAddField> fields, String channel, Map<String, String> payload, Map<String, Object> dataFields) {
        if (fields != null && !fields.isEmpty()) {
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                String elementId = entry.getKey(); // elementId를 가져옴
                String value = entry.getValue();

                // 해당 element_id와 일치하는 필드를 찾기
                ChannelAddField matchingField = fields.stream()
                        .filter(field -> field.getElementId().equals(elementId))
                        .findFirst()
                        .orElse(null);

                if (matchingField != null) {
                    // cdc_data_field 값에 $channelname을 넣어서 최종 필드 이름을 생성
                    String cdcDataField = matchingField.getCdcDataField().replace("$channelname", channel);

                    // 최종 필드 이름으로 dataFields에 값 설정
                    dataFields.put(cdcDataField, value);
                }
            }
        }
    }

    public Map<String, Object> populateUserManagementDetails(String uid, String bpid, String channel, Map<String, String> payload, HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode CDCUserProfile = getCdcUser(uid, 0);
        Map<String, Object> regCompanyData = new HashMap<>();
        Map<String, Object> accountObject = new HashMap<>();

        // 세션에서 convertAccountId 확인
        String convertAccountId = (String) session.getAttribute("convertAccountId");
        // 세션에서 convertData 가져오기
        Map<String, Object> convertData = (Map<String, Object>) session.getAttribute("convertData");
        // Channel Type 조회
        String channelType = channelRepository.selectChannelTypeSearch(channel);

        if ("CUSTOMER".equalsIgnoreCase(channelType)) {

            // convertAccountId가 비어있거나 null인 경우 처리 안함
            if (convertAccountId != null && !convertAccountId.isEmpty()) {
                // accountSearch 호출하여 회사 정보 가져오기
                String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("acctid", bpid);
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString(channel);

                List<Map<String, Object>> accountSearchResult = cpiApiService.accountSearch(dataMap, url, responseFieldMapping, null);

                if (!accountSearchResult.isEmpty()) {
                    Map<String, Object> companyInfo = accountSearchResult.get(0);

                    // Map을 JsonNode로 변환
                    JsonNode companyInfoNode = objectMapper.convertValue(companyInfo, JsonNode.class);

                    regCompanyData.put("name", companyInfo.getOrDefault("orgName", ""));
                    regCompanyData.put("country", companyInfo.getOrDefault("country", ""));
                    regCompanyData.put("state", companyInfo.getOrDefault("state", ""));
                    regCompanyData.put("city", companyInfo.getOrDefault("city", ""));
                    regCompanyData.put("street_address", companyInfo.getOrDefault("street_address", ""));
                    regCompanyData.put("phonenumber1", companyInfo.getOrDefault("phonenumber1", ""));
                    regCompanyData.put("fax", companyInfo.getOrDefault("faxno", ""));
                    regCompanyData.put("email", companyInfo.getOrDefault("email", ""));
                    regCompanyData.put("bizregno1", companyInfo.getOrDefault("bizregno1", ""));
                    regCompanyData.put("vendorcode", companyInfo.getOrDefault("vendorcode", ""));
                    regCompanyData.put("zip_code", companyInfo.getOrDefault("zip_code", ""));
                    regCompanyData.put("validStatus", companyInfo.getOrDefault("validstatus", ""));
                    regCompanyData.put("type", companyInfo.getOrDefault("type", ""));
                    regCompanyData.put("source", companyInfo.getOrDefault("source", ""));
                    regCompanyData.put("bpid", companyInfo.getOrDefault("bpid", ""));

                    // 채널 추가 필드 추출 (companyInfoNode를 infoNode처럼 전달)
                    extractChannelAddFields(companyInfoNode, CDCUserProfile, channel, accountObject, regCompanyData);

                    // 동적 필드 추가 (convertData 사용)
                    extractDynamicCompanyFields(convertData, regCompanyData, channel);
                }
            } else {
                extractChannelAddFields(null, CDCUserProfile, channel, accountObject, regCompanyData);
                extractDynamicCompanyFields(convertData, regCompanyData, channel);
                if(session.getAttribute("secCountry")!=null && session.getAttribute("secCountry")!="") {
                    regCompanyData.put("country", (String) session.getAttribute("secCountry"));
                }
            }
        } else if ("VENDOR".equalsIgnoreCase(channelType)) {
            // 기존 로직 유지
            JsonNode companyNode = getB2bOrg(bpid);
            JsonNode infoNode = companyNode.path("info");

            regCompanyData.put("name", companyNode.path("orgName").asText(""));
            regCompanyData.put("country", infoNode.path("country").path(0).asText(""));
            regCompanyData.put("state", infoNode.path("state").path(0).asText(""));
            regCompanyData.put("city", infoNode.path("city").path(0).asText(""));
            regCompanyData.put("street_address", infoNode.path("street_address").path(0).asText(""));
            regCompanyData.put("phonenumber1", infoNode.path("phonenumber1").path(0).asText(""));
            regCompanyData.put("fax", infoNode.path("fax").path(0).asText(""));
            regCompanyData.put("email", companyNode.path("email").asText(""));
            regCompanyData.put("bizregno1", infoNode.path("bizregno1").path(0).asText(""));
            regCompanyData.put("vendorcode", companyNode.path("bpid").asText(""));
            regCompanyData.put("zip_code", infoNode.path("zip_code").path(0).asText(""));
            regCompanyData.put("validStatus", companyNode.path("status").asText(""));
            regCompanyData.put("type", companyNode.path("type").asText(""));
            regCompanyData.put("source", companyNode.path("source").asText(""));
            regCompanyData.put("bpid", companyNode.path("bpid").asText(""));

            // 채널 추가 필드 추출 (infoNode 사용)
            extractChannelAddFields(infoNode, CDCUserProfile, channel, accountObject, regCompanyData);
        }

        // 사용자 정보 세팅
        String salutation = CDCUserProfile.path("salutation").asText("");
        String language = CDCUserProfile.path("profile").path("languages").asText("");
        String firstName = CDCUserProfile.path("profile").path("firstName").asText("");
        String lastName = CDCUserProfile.path("profile").path("lastName").asText("");
        JsonNode phonesNode = CDCUserProfile.path("profile").path("phones");

        String workPhone = extractWorkPhone(phonesNode);
        String[] phoneParts = splitPhone(workPhone);

        String countryCode = phoneParts[0];
        String phoneNumber = phoneParts[1];

        String secDept = CDCUserProfile.path("data").path("userDepartment").asText("");
        String jobTitle = CDCUserProfile.path("data").path("jobtitle").asText("");

        accountObject.put("salutation", salutation);
        accountObject.put("language", language);
        accountObject.put("firstName", firstName);
        accountObject.put("lastName", lastName);
        accountObject.put("country_code_work", countryCode);
        accountObject.put("work_phone", phoneNumber);
        accountObject.put("secDept", secDept);
        accountObject.put("job_title", jobTitle);

        // 최종 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("convertObject", regCompanyData);
        result.put("accountObject", accountObject);

        return result;
    }

    public String extractWorkPhone(JsonNode phonesNode) {
        if (phonesNode.isArray()) {
            for (JsonNode phoneNode : phonesNode) {
                if ("work_phone".equals(phoneNode.path("type").asText())) {
                    return phoneNode.path("number").asText("");
                }
            }
        }
        return null;
    }

    public String[] splitPhone(String workPhone) {
        String countryCode = "";
        String phoneNumber = "";
        if (workPhone != null) {
            String[] parts = workPhone.split(" ");
            for (String part : parts) {
                if (part.startsWith("+")) {
                    countryCode = part.replace("+", "").replaceAll("\\D", "");
                } else if (!part.isEmpty()) {
                    phoneNumber = part;
                }
            }
        }
        return new String[]{countryCode, phoneNumber};
    }

    public void registerOrganization(NewCompany newCompany, Map<String, String> requestParams,HttpSession session) {
        try {
            Map<String, Object> cdcParams = new HashMap<String, Object>();
            // bizCheck 및 usedBizRegNo 처리
            String bpidValue = newCompany.getBpid();
            String[] bizCheck = bpidValue.split("-");
            log.info("bizCheck: {}", Arrays.toString(bizCheck));
            String usedBizRegNo = (bizCheck.length > 1 && "NOBIZREG".equals(bizCheck[1])) ? "" : newCompany.getBizRegNo1();
            String companyVendorCode = newCompany.getVendorCode();

            // Organization 정보를 수동으로 Map에 추가
            Map<String, Object> organizationMap = new HashMap<>();
            organizationMap.put("name", newCompany.getName());
            organizationMap.put("name_search", newCompany.getName());
            organizationMap.put("bpid", bpidValue);
            organizationMap.put("bizregno1", usedBizRegNo);
            organizationMap.put("phonenumber1", newCompany.getPhoneNumber1());
            organizationMap.put("country", newCompany.getCountry());
            organizationMap.put("street_address", newCompany.getStreetAddress().toLowerCase());
            organizationMap.put("city", newCompany.getCity());
            organizationMap.put("state", newCompany.getState());
            organizationMap.put("zip_code", newCompany.getZipCode());
            organizationMap.put("type", newCompany.getType());
            organizationMap.put("regch", newCompany.getRegCh());
            organizationMap.put("vendorcode", companyVendorCode);
            organizationMap.put("industry_type", newCompany.getIndustryType()!=null ? newCompany.getIndustryType() : "");
            organizationMap.put("products", newCompany.getProducts());
            organizationMap.put("channeltype", newCompany.getChannelType()!=null ? newCompany.getChannelType() : "" );

            // Organization 정보 JSON 문자열로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String organizationJson = objectMapper.writeValueAsString(organizationMap);
            String uid = (String) session.getAttribute("cdc_uid");

            JsonNode cdcUser = getCdcUser(uid, 0);
            String firstName = cdcUser.path("profile").path("firstName").asText("");
            String lastName = cdcUser.path("profile").path("lastName").asText("");
            String email = cdcUser.path("profile").path("email").asText("");


            // Requester 정보 JSON 문자열로 변환
            String requesterJson = objectMapper.writeValueAsString(Map.of(
                    "firstName", firstName,
                    "lastName", lastName,
                    "email", email
            ));

            // CDC 파라미터 설정
            cdcParams.put("organization", organizationJson);
            cdcParams.put("requester", requesterJson);
            cdcParams.put("status", "approved");

            // Gigya 서비스 요청 실행
            GSResponse registerOrgResponse = gigyaService.executeRequest("default", "accounts.b2b.registerOrganization", cdcParams);
            log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

            String bpid = newCompany.getBpid();

            if (registerOrgResponse.getErrorCode() == 0) {
                log.info("new company with bpid {} created", bpid);
            } else {
                log.error("new company with bpid {} create failed with error: {}", bpid, registerOrgResponse.getResponseText());
            }
        } catch (Exception e) {
            log.error("Error processing registerOrganization request", e);
        }
    }

    public String extractFirstNonEmptyValue(JsonNode node) {
        if (node.isArray() && node.size() > 0) {
            for (JsonNode element : node) {
                String value = element.asText("").trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        } else if (node.isTextual()) {
            // 단일 텍스트 값이면 그 값을 반환
            return node.asText("").trim();
        }
        return ""; // 비어있거나 필드가 없는 경우 기본값 반환
    }

    public void extractDynamicCompanyFields(Map<String, Object> companyInfo, Map<String, Object> regCompanyData, String channel) {
        // channel_add_field 테이블에서 cdc_type이 'company'인 필드만 조회
        List<ChannelAddField> addFields = channelAddFieldRepository.selectAddFieldListByCdcTypeAndChannel("company", channel);

        // element_id와 매핑되는 값을 companyInfo에서 찾아서 regCompanyData에 추가
        for (ChannelAddField field : addFields) {
            String elementId = field.getElementId();
            if (companyInfo.containsKey(elementId)) {
                regCompanyData.put(elementId, companyInfo.get(elementId));
            }
        }
    }

    public void extractDynamicCompanyCdcDataFields(Map<String, Object> companyInfo, Map<String, Object> regCompanyData, String channel) {
        // channel_add_field 테이블에서 cdc_type이 'company'인 필드만 조회
        List<ChannelAddField> addFields = channelAddFieldRepository.selectAddFieldListByCdcTypeAndChannel("company", channel);

        // element_id와 매핑되는 값을 companyInfo에서 찾아서 regCompanyData에 추가
        for (ChannelAddField field : addFields) {
            String cdcDataField = field.getCdcDataField();
            if (companyInfo.containsKey(cdcDataField)) {
                regCompanyData.put(cdcDataField, companyInfo.get(cdcDataField));
            }
        }
    }

    /*
     * 1. 메소드명: newJavaApproveUser
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    자바 승인 플로우 실행 -> 승인 요청건에 대하여 승인시 호출됨
     * 2. 사용법
     *    승인하려는 CDC사용자의 정보 및 가입유형을 파라미터로 전달하여 메소드 호출
     * 3. 예시 데이터
     *    - Input: uid = "12312" , bpId = "A123123" , channel = "sba" ,requestType = "registration" , adminType = 0
     *    - Output: 성공여부 ("ok" , "failed")
     * </PRE>
     *
     * @param uid 가입 UID
     * @param bpid 가입 사용자의 회사코드
     * @param channel 가입 채널
     * @param requestType 가입 유형
     * @param adminType 부여할 승인
     * @return 성공여부 문자열 ("ok" , "failed")
     */
    public String newJavaApproveUser(String uid, String bpid, String channel, String department, String requestType,int adminType) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        //가입채널로 채널 정보 조회
        Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
        HttpServletRequest requestServlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = requestServlet.getSession();

        if (!optionalChannelObj.isPresent()) {
            log.error("Channel not found: {}", channel);
            return "failed";
        }

        Channels channelObj = optionalChannelObj.get();

        // config를 JSON으로 파싱
        JsonNode configNode = null;
        try {
            configNode = objectMapper.readTree((String) channelObj.getConfig());
        } catch (Exception e) {
            log.error("Error parsing channel config", e);
            return "failed";
        }

        boolean useBtpAccount = configNode.path("use_btp_account").asBoolean();

        Map<String, Object> cdcParams = new HashMap<>();
        cdcParams.put("siteUID", uid);
        GSResponse response = gigyaService.executeRequest("default", "accounts.notifyLogin", cdcParams);

        log.info("notifyLogin: {}", response.getResponseText());

        try {
            JsonNode responseData = objectMapper.readTree((String) response.getResponseText());
            if (responseData.has("regToken")) {
                String regToken = responseData.get("regToken").asText();
                cdcParams.clear();
                cdcParams.put("regToken", regToken);
                GSResponse finalizeResponse = gigyaService.executeRequest("default", "accounts.finalizeRegistration", cdcParams);
                log.info("finalizeRegistration: {}", finalizeResponse.getResponseText());
            }
        } catch (Exception e) {
            log.error("Error processing notifyLogin response", e);
        }

        //CDC 계정정보 조회
        JsonNode account = getCdcUser(uid, 0);
        log.info("user for approval: {}", account.toString());

        String companyVendorCode = "";
        
        if (account.has("data") && account.get("data").has("accountID")) {
            //계정에 설정된 회사코드로 회사정보 조회
            JsonNode company = getB2bOrg(bpid);

            //CDC에 해당 회사코드로 회사정보 존재시
            if (company.has("errorCode") && company.get("errorCode").asInt() == 0) {
                
                //CUSTOMER,VENDOR 유형에 따라서 다른 필드로 설정 -> VENDOR : vendorcode, CUSTOMER : bpid
                if(company.has("type") && !"VENDOR".equals(company.get("type").asText())) {
                    companyVendorCode = company.get("bpid").asText("");
                } else {
                    companyVendorCode = company.get("info").get("vendorcode").get(0).asText();
                }
                log.info("existing company with bpid {} no new creation required", bpid);
            } else {
                //CDC에 회사 미존재시
                NewCompany newCompany = null;
                BtpAccounts btpAccount = null;
                
                //BTP 테이블 사용여부에 따라서 다른 회사정보 테이블 조회 -> VENDOR : btp_account테이블 조회, CUSTOMER : newCompany조회
                if (!useBtpAccount) {
                    newCompany = newCompanyRepository.selectByBpid(account.get("data").get("accountID").asText());
                } else {
                    btpAccount = btpAccountsRepository.selectByBpid(account.get("data").get("accountID").asText());
                }

                if (newCompany != null || btpAccount != null) {
                    try {

                        // bizCheck 및 usedBizRegNo 처리
                        String bpidValue = (newCompany != null) ? newCompany.getBpid() : btpAccount.getBpid();
                        String[] bizCheck = bpidValue.split("-");
                        log.info("bizCheck: {}", Arrays.toString(bizCheck));
                        String usedBizRegNo = (bizCheck.length > 1 && "NOBIZREG".equals(bizCheck[1])) ? "" : ((newCompany != null) ? newCompany.getBizRegNo1() : btpAccount.getBizregno1());
                        companyVendorCode = (newCompany != null) ? newCompany.getVendorCode() : btpAccount.getVendorcode();;

                        // Organization 정보를 수동으로 Map에 추가
                        Map<String, Object> organizationMap = new HashMap<>();
                        organizationMap.put("name", (newCompany != null) ? newCompany.getName() : btpAccount.getName());
                        organizationMap.put("name_search", (newCompany != null) ? newCompany.getName() : btpAccount.getName());
                        organizationMap.put("bpid", bpidValue);
                        organizationMap.put("bizregno1", usedBizRegNo);
                        organizationMap.put("phonenumber1", (newCompany != null) ? newCompany.getPhoneNumber1() : btpAccount.getPhonenumber1());
                        organizationMap.put("country", (newCompany != null) ? newCompany.getCountry() : btpAccount.getCountry());
                        organizationMap.put("street_address", ((newCompany != null) ? newCompany.getStreetAddress() : btpAccount.getStreetAddress()).toLowerCase());
                        organizationMap.put("city", (newCompany != null) ? newCompany.getCity() : btpAccount.getCity());
                        organizationMap.put("state", (newCompany != null) ? newCompany.getState() : btpAccount.getState());
                        organizationMap.put("zip_code", (newCompany != null) ? newCompany.getZipCode() : btpAccount.getZipCode());
                        organizationMap.put("type", (newCompany != null) ? newCompany.getType() : btpAccount.getType());
                        organizationMap.put("regch", (newCompany != null) ? newCompany.getRegCh() : "");
                        organizationMap.put("vendorcode", (newCompany != null) ? newCompany.getVendorCode() : btpAccount.getVendorcode());
                        organizationMap.put("industry_type", (newCompany != null) ? newCompany.getIndustryType() : "");
                        organizationMap.put("products", (newCompany != null) ? newCompany.getProducts() : "");
                        organizationMap.put("channeltype", (newCompany != null) ? newCompany.getChannelType() : "");

                        // Organization 정보 JSON 문자열로 변환
                        String organizationJson = objectMapper.writeValueAsString(organizationMap);

                        // Requester 정보 JSON 문자열로 변환
                        String requesterJson = objectMapper.writeValueAsString(Map.of(
                                "firstName", account.get("profile").get("firstName").asText().toUpperCase(),
                                "lastName", account.get("profile").get("lastName").asText().toUpperCase(),
                                "email", account.get("profile").get("email").asText()
                        ));

                        // CDC 파라미터 설정
                        cdcParams.put("organization", organizationJson);
                        cdcParams.put("requester", requesterJson);
                        cdcParams.put("status", "approved");

                        // Gigya 서비스 요청 실행
                        GSResponse registerOrgResponse = gigyaService.executeRequest("default", "accounts.b2b.registerOrganization", cdcParams);
                        log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

                        company = objectMapper.readTree((String) registerOrgResponse.getResponseText());

                        if (newCompany != null) {
                            bpid = newCompany.getBpid();
                        } else {
                            bpid = btpAccount.getBpid();
                        }

                        if (registerOrgResponse.getErrorCode() == 0) {
                            log.info("new company with bpid {} created", bpid);
                        } else {
                            log.error("new company with bpid {} create failed with error: {}", bpid, registerOrgResponse.getResponseText());
                        }
                    } catch (Exception e) {
                        log.error("Error processing registerOrganization request", e);
                    }
                } else {
                    //테이블에서 회사정보 미존재시 CMDM 회사 정보 조회하여(accountSerach) 회사정보 설정
                    log.info("new company with bpid {} not found, checking in CMDM", bpid);

                    // CMDM에서 회사 데이터를 검색하기 위한 파라미터 설정
                    Map<String, Object> searchParams = new HashMap<>();
                    searchParams.put("accountId", account.get("data").get("accountID").asText());  // CDC 계정 ID를 사용하여 CMDM에서 검색
                    CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((channel));

                    // CMDM에서 데이터 검색
                    List<Map<String, Object>> companyDataList = cpiApiService.accountSearch(
                            Collections.singletonMap("acctid", account.get("data").get("accountID").asText()),
                            BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl"),
                            responseFieldMapping,
                            session
                    );

                    if (!companyDataList.isEmpty()) {
                        // CMDM에서 검색된 첫 번째 회사 데이터를 사용
                        Map<String, Object> companyData = companyDataList.get(0);

                        Map<String, Object> organization = new HashMap<>();
                        organization.put("name", companyData.getOrDefault("orgName", ""));
                        organization.put("name_search", companyData.getOrDefault("orgName", ""));
                        organization.put("bpid", companyData.getOrDefault("bpid", ""));
                        organization.put("bizregno1", companyData.getOrDefault("bizregno1", ""));
                        organization.put("phonenumber1", companyData.getOrDefault("phonenumber1", ""));
                        organization.put("country", companyData.getOrDefault("country", ""));
                        organization.put("street_address", companyData.getOrDefault("street_address", "").toString().toLowerCase());
                        organization.put("city", companyData.getOrDefault("city", ""));
                        organization.put("state", companyData.getOrDefault("state", ""));
                        organization.put("zip_code", companyData.getOrDefault("zip_code", ""));
                        organization.put("type", "CMDM");
                        organization.put("regch", companyData.getOrDefault("regch", ""));
                        //organization.put("vendorcode", companyData.getOrDefault("vendorcode", ""));
                        organization.put("industry_type", companyData.getOrDefault("industry_type", ""));
                        organization.put("channeltype", companyData.getOrDefault("channeltype", ""));

                        Map<String, Object> companyParams = (Map<String, Object>) session.getAttribute("companyParams");
                        if(companyParams!=null && companyParams.size() > 0) {
                            extractDynamicCompanyCdcDataFields(companyParams, organization, channel);
                        }

                        try {
                            String organizationJson = objectMapper.writeValueAsString(organization);

                            //회사 CDC 신규등록시 필요한 등록자명 설정
                            Map<String, Object> requester = new HashMap<>();
                            requester.put("firstName", account.get("profile").get("firstName").asText().toUpperCase());
                            requester.put("lastName", account.get("profile").get("lastName").asText().toUpperCase());
                            requester.put("email", account.get("profile").get("email").asText());

                            String requesterJson = objectMapper.writeValueAsString(requester);

                            // CDC 파라미터 설정
                            cdcParams.clear();
                            cdcParams.put("organization", organizationJson);
                            cdcParams.put("requester", requesterJson);
                            cdcParams.put("status", "approved");

                            // CDC에 회사 등록 요청
                            GSResponse registerOrgResponse = gigyaService.executeRequest("default", "accounts.b2b.registerOrganization", cdcParams);
                            log.info("registerOrganization: {}", registerOrgResponse.getResponseText());

                            bpid = (String) companyData.getOrDefault("bpid", "");

                            if (registerOrgResponse.getErrorCode() == 0) {
                                log.info("new CMDM company with bpid {} created in CDC", bpid);
                            } else {
                                log.error("new CMDM company with bpid {} create in CDC failed with error: {}", bpid, registerOrgResponse.getResponseText());
                            }
                        } catch (Exception e) {
                            log.error("Error processing registerOrganization request", e);
                        }
                    } else {
                        log.error("Company not found in CMDM for accountID: {}", account.get("data").get("accountID").asText());
                    }
                }

            }

        }

        //파라미터 초기화
        cdcParams.clear();
        String contactId = "";
        String accountId = "";

        //CDC 계정정보및 회사정보 조회
        JsonNode accountNode = getCdcUser(uid, 0);
        JsonNode companyNode = getB2bOrg(bpid);

        //채널유형이 CUSTOMER, AD가입이 아닐경우에 실행
        if("CUSTOMER".equals(channelObj.getChannelType()) && !"adRegistration".equals(requestType)) {

            if (companyNode.has("type") && !"VENDOR".equals(companyNode.get("type").asText()) && accountNode.path("data").has("accountID")) {
                log.info("user with no contactID detected, creating new contact in CMDM");

                List<Map<String, Object>> cpiResut = new ArrayList<>();
                Map<String, Object> accountData = objectMapper.convertValue(companyNode, Map.class);

                //CMDM 회사 검색을 위한 CPI URL 조회
                String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");

                Map<String,Object> dataMap = new HashMap<String,Object>();
                dataMap.put("acctid",accountData.get("bpid"));

                //CPI Response 값 설정
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString(channel);

                //CMDM 회사 검색 -> accountId로 조회
                cpiResut = cpiApiService.accountSearch(dataMap, url, responseFieldMapping,session);

                boolean isNewCompany = true;
                isNewCompany = cpiResut.isEmpty();

                //신규회사 여부 체크 -> 가입시 새로 회사 등록한 경우
                if(isNewCompany) {
                    NewCompany newCompany = newCompanyRepository.selectByBpid(bpid);
                    
                    //CMDM 신규회사 등록 CPI 호출
                    Map<String, Object> createdAccount = cpiApiService.createAccount(channel, "Context1", objectMapper.convertValue(companyNode, Map.class), session,newCompany);

                    Map<String, Object> accountMap = (Map<String, Object>) createdAccount.get("account");
                    Map<String, Object> wfobj = (Map<String, Object>) createdAccount.get("wfobj");  // 단일 Map으로 처리

                    //CMDM 회사 등록후 생성된 ACCOUNT 정보 조회
                    if (accountMap != null && !accountMap.isEmpty()) {
                        accountId = (String) accountMap.get("acctid");
                        newCompany.setBpidInCdc(accountId);

                        //새로 만들어진 accountID 신규회사 관리테이블에 등록
                        newCompanyRepository.save(newCompany);

                        updateAccountID(uid, newCompany.getBpidInCdc());
                        accountNode = getCdcUser(uid, 0);

                        Map<String, Object> orgInfo = new HashMap<>();
                        orgInfo.put("bpid", accountId);
                        orgInfo.put("wfstate", wfobj.get("wfstate"));  // 단일 Map으로 처리
                        orgInfo.put("identifystatus", account.get("identifystatus"));
//                        orgInfo.put("industry_type", newCompany.getIndustryType());
//                        orgInfo.put("products", newCompany.getProducts());
//                        orgInfo.put("channeltype", newCompany.getChannelType());

                        // CDC에 등록된 호사정보 변경
                        updateOrganization(bpid, orgInfo);

                        //권한부여 테이블에 account ID 변경
                        approvalAdminRepository.updateApprovalCompanyCode(uid,accountId);
                        List<String> uidList = wfMasterRepository.selectCompanyCodeUidList(bpid);
                        if (!uidList.isEmpty()) {
                            uidList.stream()
                                    .filter(requestorId -> !requestorId.equals(uid)) // uid와 동일하지 않은 경우만 필터링
                                    .forEach(requestorId -> updateAccountID(requestorId, newCompany.getBpidInCdc()));
                        }
                        wfMasterRepository.updateWfMasterCompanyCode(bpid,accountId);
                    }
                }

                // createContact 호출하여 새로운 Contact 생성 -> Contact생성은 CUSTOMER일때 신규여부 상관없이 무조건 호출
                Map<String, Object> createdContact = cpiApiService.createContact("Context1", objectMapper.convertValue(accountNode, Map.class), session,channel);

                if (createdContact != null && createdContact.get("contact") instanceof List) {

                    //응답값 타입에 따른 체크 -> LIST OR MAP
                    List<Map<String, Object>> contacts = (List<Map<String, Object>>) createdContact.get("contact");
                    if (!contacts.isEmpty()) {
                        //만들어진 contactID값 조회
                        contactId = contacts.get(contacts.size() - 1).get("contactid").toString();
                    }
                } else if (createdContact != null && createdContact.get("contact") instanceof Map) {
                    Map<String, Object> cmdmContact = (Map<String, Object>) createdContact.get("contact");
                    contactId = cmdmContact.getOrDefault("contactid", "").toString();
                }

                log.info("creating new contact in CMDM done, new contactID: {}", contactId);

            }
        }

        //CDC 정보 수정을 위한 파라미터 설정
        cdcParams.put("UID", uid);
        String approvalStatus = "request to join".equals(requestType) ? "inviteSent" : "approved";
        String userStatus = "request to join".equals(requestType) ? "inviteSent" : "active";

        Map<String, Object> dataFields = new HashMap<>();

        dataFields.put("lastTenureCheck",ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        //채널정보 설정
        dataFields.put("channels", Map.of(
                channel, Map.of(
                        "adminType", adminType,
                        "approvalStatus", approvalStatus,
                        "approvalStatusDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "lastLogin", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
        ));
        dataFields.put("mLoginID", maskEmail(account.get("profile").get("email").asText()));

        //채널유형(VENDOR,CUSTOMER)에 따른 값 설정
        if("VENDOR".equals(channelObj.getChannelType())) {
            dataFields.put("vendorCode", companyVendorCode);
        }
        dataFields.put("userStatus", userStatus);
        if("CUSTOMER".equals(channelObj.getChannelType()) && !"adRegistration".equals(requestType)) {
            dataFields.put("contactID", contactId);
        }

        try {
            //CDC 데이터및 preferences설정
            cdcParams.put("data", objectMapper.writeValueAsString(dataFields));
            cdcParams.put("preferences", objectMapper.writeValueAsString(Map.of(
                    "internal." + channel, Map.of("isConsentGranted", true),
                    "internal.mypage", Map.of("isConsentGranted", true)
            )));
        } catch (Exception e) {
            log.error("Error processing data fields", e);
        }

        //CDC 정보 변경 진행
        GSResponse setAccountResponse = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
        log.info("setAccountInfo: {}", setAccountResponse.getResponseText());

        //프로비저닝 호출 -> ad가입 제외
        if (!"adRegistration".equals(requestType)) {
            // Optional에서 Channels 객체를 가져옴
            Map<String, Object> configMap = channelObj.getConfigMap();

            Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                    ? (Boolean) configMap.get("java_useprovisioning")
                    : false;

            // userProvisioning이 true일 때만 프로비저닝 실행
            if (Boolean.TRUE.equals(userProvisioning)) {
                String operationType = getOperationType(requestType);

                if (operationType != null) {
                    cpiApiService.sendUidProvisioningNoConfigChecking(channel, operationType, uid);
                    log.info("Provisioning send end for requestType: {}", requestType);
                }
            }
        }

        removeUserRoleFromB2B(uid);
        account = getCdcUser(uid, 0);

        //정보 수정 완료시에 수행->이메일 발송
        if (setAccountResponse.getErrorCode() == 0) {

            ChannelApprovalStatuses approverData = channelApprovalStatusesRepository
                    .selectFirstByLoginUidAndChannelAndStatus(uid, channel, "approved")
                    .orElse(null);

            String approvalDate = Optional.ofNullable(approverData)
                    .map(ChannelApprovalStatuses::getApprovalDate)
                    .map(LocalDateTime::toString)
                    .orElse(LocalDateTime.now().toString());
            String approvalAdmin = approverData != null ? approverData.getApproverName() : "Channel Admin";

            String channelDisplayName = channelRepository.selectChannelDisplayName(channel);

            //전환일시 전환 안내 메일 발송
            if ("conversion".equals(requestType)) {
                Map<String, Object> emailDataArray = new HashMap<>();
                emailDataArray.put("template", "TEMPLET-005");
                emailDataArray.put("language", determineLanguage(account.get("profile").get("languages").asText()));
                emailDataArray.put("to", List.of(Map.of(
                        "name", account.get("profile").get("firstName").asText() + " " + account.get("profile").get("lastName").asText(),
                        "mail", account.get("profile").get("email").asText()
                )));
                emailDataArray.put("fields", Map.of(
                        "$UserName", account.get("profile").get("email").asText(),
                        "$RegDate", approvalDate,
                        "$CIAM Admin", approvalAdmin
                ));

                try {
                    String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

                    //메일발송 서버 login,password설정
                    HttpHeaders headers = createHeaders(
                            BeansUtil.getApplicationProperty("email.service.login"),
                            BeansUtil.getApplicationProperty("email.service.password")
                    );
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

                    //메일서버 URL 조회후 API 호출하여 발송 수행
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                            HttpMethod.POST,
                            request,
                            String.class
                    );

                    log.info("Email (conversion) sent successfully. Response: {}", responseEntity.getBody());
                } catch (HttpClientErrorException e) {
                    log.error("HTTP Request failed: " + e.getStatusCode() + " " + e.getStatusText());
                } catch (JsonProcessingException e) {
                    log.error("Error processing JSON", e);
                }
            }

            //신규가입일 경우 신규가입 안내 메일 발송
            if (!List.of("request to join", "conversion").contains(requestType)) {
                String approverPhoneNumber = "";
                String approverEmail = "";
                String approverName = "";

                if (approverData != null) {
                    approverPhoneNumber = ""; // Optional.ofNullable(approverData.getApproverPhone()).orElse("");
                    approverEmail = Optional.ofNullable(approverData.getApproverEmail()).orElse("");
                    approverName = Optional.ofNullable(approverData.getApproverName()).orElse("");
                }

                Map<String, Object> emailDataArray = new HashMap<>();
                emailDataArray.put("template", "TEMPLET-NEW-001");
                //emailDataArray.put("template", "TEMPLET-001");
                emailDataArray.put("language", determineLanguage(account.get("profile").get("locale").asText()));
                emailDataArray.put("to", List.of(Map.of(
                        "name", account.get("profile").get("firstName").asText() + " " + account.get("profile").get("lastName").asText(),
                        "mail", account.get("profile").get("email").asText()
                )));
                emailDataArray.put("fields", Map.of(
                        "$ChannelName", channelDisplayName,
                        "$firstName", account.get("profile").get("firstName").asText(),
                        "$lastName", account.get("profile").get("lastName").asText()
                ));

                try {
                    String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

                    //메일발송 서버 login,password설정
                    HttpHeaders headers = createHeaders(
                            BeansUtil.getApplicationProperty("email.service.login"),
                            BeansUtil.getApplicationProperty("email.service.password")
                    );
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

                    //메일서버 URL 조회후 API 호출하여 발송 수행
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                            HttpMethod.POST,
                            request,
                            String.class
                    );

                    log.info("Email sent successfully. Response: {}", responseEntity.getBody());
                } catch (HttpClientErrorException e) {
                    log.error("HTTP Request failed: " + e.getStatusCode() + " " + e.getStatusText());
                } catch (JsonProcessingException e) {
                    log.error("Error processing JSON", e);
                }
            }

            return "ok";
        } else {
            return "failed";
        }
    }

    /*
     * 1. 메소드명: setIndustryType
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    회사정보 -> IndustryType 배열형식의 문자열 조회
     * 2. 사용법
     *    회사정보 MAP 값과 industryType 필드값을 문자열 리스트로 전달하여 메소드 호출
     * 3. 예시 데이터
     *    - Input: organization {"vendorcode" : "t1231" , "channeltype" : "Sales Agent" }
     * </PRE>
     *
     * @param organization 회사 정보가 담긴 MAP
     * @param industryArray industryArray 필드값이 담긴 문자열 리스트
     */
    public static void setIndustryType(Map<String, Object> organization, List<String> industryArray) {
        String industryArrayString = industryArray.stream()
                .map(item -> "\"" + item + "\"") // 각 요소를 따옴표로 감싸기
                .collect(Collectors.joining(", ", "[", "]")); // 배열 형식으로 변환

        // 변환된 값을 organization에 추가
        organization.put("industry_type", industryArrayString);
    }

    /*
     * 1. 메소드명: getOperationType
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    프로비저닝 호출 전 가입 유형(신규가입,전환가입,확장가입,초대가입)에 따라서 프로비저닝 요청 유형 조회
     * 2. 사용법
     *    가입 유형을 파라미터로 전달하여 메소드 호출
     * 3. 예시 데이터
     *    - Input: requestType = "registration"
     *    - Output: "I" -> 프로비저닝 요청 유형 "I" 조회
     * </PRE>
     *
     * @param requestType 가입 유형
     * @return 프로비저닝 요청 유형 ( I,T ) 
     */
    public String getOperationType(String requestType) {
        switch (requestType) {
            case "conversion":
                return "T";
            case "registration":
            case "invitation":
            case "ssoAccess":
                return "I";
            default:
                return requestType;
        }
    }

    /*
     * 1. 메소드명: setAccountStatus
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CDC 계정 정보 수정 -> CDC에서 inactive된 채널 혹은 계정으로 로그인시 최근 로그인날짜,유저상태(userStatus),채널상태(approvalStatus) 변경
     * 2. 사용법
     *    로그인한 UID와 로그인채널을 파라미터로 전달하여 메소드 호출
     * 3. 예시 데이터
     *    - Input: payload = {"channel" : "sba" , "uid" : "3123123" }
     *    - Output: 성공여부 문자열 값
     * </PRE>
     *
     * @param payload 이메일,채널 정보가 담긴 MAP변수
     * @return 성공여부 문자열 값 ( Y,N )
     */
    public String setAccountStatus(Map<String, String> payload, HttpSession session, RedirectAttributes redirectAttributes) {
        String channel = payload.get("channel");
        String uid = payload.get("uid");
        ObjectMapper mapper = new ObjectMapper();
        try{
            if(!"btp".equals(channel)) {
                JsonNode accUser = getCdcUser(uid,0);
                Map<String, Object> data = mapper.convertValue(accUser.get("data"), Map.class);
                Map<String, Object> channels = mapper.convertValue(data.get("channels"), Map.class);
                Map<String, Object> channelMapData = mapper.convertValue(channels.get(payload.get("channel")), Map.class);

                String userStatus = (String) data.get("userStatus");
                String channelApprovalStatus = (String) channelMapData.get("approvalStatus");

                if("inactive".equals(channelApprovalStatus) || "inactive".equals(userStatus)) {
                    Map<String, Object> dataFields = new HashMap<>();
                    Map<String, Object> cdcParams = new HashMap<>();

                    String lastLogin = Instant.now()
                            .atOffset(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.MILLIS)  // 밀리초까지 표시하고 나머지 자릿수 제거
                            .format(DateTimeFormatter.ISO_INSTANT);

                    cdcParams.put("UID", uid);
                    dataFields.put("userStatus", "active");  // userStatus만 업데이트

                    dataFields.put("channels", Map.of(
                            channel, Map.of(
                                    "approvalStatus", "approved","lastLogin",lastLogin
                            )
                    ));
                    cdcParams.put("data", mapper.writeValueAsString(dataFields));

                    GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", cdcParams);
                    log.info("Account status updated to for UID: {}. Response: {}",  uid, response.getResponseText());
                }
            }
        }
        catch (Exception e) {
            log.error("Error updating account status for UID: {}", e);
        }

        return "Y";
    }

    /*
     * 1. 메소드명: resetPassword
     * 2. 클래스명: CdcTraitService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    CDC 패스워드 초기화 -> 로그인쪽에서 최근 로그인 90일이 지난 계정에 한해서 호출됨
     * 2. 사용법
     *    패스워드 초기화 하려는 이메일을 파라미터로 전달하여 메소드 호출
     * 3. 예시 데이터
     *    - Input: email = "jeonghwan@naver.com"
     *    - Output: 성공여부 문자열 값
     * </PRE>
     *
     * @param email 초기화할 CDC 이메일 계정
     * @return 성공여부 문자열 값 ( Y,N )
     */
    public String resetPassword(String email) {
        Map<String, Object> cdcParams = new HashMap<>();
        String success = "N";
        try {
            // CDC 파라미터 설정
            cdcParams.put("loginID", email);
            // CDC 요청 실행
            GSResponse response = gigyaService.executeRequest("default", "accounts.resetPassword", cdcParams);

            //응답값 설정
            if (response.getErrorCode() == 0) {
                success = "Y";
            }
            
            log.info("Account status updated to {} for email: {}. Response: {}", email,response.getResponseText());
        } catch (Exception e) {
            log.error("Error updating account status for email: {}", email, e);
        }

        return success;
    }

    public String updateConsents(@RequestBody Map<String, Object> requestParams, HttpSession session) {
        String uid = (String) requestParams.get("uid");
        String channel = (String) requestParams.get("channel");
        String language = (String) requestParams.get("language");
        String subsidiary = (String) requestParams.get("subsidiary");
        boolean marketingConsent = (Boolean) requestParams.getOrDefault("marketingConsent", false);

        // 약관 정보 가져오기
        Map<String, Object> consentData = consentSelector(uid, channel, language, subsidiary);

        // CDC에 약관 업데이트 준비
        Map<String, Object> preferencesFields = new HashMap<>();
        preferencesFields.put((String) consentData.get("termsCommon"), Map.of("isConsentGranted", true));
        //preferencesFields.put((String) consentData.get("termsChannel"), Map.of("isConsentGranted", true));
        preferencesFields.put((String) consentData.get("privacyCommon"), Map.of("isConsentGranted", true));
        //preferencesFields.put((String) consentData.get("privacyChannel"), Map.of("isConsentGranted", true));

        if (marketingConsent) {
            preferencesFields.put((String) consentData.get("marketingCommon"), Map.of("isConsentGranted", true));
        } else {
            preferencesFields.put((String) consentData.get("marketingCommon"), Map.of("isConsentGranted", false));
        }

        // Gigya API 호출
        Map<String, Object> gigyaParams = new HashMap<>();
        try {
            gigyaParams.put("preferences", new ObjectMapper().writeValueAsString(preferencesFields));
        } catch (JsonProcessingException e) {
            log.error("Error serializing preferences fields to JSON", e);
            return "N"; // JSON 처리 실패 시 N 반환
        }
        gigyaParams.put("UID", uid);

        GSResponse response = gigyaService.executeRequest(channel, "accounts.setAccountInfo", gigyaParams);

        if (response.getErrorCode() == 0) {
            // 동의 내용 업데이트
            consentResponseUpdated(Long.valueOf((String) consentData.get("commonTermConsentId")),
                    Long.valueOf((String) consentData.get("commonTermContentId")), uid);
//            consentResponseUpdated(Long.valueOf((String) consentData.get("channelTermsConsentId")),
//                    Long.valueOf((String) consentData.get("channelTermsContentId")), uid);
            consentResponseUpdated(Long.valueOf((String) consentData.get("commonPrivacyConsentId")),
                    Long.valueOf((String) consentData.get("commonPrivacyContentId")), uid);
//            consentResponseUpdated(Long.valueOf((String) consentData.get("channelPrivacyConsentId")),
//                    Long.valueOf((String) consentData.get("channelPrivacyContentId")), uid);

            // 마케팅 동의 여부에 따라 처리
            if (marketingConsent) {
                consentResponseUpdated(Long.valueOf((String) consentData.get("marketingConsentid")),
                        Long.valueOf((String) consentData.get("marketingcontentid")), uid);
            } else {
                saveRejectedConsent(Long.valueOf((String) consentData.get("marketingConsentid")),
                        Long.valueOf((String) consentData.get("marketingcontentid")), uid);
            }

            return "Y"; // 성공 시 Y 반환
        } else {
            log.error("Consent update failed for UID: {}", uid);
            return "N"; // 실패 시 N 반환
        }
    }

    public void consentResponseUpdated(Long consentId, Long consentContentId, String uid) {
        UserAgreedConsents termsAgreed = new UserAgreedConsents();
        termsAgreed.setConsentId(consentId);
        termsAgreed.setConsentContentId(consentContentId);
        termsAgreed.setUid(uid);
        termsAgreed.setStatus("agreed");
        userAgreedConsentsRepository.save(termsAgreed);
    }

    public void saveRejectedConsent(Long consentId, Long consentContentId, String uid) {
        UserAgreedConsents termsAgreed = new UserAgreedConsents();
        termsAgreed.setConsentId(consentId);
        termsAgreed.setConsentContentId(consentContentId);
        termsAgreed.setUid(uid);
        termsAgreed.setStatus("rejected");
        userAgreedConsentsRepository.save(termsAgreed);
    }

    @Async
    public void sendMailsAsync(
            List<Long> consentContentIds,
            List<ConsentContent> consentContentList,
            ConsentRepository consentRepository,
            UserAgreedConsentsRepository userAgreedConsentsRepository,
            MailService mailService,
            CdcTraitService cdcTraitService) {

        //AtomicInteger counter = new AtomicInteger(0); // 카운트 변수 초기화
        //AtomicInteger counter2 = new AtomicInteger(0); // 카운트 변수 초기화

        for (Long consentContentId : consentContentIds) {
            // 사용자 동의 정보 조회
            List<String> userUids = userAgreedConsentsRepository.selectDistinctUidsByConsentContentId(consentContentId);

            for (String uid : userUids) {
                // consentContentList에서 일치하는 항목을 찾기
                Optional<ConsentContent> matchedConsentContent = consentContentList.stream()
                        .filter(consentContent -> consentContent.getConsentId().equals(consentRepository.selectConsentIdByConsentContentId(consentContentId)))
                        .findFirst();

                // 일치하는 consentContent가 없으면 건너뛰기
                if (matchedConsentContent.isEmpty()) continue;

                ConsentContent consentContent = matchedConsentContent.get();

                boolean consentAlreadyAgreed = userAgreedConsentsRepository.existsByUidAndConsentContentId(uid, consentContent.getId());
                if (consentAlreadyAgreed) continue; // 이미 동의한 사용자 제외

                try {
                    //int currentCount = counter.incrementAndGet();
                    Consent consent = consentRepository.selectCoverageById(consentContent.getConsentId());
                    JsonNode cdcUser = cdcTraitService.getCdcUser(uid, 0);

                    // 사용자 상태 확인 (유효한 사용자)
                    boolean isValidUser = cdcUser.has("errorCode")
                            && cdcUser.get("errorCode").asInt() == 0
                            && "active".equals(cdcUser.path("data").path("userStatus").asText(""));

                    if (isValidUser) {
                        //System.out.println("UID : " + uid);
                        //int currentCount2 = counter2.incrementAndGet();

                        Map<String, Object> paramArr = new HashMap<>();
                        if ("privacy".equals(consent.getTypeId())) {
                            paramArr.put("template", "TEMPLET-013");
                            paramArr.put("channel", consent.getCoverage());
                            paramArr.put("CIAM Admin", consent.getCoverage());
                        } else if ("terms".equals(consent.getTypeId())) {
                            paramArr.put("template", "TEMPLET-014");
                            paramArr.put("channel", consent.getCoverage());
                            paramArr.put("CIAM Admin", consent.getCoverage());
                        } else {
                            log.warn("Unknown consent type for UID: {}, type: {}", uid, consent.getTypeId());
                            continue; // 알 수 없는 consent type에 대해서는 건너뛰기
                        }

                        paramArr.put("cdc_uid", uid);

                        mailService.sendMail(paramArr);
                        log.info("Mail Sent to UID: {} via template: {}", uid, paramArr.get("template"));
                    } else {
                        log.warn("Skipping mail for UID: {} due to invalid user status or errorCode.", uid);
                    }
                } catch (Exception e) {
                    log.error("Failed to send mail for UID: {}", uid, e);
                }
            }
        }
    }
}

