package com.biswariadi.billing_service.grpc;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public abstract class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    public void createBillingAccount(billing.BillingRequest billingrequest, StreamObserver<BillingResponse> responseObserver) {
        log.info("createBillingAccount {}", billingrequest.toString());
        BillingResponse response= BillingResponse.newBuilder().setAccountId("12345").setStatus("SUCCESS").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
