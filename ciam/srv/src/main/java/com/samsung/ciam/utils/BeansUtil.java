package com.samsung.ciam.utils;

import com.samsung.ciam.common.core.component.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * 1. FileName	: BeansUtil.java
 * 2. Package	: com.samsung.ciam.utils
 * 3. Comments	: Spring Bean 및 프로퍼티 파일의 유틸리티 메소드들을 제공하는 클래스
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

public class BeansUtil {

  /*
   * 1. 메소드명: getBean
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    Spring ApplicationContext에서 주어진 이름으로 Bean 객체를 반환
   * 2. 사용법
   *    getBean("beanName") 와 같이 호출하여 Bean 객체를 얻음
   * </PRE>
   * @param beanName 빈 객체의 이름
   * @return Object 이름에 해당하는 Bean 객체
   */
  public static Object getBean(String beanName) {
    ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
    // String array[] =  applicationContext.getBeanDefinitionNames();

    return applicationContext.getBean(beanName);
  }

  /*
   * 1. 메소드명: getApplicationProperty
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    application.yaml에 저장된 프로퍼티 값을 가져옴
   * 2. 사용법
   *    getApplicationProperty("property.key") 와 같이 호출
   * </PRE>
   * @param key 프로퍼티 키
   * @return String 프로퍼티 값, null이면 빈 문자열 반환
   */
  public static String getApplicationProperty(String key) {
    Environment env = (Environment) getBean("env");
    String value = env.getProperty(key);
    if (value == null) return "";
    return value;
  }

  /*
   * 1. 메소드명: getApplicationProperties
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    application.yaml에 저장된 여러 프로퍼티 값을 리스트로 반환
   * 2. 사용법
   *    getApplicationProperties("property.key") 와 같이 호출
   * </PRE>
   * @param key 프로퍼티 키
   * @return List<String> 프로퍼티 값 리스트
   */
  public static List<String> getApplicationProperties(String key) {
    Environment env = (Environment) getBean("env");
    String value = env.getProperty(key);
    if (value == null) return new ArrayList<String>();
    return Arrays.asList(value.split(","));
  }

  /*
   * 1. 메소드명: getApplicationProperty
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 타입으로 프로퍼티 값을 가져옴
   * 2. 사용법
   *    getApplicationProperty("property.key", Integer.class) 와 같이 호출
   * </PRE>
   * @param key 프로퍼티 키
   * @param targetType 반환 타입
   * @return T 프로퍼티 값, null이면 기본값 반환
   */
  public static <T> T getApplicationProperty(String key, Class<T> targetType) {
    Environment env = (Environment) getBean("env");
    return env.getProperty(key, targetType);
  }

  /*
   * 1. 메소드명: getApplicationProperty
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 타입으로 프로퍼티 값을 가져오고, 값이 없을 경우 기본값을 반환
   * 2. 사용법
   *    getApplicationProperty("property.key", Integer.class, 1) 와 같이 호출
   * </PRE>
   * @param key 프로퍼티 키
   * @param targetType 반환 타입
   * @param defaultValue 기본값
   * @return T 프로퍼티 값 또는 기본값
   */
  public static <T> T getApplicationProperty(String key, Class<T> targetType, T defaultValue) {
    Environment env = (Environment) getBean("env");
    return env.getProperty(key, targetType, defaultValue);
  }

  /*
   * 1. 메소드명: getApiKeyForChannel
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    지정된 채널에 대한 API 키를 반환
   * 2. 사용법
   *    getApiKeyForChannel("channelName") 와 같이 호출
   * </PRE>
   * @param channel 채널 이름
   * @return String API 키
   */
  public static String getApiKeyForChannel(String channel) {
    String key = "gigya.channels." + channel + ".apiKey";
    String apiKey = getApplicationProperty(key);
    return apiKey;
  }

  /*
   * 1. 메소드명: getRedirectChannelLoginPageURL
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 채널의 로그인 페이지 URL을 반환
   * 2. 사용법
   *    getRedirectChannelLoginPageURL("channelName") 와 같이 호출
   * </PRE>
   * @param channel 채널 이름
   * @return String 로그인 페이지 URL
   */
  public static String getRedirectChannelLoginPageURL(String channel) {
    String redirectURL = getApplicationProperty("redirectChannelLoginPageURL." + channel);
    return redirectURL;
  }

  /*
   * 1. 메소드명: getSamsungInstanceURL
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    삼성 인스턴스 URL을 반환
   * 2. 사용법
   *    getSamsungInstanceURL() 와 같이 호출
   * </PRE>
   * @return String 삼성 인스턴스 URL
   */
  public static String getSamsungInstanceURL() {
    String samsungInstanceURL = getApplicationProperty("samsungInstanceURL");
    return samsungInstanceURL;
  }

  /*
   * 1. 메소드명: getGigyaInstanceURL
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    Gigya 인스턴스 URL을 반환
   * 2. 사용법
   *    getGigyaInstanceURL() 와 같이 호출
   * </PRE>
   * @return String Gigya 인스턴스 URL
   */
  public static String getGigyaInstanceURL() {
    String gigyaInstanceURL = getApplicationProperty("gigyaInstanceURL");
    return gigyaInstanceURL;
  }

  /*
   * 1. 메소드명: getAllApiKeyForChannel
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    지정된 채널의 모든 API 키 정보를 반환
   * 2. 사용법
   *    getAllApiKeyForChannel("channelName") 와 같이 호출
   * </PRE>
   * @param channel 채널 이름
   * @return Map<String, String> API 키 정보를 포함하는 Map
   */
  public static Map<String, String> getAllApiKeyForChannel(String channel) {
    String currentChannel = "gigya.channels." + channel;

    Map<String, String> keys = new HashMap<>();
    keys.put("apiKey", getApplicationProperty(currentChannel + ".apiKey"));
    keys.put("userKey", getApplicationProperty(currentChannel + ".userKey"));
    keys.put("secretKey", getApplicationProperty(currentChannel + ".secretKey"));

    return keys;
  }

  /*
   * 1. 메소드명: getHostURL
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    PHP 및 Java의 호스트 URL을 포함하는 Map을 반환
   * 2. 사용법
   *    getHostURL() 와 같이 호출
   * </PRE>
   * @return Map<String, String> PHP 및 Java 호스트 URL
   */
  public static Map<String, String> getHostURL() {
    Map<String, String> hostUrl = new HashMap<>();

    hostUrl.put("php", getApplicationProperty("hostURL.php"));
    hostUrl.put("java", getApplicationProperty("hostURL.java"));

    return hostUrl;
  }

  /*
   * 1. 메소드명: getLoginPageForChannel
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    특정 채널에 대한 로그인 페이지 URL을 반환
   * 2. 사용법
   *    getLoginPageForChannel("channelName") 와 같이 호출
   * </PRE>
   * @param channel 채널 이름
   * @return String 로그인 페이지 URL
   */
  public static String getLoginPageForChannel(String channel) {
    String key = "loginPage.channels." + channel + ".landing-page";
    String loginPage = getApplicationProperty(key);
    return loginPage;
  }

  /*
   * 1. 메소드명: findParentKeyByValue
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    주어진 값에 해당하는 부모 키를 YAML 파일에서 찾음
   * 2. 사용법
   *    findParentKeyByValue("value") 와 같이 호출
   * </PRE>
   * @param value 찾고자 하는 값
   * @return String 값을 포함하는 부모 키
   */
  public static String findParentKeyByValue(String value) {
    // 현재 활성화된 프로파일에 따라 적절한 YAML 파일 결정
    String activeProfile = getActiveProfile();
    String yamlFileName = "application" + (activeProfile.isEmpty() ? "" : "-" + activeProfile) + ".yaml";

    Yaml yaml = new Yaml();

    try (InputStream inputStream = BeansUtil.class.getClassLoader().getResourceAsStream(yamlFileName)) {
      if (inputStream == null) {
        return ""; // 파일을 읽지 못하면 빈 문자열 반환
      }

      // YAML을 Map으로 변환
      Map<String, Object> yamlMap = yaml.load(inputStream);

      // Map에서 재귀적으로 값을 찾아 부모 키 반환
      return findParentKeyInMap(yamlMap, value, "");
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  /*
   * 1. 메소드명: findParentKeyInMap
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    Map 내부를 재귀적으로 탐색하여 주어진 값에 해당하는 부모 키를 찾음
   * 2. 사용법
   *    findParentKeyInMap(map, "value", "parentKey") 와 같이 호출
   * </PRE>
   * @param map 탐색할 Map 객체
   * @param value 찾고자 하는 값
   * @param parentKey 현재 탐색 중인 부모 키
   * @return String 값을 가진 부모 키
   */
  private static String findParentKeyInMap(Map<String, Object> map, String value, String parentKey) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      Object entryValue = entry.getValue();

      if (entryValue instanceof Map) {
        // 중첩된 Map일 경우, 재귀 호출하여 탐색
        String nestedKey = findParentKeyInMap((Map<String, Object>) entryValue, value, entry.getKey());
        if (!nestedKey.isEmpty()) {
          return nestedKey; // 부모 키 반환
        }
      } else if (value.equals(entryValue)) {
        // 값이 일치할 경우, 현재 부모 키 반환
        return parentKey;
      }
    }
    return "";  // 값을 찾지 못한 경우 빈 문자열 반환
  }

  /*
   * 1. 메소드명: getActiveProfile
   * 2. 클래스명: BeansUtil
   * 3. 작성자명: 서정환
   * 4. 작성일자: 2024. 11. 04.
   */
  /**
   * <PRE>
   * 1. 설명
   *    현재 활성화된 Spring Profile을 반환
   * 2. 사용법
   *    getActiveProfile() 와 같이 호출
   * </PRE>
   * @return String 활성화된 프로파일 이름, 없으면 기본 로컬 프로파일 반환
   */
  private static String getActiveProfile() {
    Environment env = (Environment) getBean("env");
    String[] activeProfiles = env.getActiveProfiles();

    // 프로파일이 없으면 기본 프로파일 사용
    if (activeProfiles.length == 0) {
      return "local"; // 기본적으로 로컬 프로파일로 설정
    }

    // 여러 개의 프로파일이 있으면 첫 번째 프로파일 사용
    return activeProfiles[0];
  }


}
