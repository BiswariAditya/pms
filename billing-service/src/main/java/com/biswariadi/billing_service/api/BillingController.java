package com.biswariadi.billing_service.api;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
public class BillingController {
    private final BillingServiceGrpc.BillingServiceBlockingStub stub;

    public BillingController(
            @Value("${grpc.server.port:9001}") int grpcPort,
            @Value("${billing.grpc.self-host:localhost}") String grpcHost
    ) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort).usePlaintext().build();
        this.stub = BillingServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping("/info")
    public ResponseEntity<BillingInfoResponse> getBillingInfo(
            @RequestParam("patientId") String patientId,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "email", required = false, defaultValue = "") String email
    ) {
        BillingResponse response = stub.getBillingInfo(BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build());

        return ResponseEntity.ok(new BillingInfoResponse(response.getAccountId(), response.getStatus()));
    }

    public record BillingInfoResponse(String accountId, String status) {}
}

