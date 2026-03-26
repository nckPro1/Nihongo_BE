package org.example.nihongobackend.service.impl.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.service.auth.GoogleTokenVerifierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {

    @Value("${google.oauth.client-id}")
    private String clientId;

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
}
