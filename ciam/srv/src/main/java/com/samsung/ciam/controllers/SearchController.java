package com.samsung.ciam.controllers;

import java.util.*;

import com.samsung.ciam.common.cpi.enums.CpiRequestFieldMapping;
import com.samsung.ciam.common.cpi.enums.CpiResponseFieldMapping;
import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.samsung.ciam.utils.StringUtil;
import com.samsung.ciam.services.SearchService;
import com.samsung.ciam.models.BtpAccounts;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private CpiApiService cpiApiService;

    @RequestMapping("/company")
    public Map<String, Object> searchCompany(@RequestBody Map<String, String> allParams,HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (allParams.get("country") == null || "".equals(allParams.get("country"))) {
            allParams.put("country",(String) session.getAttribute("secCountry"));
        }
        List<BtpAccounts> searchCompany = searchService.searchCompany(allParams);
        result.put("results", searchCompany);
        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon"};
        // String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon", "filter", "floatingFilter"};
        Object[][] columnList = searchService.getColumnList();

        List<Map<String,Object>> headerInfo = new ArrayList<>();
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }
        result.put("header", headerInfo);

        return result;
    }

    @RequestMapping("/ssoAccessListSerach")
    public Map<String, Object> ssoAccessListSerach(@RequestBody Map<String, String> allParams, HttpSession session, Model model) {
        Map<String, Object> result = new HashMap<>();

        result = searchService.searchSsoAccess(allParams,session);

        List<String> addChannels = (List<String>) result.get("addChannels");
        List<String> deleteChannels = (List<String>) result.get("deleteChannels");

        model.addAttribute("addChannels", addChannels);
        model.addAttribute("deleteChannels", deleteChannels);

        return result;
    }

    @RequestMapping("/partnerCompany")
    public Map<String, Object> partnerCompany(@RequestBody Map<String, Object> allParams,HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> cpiResut = new ArrayList<>();

        if(!StringUtil.isEmpty(allParams.get("accountId"))){
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.accountSerachUrl");
            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("acctid",allParams.get("accountId"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));
            cpiResut = cpiApiService.accountSearch(dataMap, url, responseFieldMapping,session);
        } else {
            String url = BeansUtil.getApplicationProperty("cpi.url") + BeansUtil.getApplicationProperty("cpi.findSimilarUrl");
            CpiRequestFieldMapping requestFieldMapping = CpiRequestFieldMapping.fromString((String) allParams.get("channel"));
            CpiResponseFieldMapping responseFieldMapping = CpiResponseFieldMapping.fromString((String) allParams.get("channel"));
            cpiResut = cpiApiService.findSimilar(allParams, url, requestFieldMapping ,responseFieldMapping ,session);
        }

        result.put("results", cpiResut);
        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon"};
        Object[][] columnList = searchService.getPartnerColumnList();

        List<Map<String,Object>> headerInfo = new ArrayList<>();
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }
        result.put("header", headerInfo);

        return result;
    }

    @RequestMapping("/auditLog")
    public Map<String, Object> searchAuditLog(@RequestBody Map<String, String> allParams) {
        Map<String, Object> result = new HashMap<>();

        String[] columnDefKey = { "field", "headerName", "checkboxSelection", "unSortIcon", "cellRenderer" };
        Object[][] columnList = {
            {"requester_uid", "Requester UID", null, true, true},
            {"email", "Login ID", null, true, null},
            {"channel", "Channel", null, true, null},
            {"type", "Menu", null, true, null},
            {"menu_type", "Menu Type", null, true, null},
            {"action", "Action", null, true, null},
            {"created_at", "Date", null, true, null}
        };
        searchService.setCustomColumnDefList(columnList);

        List<Map<String,Object>> searchAuditLogList = searchService.searchAuditLogList(allParams);
        result.put("results", searchAuditLogList);

        List<Map<String,Object>> headerInfo = new ArrayList<>();
        for(Object[] columnInfo : columnList){
            Map<String,Object> header = new HashMap<>();
            for(int idx=0; idx<columnInfo.length; idx++){
                if(columnInfo[idx]!=null){
                    header.put(columnDefKey[idx], columnInfo[idx]);
                }
            }
            headerInfo.add(header);
        }
        result.put("header", headerInfo);

        return result;
    }

    @ResponseBody
    @PostMapping("/checkCompanyDuplicate")
    public ResponseEntity<Map<String, Object>> checkCompanyDuplicate(@RequestBody Map<String, String> requestParams,
                                                                     HttpSession session) {
        // 중복 체크를 위한 요청 파라미터 생성
        Map<String, Object> companyDupCheckReq = new HashMap<>();
        companyDupCheckReq.put("filter_bizregno1", requestParams.getOrDefault("bizregno1", ""));
        companyDupCheckReq.put("filter_dunsno", requestParams.getOrDefault("dunsno", ""));
        companyDupCheckReq.put("name", requestParams.getOrDefault("name", ""));
        companyDupCheckReq.put("channel", requestParams.getOrDefault("channel", ""));
        companyDupCheckReq.put("isNewCompany", requestParams.getOrDefault("isNewCompany", ""));
        companyDupCheckReq.put("country", requestParams.getOrDefault("country", ""));

        // 중복 체크 로직 수행
        Map<String, Object> response = cpiApiService.partnerCompanyDuplicateCheck(companyDupCheckReq, session);

        if ("duplicate".equals(response.get("result"))) {
            // 중복이 발견된 경우, OK 상태로 중복 메시지 반환
            return ResponseEntity.ok(response);
        } else if ("error".equals(response.get("result"))) {
            // 오류가 발생한 경우, 서버 에러 상태로 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            // 중복이 없을 경우 OK 상태로 반환
            return ResponseEntity.ok(response);
        }
    }


}
