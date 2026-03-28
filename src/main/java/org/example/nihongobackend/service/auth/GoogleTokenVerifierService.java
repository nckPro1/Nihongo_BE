package org.example.nihongobackend.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleTokenVerifierService {
    GoogleIdToken.Payload verify(String idToken);
    GoogleIdToken.Payload verifyAuthorizationCode(String code, String redirectUri);
}
