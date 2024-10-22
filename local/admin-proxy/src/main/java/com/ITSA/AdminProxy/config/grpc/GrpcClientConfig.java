package com.ITSA.AdminProxy.config.grpc;

import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.userOrchestrator.address}")
    private String userOrchestratorAddress;

    @Value("${grpc.userOrchestrator.port}")
    private int userOrchestratorPort;

    @Value("${grpc.transactionOrchestrator.address}")
    private String transactionOrchestratorAddress;

    @Value("${grpc.transactionOrchestrator.port}")
    private int transactionOrchestratorPort;

    @Bean
    public ManagedChannel userOrchestratorChannel() {
        return createChannel(userOrchestratorAddress, userOrchestratorPort);
    }

    @Bean
    public ManagedChannel transactionOrchestratorChannel() {
        return createChannel(transactionOrchestratorAddress, transactionOrchestratorPort);
    }

    private ManagedChannel createChannel(String address, int port) {
        return io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder.forAddress(address, port)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .keepAliveTime(60, TimeUnit.MINUTES)
                .keepAliveTimeout(20, TimeUnit.MINUTES)
                .build();
    }
}
