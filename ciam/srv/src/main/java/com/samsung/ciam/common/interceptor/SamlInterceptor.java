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

@Component
public class SamlInterceptor implements HandlerInterceptor {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private CdcTraitService cdcTraitService;

  @Autowired
  private MenuAccessControlRepository menuAccessControlRepository;

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

