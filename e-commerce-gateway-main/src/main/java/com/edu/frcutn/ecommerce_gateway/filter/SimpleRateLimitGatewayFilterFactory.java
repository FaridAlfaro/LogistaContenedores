package com.edu.frcutn.ecommerce_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SimpleRateLimitGatewayFilterFactory
        extends AbstractGatewayFilterFactory<SimpleRateLimitGatewayFilterFactory.Config> {

    // Guarda una imagen por cada ip
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public SimpleRateLimitGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = resolveKey(exchange); // Se obtiene la ip de la request

            long now = System.currentTimeMillis();
            long windowMs = Duration.ofSeconds(config.getWindowSeconds()).toMillis();

            Window w = windows.compute(key, (k, old) -> {

                // Si no hay una ventana para una IP, se crea
                if (old == null || now - old.windowStart >= windowMs) {
                    return new Window(now, new AtomicInteger(1));
                } else {
                    // Si existe la ventana, se incrementa el contador
                    old.counter.incrementAndGet();
                    return old;
                }
            });

            // Si el contador es mayor a la cantidad de Request máximas, no se deja pasar
            int current = w.counter.get();
            if (current > config.getRequests()) {
                log.warn("Rate limit excedido para clave={} ({} solicitudes en {} segundos)",
                        key, current, config.getWindowSeconds());
                return tooManyRequests(exchange);
            }

            return chain.filter(exchange);
        };
    }

    // Si se pasa del límite de requests:
    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        exchange.getResponse().getHeaders()
                .add("X-Rate-Limit", "Too many requests");
        return exchange.getResponse().setComplete();
    }

    // Se obtiene la ip de cada request
    private String resolveKey(ServerWebExchange exchange) {
        var remote = exchange.getRequest().getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "unknown"; // fallback
    }

    public static class Config {
        // Máximo de requests en la ventana
        private int requests = 30;
        // Ventana en segundos
        private long windowSeconds = 1;

        public int getRequests() {
            return requests;
        }

        public void setRequests(int requests) {
            this.requests = requests;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }

    private static class Window {
        final long windowStart;
        final AtomicInteger counter;

        private Window(long windowStart, AtomicInteger counter) {
            this.windowStart = windowStart;
            this.counter = counter;
        }
    }
}
