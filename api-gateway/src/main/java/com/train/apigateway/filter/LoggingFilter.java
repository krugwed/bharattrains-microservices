package com.train.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        System.out.println("Incoming Request → " + method + " " + path);

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    System.out.println("Response Status → " +
                            exchange.getResponse().getStatusCode());
                })
        );
    }
}