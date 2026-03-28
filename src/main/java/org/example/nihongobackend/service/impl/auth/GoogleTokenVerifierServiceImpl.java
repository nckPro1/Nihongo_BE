package org.example.nihongobackend.service.impl.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.service.auth.GoogleTokenVerifierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Service
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {

    @Value("${google.oauth.client-id}")
    private String clientId;
    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build();

    @Override
    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new UnauthorizedException("Google email is not verified");
            }

            return payload;
        } catch (GeneralSecurityException ex) {
            throw new UnauthorizedException("Failed to verify Google token");
        } catch (Exception ex) {
            if (ex instanceof UnauthorizedException) {
                throw (UnauthorizedException) ex;
            }
            throw new UnauthorizedException("Invalid Google token");
        }
    }

    @Override
    public GoogleIdToken.Payload verifyAuthorizationCode(String code, String redirectUri) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = webClient.post()
                    .uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(
                            "code=" + encode(code)
                                    + "&client_id=" + encode(clientId)
                                    + "&client_secret=" + encode(clientSecret)
                                    + "&redirect_uri=" + encode(redirectUri)
                                    + "&grant_type=authorization_code"
                    )
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || tokenResponse.get("id_token") == null) {
                throw new UnauthorizedException("Failed to exchange Google authorization code");
            }
            return verify(String.valueOf(tokenResponse.get("id_token")));
        } catch (Exception ex) {
            if (ex instanceof UnauthorizedException) {
                throw (UnauthorizedException) ex;
            }
            throw new UnauthorizedException("Failed to verify Google authorization code");
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
