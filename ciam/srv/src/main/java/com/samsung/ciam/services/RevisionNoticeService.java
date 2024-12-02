package com.samsung.ciam.services;

import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RevisionNoticeService {

    @Autowired
    private RevisionNoticeRepository revisionNoticeRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private WfMasterRepository wfMasterRepository;

    @PersistenceContext
    private EntityManager entityManager;


    public ModelAndView revisionNotice(HttpServletRequest request, HttpSession session) {

        ModelAndView modelAndView = new ModelAndView("myPage");

        //채널 정보 가져옴
        List<Channels> channels  = new ArrayList<Channels>();
        channels = channelRepository.findAll();
        modelAndView.addObject("channels", channels);

        modelAndView.addObject("content", "fragments/myPage/revisionNotice");
        modelAndView.addObject("menu", "revisionNotice");
        modelAndView.addObject("role", session.getAttribute("btp_myrole"));

        return modelAndView;
    }


    public Map<String, Object> getRevisionNotice(Map<String, String> allParams, HttpSession session) {

        Map<String, Object> gridData = new HashMap<>();

        StringBuilder queryBuilder = new StringBuilder("SELECT id, revision_title, revision_contents, language_id, status, type, ");
        queryBuilder.append( "TO_CHAR(apply_at, 'YYYY-MM-DD HH24:MI:SS') as apply_at, TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at, TO_CHAR(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at, ");
        queryBuilder.append("channel, subsidiary ");
        queryBuilder.append("FROM revision_notice rn ");
        queryBuilder.append("WHERE 1=1 ");

        // 동적 조건 추가
        if (allParams.get("startDate") != null) {
            queryBuilder.append("AND rn.created_at >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI:SS') ");
        }

        if (allParams.get("endDate") != null) {
            queryBuilder.append("AND rn.created_at <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (!"all".equals(allParams.get("status"))) {
            queryBuilder.append("AND rn.status = :status ");
        }
        if (!"all".equals(allParams.get("type"))) {
            queryBuilder.append("AND rn.type = :type ");
        }
        if (!"all".equals(allParams.get("channel"))) {
            allParams.put("channel", "%" + allParams.get("channel") + "%" );
            queryBuilder.append("AND rn.channel LIKE :channel ");
        }
        if (allParams.get("revisionsearch") != null && !allParams.get("revisionsearch").isEmpty()) {
            queryBuilder.append("AND (rn.revision_title LIKE :revisionsearch or rn.revision_contents LIKE :revisionsearch) ");
        }

        queryBuilder.append("ORDER BY created_at DESC");

        Query query = entityManager.createNativeQuery(queryBuilder.toString());

        // 파라미터 바인딩
        if (allParams.get("startDate") != null) {
            // String 날짜를 타임스탬프로 변환 (날짜 형식이 올바른지 확인하고 시간 부분 추가)
            String startDateString = allParams.get("startDate") + " 00:00:00";  // 시간 부분 추가
            Timestamp startDate = Timestamp.valueOf(startDateString);
            query.setParameter("startDate", startDate);
        }
        if (allParams.get("endDate") != null) {
            // String 날짜를 타임스탬프로 변환 (날짜 형식이 올바른지 확인하고 시간 부분 추가)
            String endDateString = allParams.get("endDate") + " 23:59:59";  // 시간 부분 추가
            Timestamp endDate = Timestamp.valueOf(endDateString);
            query.setParameter("endDate", endDate);
        }
        if (!"all".equals(allParams.get("status"))) {
            query.setParameter("status", allParams.get("status"));
        }
        if (!"all".equals(allParams.get("type"))) {
            query.setParameter("type", allParams.get("type"));
        }
        if (!"all".equals(allParams.get("channel"))) {
            query.setParameter("channel", allParams.get("channel"));
        }
        if (allParams.get("revisionsearch") != null && !allParams.get("revisionsearch").isEmpty()) {
            query.setParameter("revisionsearch", "%" + allParams.get("revisionsearch") + "%"); // 이메일 검색시 LIKE로 처리
        }

        List<Object[]> revisionsNotice = query.getResultList();

        List<Map<String, Object>> revisionData = new ArrayList<>();

        for (Object[] revisions : revisionsNotice) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("id", revisions[0]);
            rowData.put("revision_title", revisions[1]);
            rowData.put("revision_contents", revisions[2]);
            rowData.put("language_id", revisions[3]);
            rowData.put("status", revisions[4]);
            rowData.put("type", revisions[5]);
            rowData.put("apply_at", revisions[6]);
            rowData.put("created_at", revisions[7]);
            rowData.put("updated_at", revisions[8]);
            rowData.put("channel", revisions[9]);
            rowData.put("subsidiary", revisions[10]);
            revisionData.add(rowData);
        }

        gridData.put("result", revisionData);

        return gridData;
    }


    //아래 파트는 StringBuilder와 entityManager.createNativeQuery를 사용해 DB를 조회한 방식

//    public RedirectView revisionListDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
//
//        String id = String.valueOf(payload.get("id"));
//
//        StringBuilder queryBuilder = new StringBuilder("SELECT id, revision_title, revision_contents, language_id, status, kind, ");
//        queryBuilder.append( "TO_CHAR(apply_at, 'YYYY-MM-DD HH24:MI:SS') as apply_at, TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at, TO_CHAR(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at, ");
//        queryBuilder.append("coverage, location, subsidiary ");
//        queryBuilder.append("FROM revision_list rl ");
//        queryBuilder.append("WHERE 1=1 ");
//        queryBuilder.append("AND rl.id = CAST(:id AS bigint) ");
//
//        Query query = entityManager.createNativeQuery(queryBuilder.toString());
//        query.setParameter("id", Long.parseLong(id)); // 파라미터 바인딩
//
//        List<Object[]> revisionsList = query.getResultList();
//
//        List<Map<String, Object>> revisionData = new ArrayList<>();
//
//        for (Object[] revisions : revisionsList) {
//            Map<String, Object> rowData = new HashMap<>();
//            rowData.put("id", revisions[0]);
//            rowData.put("revision_title", revisions[1]);
//            rowData.put("revision_contents", revisions[2]);
//            rowData.put("language_id", revisions[3]);
//            rowData.put("status", revisions[4]);
//            rowData.put("kind", revisions[5]);
//            rowData.put("apply_at", revisions[6]);
//            rowData.put("created_at", revisions[7]);
//            rowData.put("updated_at", revisions[8]);
//            rowData.put("coverage", revisions[9]);
//            rowData.put("location", revisions[10]);
//            rowData.put("subsidiary", revisions[11]);
//            revisionData.add(rowData);
//        }
//        //세션에 revisionData 저장
//        session.setAttribute("revisionDetailData", revisionData);
//
//        return new RedirectView("/myPage/revisionListDetail");
//    }


    //아래 파트는 repository를 사용해 DB를 조회한 방식

//    public RedirectView revisionListDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
//
//        String id = payload.get("id");
//        Long revisionId = Long.parseLong(id);
//
//        List<Object[]> revisionsList = revisionListRepository.findRevisionById(revisionId);
//
//        List<Map<String, Object>> revisionData = new ArrayList<>();
//
//        for (Object[] revisions : revisionsList) {
//            Map<String, Object> rowData = new HashMap<>();
//            rowData.put("id", revisions[0]);
//            rowData.put("revision_title", revisions[1]);
//            rowData.put("revision_contents", revisions[2]);
//            rowData.put("language_id", revisions[3]);
//            rowData.put("status", revisions[4]);
//            rowData.put("kind", revisions[5]);
//            rowData.put("apply_at", revisions[6]);
//            rowData.put("created_at", revisions[7]);
//            rowData.put("updated_at", revisions[8]);
//            rowData.put("coverage", revisions[9]);
//            rowData.put("location", revisions[10]);
//            rowData.put("subsidiary", revisions[11]);
//            revisionData.add(rowData);
//        }
//
//        //세션에 revisionData 저장
//        session.setAttribute("revisionDetailData", revisionData);
//
//        return new RedirectView("/myPage/revisionListDetail");
//    }


    //아래 파트는 JPA를 사용해 DB를 조회한 방식

    public RedirectView revisionNoticeDetail(Map<String, String> payload, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {

        String id = payload.get("id");
        Long revisionId = Long.parseLong(id);
        List<Object[]> revisionsNotice = revisionNoticeRepository.findRevisionById(revisionId);

        List<Map<String, Object>> revisionData = new ArrayList<>();
        for (Object[] revisions : revisionsNotice) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("id", revisions[0]);
            rowData.put("revision_title", revisions[1]);
            rowData.put("revision_contents", revisions[2]);
            rowData.put("language_id", revisions[3]);
            rowData.put("status", revisions[4]);
            rowData.put("type", revisions[5]);
            rowData.put("apply_at", revisions[6]);
            rowData.put("created_at", revisions[7]);
            rowData.put("updated_at", revisions[8]);
            rowData.put("channel", revisions[9]);
            rowData.put("subsidiary", revisions[10]);
            revisionData.add(rowData);
        }

        // 세션에 저장
        session.setAttribute("revisionDetailData", revisionData);

        return new RedirectView("/myPage/revisionNoticeDetail");
    }


    public ModelAndView revisionNoticeDetail(Map<String, Object> payload, HttpServletRequest request, HttpSession session, Model model) {
        List<Map<String, Object>> revisionData = (List<Map<String, Object>>) session.getAttribute("revisionDetailData");

        if (revisionData != null) {
            model.addAttribute("revisionData", revisionData);  // 모델에 revisionData 추가
            session.removeAttribute("revisionDetailData");      // 세션에서 데이터 제거 (일회성 사용)
        }

        ModelAndView modelAndView = new ModelAndView("myPage");

        modelAndView.addObject("payload", payload);
        modelAndView.addObject("content", "fragments/myPage/revisionNoticeDetail");
        modelAndView.addObject("menu", "revisionNotice");

        return modelAndView;
    }

}
