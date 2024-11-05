package com.samsung.ciam.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 1. FileName	: EncryptUtil.java
 * 2. Package	: com.samsung.ciam.utils
 * 3. Comments	: AES GCM 모드를 사용하여 데이터 암호화와 복호화를 수행하는 유틸리티 클래스
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

@Slf4j
public class EncryptUtil {

    private static final int GCM_TAG_LENGTH = 16; // GCM 태그 길이 (16바이트)
    private static final int GCM_IV_LENGTH = 12; // GCM IV 길이 (12바이트)

    /*
     * 1. 메소드명: encryptData
     * 2. 클래스명: EncryptUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    주어진 데이터를 AES GCM 모드를 사용하여 암호화하고 Base64로 인코딩하여 반환
     * 2. 사용법
     *    encryptData("데이터") 와 같이 사용하여 암호화된 데이터를 얻음
     * </PRE>
     * @param data 암호화할 데이터
     * @return String 암호화된 Base64 문자열, 암호화 실패 시 null 반환
     */
    public final static String encryptData(String data) {
        try {
            // Base64로 인코딩된 APP_KEY 디코딩
            String appKey = BeansUtil.getApplicationProperty("encrypt.appKey");
            byte[] decodedKey = Base64.getDecoder().decode(appKey);

            // AES 비밀키 생성
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            // 초기화 벡터(IV) 생성
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv); // 태그 길이는 비트 단위로 설정

            // AES 암호화 객체 초기화
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            // 데이터 암호화
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // IV와 암호화된 데이터를 결합하여 Base64로 인코딩하여 문자열로 변환
            byte[] combinedData = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combinedData, 0, iv.length);
            System.arraycopy(encryptedData, 0, combinedData, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combinedData);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            return null;
        }
    }

    /*
     * 1. 메소드명: decryptData
     * 2. 클래스명: EncryptUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    AES GCM 모드를 사용하여 Base64로 인코딩된 암호화 데이터를 복호화하여 원본 문자열로 반환
     * 2. 사용법
     *    decryptData("암호화된 데이터") 와 같이 사용하여 복호화된 데이터를 얻음
     * </PRE>
     * @param encryptedData 복호화할 Base64 인코딩된 암호화 데이터
     * @return String 복호화된 원본 문자열, 복호화 실패 시 null 반환
     */
    public static String decryptData(String encryptedData) {
        try {
            // Base64로 인코딩된 APP_KEY 디코딩
            String appKey = BeansUtil.getApplicationProperty("encrypt.appKey");
            byte[] decodedKey = Base64.getDecoder().decode(appKey);

            // AES 비밀키 생성
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            // Base64로 인코딩된 암호화 데이터를 디코딩
            byte[] combinedData = Base64.getDecoder().decode(encryptedData);

            // IV 추출
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combinedData, 0, iv, 0, iv.length);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv); // 태그 길이는 비트 단위로 설정

            // 암호화된 데이터 추출
            byte[] encryptedBytes = new byte[combinedData.length - iv.length];
            System.arraycopy(combinedData, iv.length, encryptedBytes, 0, encryptedBytes.length);

            // AES 복호화 객체 초기화
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            // 데이터 복호화
            byte[] decryptedData = cipher.doFinal(encryptedBytes);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            return null;
        }
    }

    /*
     * 1. 메소드명: encodeBase64
     * 2. 클래스명: EncryptUtil
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    문자열 데이터를 Base64로 인코딩하여 반환
     * 2. 사용법
     *    encodeBase64("원본 문자열") 과 같이 사용하여 인코딩된 문자열을 얻음
     * </PRE>
     * @param value 인코딩할 문자열 데이터
     * @return String Base64 인코딩된 문자열
     */
    public static String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}