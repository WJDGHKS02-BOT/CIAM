package com.samsung.ciam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.dto.ConsentStatusDTO;
import com.samsung.ciam.dto.UserAgreedConsentDTO;
import com.samsung.ciam.models.Channels;
import com.samsung.ciam.models.Consent;
import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.models.UserAgreedConsents;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.repositories.ConsentContentRepository;
import com.samsung.ciam.repositories.ConsentRepository;
import com.samsung.ciam.repositories.UserAgreedConsentsRepository;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsentProfileService {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ConsentContentRepository consentContentRepository;

    @Autowired
    private UserAgreedConsentsRepository userAgreedConsentsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CpiApiService cpiApiService;


    public ModelAndView index(HttpSession session, Model model) {
        String cdcUid = (String) session.getAttribute("cdc_uid");
        JsonNode accUser = cdcTraitService.getCdcUser(cdcUid, 0);
        List<String> euCountries = Arrays.asList("AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE");

        ConsentStatusDTO consentStatusDTO = new ConsentStatusDTO();
        consentStatusDTO.setCommonTermsId("");
        consentStatusDTO.setCommonPrivacyId("");
        consentStatusDTO.setCommonMarketingId("");
        consentStatusDTO.setUserAgreedMarketingId("");
        consentStatusDTO.setCommonTerms(ConsentContent.notFound());
        consentStatusDTO.setCommonPrivacy(ConsentContent.notFound());
        consentStatusDTO.setCommonMarketing(ConsentContent.notFound());
        consentStatusDTO.setCommonTermsAgreed(false);
        consentStatusDTO.setCommonPrivacyAgreed(false);
        consentStatusDTO.setCommonMarketingAgreed(false);
        consentStatusDTO.setCommonTermsAgreedDate("");
        consentStatusDTO.setCommonPrivacyAgreedDate("");
        consentStatusDTO.setCommonMarketingAgreedDate("");

        List<UserAgreedConsents> userAgreedConsentIds = userAgreedConsentsRepository.selectGroupedConsentsByUid(cdcUid);
        List<Long> consentIds = userAgreedConsentIds.stream()
                .map(UserAgreedConsents::getConsentId)
                .collect(Collectors.toList());
        List<Consent> consents = consentRepository.selectConsentList(consentIds);

        for (Consent consent : consents) {
            if ("common".equals(consent.getCoverage())) {
                if ("terms".equals(consent.getTypeId())) {
                    processCommonTerms(session, cdcUid, consentStatusDTO, consent, userAgreedConsentIds);
                } else if ("privacy".equals(consent.getTypeId())) {
                    processCommonPrivacy(session, cdcUid, consentStatusDTO, consent, userAgreedConsentIds);
                } else if ("b2b".equals(consent.getTypeId())) {
                    processCommonMarketing(session, accUser, consentStatusDTO, consent, userAgreedConsentIds);
                }
            }
        }

        JsonNode channels = accUser.path("data").path("channels");
        String sessionChannel = (String) session.getAttribute("session_channel");

        channels.fieldNames().forEachRemaining(key -> {
            JsonNode channel = channels.path(key);
            if (sessionChannel != null && sessionChannel.equals(key)) {
                if (channel.has("approvalStatus") && "approved".equals(channel.path("approvalStatus").asText())) {
                    processChannel((ObjectNode) channel, key, session, cdcUid, userAgreedConsentIds, consents, consentStatusDTO);
                }
            }
        });

        // 날짜 형식 지정
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Object> gridData = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> userConsentHistory = new ArrayList<>();
        String country = accUser.path("profile").path("country").asText("");

        // Common Terms 그리드 설정
        Map<String, Object> commonTermsMap = new HashMap<>();
        commonTermsMap.put("consent", "Common Terms");
        commonTermsMap.put("version", consentStatusDTO.getCommonTerms() != null ? formatVersion(consentStatusDTO.getCommonTerms().getVersion()) : ""); // 버전 형식 변경
        commonTermsMap.put("releaseDate", consentStatusDTO.getCommonTerms() != null ? formatDate(consentStatusDTO.getCommonTerms().getCreatedAt(), dateFormatter) : ""); // 날짜 형식 변경
        if ("KR".equals(country)) {
            commonTermsMap.put("location", "Korea");
        } else {
            commonTermsMap.put("location", "Outside Korea");
        }
        commonTermsMap.put("agreementDate", consentStatusDTO.getCommonTermsAgreedDate() != null ? formatStringToDate(consentStatusDTO.getCommonTermsAgreedDate(), dateFormatter) : ""); // 날짜 형식 변경
        commonTermsMap.put("agreement", formatAgreement(consentStatusDTO.isCommonTermsAgreed())); // agreement 값 변경
        commonTermsMap.put("termsId", consentStatusDTO.getCommonTermsId() != null ? consentStatusDTO.getCommonTermsId().toString() : "");
        commonTermsMap.put("consentId", consentStatusDTO.getCommonTerms() != null && consentStatusDTO.getCommonTerms().getConsentId() != null
                ? consentStatusDTO.getCommonTerms().getConsentId().toString()
                : "");
        commonTermsMap.put("content", consentStatusDTO.getCommonTerms() != null ? consentStatusDTO.getCommonTerms().getContent() : "");
        commonTermsMap.put("requiredYn", "Y");
        results.add(commonTermsMap);

        // Common Privacy 그리드 설정
        Map<String, Object> commonPrivacyMap = new HashMap<>();
        commonPrivacyMap.put("consent", "Common Privacy");
        commonPrivacyMap.put("version", consentStatusDTO.getCommonPrivacy() != null ? formatVersion(consentStatusDTO.getCommonPrivacy().getVersion()) : ""); // 버전 형식 변경
        commonPrivacyMap.put("releaseDate", consentStatusDTO.getCommonPrivacy() != null ? formatDate(consentStatusDTO.getCommonPrivacy().getCreatedAt(), dateFormatter) : ""); // 날짜 형식 변경
        if ("KR".equals(country)) {
            commonPrivacyMap.put("location", "Korea");
        } else if ("US".equals(country)) {
            commonPrivacyMap.put("location", "USA");
        } else if (euCountries.contains(country)) {
            commonPrivacyMap.put("location", "EU");
        } else if ("LA".equals(country)) {
            commonPrivacyMap.put("location", "Lao People's Democratic Republic");
        } else {
            commonPrivacyMap.put("location", "Outside EU");
        }
        commonPrivacyMap.put("agreementDate", consentStatusDTO.getCommonPrivacyAgreedDate() != null ? formatStringToDate(consentStatusDTO.getCommonPrivacyAgreedDate(), dateFormatter) : ""); // 날짜 형식 변경
        commonPrivacyMap.put("agreement", formatAgreement(consentStatusDTO.isCommonPrivacyAgreed())); // agreement 값 변경
        commonPrivacyMap.put("termsId", consentStatusDTO.getCommonPrivacyId() != null ? consentStatusDTO.getCommonPrivacyId().toString() : "");
        // commonPrivacy 수정
        commonPrivacyMap.put("consentId", consentStatusDTO.getCommonPrivacy() != null && consentStatusDTO.getCommonPrivacy().getConsentId() != null
                ? consentStatusDTO.getCommonPrivacy().getConsentId().toString()
                : "");
        commonPrivacyMap.put("content", consentStatusDTO.getCommonPrivacy() != null ? consentStatusDTO.getCommonPrivacy().getContent() : "");
        commonPrivacyMap.put("requiredYn", "Y");
        results.add(commonPrivacyMap);

        // Common Marketing 그리드 설정
        Map<String, Object> commonMarketingMap = new HashMap<>();
        commonMarketingMap.put("consent", "Common Marketing");
        commonMarketingMap.put("version", consentStatusDTO.getCommonMarketing() != null ? formatVersion(consentStatusDTO.getCommonMarketing().getVersion()) : ""); // 버전 형식 변경
        commonMarketingMap.put("releaseDate", consentStatusDTO.getCommonMarketing() != null ? formatDate(consentStatusDTO.getCommonMarketing().getCreatedAt(), dateFormatter) : ""); // 날짜 형식 변경
        commonMarketingMap.put("location", "All countries");
        commonMarketingMap.put("agreementDate", consentStatusDTO.getCommonMarketingAgreedDate() != null ? formatStringToDate(consentStatusDTO.getCommonMarketingAgreedDate(), dateFormatter) : ""); // 날짜 형식 변경
        commonMarketingMap.put("agreement", formatAgreement(consentStatusDTO.isCommonMarketingAgreed())); // agreement 값 변경
        commonMarketingMap.put("termsId", consentStatusDTO.getCommonMarketingId() != null ? consentStatusDTO.getCommonMarketingId().toString() : "");
        commonMarketingMap.put("consentId", consentStatusDTO.getCommonMarketing() != null && consentStatusDTO.getCommonMarketing().getConsentId() != null
                ? consentStatusDTO.getCommonMarketing().getConsentId().toString()
                : "");
        commonMarketingMap.put("content", consentStatusDTO.getCommonMarketing() != null ? consentStatusDTO.getCommonMarketing().getContent() : "");
        commonMarketingMap.put("requiredYn", "N");
        results.add(commonMarketingMap);

        // Channel Terms 그리드 설정
        Map<String, Object> channelTermsMap = new HashMap<>();
        channelTermsMap.put("consent", session.getAttribute("session_display_channel") + " Terms");
        channelTermsMap.put("version", consentStatusDTO.getChannelTerms() != null ? formatVersion(consentStatusDTO.getChannelTerms().getVersion()) : ""); // 버전 형식 변경
        channelTermsMap.put("releaseDate", consentStatusDTO.getChannelTerms() != null ? formatDate(consentStatusDTO.getChannelTerms().getCreatedAt(), dateFormatter) : ""); // 날짜 형식 변경
        if ("KR".equals(country)) {
            channelTermsMap.put("location", "Korea");
        } else {
            channelTermsMap.put("location", "Outside Korea");
        }
        channelTermsMap.put("agreementDate", consentStatusDTO.getChannelTermsAgreedDate() != null ? formatStringToDate(consentStatusDTO.getChannelTermsAgreedDate(), dateFormatter) : ""); // 날짜 형식 변경
        channelTermsMap.put("agreement", formatAgreement(consentStatusDTO.isChannelTermsAgreed())); // agreement 값 변경
        channelTermsMap.put("termsId", consentStatusDTO.getChannelTermsAgreeId() != null ? consentStatusDTO.getChannelTermsAgreeId() : "");
        channelTermsMap.put("consentId", consentStatusDTO.getChannelTerms() != null && consentStatusDTO.getChannelTerms().getConsentId() != null
                ? consentStatusDTO.getChannelTerms().getConsentId().toString()
                : "");
        channelTermsMap.put("content", consentStatusDTO.getChannelTerms() != null ? consentStatusDTO.getChannelTerms().getContent() : "");
        channelTermsMap.put("requiredYn", "Y");
        if (consentStatusDTO.getChannelTermsAgreeId() != null) {
            results.add(channelTermsMap);
        }
        // Channel Privacy 그리드 설정
        Map<String, Object> channelPrivacyMap = new HashMap<>();
        channelPrivacyMap.put("consent", session.getAttribute("session_display_channel") + " Privacy");
        channelPrivacyMap.put("version", consentStatusDTO.getChannelPrivacy() != null ? formatVersion(consentStatusDTO.getChannelPrivacy().getVersion()) : ""); // 버전 형식 변경
        channelPrivacyMap.put("releaseDate", consentStatusDTO.getChannelPrivacy() != null ? formatDate(consentStatusDTO.getChannelPrivacy().getCreatedAt(), dateFormatter) : ""); // 날짜 형식 변경
        if ("KR".equals(country)) {
            channelPrivacyMap.put("location", "Korea");
        } else if ("US".equals(country)) {
            channelPrivacyMap.put("location", "USA");
        } else if (euCountries.contains(country)) {
            channelPrivacyMap.put("location", "EU");
        } else if ("LA".equals(country)) {
            channelPrivacyMap.put("location", "Lao People's Democratic Republic");
        } else {
            channelPrivacyMap.put("location", "Outside EU");
        }
        channelPrivacyMap.put("agreementDate", consentStatusDTO.getChannelPrivacyAgreedDate() != null ? formatStringToDate(consentStatusDTO.getChannelPrivacyAgreedDate(), dateFormatter) : ""); // 날짜 형식 변경
        channelPrivacyMap.put("agreement", formatAgreement(consentStatusDTO.isChannelPrivacyAgreed())); // agreement 값 변경
        channelPrivacyMap.put("termsId", consentStatusDTO.getChannelPrivacyId() != null ? consentStatusDTO.getChannelPrivacyId().toString() : "");
        channelPrivacyMap.put("consentId", consentStatusDTO.getChannelPrivacy() != null && consentStatusDTO.getChannelPrivacy().getConsentId() != null
                ? consentStatusDTO.getChannelPrivacy().getConsentId().toString()
                : "");
        channelPrivacyMap.put("content", consentStatusDTO.getChannelPrivacy() != null ? consentStatusDTO.getChannelPrivacy().getContent() : "");
        channelPrivacyMap.put("requiredYn", "Y");
        if (consentStatusDTO.getChannelPrivacyId() != null) {
            results.add(channelPrivacyMap);
        }
        gridData.put("userConsent", results);
        userConsentHistory = getUserAgreedConsentsWithVersion(cdcUid,country,sessionChannel);
        gridData.put("userConsentHistory", userConsentHistory);

        ModelAndView modelAndView = new ModelAndView("myPage");
        String content = "fragments/myPage/consent";
        String menu = "consent";
        modelAndView.addObject("content", content);
        modelAndView.addObject("menu", menu);
        modelAndView.addObject("accUser", accUser);
        modelAndView.addObject("gridData", gridData);

        return modelAndView;
    }

    public void processCommonTerms(HttpSession session, String cdcUid, ConsentStatusDTO consentStatusDTO, Consent consent, List<UserAgreedConsents> userAgreedConsentIds) {
        boolean isConsentIdPresent = userAgreedConsentIds.stream()
                .anyMatch(agreedConsent -> agreedConsent.getConsentId().equals(consent.getId()));

        if (isConsentIdPresent && consentStatusDTO.getCommonTermsId().isEmpty()) {
            ConsentContent contentOpt = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                    consent.getId(), (String) session.getAttribute("cdc_language"), "published"
            );

            if (contentOpt != null) {
                consentStatusDTO.setCommonTerms(contentOpt);
                Optional<UserAgreedConsents> commonTermsUserOpt = userAgreedConsentsRepository.selectConsentId(consent.getId(), cdcUid, contentOpt.getId());
                if (commonTermsUserOpt.isPresent()) {
                    UserAgreedConsents commonTermsUser = commonTermsUserOpt.get();
                    consentStatusDTO.setCommonTermsAgreed("agreed".equals(commonTermsUser.getStatus()));
                    consentStatusDTO.setCommonTermsId(commonTermsUser.getId().toString());
                    consentStatusDTO.setCommonTermsAgreedDate(commonTermsUser.getUpdatedAt().toString());
                }
            } else {
                ConsentContent contentOptDefault = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                        consent.getId(), consent.getDefaultLanguage(), "published"
                );

                if (contentOptDefault != null) {
                    consentStatusDTO.setCommonTerms(contentOptDefault);
                    Optional<UserAgreedConsents> commonTermsUserOpt = userAgreedConsentsRepository.selectConsentId(consent.getId(), cdcUid, contentOptDefault.getId());
                    if (commonTermsUserOpt.isPresent()) {
                        UserAgreedConsents commonTermsUser = commonTermsUserOpt.get();
                        consentStatusDTO.setCommonTermsAgreed("agreed".equals(commonTermsUser.getStatus()));
                        consentStatusDTO.setCommonTermsId(commonTermsUser.getId().toString());
                        consentStatusDTO.setCommonTermsAgreedDate(commonTermsUser.getUpdatedAt().toString());
                    }
                }
            }
            //consentStatusDTO.setCommonTermsId(consent.getId().toString());
        }
    }

    public void processCommonPrivacy(HttpSession session, String cdcUid, ConsentStatusDTO consentStatusDTO, Consent consent, List<UserAgreedConsents> userAgreedConsentIds) {
        boolean isConsentIdPresent = userAgreedConsentIds.stream()
                .anyMatch(agreedConsent -> agreedConsent.getConsentId().equals(consent.getId()));

        if (isConsentIdPresent && consentStatusDTO.getCommonPrivacyId().isEmpty()) {
            ConsentContent contentOpt = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                    consent.getId(), (String) session.getAttribute("cdc_language"), "published"
            );

            if (contentOpt != null) {
                consentStatusDTO.setCommonPrivacy(contentOpt);
                Optional<UserAgreedConsents> commonPrivacyUserOpt = userAgreedConsentsRepository.selectConsentId(consent.getId(), cdcUid, contentOpt.getId());
                if (commonPrivacyUserOpt.isPresent()) {
                    UserAgreedConsents commonPrivacyUser = commonPrivacyUserOpt.get();
                    consentStatusDTO.setCommonPrivacyAgreed("agreed".equals(commonPrivacyUser.getStatus()));
                    consentStatusDTO.setCommonPrivacyId(commonPrivacyUser.getId().toString());
                    consentStatusDTO.setCommonPrivacyAgreedDate(commonPrivacyUser.getUpdatedAt().toString());

                }
            } else {
                ConsentContent contentOptDefault = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                        consent.getId(), consent.getDefaultLanguage(), "published"
                );

                if (contentOptDefault != null) {
                    consentStatusDTO.setCommonPrivacy(contentOptDefault);
                    Optional<UserAgreedConsents> commonPrivacyUserOpt = userAgreedConsentsRepository.selectConsentId(consent.getId(), cdcUid, contentOptDefault.getId());
                    if (commonPrivacyUserOpt.isPresent()) {
                        UserAgreedConsents commonPrivacyUser = commonPrivacyUserOpt.get();
                        consentStatusDTO.setCommonPrivacyAgreed("agreed".equals(commonPrivacyUser.getStatus()));
                        consentStatusDTO.setCommonPrivacyId(commonPrivacyUser.getId().toString());
                        consentStatusDTO.setCommonPrivacyAgreedDate(commonPrivacyUser.getUpdatedAt().toString());

                    }
                }
            }
            //consentStatusDTO.setCommonPrivacyId(consent.getId().toString());
        }
    }

    public void processCommonMarketing(HttpSession session, JsonNode accUser, ConsentStatusDTO consentStatusDTO, Consent consent, List<UserAgreedConsents> userAgreedConsentIds) {
        Long useMktConsent = "KR".equals(accUser.path("profile").path("country").asText()) ? Long.parseLong(BeansUtil.getApplicationProperty("marketing.kr.id")) : Long.parseLong(BeansUtil.getApplicationProperty("marketing.other.id"));

        boolean isConsentIdPresent = userAgreedConsentIds.stream()
                .anyMatch(agreedConsent -> agreedConsent.getConsentId().equals(useMktConsent));

        if (isConsentIdPresent && consentStatusDTO.getCommonMarketingId().isEmpty()) {
            consentStatusDTO.setCommonMarketingId(useMktConsent.toString());

            ConsentContent contentOpt;
            if ("KR".equals(accUser.path("profile").path("country").asText())) {
                contentOpt = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                        useMktConsent, "ko", "published"
                );
            } else {
                contentOpt = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                        useMktConsent, "en", "published"
                );
            }

            if (contentOpt != null) {
                consentStatusDTO.setCommonMarketing(contentOpt);
                Optional<UserAgreedConsents> commonMarketingUserOpt = userAgreedConsentsRepository.selectConsentId(useMktConsent, (String) session.getAttribute("cdc_uid"), contentOpt.getId());
                if (commonMarketingUserOpt.isPresent()) {
                    UserAgreedConsents commonMarketingUser = commonMarketingUserOpt.get();
                    consentStatusDTO.setCommonMarketingAgreed("agreed".equals(commonMarketingUser.getStatus()));
                    consentStatusDTO.setCommonMarketingAgreedDate(commonMarketingUser.getUpdatedAt().toString());
                    consentStatusDTO.setCommonMarketingId(commonMarketingUser.getId().toString());
                    consentStatusDTO.setUserAgreedMarketingId(commonMarketingUser.getId().toString());
                }
            } else {
                ConsentContent contentOptDefault = consentContentRepository.selectConsent(
                        useMktConsent, "published"
                );

                if (contentOptDefault != null) {
                    consentStatusDTO.setCommonMarketing(contentOptDefault);
                    Optional<UserAgreedConsents> commonMarketingUserOpt = userAgreedConsentsRepository.selectConsentId(useMktConsent, (String) session.getAttribute("cdc_uid"), contentOptDefault.getId());
                    if (commonMarketingUserOpt.isPresent()) {
                        UserAgreedConsents commonMarketingUser = commonMarketingUserOpt.get();
                        consentStatusDTO.setCommonMarketingAgreed("agreed".equals(commonMarketingUser.getStatus()));
                        consentStatusDTO.setCommonMarketingAgreedDate(commonMarketingUser.getUpdatedAt().toString());
                        consentStatusDTO.setCommonMarketingId(commonMarketingUser.getId().toString());
                        consentStatusDTO.setUserAgreedMarketingId(commonMarketingUser.getId().toString());
                    }
                }
            }
        }
    }

    public void processChannel(ObjectNode channel, String key, HttpSession session, String cdcUid, List<UserAgreedConsents> userAgreedConsentIds, List<Consent> consents, ConsentStatusDTO consentStatusDTO) {
        channel.put("termId", "");
        channel.put("privacyId", "");

        for (Consent consent : consents) {
            if (consent.getCoverage().equals(key) && "terms".equals(consent.getTypeId())) {
                if (containsConsentId(userAgreedConsentIds, consent.getId()) && channel.path("termId").asText().isEmpty()) {
                    channel.put("termId", consent.getId().toString());
                    processConsentContent(channel, consent, session, cdcUid, "term", consentStatusDTO);
                }
            }

            if (consent.getCoverage().equals(key) && "privacy".equals(consent.getTypeId())) {
                if (containsConsentId(userAgreedConsentIds, consent.getId()) && channel.path("privacyId").asText().isEmpty()) {
                    channel.put("privacyId", consent.getId().toString());
                    processConsentContent(channel, consent, session, cdcUid, "privacy", consentStatusDTO);
                }
            }
        }
    }

    public void processConsentContent(ObjectNode channel, Consent consent, HttpSession session, String cdcUid, String type, ConsentStatusDTO consentStatusDTO) {
        ConsentContent content = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(
                consent.getId(), (String) session.getAttribute("cdc_language"), "published"
        );

        if (content != null) {
            if ("term".equals(type)) {
                consentStatusDTO.setChannelTerms(content);
            } else if ("privacy".equals(type)) {
                consentStatusDTO.setChannelPrivacy(content);
            }
            channel.set(type + "Content", objectMapper.convertValue(content, JsonNode.class));
            updateConsentAgreementStatus(channel, consent, cdcUid, content, type, consentStatusDTO);
        } else {
            ConsentContent defaultContent = consentContentRepository.selectLatestByConsentIdAndIdAndStatusId(
                    consent.getId(), "published"
            );
            if ("term".equals(type)) {
                consentStatusDTO.setChannelTerms(defaultContent);
            } else if ("privacy".equals(type)) {
                consentStatusDTO.setChannelPrivacy(defaultContent);
            }
            if (defaultContent != null) {
                channel.set(type + "Content", objectMapper.convertValue(defaultContent, JsonNode.class));
                updateConsentAgreementStatus(channel, consent, cdcUid, defaultContent, type, consentStatusDTO);
            }
        }
    }

    public void updateConsentAgreementStatus(ObjectNode channel, Consent consent, String cdcUid, ConsentContent content, String type, ConsentStatusDTO consentStatusDTO) {
        UserAgreedConsents consentUser = userAgreedConsentsRepository.selectConsentId(consent.getId(), cdcUid, content.getId())
                .orElse(null);

        if (consentUser != null) {
            boolean agreed = "agreed".equals(consentUser.getStatus());
            if ("term".equals(type)) {
                consentStatusDTO.setChannelTermsAgreed(agreed);
                consentStatusDTO.setChannelTermsAgreeId(consentUser.getId().toString());
                consentStatusDTO.setChannelTermsAgreedDate(consentUser.getCreatedAt().toString());
            } else if ("privacy".equals(type)) {
                consentStatusDTO.setChannelPrivacyAgreed(agreed);
                consentStatusDTO.setChannelPrivacyId(consentUser.getId().toString());
                consentStatusDTO.setChannelPrivacyAgreedDate(consentUser.getCreatedAt().toString());
            }
        } else {
            if ("term".equals(type)) {
                consentStatusDTO.setChannelTermsAgreed(false);
                consentStatusDTO.setChannelTermsAgreeId("");
                consentStatusDTO.setChannelTermsAgreedDate("");
            } else if ("privacy".equals(type)) {
                consentStatusDTO.setChannelPrivacyAgreed(false);
                consentStatusDTO.setChannelPrivacyId("");
                consentStatusDTO.setChannelPrivacyAgreedDate("");
            }
        }
    }

    public boolean containsConsentId(List<UserAgreedConsents> userAgreedConsentIds, Long consentId) {
        return userAgreedConsentIds.stream().anyMatch(agreedConsent -> agreedConsent.getConsentId().equals(consentId));
    }

    public String formatStringToDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        return dateTime.format(formatter);
    }

    // Helper method to format version number as String
    public String formatVersion(Double version) {
        return version != null ? String.valueOf(version.intValue()) : "";
    }

    // Helper method to format agreement status as String
    public String formatAgreement(boolean agreed) {
        return agreed ? "Y" : "N";
    }

    public String formatDate(LocalDateTime date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "";
    }

    public List<Map<String, Object>> getUserAgreedConsentsWithVersion(String uid, String country,String sessionChannel) {
        String query = "SELECT " +
                "CASE " +
                "    WHEN c.coverage != 'common' THEN " +
                "        CASE " +
                "            WHEN c2.channel_display_name IS NOT NULL THEN " +
                "                UPPER(c2.channel_display_name) || ' ' || INITCAP(CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) " +
                "            ELSE " +
                "                INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) " +
                "        END " +
                "    ELSE " +
                "        INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) " +
                "END AS consent, " +
                "cc.version AS version, " +
                "TO_CHAR(uac.updated_at, 'YYYY-MM-DD') AS updated_at, " +
                "CASE " +
                "    WHEN c.type_id = 'privacy' THEN " +
                "        CASE " +
                "            WHEN :country = 'KR' THEN 'Korea' " +
                "            WHEN :country = 'US' THEN 'USA' " +
                "            WHEN :country IN ('AT', 'BE', 'BG', 'HR', 'CY', 'CZ', 'DK', 'EE', 'FI', 'FR', 'DE', 'GR', 'HU', 'IE', 'IT', 'LV', 'LT', 'LU', 'MT', 'NL', 'PL', 'PT', 'RO', 'SK', 'SI', 'ES', 'SE') THEN 'EU' " +
                "            WHEN :country = 'LA' THEN 'Lao Peoples Democratic Republic' " +
                "            ELSE 'Outside EU' " +
                "        END " +
                "    WHEN c.type_id = 'terms' THEN " +
                "        CASE " +
                "            WHEN :country = 'KR' THEN 'Korea' " +
                "            ELSE 'Outside Korea' " +
                "        END " +
                "    ELSE 'All countries' " +
                "END AS location, " +
                "cc.content AS content, " +
                "uac.id AS termsId " +
                "FROM user_agreed_consents uac " +
                "JOIN consent_contents cc ON uac.consent_content_id = cc.id " +
                "JOIN consents c ON uac.consent_id = c.id " +
                "LEFT JOIN channels c2 ON c2.channel_name = c.coverage AND c2.channel_name = :sessionChannel " +
                "WHERE uac.uid = :uid AND (c.coverage = 'common' OR c2.channel_name = :sessionChannel) " +
                "ORDER BY " +
                "CASE " +
                "    WHEN INITCAP(CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END || ' ' || c.coverage) LIKE 'Common%' THEN 1 " +
                "    ELSE 2 " +
                "END, " +
                "CASE " +
                "    WHEN INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) = 'Common Terms' THEN 1 " +
                "    WHEN INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) = 'Common Privacy' THEN 2 " +
                "    WHEN INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) = 'Common Marketing' THEN 3 " +
                "    WHEN INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) LIKE '% Terms' THEN 4 " +
                "    WHEN INITCAP(c.coverage || ' ' || CASE WHEN c.type_id = 'b2b' THEN 'Marketing' ELSE c.type_id END) LIKE '% Privacy' THEN 5 " +
                "    ELSE 6 " +
                "END";

        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("uid", uid)
                .setParameter("country", country)
                .setParameter("sessionChannel", sessionChannel)
                .getResultList();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> termsMap = new HashMap<>();
            termsMap.put("consent", result[0]);
            termsMap.put("version", formatVersion((Double) result[1]));
            termsMap.put("agreementDate", result[2]);
            termsMap.put("location", result[3]);
            termsMap.put("content", result[4]);
            termsMap.put("termsId", result[5].toString());
            mappedResults.add(termsMap);
        }

        return mappedResults;
    }

    public RedirectView marketingUpdate(Map<String, Object> payload, HttpSession session, RedirectAttributes redirectAttributes,HttpServletRequest request) {
        List<Map<String, String>> updatedConsents;
        String uid = (String) session.getAttribute("cdc_uid");
        String language = (String) session.getAttribute("cdc_language");

        try {
            updatedConsents = objectMapper.readValue((String) payload.get("updatedConsent"), new TypeReference<List<Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing updatedConsent data", e);
            return new RedirectView(request.getHeader("Referer"));
        }

        for (Map<String, String> data : updatedConsents) {
            Long consentId = Long.parseLong(data.get("consentId"));
            Optional<Consent> consentOpt = consentRepository.findById(consentId);

            if (consentOpt.isPresent()) {
                Consent consent = consentOpt.get();
                Map<String, Object> gigyaParams = new HashMap<>();
                boolean marketingTerms = Boolean.parseBoolean(data.get("agreeStatus"));

                Map<String, Object> preferencesFields = new HashMap<>();
                preferencesFields.put(consent.getCdcConsentId(), Map.of("isConsentGranted", marketingTerms));

                try {
                    gigyaParams.put("preferences", objectMapper.writeValueAsString(preferencesFields));
                } catch (JsonProcessingException e) {
                    log.error("Error processing preferences data", e);
                    return new RedirectView(request.getHeader("Referer"));
                }
                gigyaParams.put("UID", session.getAttribute("cdc_uid"));

                GSResponse response = gigyaService.executeRequest("default", "accounts.setAccountInfo", gigyaParams);
                redirectAttributes.addFlashAttribute("responseErrorCode", response.getErrorCode());
                log.info("update mkt consent: {}", response.getResponseText());

                String channel = (String) session.getAttribute("session_channel");
                String channelType = channelRepository.selectChannelTypeSearch(channel);

                Optional<Channels> optionalChannelObj = channelRepository.selectByChannelName(channel);
                Channels channelObj = optionalChannelObj.get();
                Map<String, Object> configMap = channelObj.getConfigMap();

                Boolean userProvisioning = configMap != null && configMap.containsKey("java_useprovisioning")
                        ? (Boolean) configMap.get("java_useprovisioning")
                        : false;

                if (userProvisioning) {
                    cpiApiService.sendUidProvisioningNoConfigChecking(channel, "U",uid);
                }

                if (response.getErrorCode() == 0) {
                    if (marketingTerms) {
                        consentResponseUpdated(consentId, data, "agreed", consent,uid,language);
                    } else {
                        saveRejectedConsent(consentId, data, "rejected");
                    }
                    JsonNode accountNode = cdcTraitService.getCdcUser(uid, 0);
//                    if("CUSTOMER".equals(channelType)) {
//                        cpiApiService.updateContact("context",objectMapper.convertValue(accountNode, Map.class),channel);
//                    }
                } else {
                    return new RedirectView(request.getHeader("Referer"));
                }
            } else {
                return new RedirectView(request.getHeader("Referer"));
            }
        }
        return new RedirectView("/myPage/consent");
    }

    private void consentResponseUpdated(Long consentId, Map<String, String> request, String status,Consent consent,String uid,String language) {
        ConsentContent content = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(consentId, language,"published");

        if (content != null) {
            saveUserAgreedConsent(request, content, status);
            sendMktUpdate(uid);
        } else {
            ConsentContent content2 = consentContentRepository.selectLatestByConsentIdAndLanguageIdAndStatusId(consentId, consent.getDefaultLanguage(),"published");

            if (content2 != null) {
                saveUserAgreedConsent(request, content2, status);
                sendMktUpdate(uid);
            } else {
                log.error("No content found for consent id: {}", consentId);
            }
        }
    }

    private void saveRejectedConsent(Long consentId, Map<String, String> request, String status) {
        Optional<UserAgreedConsents> oldConsentOpt = userAgreedConsentsRepository.findById(Long.parseLong(request.get("termsId")));

        if (oldConsentOpt.isPresent()) {
            UserAgreedConsents oldConsent = oldConsentOpt.get();
            oldConsent.setStatus(status);
            userAgreedConsentsRepository.save(oldConsent);
            log.info("Updated old consent to rejected for id: {}", oldConsent.getId());
        } else {
            log.error("No previous consent found for id: {}", request.get("userAgreedMarketingId"));
        }
    }

    private void saveUserAgreedConsent(Map<String, String> request, ConsentContent content, String status) {
        Optional<UserAgreedConsents> oldConsentOpt = userAgreedConsentsRepository.findById(Long.parseLong(request.get("termsId")));
        if (oldConsentOpt.isPresent()) {
            UserAgreedConsents oldConsent = oldConsentOpt.get();
//            newConsent.setConsentContentId(content.getId());
//            newConsent.setUid(request.get("uid"));
            oldConsent.setStatus(status);
            userAgreedConsentsRepository.save(oldConsent);
        } else {
            log.error("No previous consent found for id: {}", request.get("userAgreedMarketingId"));
        }
    }

    private void sendMktUpdate(String uid) {
        boolean sendMktUpdateToCmdm = true; // replace with actual config value
        boolean sendMktUpdateToSfdc = true; // replace with actual config value

        /*if (sendMktUpdateToCmdm) {
            JsonNode cdcUser = cdcService.getAccountInfo(uid);

            if (cdcUser.has("data") && cdcUser.get("data").has("contactID")) {
                String contactId = cdcUser.get("data").get("contactID").asText();
                cmdmApiService.updateContact("Context1", contactId, cdcUser);
            }
        }

        if (sendMktUpdateToSfdc) {
            JsonNode cdcUser = cdcService.getAccountInfo(uid);

            if (cdcUser.has("profile") && cdcUser.get("profile").has("email")) {
                sfdcService.SFDCUpdateMktConsent(cdcUser);
            }
        }*/
    }

}