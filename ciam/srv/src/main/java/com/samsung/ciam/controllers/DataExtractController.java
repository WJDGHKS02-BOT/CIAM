package com.samsung.ciam.controllers;

import com.samsung.ciam.repositories.WfMasterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1. FileName	: DataExtractController.java
 * 2. Package	: com.samsung.ciam.controllers
 * 3. Comments	: 승인된 요청 목록을 조회하여 화면에 표시하는 컨트롤러 클래스
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */

@RestController
public class DataExtractController {

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * 1. 메소드명: approvalList
     * 2. 클래스명: DataExtractController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    승인된 요청 목록을 조회하여 approvalList 화면에 표시.
     * 2. 사용법
     *    "/approvalList" URL로 접근 시 승인된 요청 데이터가 화면에 렌더링됨.
     * 3. 예시 데이터
     *    - Input: 없음
     *    - Output: ModelAndView 객체에 승인된 요청 목록 데이터를 담아 반환.
     * </PRE>
     *
     * @param request HttpServletRequest 객체
     * @param model Model 객체
     * @return 승인된 요청 목록 화면을 위한 ModelAndView 객체
     */
    @GetMapping("/approvalList")
    public ModelAndView approvalList(
            HttpServletRequest request,
            Model model
    ) {
        // 승인된 요청 목록을 조회하는 쿼리문 정의
        String query = "SELECT channel, cc.\"name\", requestor_email, requestor_company_name, requestor_company_code, requested_date " +
                "FROM wf_master T1 " +
                "LEFT JOIN common_code cc ON cc.\"code\" = T1.workflow_code AND cc.\"header\" = 'REQUEST_TYPE_CODE' WHERE T1.status= 'approved' " +
                "ORDER BY requested_date DESC";

        // EntityManager를 통해 쿼리 실행 및 결과 리스트 반환
        List<Object[]> wfMasterList = entityManager.createNativeQuery(query).getResultList();

        // 결과 데이터를 화면 표시용 Map 형식으로 변환하여 저장
        List<Map<String, Object>> approvalData = new ArrayList<>();

        // 각 결과 행을 Map 형태로 변환하여 approvalData 리스트에 추가
        for (Object[] wfMaster : wfMasterList) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("channel", wfMaster[0]);           // 요청 채널
            rowData.put("requestType", wfMaster[1]);       // 요청 유형
            rowData.put("requestorId", wfMaster[2]);       // 요청자 이메일
            rowData.put("Company", wfMaster[3]);           // 요청자 회사명
            rowData.put("CompanyCode", wfMaster[4]);       // 요청자 회사 코드
            rowData.put("requestedDate", wfMaster[5]);     // 요청 날짜

            approvalData.add(rowData);                     // approvalData 리스트에 추가
        }

        // ModelAndView 객체를 생성하고 화면 템플릿에 데이터 전달
        ModelAndView modelAndView = new ModelAndView("approvalList");
        modelAndView.addObject("approvalData", approvalData);  // 승인된 요청 목록 데이터
        modelAndView.addObject("channel", "test");             // 임시 테스트 채널 데이터

        return modelAndView; // approvalList 화면을 렌더링할 ModelAndView 객체 반환
    }
}
