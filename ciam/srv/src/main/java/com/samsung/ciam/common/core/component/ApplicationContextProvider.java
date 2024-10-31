package com.samsung.ciam.common.core.component;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {


    private static ApplicationContext applicationContext;


    /**
     * <PRE>
     * 1. 설명
     *
     * 2. 사용법
     *
     * </PRE>
     *   @param applicationContext
     *   @throws BeansException
     */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        ApplicationContextProvider.applicationContext = applicationContext;

    }

    /**
     * <PRE>
     * 1. 설명
     *		spring IoC Container(ApplicationContext)에서 관리되는 Bean을 찾기 위해 ApplicationContext 객체를 가져옴
     * 2. 사용법
     *		static 메서드 및  new로 생성한 인스턴스에서 Bean을 참조해야 할 경우 이용
     * </PRE>
     *   @return
     */
    public static   ApplicationContext getApplicationContext() {
        return applicationContext;
    }




}

