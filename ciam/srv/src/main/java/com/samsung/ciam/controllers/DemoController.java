package com.samsung.ciam.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 1. FileName	: CpiApiService.java
 * 2. Package	: com.samsung.ciam.common.cpi.service
 * 3. Comments	: local,dev환경 데모용 페이지 컨트롤러
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

@Controller
@Profile({"local", "dev"}) // 로컬 및 개발 환경에서만 활성화되는 컨트롤러
public class DemoController {

  /*
   * 1. 메소드명: demo
   * 2. 클래스명: DemoController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    데모 페이지 View를 호출하여 welcome 화면을 렌더링.
   * 2. 사용법
   *    "/demo" URL로 접근하면 welcome 페이지가 표시됨.
   * </PRE>
   *
   * @return welcome 화면을 위한 ModelAndView 객체
   */
  @GetMapping("/demo")
  public ModelAndView demo() {
    return new ModelAndView("welcome");
  }

  /*
   * 1. 메소드명: testconsent
   * 2. 클래스명: DemoController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    약관 동의 테스트 페이지 View를 호출하여 testconsent 화면을 렌더링.
   * 2. 사용법
   *    "/testconsent" URL로 접근하면 약관 동의 테스트 페이지가 표시됨.
   * </PRE>
   *
   * @return 약관 테스트 화면을 위한 ModelAndView 객체
   */
  @GetMapping("/testconsent")
  public ModelAndView testconsent() {
    return new ModelAndView("testconsent");
  }

  /*
   * 1. 메소드명: testgetAccountInfo
   * 2. 클래스명: DemoController
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    계정 정보 조회 테스트 페이지 View를 호출하여 testgetAccountInfo 화면을 렌더링.
   * 2. 사용법
   *    "/testgetAccountInfo" URL로 접근하면 계정 정보 조회 테스트 페이지가 표시됨.
   * </PRE>
   *
   * @return 계정 조회 테스트 화면을 위한 ModelAndView 객체
   */
  @GetMapping("/testgetAccountInfo")
  public ModelAndView testgetAccountInfo() {
    return new ModelAndView("testgetAccountInfo");
  }

}