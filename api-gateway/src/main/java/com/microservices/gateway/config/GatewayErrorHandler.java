package com.microservices.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        String path = exchange.getRequest().getURI().getPath();

        if (ex instanceof ResponseStatusException rse) {
            return writeJson(
                exchange,
                HttpStatus.valueOf(rse.getStatusCode().value()),
                Map.of(
                    "error",
                    rse.getReason() != null ? rse.getReason() : "Erreur",
                    "status",
                    rse.getStatusCode().value()
                )
            );
        }

        if (path.startsWith("/api/") && isConnectionError(ex)) {
            String service = guessService(path);
            return writeJson(
                exchange,
                HttpStatus.SERVICE_UNAVAILABLE,
                Map.of(
                    "error",
                    "Service indisponible",
                    "service",
                    service,
                    "status",
                    503
                )
            );
        }

        return writeJson(
            exchange,
            HttpStatus.INTERNAL_SERVER_ERROR,
            Map.of("error", "Erreur interne", "status", 500)
        );
    }

    private boolean isConnectionError(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String name = current.getClass().getSimpleName();
            String msg = current.getMessage();
            if (
                name.contains("Connect") ||
                name.contains("NoHost") ||
                (msg != null &&
                    (msg.contains("Connection refused") ||
                        msg.contains("connection timed out") ||
                        msg.contains("No route to host") ||
                        msg.contains("finishConnect")))
            ) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String guessService(String path) {
        if (path.startsWith("/api/auth")) return "auth-service";
        if (path.startsWith("/api/users")) return "user-service";
        if (path.startsWith("/api/messages")) return "messaging-service";
        if (
            path.startsWith("/api/properties") ||
            path.startsWith("/api/reservations")
        ) return "property-service";
        return "unknown";
    }

    private Mono<Void> writeJson(
        ServerWebExchange exchange,
        HttpStatus status,
        Map<String, Object> body
    ) {
        exchange.getResponse().setStatusCode(status);
        exchange
            .getResponse()
            .getHeaders()
            .setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = (
                "{\"error\":\"Erreur\",\"status\":" +
                status.value() +
                "}"
            ).getBytes();
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
