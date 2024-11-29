package com.samsung.ciam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.ciam.models.ChannelInvitation;
import com.samsung.ciam.models.Channels;
import com.samsung.ciam.repositories.ChannelInvitationRepository;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.utils.BeansUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1. FileName   : MailService.java
 * 2. Package    : com.samsung.ciam.services
 * 3. Comments   : 이메일 전송 서비스로, 사용자의 정보에 맞는 이메일을 구성하고 전송을 담당함
 * 4. Author     : 서정환
 * 5. DateTime   : 2024. 11. 04.
 * 6. History    :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date        | Name          | Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.       | 서정환           | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@Slf4j
@Service
public class MailService {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;


    /*
     * 1. 메소드명: sendMail
     * 2. 클래스명: MailService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    이메일 내용을 구성하여 API를 통해 전송하는 메소드
     * 2. 사용법
     *    paramArray에 필요한 이메일 전송 정보를 담아 호출
     * 3. 예시 데이터
     *    - Input:
     *      paramArray = {
     *          "channel": "SFDC",
     *          "template": "TEMPLET-002",
     *          "approverName": "John Doe"
     *      }
     *    - Output:
     *      성공 시 이메일 전송 응답 데이터, 실패 시 에러 정보를 포함한 Map 반환
     * </PRE>
     * @param paramArray 이메일 전송에 필요한 정보가 담긴 Map
     * @return 전송 결과에 대한 응답 Map
     */
    public Map<String, Object> sendMail(Map<String, Object> paramArray) {
        Map<String, Object> emailDataArray = populateMailContent(paramArray);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();  // To store the result or error
        try {
            // 이메일 데이터 JSON으로 변환
            String emailDataJson = objectMapper.writeValueAsString(emailDataArray);

            // 헤더 설정 (기본 인증 포함)
            HttpHeaders headers = createHeaders(
                    BeansUtil.getApplicationProperty("email.service.login"),
                    BeansUtil.getApplicationProperty("email.service.password")
            );

            // 요청 엔터티 생성
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(emailDataJson, headers);

            // 이메일 전송 API 호출
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BeansUtil.getApplicationProperty("email.service.url") + "/mail",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("Email response: {}", responseEntity.getBody());
            return objectMapper.readValue(responseEntity.getBody(), Map.class); // 응답 데이터 파싱하여 반환
        } catch (HttpClientErrorException e) {
            // HTTP 에러 처리
            String responseBody = e.getResponseBodyAsString();
            log.error("HTTP Request failed with status {} and response: {}", e.getStatusCode(), responseBody);

            responseMap.put("error", "HTTP error occurred");
            responseMap.put("statusCode", e.getStatusCode());
            responseMap.put("response", responseBody);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("Error sending email", e);

            responseMap.put("error", "Unexpected error occurred");
            responseMap.put("exception", e.getMessage());
            return responseMap; // Return error information without throwing
        }
        return responseMap;
    }

    /*
     * 1. 메소드명: createHeaders
     * 2. 클래스명: MailService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    이메일 API에 필요한 기본 인증 헤더 생성
     * 2. 사용법
     *    이메일 API 호출 시 HTTP 헤더로 설정
     * 3. 예시 데이터
     *    - Input: username = "user", password = "pass"
     *    - Output: 기본 인증이 포함된 HttpHeaders 객체
     * </PRE>
     * @param username API 인증용 사용자 이름
     * @param password API 인증용 비밀번호
     * @return 인증 정보가 설정된 HttpHeaders 객체
     */
    private HttpHeaders createHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /*
     * 1. 메소드명: populateMailContent
     * 2. 클래스명: MailService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    이메일 템플릿에 맞는 내용을 생성하여 구성
     * 2. 사용법
     *    paramArray에서 필요한 정보를 가져와 이메일 콘텐츠 생성
     * 3. 예시 데이터
     *    - Input: paramArray = { "channel": "SFDC", "template": "TEMPLET-002" }
     *    - Output: 이메일 콘텐츠가 담긴 Map 객체
     * </PRE>
     * @param paramArray 이메일 콘텐츠 구성에 필요한 정보가 담긴 Map
     * @return 구성된 이메일 데이터가 담긴 Map 객체
     */
    private Map<String, Object> populateMailContent(Map<String, Object> paramArray) {
        Map<String, Object> emailDataArray = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();

        try {
            // 필수 파라미터 검증
            if (!paramArray.containsKey("channel")) {
                throw new IllegalArgumentException("channel is required");
            }

            // 사용자 정보 조회
            JsonNode CDCUser = cdcTraitService.getCdcUser(paramArray.get("cdc_uid").toString(), 0);

            // 기본적인 이메일 수신자 정보 구성
            String firstName = CDCUser.path("profile").path("firstName").asText("");
            String lastName = CDCUser.path("profile").path("lastName").asText("");
            String email = CDCUser.path("profile").path("email").asText("");
            String language = cdcTraitService.determineLanguage(CDCUser.path("profile").path("locale").asText("en"));

            emailDataArray.put("template", paramArray.get("template"));
            emailDataArray.put("language", language);
            emailDataArray.put("to", List.of(Map.of(
                    "name", firstName + " " + lastName,
                    "mail", email
            )));

            // 채널 조회
            Channels channel = channelRepository.findByChannelName(paramArray.get("channel").toString())
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

            // 템플릿별로 필요한 데이터 필드 설정
            switch (paramArray.get("template").toString()) {
                case "TEMPLET-002":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel Admin", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    fields.put("$CIAM Admin", "CIAM ADMIN");
                    break;
                case "TEMPLET-031":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel.user", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    break;
                case "TEMPLET-030":
                    emailDataArray.put("channel", "SFDC");
                    fields.put("$Channel.user", paramArray.get("approverName"));
                    fields.put("$Channel", paramArray.get("channel"));
                    fields.put("$Reject.Reason", paramArray.get("reason"));
                    break;
                case "TEMPLET-032":
                    try {
                        ChannelInvitation channelInvitation = channelInvitationRepository.findById(Long.parseLong(paramArray.get("ch_inv_id").toString()))
                                .orElseThrow(() -> new IllegalArgumentException("Channel invitation not found"));

                        fields.put("$Channel", channel.getChannelDisplayName());
                        fields.put("$Channel.TEMP.User", firstName + " " + lastName);
                        fields.put("$Channel.User", createRegistrationLink(paramArray, channelInvitation.getToken()));
                        fields.put("$CIAM Admin", "CIAM Admin");
                        fields.put("$docURL", createDocURL(paramArray, channelInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                    } catch (Exception e) {
                        log.error("Error processing template TEMPLET-032: {}", e.getMessage());
                    }
                    break;
                case "TEMPLET-NEW-005":
                    try {
                        ChannelInvitation channelNewInvitation = channelInvitationRepository.findById(Long.parseLong(paramArray.get("ch_inv_id").toString()))
                                .orElseThrow(() -> new IllegalArgumentException("Channel invitation not found"));

                        fields.put("$Channel", channel.getChannelDisplayName());
                        fields.put("$Channel.TEMP.User", firstName + " " + lastName);
                        fields.put("$Channel.User", createRegistrationLink(paramArray, channelNewInvitation.getToken()));
                        fields.put("$CIAM Admin", "CIAM Admin");
                        fields.put("$docURL", createDocURL(paramArray, channelNewInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                        fields.put("$firstName", paramArray.get("firstName"));
                        fields.put("$lastName", paramArray.get("lastName"));
                    } catch (Exception e) {
                        log.error("Error processing template TEMPLET-NEW-005: {}", e.getMessage());
                    }
                    break;
                case "TEMPLET-NEW-002":
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestEmail", paramArray.get("approvalRequestEmail"));
                    break;
                case "TEMPLET-NEW-004":
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestEmail", paramArray.get("approvalRequestEmail"));
                    fields.put("$ApprovalRequestDate", paramArray.get("approvalRequestDate"));
                    break;
                case "TEMPLET-NEW-006": // 재직인증 승인요청 알람
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    //fields.put("$docURL", createDocURL(paramArray, channelNewInvitation.getToken(), CDCUser.path("profile").path("languages").asText("")));
                break;
                case "TEMPLET-NEW-008": // 재직 인증 반려 
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$EmployeeVerificationRejector", paramArray.get("EmployeeVerificationRejector"));
                    fields.put("$EmployeeVerificationRejectReason", paramArray.get("EmployeeVerificationRejectReason"));
                break;
                case "TEMPLET-NEW-009": // 재직 인증 기한 마감
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$EmployeeVerificationRejector", paramArray.get("EmployeeVerificationRejector"));
                    fields.put("$EmployeeVerificationRejectReason", paramArray.get("EmployeeVerificationRejectReason"));
                break;
                case "TEMPLET-NEW-003": // Admin이 가입 승인 반려 시점
                    fields.put("$ChannelName", channel.getChannelDisplayName());
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$ApprovalRequestRejector", paramArray.get("ApprovalRequestRejector"));
                    fields.put("$ApprovalRequestRejectReason", paramArray.get("ApprovalRequestRejectReason"));
                break;
                case "TEMPLET-NEW-012": // 권한 변경 반려 안내, Admin이 Role Manager에서 권한 변경을 거절한 시점
                    fields.put("$firstName", paramArray.get("firstName"));
                    fields.put("$lastName", paramArray.get("lastName"));
                    fields.put("$RoleManagerRejector", paramArray.get("RoleManagerRejector"));
                    fields.put("$RoleManagerRejectReason", paramArray.get("RoleManagerRejectReason"));
                break;
                case "TEMPLET-013": // Added for privacy template
                    fields.put("$Channel", channel.getChannelDisplayName());
                    fields.put("$CIAM Admin", ((List<Map<String, String>>) emailDataArray.get("to"))
                            .stream()
                            .findFirst() // 첫 번째 항목 가져오기
                            .map(map -> map.get("name")) // "name" 값 가져오기
                            .orElse("Default Name"));
                    break;
                case "TEMPLET-014": // Added for terms template
                    fields.put("$Channel", channel.getChannelDisplayName());
                    fields.put("$CIAM Admin", ((List<Map<String, String>>) emailDataArray.get("to"))
                            .stream()
                            .findFirst() // 첫 번째 항목 가져오기
                            .map(map -> map.get("name")) // "name" 값 가져오기
                            .orElse("Default Name"));
                    break;
                default:
                    log.error("Invalid template type: {}", paramArray.get("template").toString());
            }

            emailDataArray.put("fields", List.of(fields));
        } catch (Exception e) {
            log.error("Error in populateMailContent method: {}", e.getMessage());
            // 예외를 다시 던지지 않고 메서드가 종료되도록 함
        }

        return emailDataArray;
    }

    /*
     * 1. 메소드명: createRegistrationLink
     * 2. 클래스명: MailService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자 이메일 템플릿에 삽입할 등록 링크 생성(채널초대때 사용)
     * 2. 사용법
     *    템플릿의 $Channel.User 필드에 삽입하여 사용
     * 3. 예시 데이터
     *    - Input: paramArray = { "channel": "SFDC" }, token = "abc123"
     *    - Output: https://example.com/registration/company?param=SFDC&t=abc123&channelType=SFDC
     * </PRE>
     * @param paramArray 채널 및 링크 생성에 필요한 정보가 담긴 Map
     * @param token 사용자 고유 토큰
     * @return 완성된 등록 링크
     */
    public String createRegistrationLink(Map<String, Object> paramArray, String token) {
        return String.format("%s/registration/company?param=%s&t=%s&channelType=%s",
                BeansUtil.getApplicationProperty("app.base.url"), paramArray.get("channel"), token,paramArray.get("channelType"));
    }

    /*
     * 1. 메소드명: createDocURL
     * 2. 클래스명: MailService
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    사용자 이메일 템플릿에 삽입할 문서 링크 생성(채널초대때 사용)
     * 2. 사용법
     *    템플릿의 $docURL 필드에 삽입하여 사용
     * 3. 예시 데이터
     *    - Input: paramArray = { "channel": "SFDC" }, token = "abc123", language = "en"
     *    - Output: https://example.com/registration/company?param=SFDC&t=abc123&lang=en&channelType=SFDC
     * </PRE>
     * @param paramArray 채널 및 링크 생성에 필요한 정보가 담긴 Map
     * @param token 사용자 고유 토큰
     * @param language 사용자 언어 설정
     * @return 완성된 문서 링크
     */
    public String createDocURL(Map<String, Object> paramArray, String token, String language) {
        return String.format("%s/registration/company?param=%s&t=%s&lang=%s&channelType=%s",
                BeansUtil.getApplicationProperty("app.base.url"), paramArray.get("channel"), token, language,paramArray.get("channelType"));
    }
}