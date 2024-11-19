package com.samsung.ciam.services;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSResponse;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.common.gigya.service.GigyaService;
import com.samsung.ciam.repositories.ChannelRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;

import com.samsung.ciam.models.*;
import com.samsung.ciam.utils.StringUtil;
import com.samsung.ciam.repositories.BtpAccountsRepository;

@Slf4j
@Service
public class SearchService {

    private Object[][] customColumnDefList;


    @Autowired
    private CpiApiService apiService;

    @Autowired
    BtpAccountsRepository btpAccountsRepository;

    @Autowired
    private GigyaService gigyaService;

    @Autowired
    private AuditLogService auditLogService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ChannelRepository channelRepository;

    public void setCustomColumnDefList(Object[][] customColumnDefList){
        this.customColumnDefList = customColumnDefList;
    }

    public List<BtpAccounts> searchCompany(Map<String, String> param) {
        QBtpAccounts qBtpAccounts = QBtpAccounts.btpAccounts;
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);

        Object[][] columnDefList = {
                {"name", "Company Name", true, true},
                {"country", "Country", null, true},
                {"bizregno1", "Biz License No.", null, true},
                {"representative", "Representative", null, true},
                {"dunsno", "DUNS", null, true},
                {"vendorcode", "Vendor Code", null, true},
                {"validStatus", "Status", null, true}
        };

        // 컬럼 이름을 추출해서 String[] 배열로 변환
        String[] columnNameList = new String[columnDefList.length];
        for (int i = 0; i < columnDefList.length; i++) {
            columnNameList[i] = (String) columnDefList[i][0];
        }

        // 특정 조건: vendorcode 값이 있으면 country 조건을 무시한다.
        // vendorcode가 있으면 country 조건을 무시
        BooleanExpression vendorcodeCondition = this.getColumnExpression(qBtpAccounts, columnNameList[5], param.get(columnNameList[5]));
        String countryParam = param.get(columnNameList[1]);
        BooleanExpression countryCondition = vendorcodeCondition == null && !"other".equalsIgnoreCase(countryParam)
                ? this.getColumnExpression(qBtpAccounts, columnNameList[1], countryParam)
                : null;
        // BooleanExpression을 다중 조건으로 묶음
        BooleanExpression conditions = qBtpAccounts.isNotNull();  // 항상 true로 시작 (조건을 붙이기 위한 베이스)

        conditions = conditions
                .and(this.getColumnExpression(qBtpAccounts, columnNameList[0], param.get(columnNameList[0])))
                .and(countryCondition)  // 동적으로 country 조건 적용
                .and(this.getColumnExpression(qBtpAccounts, columnNameList[2], param.get(columnNameList[2])))
                .and(this.getColumnExpression(qBtpAccounts, columnNameList[3], param.get(columnNameList[3])))
                .and(this.getColumnExpression(qBtpAccounts, columnNameList[4], param.get(columnNameList[4])))
                .and(vendorcodeCondition)  // vendorcode 조건 적용
                .and(this.getColumnExpression(qBtpAccounts, columnNameList[6], param.get(columnNameList[6])));

        // Fetch 실행
        List<BtpAccounts> btpAccountsList = jpaQueryFactory.select(qBtpAccounts)
                .from(qBtpAccounts)
                .where(conditions)
                .orderBy(qBtpAccounts.name.asc())
                .fetch();
        return btpAccountsList;
    }

    public List<Map<String,Object>> searchAuditLogList(Map<String, String> param) {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);

        return auditLogService.getAuditLogList(jpaQueryFactory, param, this.getColumnNameList());
    }

    public String[] getColumnNameList() {
        int index = 0;
        String[] columnNames;
        if(this.customColumnDefList!=null){
            columnNames = new String[this.customColumnDefList.length];
            for (Object[] columnInfo : this.customColumnDefList) {
                columnNames[index++] = (String) columnInfo[0];
            }
        }else{
            columnNames = new String[0];
        }
        return columnNames;
    }

    public String[][] getColumnInfoList() {
        // 직접 선언한 컬럼 정보를 사용
        Object[][] columnDefList = {
                {"name", "Company Name"},
                {"country", "Country"},
                {"bizregno1", "Biz License No."},
                {"representative", "Representative"},
                {"dunsno", "DUNS"},
                {"vendorcode", "Vendor Code"},
                {"validStatus", "Status"}
        };

        String[][] columnInfoList = new String[columnDefList.length][2];
        int index = 0;
        for (Object[] columnInfo : columnDefList) {
            columnInfoList[index][0] = (String) columnInfo[0];
            columnInfoList[index++][1] = (String) columnInfo[1];
        }
        return columnInfoList;
    }

    public Object[][] getColumnList() {
        // 직접 선언한 컬럼 리스트 반환
        return new Object[][] {
                {"name", "Company Name", true, true},
                {"country", "Country", null, true},
                {"bizregno1", "Biz License No.", null, true},
                {"representative", "Representative", null, true},
                {"dunsno", "DUNS", null, true},
                {"vendorcode", "Vendor Code", null, true},
                {"validStatus", "Status", null, true}
        };
    }

    public Object[][] getPartnerColumnList() {
        // 직접 선언한 partnerColumnDefList 배열을 반환
        return new Object[][] {
                {"orgName", "Company Name", true, true},
                {"country", "Business Location", null, true},
                {"bizregno1", "Biz License No1", null, true},
                {"bizregno2", "Biz License No2", null, true},
                {"vatno", "VAT NO", null, true},
                {"state", "State", null, true},
                {"city", "City", null, true},
                {"street_address", "Street", null, true},
                {"zip_code", "Zip/Postal Code", null, true},
                {"dunsno", "DUNS No", null, true},
                {"rank", "Matching Score", null, true},
                {"validStatus", "Status", null, true},
                {"bpid", "accountId", null, true}
        };
    }

    private BooleanExpression getColumnExpression(QBtpAccounts qBtpAccounts, String columnName, String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        switch (columnName) {
            case "name":
                return qBtpAccounts.name.containsIgnoreCase(value);

            case "country":
                return qBtpAccounts.country.containsIgnoreCase(value);

            case "bizregno1":
                return qBtpAccounts.bizregno1.containsIgnoreCase(value);

            case "representative":
                return qBtpAccounts.representative.containsIgnoreCase(value);

            case "dunsno":
                return qBtpAccounts.dunsno.containsIgnoreCase(value);

            case "vendorcode":
                return qBtpAccounts.vendorcode.eq(value);

            case "validStatus":
                return qBtpAccounts.validStatus.containsIgnoreCase(value);

            default:
                return null;
        }
    }

    public Map<String, Object> searchSsoAccess(Map<String, String> param, HttpSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> gridData = new HashMap<>();
        String currentChannel = param.get("channels");
        String regSource = (String) session.getAttribute("cdc_regSource");
        String cdcUid = (String) session.getAttribute("cdc_uid");

        // cdc_uid를 쿼리에 포함합니다.
        String query = "SELECT data.channels,data.isCIAMAdmin FROM Account WHERE UID = '" + cdcUid + "'";
        GSResponse response = gigyaService.executeRequest("", "accounts.search", query);

        // Fetch all channels with channelDisplayName
        String channelType = channelRepository.selectChannelType(regSource);
        List<String> excludedChannels = Arrays.asList("partnerhub", "edo", "eBiz", "toolmate", "e2e", "ets", "mmp");

        List<Channels> filteredChannels = channelRepository.selectChannelTypeList(channelType);

        List<Channels> allChannels = filteredChannels.stream()
                .filter(channel -> !excludedChannels.contains(channel.getChannelName()))
                .collect(Collectors.toList());

        // Find the channelDisplayName
        String curretnChannelDisplayName = allChannels.stream()
                .filter(ch -> ch.getChannelName().equals(currentChannel))
                .map(Channels::getChannelDisplayName)
                .findFirst()
                .orElse(currentChannel); // Use channelName if displayName not found

        String jsonString = response.getResponseText();//"{ \"channels\": { \"gmapda\": { \"approvalStatus\": \"approved\", \"lastLogin\": \"2023-11-29T08:49:01.536Z\", \"adminType\": \"1\", \"approvalStatusDate\": \"2023-11-29 17:39:09\" }, \"gmapvd\": { \"approvalStatus\": \"approved\", \"adminType\": \"1\" } } }";
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode dataNode = resultsNode.get(0).path("data");
                JsonNode channelsNode = dataNode.path("channels");

                Set<String> activeChannels = new HashSet<>();
                List<Map<String, String>> addChannels = new ArrayList<>();
                List<Map<String, String>> deleteChannels = new ArrayList<>();

//                List<Map<String, String>> headers = new ArrayList<>();
//                headers.add(createHeaderMap("Accessible Channel", "channelName"));
//                headers.add(createHeaderMap("Granted Role", "adminType"));
//                headers.add(createHeaderMap("Authorized Date", "approvalStatusDate"));
//                headers.add(createHeaderMap("Status", ""));
//                headers.add(createHeaderMap("Channel Access", ""));

                List<Map<String, Object>> results = new ArrayList<>();

                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = channelsNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> field = fieldsIterator.next();
                    String channelName = field.getKey();
                    JsonNode channelDetails = field.getValue();

                    // Find the channelDisplayName
                    String channelDisplayName = allChannels.stream()
                            .filter(ch -> ch.getChannelName().equals(channelName))
                            .map(Channels::getChannelDisplayName)
                            .findFirst()
                            .orElse(channelName); // Use channelName if displayName not found


                    // 특정 채널 제외하기
                    if (channelDisplayName.equalsIgnoreCase(curretnChannelDisplayName)) {
                        continue; // 해당 채널을 건너뛰고 다음으로
                    }

                    String approvalStatus = channelDetails.path("approvalStatus").asText("");

                    // "disabled" 상태인 경우에도 addChannels에 포함되도록 함
                    if ("disabled".equals(approvalStatus)) {
                        addChannels.add(Map.of("channelName", channelName, "channelDisplayName", channelDisplayName));
                    }

                    if (!"approved".equals(approvalStatus)) {
                        continue; // approvalStatus가 approved가 아니면 건너뛰기
                    }

                    // lastLogin 필드가 없거나 값이 비어 있으면 제외
                    if (!channelDetails.has("lastLogin") || channelDetails.path("lastLogin").asText("").isEmpty()) {
                        continue;
                    }

                    // active 상태인 채널을 저장
                    String lastLogin = channelDetails.path("lastLogin").asText("");
                    String status = determineStatus(lastLogin);

                    if ("active".equals(status)) {
                        activeChannels.add(channelDisplayName);
                        deleteChannels.add(Map.of("channelName", channelName, "channelDisplayName", channelDisplayName)); // active인 경우 delete 목록에 추가
                    } else {
                        addChannels.add(Map.of("channelName", channelName, "channelDisplayName", channelDisplayName)); // inactive 또는 other 상태인 경우 add 목록에 추가
                    }

                    Map<String, Object> channelMap = new HashMap<>();
                    channelMap.put("channelDisplayName", channelDisplayName);
                    channelMap.put("channelName", channelName);
                    channelMap.put("approvalStatus", channelDetails.path("approvalStatus").asText(""));
                    channelMap.put("lastLogin", channelDetails.path("lastLogin").asText(""));

                    // adminType 설정 로직
                    String adminType = determineAdminType(channelDetails, dataNode);
                    channelMap.put("adminType", adminType);

                    // approvalStatusDate 변환 로직
                    String approvalStatusDate = channelDetails.path("approvalStatusDate").asText("");
                    approvalStatusDate = formatDate(approvalStatusDate);
                    channelMap.put("approvalStatusDate", approvalStatusDate);
                    channelMap.put("status", status);

                    results.add(channelMap);
                }

                for (Channels channel : allChannels) {
                    String channelName = channel.getChannelName();

                    // 해당 채널의 approvalStatus 확인
                    JsonNode channelDetails = channelsNode.path(channelName);
                    String approvalStatus = channelDetails.path("approvalStatus").asText("");

                    // approvalStatus가 "pending"이 아닌 경우만 addChannels에 추가
                    if (!"pending".equals(approvalStatus) &&
                            !activeChannels.contains(channel.getChannelDisplayName()) &&
                            addChannels.stream().noneMatch(map -> map.get("channelDisplayName").equals(channel.getChannelDisplayName())) &&
                            !channel.getChannelDisplayName().equalsIgnoreCase(currentChannel)) {
                        addChannels.add(Map.of("channelName", channel.getChannelName(), "channelDisplayName", channel.getChannelDisplayName()));
                    }
                }

                //gridData.put("header", headers);
                gridData.put("result", results);
                gridData.put("addChannels", addChannels);
                gridData.put("deleteChannels", deleteChannels);
            }


        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return gridData;
    }

    public String determineAdminType(JsonNode channelDetails, JsonNode dataNode) {
        // 우선적으로 isCIAMAdmin을 체크
        if (dataNode.path("isCIAMAdmin").asBoolean(false)) {
            return "CIAMAdmin";
        }

        // adminType을 ChannelSystemAdmin, ChannelBusinessAdmin, CompanyAdmin, User로 설정
        String adminType = channelDetails.path("adminType").asText("");
        switch (adminType) {
            case "1":
                return "ChannelSystemAdmin";
            case "2":
                return "ChannelBusinessAdmin";
            case "3":
                return "CompanyAdmin";
            default:
                return "User";
        }
    }

    public Map<String, String> createHeaderMap(String headerName, String field) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("headerName", headerName);
        headerMap.put("field", field);
        return headerMap;
    }

    public String formatDate(String dateStr) {
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
        );
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        for (String format : dateFormats) {
            try {
                // Remove millisecond and timezone parts if present
                int endIndex = dateStr.indexOf('.');
                if (endIndex != -1) {
                    dateStr = dateStr.substring(0, endIndex);
                }
                Date date = new SimpleDateFormat(format, Locale.ENGLISH).parse(dateStr);
                return outputFormat.format(date);
            } catch (Exception e) {
                // Continue to next format
            }
        }
        return dateStr; // 변환에 실패한 경우 원본 문자열 반환
    }

    public String determineStatus(String lastLogin) {
        try {
            SimpleDateFormat sdf;
            Date lastLoginDate;

            if (lastLogin.contains("T")) {
                // 'T'가 포함된 경우
                String datePart = lastLogin.split("T")[0];
                String timePart = lastLogin.split("T")[1].split("\\.")[0];
                String dateTimePart = datePart + "T" + timePart;

                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                lastLoginDate = sdf.parse(dateTimePart);
            } else {
                // 'T'가 포함되지 않은 경우
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                lastLoginDate = sdf.parse(lastLogin);
            }

            Date currentDate = new Date();
            long diffInMillis = currentDate.getTime() - lastLoginDate.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            return diffInDays >= 90 ? "inactive" : "active";
        } catch (Exception e) {
            log.error("Error parsing date: " + lastLogin, e);
            return "unknown"; // 오류가 발생한 경우 상태를 알 수 없음
        }
    }

}
