package com.microservices.authservice.config;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Validation TLS/hostname assouplie volontairement : la communication
    // inter-services se fait en HTTPS avec certificats auto-signés internes
    // (réseau Docker privé), non exposés publiquement.
    @SuppressWarnings({ "java:S4830", "java:S5527" })
    @Bean
    public RestTemplate restTemplate() throws Exception {
        TrustManager[] trustAll = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                    X509Certificate[] certs,
                    String t
                ) {}

                public void checkServerTrusted(
                    X509Certificate[] certs,
                    String t
                ) {}
            },
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new SecureRandom());

        SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(
                    HttpURLConnection connection,
                    String httpMethod
                ) throws java.io.IOException {
                    if (connection instanceof HttpsURLConnection) {
                        HttpsURLConnection httpsConnection =
                            (HttpsURLConnection) connection;
                        httpsConnection.setSSLSocketFactory(
                            sslContext.getSocketFactory()
                        );
                        httpsConnection.setHostnameVerifier(
                            (hostname, session) -> true
                        );
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };

        return new RestTemplate(factory);
    }
}
