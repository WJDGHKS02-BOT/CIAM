package com.samsung.ciam.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class SingleIdMfaJwtToken {

  public static String encrypt(String plaintext, SecretKey key) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);

    // Get IV from the cipher
    byte[] iv = cipher.getIV();

    // Encrypt the plaintext
    byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

    // Combine IV and encrypted data
    byte[] ivAndEncrypted = new byte[iv.length + encryptedBytes.length];
    System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length);
    System.arraycopy(encryptedBytes, 0, ivAndEncrypted, iv.length, encryptedBytes.length);

    // Base64 encode the result
    return Base64.getEncoder().encodeToString(ivAndEncrypted);

  }

  public static String createJwtToken(String uid, String systemId, String requestId, String email, String displayUid
      , long expirationMills, CompositeKey compositeKey, String rtn) throws Exception {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + expirationMills);

    String jwToken = Jwts.builder()
        .claim("uid", uid)
        .claim("sys", systemId)
        .claim("req", requestId)
        .claim("displayUid", displayUid)
        .claim("email", encrypt(email, compositeKey.getEncryptionKey()))
        .claim("rtn", rtn)
        .setNotBefore(now)
        .setIssuedAt(now)
        .setExpiration(expirationDate)
        .signWith(compositeKey.getJwtSigningKey(), SignatureAlgorithm.HS256)
        .compact();
    return jwToken;
  }

  public static Claims verifySingleIDToken(String jwToken, CompositeKey compositeKey, long clockSkewSeconds) {

    Claims claims = Jwts.parserBuilder()
        .setAllowedClockSkewSeconds(clockSkewSeconds)
        .setSigningKey(compositeKey.getJwtSigningKey())
        .build()
        .parseClaimsJws(jwToken)
        .getBody();
    return claims;
  }

  public static class CompositeKey {
    private final SecretKey jwtSigningKey;
    private final SecretKey encryptionKey;

    public CompositeKey(byte[] masterKeyBytes) {
      this.jwtSigningKey = deriveSigningKey(masterKeyBytes);
      this.encryptionKey = deriveEncryptionKey(masterKeyBytes);
    }

    private SecretKey deriveSigningKey(byte[] masterKeyBytes) {
      return new SecretKeySpec(masterKeyBytes, "HmacSHA256");
    }

    private SecretKey deriveEncryptionKey(byte[] masterKeyBytes) {
      return new SecretKeySpec(masterKeyBytes, "AES");
    }

    public SecretKey getJwtSigningKey() {
      return jwtSigningKey;
    }

    public SecretKey getEncryptionKey() {
      return encryptionKey;
    }
  }
}
