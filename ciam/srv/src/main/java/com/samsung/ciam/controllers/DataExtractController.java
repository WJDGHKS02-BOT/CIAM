package com.samsung.ciam.controllers;

import com.samsung.ciam.common.cpi.service.CpiApiService;
import com.samsung.ciam.models.WfMaster;
import com.samsung.ciam.repositories.WfMasterRepository;
import com.samsung.ciam.services.SystemService;
import com.samsung.ciam.utils.BeansUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DataExtractController {

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/approvalList")
    public ModelAndView approvalList(
            HttpServletRequest request,
            Model model
    ) {

        String query = "SELECT channel, cc.\"name\", requestor_email, requestor_company_name, requestor_company_code, requested_date " +
                "FROM wf_master T1 " +
                "LEFT JOIN common_code cc ON cc.\"code\" = T1.workflow_code AND cc.\"header\" = 'REQUEST_TYPE_CODE' WHERE T1.status= 'approved' " +
                "ORDER BY requested_date DESC";

        List<Object[]> wfMasterList = entityManager.createNativeQuery(query).getResultList();

        List<Map<String, Object>> approvalData = new ArrayList<>();

        // 쿼리 결과를 Map으로 변환하여 리스트에 추가
        for (Object[] wfMaster : wfMasterList) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("channel", wfMaster[0]);
            rowData.put("requestType", wfMaster[1]);
            rowData.put("requestorId", wfMaster[2]);
            rowData.put("Company", wfMaster[3]);
            rowData.put("CompanyCode", wfMaster[4]);
            rowData.put("requestedDate", wfMaster[5]);

            approvalData.add(rowData);
        }

        ModelAndView modelAndView = new ModelAndView("approvalList");
        modelAndView.addObject("approvalData", approvalData);
        modelAndView.addObject("channel", "test");

        return modelAndView;
    }
}
