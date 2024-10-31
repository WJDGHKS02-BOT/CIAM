package com.samsung.ciam.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class EncryptUtil {

    private static final int GCM_TAG_LENGTH = 16; // GCM 태그 길이 (16바이트)
    private static final int GCM_IV_LENGTH = 12; // GCM IV 길이 (12바이트)

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

    public static String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}