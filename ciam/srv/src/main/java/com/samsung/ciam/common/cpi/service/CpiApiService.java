package com.samsung.ciam.common.cpi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.ciam.common.cpi.enums.CpiRequestFieldMapping;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CpiApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private SubsidiaryRepository subsidiaryRepository;

    @Autowired
    private NewSubsidiaryRepository newSubsidiaryRepository;

    @Autowired
    private ChannelInvitationRepository channelInvitationRepository;

    public List<Map<String, Object>> accountSearch(Map<String, Object> paramArray, String url, CpiResponseFieldMapping responseFieldMapping, HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonData = objectMapper.writeValueAsString(paramArray);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Basic Auth 헤더 설정
            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("api response: {}", responseEntity.getBody());
            Map<String, Object> originalResponse = objectMapper.readValue(responseEntity.getBody(), Map.class);
            return mapResponseFields(originalResponse, responseFieldMapping.getFieldMap(),"account");
        } catch (Exception e) {
            log.error("Error sending api", e);
            throw new RuntimeException("Error sending api", e);
        }
    }

    public List<Map<String, Object>> mapResponseFields(Map<String, Object> originalResponse, Map<String, String> fieldMap,String dataSource) {
        List<Map<String, Object>> mappedResponseList = new ArrayList<>();
        Object accountDataObject = originalResponse.get(dataSource);

        if (accountDataObject instanceof List) {
            // account 필드가 List인 경우
            List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;
            for (Map<String, Object> accountData : accountDataList) {
                mappedResponseList.add(mapSingleAccount(accountData, fieldMap));
            }
        } else if (accountDataObject instanceof Map) {
            // account 필드가 단일 Map인 경우
            Map<String, Object> accountData = (Map<String, Object>) accountDataObject;
            mappedResponseList.add(mapSingleAccount(accountData, fieldMap));
        }

        return mappedResponseList;
    }

    public Map<String, Object> mapSingleAccount(Map<String, Object> accountData, Map<String, String> fieldMap) {
        Map<String, Object> mappedResponse = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String targetField = entry.getKey();
            String sourceField = entry.getValue();
            mappedResponse.put(targetField, accountData.get(sourceField));
        }
        return mappedResponse;
    }

    public List<Map<String, Object>> findSimilar(Map<String, Object> paramArray, String url, CpiRequestFieldMapping requestFieldMapping,CpiResponseFieldMapping responseFieldMapping,HttpSession sessions) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> fieldMap = requestFieldMapping.getFieldMap();

            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                String requestKey = entry.getKey();
                String apiKey = entry.getValue();
                requestBody.put(apiKey, paramArray.getOrDefault(requestKey, ""));
            }
            //requestBody.putAll(requestFieldMapping.getDefaultValues());

            // Add common fields
            requestBody.put("name_gl", paramArray.getOrDefault("name", "")); // Assume "filter_Name" is used for both name_lo and name_gl
            requestBody.put("showcmdmlist", true);
            requestBody.put("getmaxrank_record", false); // Fixed value field
            // convertYn 세션 값 확인
            if (sessions.getAttribute("convertYn") != null && "Y".equals(sessions.getAttribute("convertYn").toString())) {
                requestBody.put("showdnblist", false);
            } else {
                requestBody.put("showdnblist", true);
            }
            requestBody.put("country", sessions.getAttribute("secCountry") != null ? sessions.getAttribute("secCountry").toString() : paramArray.getOrDefault("country", ""));
            if(paramArray.get("isNewCompany")!=null && "true".equals(paramArray.get("isNewCompany"))) {
                requestBody.put("isNewCompany","true");
            } else {
                requestBody.put("isNewCompany","false");
            }
            String jsonData = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("api response: {}", responseEntity.getBody());
            Map<String, Object> originalResponse = objectMapper.readValue(responseEntity.getBody(), Map.class);


            // Add validation error info to each map in the list if applicable
            if ("1040".equals(originalResponse.get("msgcode"))) {
                Map<String, Object> validationErrorMap = new HashMap<>();
                validationErrorMap.put("validationError", true);
                validationErrorMap.put("msg", "bizregno1 validation error");
                resultList.add(validationErrorMap);
            } else {
                List<Map<String, Object>> duplist = mapResponseFields(originalResponse, responseFieldMapping.getFieldMap(), "duplist");
                resultList.addAll(duplist);

                // dnblist 처리
                if("false".equals(requestBody.get("isNewCompany"))) {
                    List<Map<String, Object>> dnblist = (List<Map<String, Object>>) originalResponse.get("dnblist");
                    if (dnblist != null && !dnblist.isEmpty()) {
                        for (Map<String, Object> dnbItem : dnblist) {
                            Map<String, Object> dnbAccountData = getDnbAccountData(dnbItem);
                            resultList.add(dnbAccountData);
                        }
                    }
                }
            }

            return resultList;
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("HTTP Request failed with status {} and response: {}", e.getStatusCode(), responseBody);
            throw e;
        } catch (Exception e) {
            log.error("Error sending api", e);
            throw new RuntimeException("Error sending api", e);
        }
    }

    public Map<String, Object> getDnbAccountData(Map<String, Object> responseData) {
        // Extract phone numbers
        List<String> telephones = ((List<Map<String, String>>) responseData.getOrDefault("telephone", new ArrayList<>()))
                .stream()
                .map(phone -> phone.getOrDefault("telephoneNumber", ""))
                .collect(Collectors.toList());

        // Extract registration numbers
        List<String> regNumbers = ((List<Map<String, String>>) responseData.getOrDefault("registrationNumbers", new ArrayList<>()))
                .stream()
                .map(reg -> reg.getOrDefault("registrationNumber", ""))
                .collect(Collectors.toList());

        // Extract primary address
        Map<String, Object> primaryAddress = (Map<String, Object>) responseData.getOrDefault("primaryAddress", new HashMap<>());

        // Initialize the result map
        Map<String, Object> dnbAccountData = new HashMap<>();

        // Populate the result map with info fields
        dnbAccountData.put("country", ((Map<String, String>) primaryAddress.getOrDefault("addressCountry", new HashMap<>())).getOrDefault("isoAlpha2Code", ""));
        dnbAccountData.put("city", ((Map<String, String>) primaryAddress.getOrDefault("addressLocality", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("state", ((Map<String, String>) primaryAddress.getOrDefault("addressRegion", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("street_address", ((Map<String, String>) primaryAddress.getOrDefault("streetAddress", new HashMap<>())).getOrDefault("line1", ""));

        // Handle postalCode as a string
        String postalCode = (String) primaryAddress.getOrDefault("postalCode", "");
        dnbAccountData.put("zip_code", postalCode);

        dnbAccountData.put("region", ((Map<String, String>) primaryAddress.getOrDefault("addressRegion", new HashMap<>())).getOrDefault("name", ""));
        dnbAccountData.put("dunsno", responseData.getOrDefault("duns", ""));
        dnbAccountData.put("phonenumber1", telephones.size() > 0 ? telephones.get(0) : "");
        dnbAccountData.put("bizregno1", regNumbers.size() > 0 ? regNumbers.get(0) : "");
        dnbAccountData.put("phonenumber2", telephones.size() > 1 ? telephones.get(1) : "");

        // Add additional fields
        dnbAccountData.put("bpid", "");
        dnbAccountData.put("orgId", "");
        dnbAccountData.put("name", responseData.getOrDefault("primaryName", ""));
        dnbAccountData.put("orgName", responseData.getOrDefault("primaryName", ""));
        dnbAccountData.put("type", "");
        dnbAccountData.put("dnbListYn", true); // dnbList 항목임을 표시

        return dnbAccountData;
    }

    //@Async
    public Map<String, Object> createContact(String context, Map<String, Object> userData, HttpSession session,String channelName) {
        ObjectMapper objectMapper = new ObjectMapper();
        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.createContactUrl");

        Map<String, Object> queryParams = Map.of("context", context);

        String CMDMMobilePhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "mobile_phone");
        String CMDMWorkPhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "work_phone");
        String CMDMFaxPhoneString = getPhoneNumber((Map<String, Object>) userData.get("profile"), "fax");

        log.info("createContact: {}", userData);

        String usedChannelName = "";
        String origin = "";
        String subsidiary = getDataValue(userData, "subsidiary");
        String companyCode=Optional.ofNullable(newSubsidiaryRepository.selectCompanyCode(subsidiary))
                .orElse(""); // 값이 null이면 빈 문자열로 대체
        String regSource = Optional.ofNullable((String) userData.get("regSource")).orElse("").toLowerCase();

        Optional<Channels> channelInfo = channelRepository.findByChannelName(regSource);

        Channels channel = channelRepository.findByChannelName(channelName)
                .orElseGet(() -> channelRepository.findByRegSouce(regSource).orElse(null));

        if (channel != null) {
            usedChannelName = channel.getCmdmRegch();
            origin = channel.getSfdcRegch();
        } else {
            origin = usedChannelName = regSource;
        }

        String userCountry = getProfileValue(userData, "country");
        Map<String, Object> marketingConsent = getMarketingConsent(userData);

        String Mktg_gbValue = "";
        String optIn = "N";
        String optOut = "Y";

        if (Boolean.parseBoolean(Optional.ofNullable(marketingConsent)
                .map(mc -> String.valueOf(mc.get("isConsentGranted")))  // null일 경우 "null" 문자열이 반환되지 않도록 처리
                .orElse("false"))) {
            Mktg_gbValue = "ALL";
            optIn = "Y";
            optOut = "N";
        }

        if ("FR".equals(userCountry) && marketingConsent == null) {
            optIn = "B";
            optOut = "B";
        }

        Map<String, String> SFDC_TO_CMDM_DEPT_MAP = commonCodeRepository.findByHeader("BIZ_WITH_DEPT").stream()
                .collect(Collectors.toMap(
                        CommonCode::getName,
                        CommonCode::getValue,
                        (existingValue, newValue) -> existingValue // 중복된 키가 있을 경우 기존 값을 유지
                ));

        String CMDMDepartment = SFDC_TO_CMDM_DEPT_MAP.getOrDefault(getDataValue(userData, "userDepartment"), "");

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("lastname_lo", getProfileValue(userData, "lastName"));
        bodyParams.put("firstname_lo", getProfileValue(userData, "firstName"));
        bodyParams.put("acctid", getDataValue(userData, "accountID"));
        bodyParams.put("regch", "CIAM");
        bodyParams.put("origin", origin); // 하드코딩
        bodyParams.put("email", getProfileValue(userData, "email"));
        bodyParams.put("opt_in", optIn);
        bodyParams.put("opt_indate", java.time.LocalDate.now().toString());
        bodyParams.put("middlename_lo", getDataValue(userData, "middleName"));
        bodyParams.put("title", getDataValue(userData, "salutation"));
        bodyParams.put("jobtitle", getDataValue(userData, "jobtitle"));
        bodyParams.put("deptname", CMDMDepartment);
        bodyParams.put("telno", CMDMWorkPhoneString);
        bodyParams.put("mobileno", CMDMMobilePhoneString);
        bodyParams.put("faxno", CMDMFaxPhoneString);
        bodyParams.put("countrycode", userCountry);
        bodyParams.put("street_lo", getProfileValue(userData, "address"));
        bodyParams.put("city_lo", getProfileValue(userData, "city"));
        bodyParams.put("district_lo", getProfileValue(userData, "state"));
        bodyParams.put("postalcode", getProfileValue(userData, "zip"));
        bodyParams.put("userid", getProfileValue(userData, "username"));
        bodyParams.put("companycode", companyCode);
        bodyParams.put("contactowner", getDataValue(userData, "ownerEmployeeID"));
        bodyParams.put("mktggb", Mktg_gbValue);
        bodyParams.put("opt_out", optOut);
        bodyParams.put("opt_outdate", java.time.LocalDate.now().toString());
        bodyParams.put("creatorid", "CIAM");
        bodyParams.put("partner_accountid", "");
        bodyParams.put("privateconsent_yn", "Y");
        bodyParams.put("thirdpartyuse_yn", "");
        bodyParams.put("contactid", getDataValue(userData, "contactID"));

        log.info("Contact body params: {}", bodyParams);

        try {
            ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
            Map<String, Object> responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);

            if (!responseData.containsKey("contact")) {
                log.error("No contact created. " + responseEntity.getBody());
                throw new Exception("No contact created. " + responseEntity.getBody());
            }

            log.info("New CMDM contact created: {}", responseData);
            return responseData;
        } catch (Exception e) {
            log.error("Error creating contact: {}", e.getMessage());
            throw new RuntimeException("Error creating contact", e);
        }
    }

    public String getProfileValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (String) profile.get(key))
                .orElse("");
    }

    public String getDataValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("data"))
                .map(data -> (String) data.get(key))
                .orElse("");
    }

    public String getWorkValue(Map<String, Object> userData, String key) {
        return Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (Map<String, Object>) profile.get("work"))
                .map(work -> (String) work.get(key))
                .orElse("");
    }

    public void extractDataFromObject(Map<String, Object> organizationInfo) {
        organizationInfo.replaceAll((key, value) -> {
            if (value instanceof List<?>) {
                // List를 검사하고, 만약 List<String>이 아닌 경우, 각 요소를 문자열로 변환 후 결합
                List<?> listValue = (List<?>) value;
                return listValue.stream()
                        .map(Object::toString) // 요소를 문자열로 변환
                        .collect(Collectors.joining(", "));
            }
            return value; // List가 아닌 경우 원래 값을 유지
        });
    }

    public static List<String> parseIndustryType(String industryType) {
        List<String> industryArr = new ArrayList<>();
        try {
            if (industryType.startsWith("[") && industryType.endsWith("]")) {
                // 문자열에서 대괄호를 제거하고 내부 문자열을 리스트로 변환
                String jsonArrayString = industryType.substring(1, industryType.length() - 1);

                // 콤마로 구분된 값들을 리스트로 분할
                industryArr = Arrays.asList(jsonArrayString.split(",\\s*"));

                // 문자열 요소를 처리하여 각 요소의 양쪽 공백이나 따옴표 제거
                industryArr = industryArr.stream()
                        .map(String::trim) // 양쪽 공백 제거
                        .map(s -> s.replaceAll("^\"|\"$", "")) // 양쪽의 따옴표 제거
                        .collect(Collectors.toList());
            } else {
                // industry_type이 단순한 문자열인 경우
                industryArr.add(industryType);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return industryArr;
    }

    public Map<String, Object> createAccount(String channelName, String context, Map<String, Object> accountData, HttpSession session, NewCompany newCompany) {
        ObjectMapper objectMapper = new ObjectMapper();
        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.createAccountUrl");

        Map<String, Object> queryParams = Map.of("context", context);
        Map<String, Object> infoData = (Map<String, Object>) accountData.get("info");

        extractDataFromObject(infoData);

        log.info("createAccount: {}", accountData);

        String regSource = Optional.ofNullable(infoData.get("regch")).orElse("").toString();
        String companyCode="";
        if(newCompany != null) {
            companyCode = Optional.ofNullable(newSubsidiaryRepository.selectCompanyCode(newCompany.getSubsidiary()))
                    .orElse(""); // 값이 null이면 빈 문자열로 대체
        }
        Channels channel = channelRepository.findByChannelName(channelName)
                .orElseGet(() -> channelRepository.findByRegSouce(regSource).orElse(null));

        String usedChannelName;
        String origin;
        if (channel != null) {
            usedChannelName = channel.getCmdmRegch();
            origin = channel.getSfdcRegch();
        } else {
            origin = usedChannelName = regSource;
        }

        List<String> industryArr = new ArrayList<>();
        String industryType = infoData.get("industry_type").toString();
        industryArr = parseIndustryType(industryType);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("name_lo", accountData.getOrDefault("orgName",""));
        bodyParams.put("country", infoData.getOrDefault("country",""));
        bodyParams.put("regch", "CIAM");
        bodyParams.put("origin", origin);
        //bodyParams.put("origin", "");
        bodyParams.put("requesttype", "SELF");
        bodyParams.put("creatorid", "CIAM");

        bodyParams.put("bizregno1", infoData.get("bizregno1"));
        bodyParams.put("vatno", infoData.getOrDefault("vatno",""));
        // newCompany가 null이 아닐 때만 처리
        if (newCompany != null) {
            bodyParams.put("dunsno", Optional.ofNullable(newCompany.getDunsNo()).orElse(""));
        } else {
            bodyParams.put("dunsno", "");  // newCompany가 null일 때 빈 값 설정
        }
        bodyParams.put("ciscode", infoData.getOrDefault("ciscode",""));
        bodyParams.put("name_gl", accountData.getOrDefault("orgName",""));
        bodyParams.put("accountwebsite", infoData.getOrDefault("accountwebsite",""));
        bodyParams.put("accountowner", "");
        bodyParams.put("street_lo", infoData.getOrDefault("street_address",""));
        bodyParams.put("city_lo", infoData.getOrDefault("city",""));
        bodyParams.put("district_lo", infoData.getOrDefault("state",""));
        bodyParams.put("street_gl", infoData.getOrDefault("street_address",""));
        bodyParams.put("city_gl", infoData.getOrDefault("city",""));
        bodyParams.put("district_gl", infoData.getOrDefault("district_gl",""));
        bodyParams.put("postalcode", infoData.getOrDefault("zip_code",""));
        bodyParams.put("email", infoData.getOrDefault("email",""));
        bodyParams.put("phonenumber1", infoData.getOrDefault("phonenumber1",""));
        bodyParams.put("phonenumber2", infoData.getOrDefault("phonenumber2",""));
        bodyParams.put("faxno", infoData.getOrDefault("faxno",""));
        bodyParams.put("industrytype2", industryArr.isEmpty() ? "" : industryArr.get(0));
        bodyParams.put("companycode", companyCode);
        log.info("Account body params: {}", bodyParams);

        try {
            boolean isAccount = false;
            Map<String, Object> responseData = new HashMap<>();

            if (accountData.containsKey("bpid")) {
                Map<String, Object> paramArray = new HashMap<>();
                paramArray.put("acctid", accountData.get("bpid"));

                String accountSerachUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");
                CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString(channelName);
                List<Map<String, Object>> searchResults = accountSearch(paramArray, accountSerachUrl, responseFieldMapping, session);
                isAccount = !searchResults.isEmpty();

                if (isAccount) {
                    responseData = searchResults.get(0);
                    log.info("Existing CMDM account found: {}", responseData);
                }
            }

            if (!isAccount) {
                ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
                responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);
                log.info("New CMDM account created: {}", responseData);
            }

            if (!responseData.containsKey("account")) {
                throw new Exception("No account created. " + objectMapper.writeValueAsString(responseData));
            }

            return responseData;
        } catch (Exception e) {
            log.error("Error creating account: {}", e.getMessage());
            String accountId = extractAccountIdFromError(e.getMessage());
            if (!accountId.isEmpty()) {
                Map<String, Object> paramArray = new HashMap<>();
                paramArray.put("accountId", accountId);
                paramArray.put("context", "Context1");

                List<Map<String, Object>> existingAccount = accountSearch(paramArray, apiUrl, null, session);
                if (!existingAccount.isEmpty()) {
                    return existingAccount.get(0);
                }
            }
            throw new RuntimeException("Error creating account", e);
        }
    }

    public String extractAccountIdFromError(String errorMessage) {
        try {
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.error("Error extracting account ID: {}", e.getMessage());
        }
        return "";
    }

    public ResponseEntity<String> sendRequest(Map<String, Object> bodyParams, String apiUrl, Map<String, Object> queryParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyParams, headers);

        return restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class, queryParams);
    }

    public String getPhoneNumber(Map<String, Object> profile, String phoneType) {
        List<Map<String, Object>> phones = (List<Map<String, Object>>) profile.get("phones");

        if (phones == null) {
            return "";  // phones 리스트가 null인 경우 빈 문자열 반환
        }

        return phones.stream()
                .filter(phone -> phoneType.equals(phone.get("type")))
                .map(phone -> (String) phone.get("number"))
                .findFirst()
                .orElse("");
    }

    public Map<String, Object> getMarketingConsent(Map<String, Object> userData) {
        boolean isProd = "prod".equals(BeansUtil.getApplicationProperty("server"));
        String userCountry = Optional.ofNullable((Map<String, Object>) userData.get("profile"))
                .map(profile -> (String) profile.get("country"))
                .orElse("");
        Map<String, Object> marketingConsent = null;

        Map<String, Object> preferences = (Map<String, Object>) userData.get("preferences");
        Map<String, Object> b2b = preferences != null ? (Map<String, Object>) preferences.get("b2b") : null;
        Map<String, Object> common = b2b != null ? (Map<String, Object>) b2b.get("common") : null;

        switch (userCountry) {
            case "KR":
                if (isProd) {
                    Map<String, Object> noneu = common != null ? (Map<String, Object>) common.get("noneu") : null;
                    if (noneu != null) {
                        marketingConsent = (Map<String, Object>) noneu.get("kr");
                    }
                } else {
                    Map<String, Object> noneu = common != null ? (Map<String, Object>) common.get("noneu") : null;
                    if (common != null) {
                        marketingConsent = (Map<String, Object>) common.get("kr");
                    }
                }
                break;
            default:
                if (isProd) {
                    Map<String, Object> all = common != null ? (Map<String, Object>) common.get("all") : null;
                    if (all != null) {
                        marketingConsent = (Map<String, Object>) all.get("af");
                    }
                } else {
                    if (common != null) {
                        marketingConsent = (Map<String, Object>) common.get("all");
                    }
                }
        }

        return marketingConsent;
    }

    @Async
    public Future<ResponseEntity<String>> sendUidProvisioningNoConfigChecking(String channel,String requestType,String uid) {
        // channel을 통해 채널 정보 조회
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> bodyParams = new HashMap<>(); // API body에 들어갈 값
        Map<String, Object> queryParams = new HashMap<>(); // 필요시 queryParams를 설정하십시오.
        String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.provisioningUrl");
        Optional<Channels> optionalChannel = channelRepository.findByChannelName(channel);

        if (optionalChannel.isPresent()) {
            Channels channelData = optionalChannel.get();
            Map<String, Object> configMap = channelData.getConfigMap();
            Map<String, Object> javaProvisioningMap = (Map<String, Object>) configMap.get("java_provisioning");

            // 가변 필드인 channel, requestType, UID 설정
            bodyParams.put("channel", channel);  // 전달받은 channel 값
            bodyParams.put("requestType", requestType);  // 전달받은 requestType 값
            bodyParams.put("UID", uid);  // 전달받은 UID 값
            bodyParams.put("type", channelData.getChannelType().toLowerCase());

            // 제외할 필드 정의
            Set<String> excludedKeys = Set.of("channel", "requestType", "UID","type","provisioning_type");

            // javaProvisioningMap에서 제외할 필드 외의 나머지 필드 추가
            javaProvisioningMap.forEach((key, value) -> {
                if (!excludedKeys.contains(key)) {
                    bodyParams.put(key, value);  // 제외할 필드가 아닌 경우 bodyParams에 추가
                }
            });

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic UDIwMDc2MTUxNDY6MXFhelhTV0AjRQ==");

            // HTTP 요청 준비
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyParams, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class, queryParams);

            return new AsyncResult<>(responseEntity);

        } else {
            log.error("Channel not found: {}", channel);
            throw new RuntimeException("Channel not found: " + channel);
        }
    }

    public Map<String, Object> partnerCompanyDuplicateCheck(Map<String, Object> allParams, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.findSimilarUrl");
            CpiRequestFieldMapping requestFieldMapping = CpiRequestFieldMapping.fromString((String) allParams.get("channel"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));
            allParams.put("isNewCompany",allParams.get("isNewCompany"));
            allParams.put("country",allParams.get("country"));

            // findSimilar 메소드 호출하여 결과값 가져오기
            List<Map<String, Object>> cpiResult = findSimilar(allParams, url, requestFieldMapping, responseFieldMapping, session);

            // Check for validation error in the first map
            if (!cpiResult.isEmpty() && cpiResult.get(0).containsKey("validationError")) {
                result.put("result", "validationError");
                result.put("msg", cpiResult.get(0).get("msg"));
                return result;
            }

            // rank가 100인 항목 찾기
            for (Map<String, Object> item : cpiResult) {
                if (item.get("rank") != null && item.get("rank").equals(100)) {
                    String accountId = (String) item.get("bpid");
                    result.put("result", "duplicate");
                    result.put("msg", "Duplicate company found. The account ID : " + accountId);
                    result.put("accountId", accountId);
                    return result;
                }
            }

            // rank가 100인 항목이 없는 경우
            result.put("result", "success");
            result.put("msg", "No duplicate with rank 100 found");
        } catch (Exception e) {
            result.put("result", "error");
            result.put("msg", "An error occurred: " + e.getMessage());
        }

        return result;
    }

    public void saveSubsidiaries(Map<String, Object> requestData) {
        // 모든 데이터를 먼저 삭제
        newSubsidiaryRepository.deleteAll();

        // company 리스트 추출
        List<Map<String, Object>> companies = (List<Map<String, Object>>) requestData.get("company");

        if (companies != null && !companies.isEmpty()) {
            List<NewSubsidiary> subsidiaries = new ArrayList<>();

            for (Map<String, Object> company : companies) {
                if ("Y".equals(company.get("cmdm_useyn"))) {
                    NewSubsidiary subsidiary = new NewSubsidiary();
                    subsidiary.setCountryCode((String) company.get("country_code"));
                    subsidiary.setRequestChannel((String) company.get("request_channel"));
                    subsidiary.setErpUseYn((String) company.get("erp_useyn"));
                    subsidiary.setChangerId((String) company.get("changer_id"));

                    // change_date 변환 처리 (한국 시간 기준)
                    String changeDateStr = (String) company.get("change_date");
                    if (changeDateStr != null && !changeDateStr.isEmpty()) {
                        LocalDateTime changeDate = LocalDateTime.parse(changeDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        Timestamp changeDateTimestamp = Timestamp.valueOf(changeDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
                        subsidiary.setChangeDate(changeDateTimestamp.toLocalDateTime());
                    }

                    subsidiary.setCompanyName((String) company.get("company_name"));
                    subsidiary.setCompanyAbbreviation((String) company.get("company_abbreviation"));
                    subsidiary.setCreatorId((String) company.get("creator_id"));
                    subsidiary.setCmdmUseYn((String) company.get("cmdm_useyn"));
                    subsidiary.setCompanyCode((String) company.get("company_code"));

                    // CreateDate는 빈 값일 수 있으므로 예외 처리
                    String createDateStr = (String) company.get("create_date");
                    if (createDateStr != null && !createDateStr.isEmpty()) {
                        LocalDateTime createDate = LocalDateTime.parse(createDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        Timestamp createDateTimestamp = Timestamp.valueOf(createDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
                        subsidiary.setCreateDate(createDateTimestamp.toLocalDateTime());
                    }

                    subsidiaries.add(subsidiary);
                }
            }

            // 데이터 저장
            newSubsidiaryRepository.saveAll(subsidiaries);
        }
    }


    public void invitationCompanyMerge(Map<String, Object> requestData) {
        // 파라미터로 들어온 bpid와 mergeinto 값 추출
        String bpid = (String) requestData.get("bpid");
        String mergeInto = (String) requestData.get("mergeInto");

        // status가 pending이고 bpid가 B인 데이터를 찾음
        List<ChannelInvitation> invitations = channelInvitationRepository.findByBpidAndStatus(bpid, "pending");

        // 해당 데이터의 bpid 값을 A로 업데이트
        for (ChannelInvitation invitation : invitations) {
            invitation.setBpid(mergeInto);
        }

        // 변경된 데이터를 DB에 저장
        channelInvitationRepository.saveAll(invitations);
    }

    public Map<String, Object> updateContact(String context, Map<String, Object> userData,String channelName) {
        ObjectMapper objectMapper = new ObjectMapper();
        String apiUrl = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.updateContactUrl");

        Map<String, Object> queryParams = Map.of("context", context);

        log.info("updateContact: {}", userData);

        String userCountry = getProfileValue(userData, "country");
        Map<String, Object> marketingConsent = getMarketingConsent(userData);

        String Mktg_gbValue = "";
        String optIn = "N";
        String optOut = "Y";

        if (Boolean.parseBoolean(Optional.ofNullable(marketingConsent)
                .map(mc -> String.valueOf(mc.get("isConsentGranted")))  // null일 경우 "null" 문자열이 반환되지 않도록 처리
                .orElse("false"))) {
            Mktg_gbValue = "ALL";
            optIn = "Y";
            optOut = "N";
        }

        if ("FR".equals(userCountry) && marketingConsent == null) {
            optIn = "B";
            optOut = "B";
        }

        Map<String, String> SFDC_TO_CMDM_DEPT_MAP = commonCodeRepository.findByHeader("BIZ_WITH_DEPT").stream()
                .collect(Collectors.toMap(
                        CommonCode::getName,
                        CommonCode::getValue,
                        (existingValue, newValue) -> existingValue // 중복된 키가 있을 경우 기존 값을 유지
                ));

        String CMDMDepartment = SFDC_TO_CMDM_DEPT_MAP.getOrDefault(getDataValue(userData, "userDepartment"), "");

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("opt_in", optIn);
        bodyParams.put("opt_indate", java.time.LocalDate.now().toString());
        bodyParams.put("mktggb", Mktg_gbValue);
        bodyParams.put("opt_out", optOut);
        bodyParams.put("opt_outdate", java.time.LocalDate.now().toString());
        bodyParams.put("contactid", getDataValue(userData, "contactID"));

        log.info("Contact body params: {}", bodyParams);

        try {
            ResponseEntity<String> responseEntity = sendRequest(bodyParams, apiUrl, queryParams);
            Map<String, Object> responseData = objectMapper.readValue(responseEntity.getBody(), Map.class);

            if (!responseData.containsKey("contact")) {
                log.error("No contact created. " + responseEntity.getBody());
                throw new Exception("No contact created. " + responseEntity.getBody());
            }

            log.info("New CMDM contact created: {}", responseData);
            return responseData;
        } catch (Exception e) {
            log.error("Error creating contact: {}", e.getMessage());
            throw new RuntimeException("Error creating contact", e);
        }
    }
}
