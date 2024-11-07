package com.samsung.ciam.saml;

import java.util.List;
import java.util.Map;

/**
 * 1. FileName	: SamlInterceptor.java
 * 2. Package	: com.samsung.ciam.common.interceptor
 * 3. Comments	: 사용자가 SAML 인증을 통해 로그인할 때 발생하는 이벤트를 정의하는 클래스
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
public class OneloginLoginEvent {

    private Map<String, List<String>> userAttributes;

    /*
     * 1. 메소드명: OneloginLoginEvent (생성자)
     * 2. 클래스명: OneloginLoginEvent
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 사용자 속성을 사용하여 OneloginLoginEvent 객체를 초기화.
     * 2. 사용법
     *    사용자가 인증을 완료한 후, 관련된 사용자 속성을 저장할 때 사용.
     * 3. 예시 데이터
     *    - Input: userAttributes - 사용자 속성 맵 (예: "email" -> ["user@example.com"])
     *    - Output: OneloginLoginEvent 객체
     * </PRE>
     * @param userAttributes 사용자 속성 맵 (속성 이름 -> 값 목록)
     */
    public OneloginLoginEvent(Map<String, List<String>> userAttributes) {
        this.userAttributes = userAttributes;
    }

    /*
     * 1. 메소드명: getUserAttributes
     * 2. 클래스명: OneloginLoginEvent
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그인 이벤트의 사용자 속성을 반환.
     * 2. 사용법
     *    로그인한 사용자의 속성 정보를 조회할 때 사용.
     * 3. 예시 데이터
     *    - Output: Map<String, List<String>> 형태의 사용자 속성 맵
     * </PRE>
     * @return Map<String, List<String>> 사용자 속성 맵
     */
    public Map<String, List<String>> getUserAttributes() {
        return userAttributes;
    }

    /*
     * 1. 메소드명: setUserAttributes
     * 2. 클래스명: OneloginLoginEvent
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그인 이벤트의 사용자 속성을 설정.
     * 2. 사용법
     *    새로운 사용자 속성 데이터를 설정할 때 사용.
     * 3. 예시 데이터
     *    - Input: userAttributes - 새로운 사용자 속성 맵
     * </PRE>
     * @param userAttributes 새로운 사용자 속성 맵 (속성 이름 -> 값 목록)
     */
    public void setUserAttributes(Map<String, List<String>> userAttributes) {
        this.userAttributes = userAttributes;
    }
}
