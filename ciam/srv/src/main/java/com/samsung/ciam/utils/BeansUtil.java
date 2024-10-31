package com.samsung.ciam.utils;

import com.samsung.ciam.common.core.component.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

public class BeansUtil {

  /**
   * <PRE>
   * 1. 설명
   * Bean 이름에 대한 빈 객체를 얻어온다.
   * 2. 사용법
   *
   * </PRE>
   *
   * @param beanName 빈객체명
   * @return
   */
  public static Object getBean(String beanName) {
    ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
    // String array[] =  applicationContext.getBeanDefinitionNames();

    return applicationContext.getBean(beanName);
  }

  /**
   * <PRE>
   * 어플리케이션 프로퍼티스 파일(application.yml)에 저장되어 있는 프로퍼티 값을 가져온다.
   * <p>
   * 2. 사용법
   * BeansUtil.getApplicationProperty("icignal.aws.region");
   * </PRE>
   *
   * @param key
   * @return 프로퍼티키에 대한 값
   */
  public static String getApplicationProperty(String key) {
    Environment env = (Environment) getBean("env");
    String value = env.getProperty(key);
    if (value == null) return "";
    return value;
  }


  public static List<String> getApplicationProperties(String key) {
    Environment env = (Environment) getBean("env");
    String value = env.getProperty(key);
    if (value == null) return new ArrayList<String>();
    return Arrays.asList(value.split(","));
  }

  /**
   * <PRE>
   * 1. 설명
   * 어플리케이션 프로퍼티스 파일(application.yml)에 저장되어 있는 프로퍼티 값을 가져온다.
   * 2. 사용법
   * int val = BeansUtils.getApplicationProperty("icignal.login.lock.cnt", Integer.class)
   * </PRE>
   *
   * @param key
   * @param targetType
   * @return
   */
  public static <T> T getApplicationProperty(String key, Class<T> targetType) {
    Environment env = (Environment) getBean("env");
    return env.getProperty(key, targetType);
  }

  /**
   * <PRE>
   * 1. 설명
   * 어플리케이션 프로퍼티스 파일(application.yml)에 저장되어 있는 프로퍼티 값을 가져온다.
   * <p>
   * 2. 사용법
   * ex) BeansUtil.getApplicationProperty("icignal.async.thread-pool.init", Integer.class , 1)
   * </PRE>
   *
   * @param key          프로퍼티 키
   * @param targetType   반환타입.
   * @param defaultValue null일경우 디폴트값
   * @return
   */

  public static <T> T getApplicationProperty(String key, Class<T> targetType, T defaultValue) {
    Environment env = (Environment) getBean("env");
    return env.getProperty(key, targetType, defaultValue);
  }

  public static String getApiKeyForChannel(String channel) {
    String key = "gigya.channels." + channel + ".apiKey";
    String apiKey = getApplicationProperty(key);
    return apiKey;
  }

  public static String getRedirectChannelLoginPageURL(String channel) {
    String redirectURL = getApplicationProperty("redirectChannelLoginPageURL." + channel);
    return redirectURL;
  }

  public static String getSamsungInstanceURL() {
    String samsungInstanceURL = getApplicationProperty("samsungInstanceURL");
    return samsungInstanceURL;
  }

  public static String getGigyaInstanceURL() {
    String gigyaInstanceURL = getApplicationProperty("gigyaInstanceURL");
    return gigyaInstanceURL;
  }

  public static Map<String, String> getAllApiKeyForChannel(String channel) {
    String currentChannel = "gigya.channels." + channel;

    Map<String, String> keys = new HashMap<>();
    keys.put("apiKey", getApplicationProperty(currentChannel + ".apiKey"));
    keys.put("userKey", getApplicationProperty(currentChannel + ".userKey"));
    keys.put("secretKey", getApplicationProperty(currentChannel + ".secretKey"));

    return keys;
  }

  public static Map<String, String> getHostURL() {
    Map<String, String> hostUrl = new HashMap<>();

    hostUrl.put("php", getApplicationProperty("hostURL.php"));
    hostUrl.put("java", getApplicationProperty("hostURL.java"));

    return hostUrl;
  }

  public static String getLoginPageForChannel(String channel) {
    String key = "loginPage.channels." + channel + ".landing-page";
    String loginPage = getApplicationProperty(key);
    return loginPage;
  }

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

  /**
   * 재귀적으로 Map 내부를 탐색하여 주어진 값을 가진 **부모 키**를 찾는다.
   *
   * @param map       탐색할 Map
   * @param value     찾고자 하는 값
   * @param parentKey 현재 탐색 중인 부모 키
   * @return 값을 가진 부모 키, 없으면 빈 문자열 반환
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

  /**
   * 현재 활성화된 Spring Profile을 반환.
   *
   * @return 활성화된 프로파일 이름, 없으면 빈 문자열
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
