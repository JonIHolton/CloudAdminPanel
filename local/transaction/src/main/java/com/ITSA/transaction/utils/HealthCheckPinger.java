// package com.ITSA.users.utils;

// import io.grpc.health.v1.HealthCheckRequest;
// import io.grpc.health.v1.HealthGrpc;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// public class HealthCheckPinger {

//     private final ManagedChannel channel;
//     private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

//     public HealthCheckPinger(ManagedChannel channel) {
//         this.channel = channel;
//     }

//     public void start() {
//         // Schedule a task to run every minute (or any desired frequency)
//         scheduler.scheduleAtFixedRate(() -> {
//             try {
//                 HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
//                 HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("your_service_name").build();
//                 // Assuming "your_service_name" is the service you want to check. Use an empty string for overall health
//                 healthStub.check(request);
//                 System.out.println("Health check passed");
//             } catch (Exception e) {
//                 System.err.println("Health check failed: " + e.getMessage());
//                 // Handle failed health check if necessary
//             }
//         }, 0, 1, TimeUnit.MINUTES); // Adjust the initial delay and period as needed
//     }

//     public void shutdown() {
//         scheduler.shutdownNow();
//         try {
//             if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
//                 System.err.println("Scheduler did not terminate");
//             }
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//         }
//     }
// }
