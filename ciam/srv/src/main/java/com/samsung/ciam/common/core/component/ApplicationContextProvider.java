package com.samsung.ciam.common.core.component;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 1. FileName	: ApplicationContextProvider.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: Spring ApplicationContext를 가져와 static 메서드나 인스턴스에서 Bean을 참조할 수 있도록 제공
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

/*
 * 1. 클래스명	: ApplicationContextProvider
 * 2. 파일명	: ApplicationContextProvider.java
 * 3. 패키지명	: com.samsung.ciam.common.core.component
 * 4. 작성자명	: 서정환
 * 5. 작성일자	: 2024. 11. 04.
 */

@Component
public class ApplicationContextProvider implements ApplicationContextAware {


    private static ApplicationContext applicationContext;


    /*
     * 1. 메소드명: setApplicationContext
     * 2. 클래스명: ApplicationContextProvider
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Spring이 제공하는 ApplicationContext 객체를 설정하는 메서드로,
     *    ApplicationContextAware 인터페이스의 구현을 통해 호출됨
     * 2. 사용법
     *    setApplicationContext(ApplicationContext applicationContext)
     * </PRE>
     * @param applicationContext Spring의 IoC 컨테이너
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        ApplicationContextProvider.applicationContext = applicationContext;

    }

    /*
     * 1. 메소드명: getApplicationContext
     * 2. 클래스명: ApplicationContextProvider
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Spring IoC 컨테이너(ApplicationContext)에서 관리되는 Bean을 찾기 위해 ApplicationContext 객체를 가져옴
     * 2. 사용법
     *    static 메서드나 new로 생성한 인스턴스에서 Bean을 참조해야 할 경우 이용
     * </PRE>
     * @return ApplicationContext Spring의 IoC 컨테이너
     */
    public static   ApplicationContext getApplicationContext() {
        return applicationContext;
    }




}

