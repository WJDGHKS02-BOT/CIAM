package com.samsung.ciam.common.interceptor;

import com.samsung.ciam.models.Channels;
import com.samsung.ciam.models.MenuAccessControl;
import com.samsung.ciam.models.Users;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.repositories.MenuAccessControlRepository;
import com.samsung.ciam.services.CdcTraitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1. FileName	: SamlInterceptor.java
 * 2. Package	: com.samsung.ciam.common.interceptor
 * 3. Comments	: SAML 인증 요청에 대한 인터셉터로, 사용자 인증 및 채널 선택을 수행
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

@Component
public class SamlInterceptor implements HandlerInterceptor {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private CdcTraitService cdcTraitService;

  @Autowired
  private MenuAccessControlRepository menuAccessControlRepository;

  /*
   * 1. 메소드명: preHandle
   * 2. 클래스명: SamlInterceptor
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    요청을 가로채어 사용자 인증, 채널 선택, 및 메뉴 접근 권한을 처리하는 메소드
   * 2. 사용법
   *    Spring의 인터셉터 체인에서 자동 호출됨
   * 3. 예시 데이터
   *    - Input (HTTP 요청):
   *      request URI: /myPage/personalInformation
   *      session data: authenticatedUser = null (인증되지 않은 사용자)
   *    - Output:
   *      인증되지 않은 사용자일 경우, 로그인 페이지(/sso/login)로 리다이렉트
   * </PRE>
   * @param request HTTP 요청 객체
   * @param response HTTP 응답 객체
   * @param handler 처리 핸들러
   * @return boolean 접근을 허용할 경우 true, 차단할 경우 false
   * @throws Exception 예외 발생 시
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //====================================하드코딩 영역
//    Users tempUser = new Users(); // 추후 삭제 필요 (임시 코드)
//    //dragon03@yopmail.com  84ceb1b88ff04e1fb26be773bc440bd5
//    //btptest@yopmail.com b3de2636ecb04a448b1d12ca1ef063ac
//    //384a2467aa5b4af09d4e9528199fcbda
//
//    tempUser.setCdcUid("54bdae751ad84b85ba4abebd4ee80bb7"); // 추후 삭제 필요 (임시 코드)
//    request.getSession().setAttribute("cdc_uid", "54bdae751ad84b85ba4abebd4ee80bb7"); // 추후 삭제 필요 (임시 코드)
//
//    request.getSession().setAttribute("authenticatedUser", tempUser); // 추후 삭제 필요 (임시 코드)
//
//    //tempUser.setCdcUid("384a2467aa5b4af09d4e9528199fcbda"); // 추후 삭제 필요 (임시 코드)
//    //request.getSession().setAttribute("cdc_uid","384a2467aa5b4af09d4e9528199fcbda"); // 추후 삭제 필요 (임시 코드)
//
////        request.getSession().setAttribute("cdc_uid","b3de2636ecb04a448b1d12ca1ef063ac"); // 추후 삭제 필요 (임시 코드)
//
//    request.getSession().setAttribute("cdc_companyid", "A020153325"); // 추후 삭제 필요 (임시 코드)
//    Set<String> channelSet = new HashSet<>(); // 추후 삭제 필요 (임시 코드)
//    //channelSet.add("gmapvd"); // 추후 삭제 필요 (임시 코드)
//    channelSet.add("sba"); // 추후 삭제 필요 (임시 코드)
//    request.getSession().setAttribute("cdc_channels", channelSet); // 추후 삭제 필요 (임시 코드)
//    request.getSession().setAttribute("cdc_regSource", "SBA"); // 추후 삭제 필요 (임시 코드)
//    request.getSession().setAttribute("cdc_email", "sub_jeonghwan115@yopmail.com"); // 추후 삭제 필요 (임시 코드)
//    request.getSession().setAttribute("cdc_language", "en"); // 추후 삭제 필요 (임시 코드)
////        request.getSession().setAttribute("cdc_email","btptest@yopmail.com"); // 추후 삭제 필요 (임시 코드)

    //

    // 세션에서 인증된 사용자 정보 가져오기
    Users authenticatedUser = (Users) request.getSession().getAttribute("authenticatedUser");

    // 인증된 사용자가 없는 경우 로그인 페이지로 리다이렉트
    if (authenticatedUser == null) {
      String path = request.getRequestURI();
      request.getSession().setAttribute("relayState", path);
      response.sendRedirect("/sso/login");
      return false;
    } else if (request.getSession().getAttribute("session_channel") == null) {
      List<Channels> channelList = channelRepository.findAll();
      Set<String> cdcChannels = (Set<String>) request.getSession().getAttribute("cdc_channels");

      // cdc_channels와 일치하는 채널만 남기기
      List<Channels> matchedChannels = new ArrayList<>();
      for (Channels channel : channelList) {
        if (cdcChannels.contains(channel.getChannelName())) {
          matchedChannels.add(channel);
        }
      }

      // 필터링된 채널이 하나만 남은 경우 해당 채널을 세션에 저장하고 페이지 새로고침
      if (matchedChannels.size() == 1) {
        request.getSession().setAttribute("channels", matchedChannels);
        request.getSession().setAttribute("selectedChannelRedirectUrl", request.getRequestURI());//페이지 새로고침 X

        Channels channel = matchedChannels.get(0);
        request.getSession().setAttribute("session_channel", channel.getChannelName());
        request.getSession().setAttribute("session_display_channel", channel.getChannelDisplayName());
        cdcTraitService.setAdminSession(request.getSession());
        response.sendRedirect(request.getRequestURI());
        return false;
      } else {
        // 채널 선택 페이지로 리다이렉트
        request.getSession().setAttribute("channels", matchedChannels);
        request.getSession().setAttribute("isMultiChannel", "Y");//페이지 새로고침 X
        //request.getSession().setAttribute("session_channel", "");//페이지 새로고침 X
        request.getSession().setAttribute("selectedChannelRedirectUrl", request.getRequestURI());//페이지 새로고침 X
        response.sendRedirect("/myPage/selectChannel");
        //request.getRequestDispatcher("/myPage/selectChannel").forward(request, response);
        return false;
      }

    }

    // HTTP 메서드가 GET인지 확인
    if ("GET".equalsIgnoreCase(request.getMethod())) {

      // 사용자 역할 가져오기
      String role = (String) request.getSession().getAttribute("btp_myrole");
      String requestUri = request.getRequestURI();
      // 세션 ID가 붙어 있을 경우 제거
      String cleanedRequestUri = requestUri.split(";")[0];
      // URI를 '/'로 나눈 후 마지막 부분을 추출
      String[] uriParts = cleanedRequestUri.split("/");
      String menuId = uriParts.length > 2 ? uriParts[uriParts.length - 1] : "";

      // DB에서 메뉴가 menu_access_control에 존재하는지 확인
      boolean isMenuExist = menuAccessControlRepository.existsByMenuId(menuId);

      // 요청된 메뉴가 menu_access_control에 없으면 권한 체크를 하지 않음
      if (!isMenuExist) {
        return true; // 권한 체크를 하지 않고 요청을 계속 진행
      }

      String channel = (String) request.getSession().getAttribute("session_channel");

      // DB에서 해당 사용자의 역할에 따라 메뉴 접근 권한을 확인
      List<MenuAccessControl> accessibleMenus = menuAccessControlRepository.selectByRole(role, channel);
      boolean hasAccess = accessibleMenus.stream().anyMatch(menu -> menu.getMenuId().equals(menuId));

      // 접근 권한이 없는 경우 403 페이지로 리다이렉트
      if (!hasAccess) {
        response.sendRedirect("/error/errorPage?message=You do not have permission to access this page.");
        return false;
      }
    }

    return true;
  }
}


