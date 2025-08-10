package com.unify.app.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtService {

  @Value("${jwt.signer-key}")
  private String singerKey;

  @Value("${jwt.expiration-time-in-days}")
  private int expirationTimeInDays;

  public TokenGenerared generateToken(String email) {
    String jti = UUID.randomUUID().toString();
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(email)
        .jwtID(jti)
        .issuer("unify.com")
        .issueTime(new Date())
        .expirationTime(new Date(Instant.now().plus(expirationTimeInDays, ChronoUnit.DAYS).toEpochMilli()))
        .build();

    Payload payload = new Payload(claimsSet.toJSONObject());
    JWSObject jwsObject = new JWSObject(header, payload);
    try {
      jwsObject.sign(new MACSigner(singerKey.getBytes()));
      return new TokenGenerared(jti, jwsObject.serialize());
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean validToken(String token) {
    try {
      SignedJWT signed = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(singerKey);
      Date expirationTime = signed.getJWTClaimsSet().getExpirationTime();
      return signed.verify(verifier) && expirationTime.after(new Date());
    } catch (ParseException | JOSEException e) {

      System.out.println("Error validating token: " + e.getMessage());
      throw new RuntimeException("Invalid token format or verification failed", e);
    }
  }

  public String getTokenFromRequest(HttpServletRequest request) {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null) {
      return request.getParameter("token");
    }
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  public String extractUsername(String token) {
    try {
      SignedJWT signed = SignedJWT.parse(token);
      return signed.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public String extractJti(String token) {
    try {
      SignedJWT signed = SignedJWT.parse(token);
      return signed.getJWTClaimsSet().getJWTID();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

}
