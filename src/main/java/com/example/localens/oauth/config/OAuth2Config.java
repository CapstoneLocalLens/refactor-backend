package com.example.localens.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(googleClientRegistration(), kakakoClientRegistration(), naverClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("YOUR_GOOGLE_CLIENT_ID")
                .clientSecret("YOUR_GOOGLE_CLIENT_SECRET")
                .redirectUri("{baseUrl}/login/oauth2/code/goole")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    private ClientRegistration kakakoClientRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId("YOUR_KAKAKO_CLIENT_ID")
                .clientSecret("YOUR_KAKAKO_CLIENT_SECRET")
                .redirectUri("{baseUrl}/login/oauth2/code/kakao")
                .scope("account_email","profile_nickname")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();
    }

    private ClientRegistration naverClientRegistration() {
        return ClientRegistration.withRegistrationId("naver")
                .clientId("YOUR_NAVER_CLIENT_ID")
                .clientSecret("YOUR_NAVER_CLIENT_SECRET")
                .redirectUri("{baseUrl}/login/oauth2/code/naver")
                .scope("name","email")
                .authorizationUri("https://nid.anver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .clientName("Naver")
                .build();
    }
}
