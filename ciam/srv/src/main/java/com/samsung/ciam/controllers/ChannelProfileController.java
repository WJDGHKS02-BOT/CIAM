package com.samsung.ciam.controllers;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.services.CdcTraitService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 1. 파일명   : ChannelProfileController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 특정 채널의 프로필로 자동 설정한 후 마이페이지로 이동하기 위한 컨트롤러
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜        | 이름          | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Controller
@RequestMapping("/channel-profile")
public class ChannelProfileController {

    @Autowired
    private CdcTraitService cdcTraitService;

    @Autowired
    private ChannelRepository channelRepository;

    /*
     * 1. 메소드명: convert
     * 2. 클래스명: ChannelProfileController
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    특정 채널의 프로필을 세션에 설정하고, 설정된 채널과 함께 마이페이지로 이동
     * 2. 사용법
     *    /channel-profile/{channel} 경로로 GET 요청 시 해당 채널의 프로필을 세션에 저장 후 마이페이지로 리다이렉트
     * 3. 예시 데이터
     *    - Input: channel 파라미터 (예: "SamsungTV")
     *    - Output: /myPage/personalInformation으로 리다이렉트
     * </PRE>
     * @param channel 채널 이름
     * @param servletRequest Servlet 요청 객체
     * @param request HTTP 요청 객체
     * @param model 모델 객체
     * @param session 사용자 세션
     * @return 마이페이지로 리다이렉트 뷰
     */
    @GetMapping("/{channel}")
    public RedirectView convert(
            @PathVariable String channel,
            ServletRequest servletRequest,
            HttpServletRequest request,
            Model model,
            HttpSession session
    ) {

        //채널 표시명 조회
        String channelDisplayName = channelRepository.selectChannelDisplayName(channel);

        //채널정보로 세션 저장
        session.setAttribute("session_channel",channel);
        session.setAttribute("session_display_channel",channelDisplayName);

        //권한 설정
        cdcTraitService.setAdminSession(session);

        //VIEW 이동
        return new RedirectView("/myPage/personalInformation");
    }
}
