package com.samsung.ciam.controllers;

import com.samsung.ciam.utils.BeansUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 1. 파일명   : ChannelPasswordController.java
 * 2. 패키지   : com.samsung.ciam.controllers
 * 3. 설명     : 사용자의 비밀번호 재설정을 위한 화면을 구성하는 컨트롤러
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
public class ChannelPasswordController {

  /*
   * 1. 메소드명: resetpwd
   * 2. 클래스명: ChannelPasswordController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    비밀번호 재설정 페이지를 구성하여 반환
   * 2. 사용법
   *    /resetpwd 경로로 GET 요청 시 비밀번호 재설정 페이지를 반환
   * 3. 예시 데이터
   *    - Input: 없음
   *    - Output: 비밀번호 재설정 화면이 포함된 registration 템플릿
   * </PRE>
   * @param model 화면 구성에 필요한 모델 객체
   * @return 비밀번호 재설정 페이지로의 뷰 이름
   */
  @GetMapping("resetpwd")
  public String resetpwd(Model model) {
    String cdcKey = BeansUtil.getApiKeyForChannel("default");
    model.addAttribute("apiKey", cdcKey);

    String content = "fragments/signin/portalResetpwd";
    String headScript = "fragments/signin/headScript";
    String loginAPISrc = "https://cdns.gigya.com/js/gigya.js?apikey=";

    Boolean isBTPLogin = true;

    model.addAttribute("content", content);
    model.addAttribute("headScript", headScript);
    model.addAttribute("loginAPISrc", loginAPISrc);
    model.addAttribute("isBTPLogin", isBTPLogin);

    return "registration";
  }
}
