package com.ITSA.transaction;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ITSA.transaction.grpc.TransactionServiceImpl;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50053; // Default gRPC port, change as needed
        Server server = ServerBuilder.forPort(port)
                .addService(new TransactionServiceImpl())
                .keepAliveTime(300, TimeUnit.SECONDS)
                .permitKeepAliveTime(600, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .build();

        System.out.println("Starting server on port " + port);
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }
}
