package com.example.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

@Component
public class TokenFilter implements Filter {

    public static RSAPublicKey getPublicKey(String key) throws GeneralSecurityException {
        byte[] encoded = Base64.decodeBase64(key);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }

    private boolean tokenVerifier(String token) throws GeneralSecurityException {
        token = token.replace("Bearer", "");

        DecodedJWT jwt = JWT.decode(token);
        ResponseEntity<String> response = new RestTemplate().getForEntity(jwt.getIssuer(), String.class);
        if(response.getStatusCodeValue() == 200){
            String publicKey = new Gson().fromJson(response.getBody(), JsonObject.class).get("public_key").getAsString();
            try {
                Algorithm algorithm = Algorithm.RSA256(getPublicKey(publicKey), null);
                JWTVerifier verifier = JWT.require(algorithm).build();
                verifier.verify(token);
                return true;
            } catch (Exception e){
                System.out.println("Exception in verifying " + e.toString());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;

        try {
            if (tokenVerifier(httpServletRequest.getHeader("Authorization").replace("Bearer ", ""))) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (GeneralSecurityException | JWTDecodeException e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
