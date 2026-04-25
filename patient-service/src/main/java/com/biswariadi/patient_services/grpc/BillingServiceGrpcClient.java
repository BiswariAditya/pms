package com.biswariadi.patient_services.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
public class BillingServiceGrpcClient {
    private final BillingServiceGrpc.BillingServiceBlockingStub billingServiceStub;

    public BillingServiceGrpcClient(@Value("${billing.grpc.host:billing-service}") String serverAddress,
                                    @Value("${billing.grpc.port:9001}") int serverPort) {
        log.info("Initializing BillingServiceGrpcClient with server address: {} and port: {}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
        billingServiceStub = BillingServiceGrpc.newBlockingStub(channel);
    }
    public void createBillingAccount(String patientId, String patientName, String patientEmail) {
        log.info("Creating billing account for patientId: {}, patientName: {}", patientId, patientName);
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        BillingResponse response = billingServiceStub.getBillingInfo(request);
        log.info("Received billing account creation response: {}", response);
    }
}
