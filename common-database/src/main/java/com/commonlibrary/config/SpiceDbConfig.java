package com.commonlibrary.config;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpiceDbConfig {

    private final SpiceDBProperties properties;

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel managedChannel() {
        log.info(">>>> Connecting to SpiceDB at {}:{} (SSL: {})",
                properties.getHost(), properties.getPort(), properties.isUseSsl());

        ManagedChannelBuilder<?> builder = ManagedChannelBuilder
                .forAddress(properties.getHost(), properties.getPort());

        if (properties.isUseSsl()) {
            return builder.useTransportSecurity().build();
        } else {
            return builder.usePlaintext().build();
        }
    }

    @Bean
    public PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsServiceStub(ManagedChannel channel) {
        return PermissionsServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken(properties.getToken()));
    }

    @Bean
    public SchemaServiceGrpc.SchemaServiceBlockingStub schemaServiceStub(ManagedChannel channel) {
        return SchemaServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken(properties.getToken()));
    }
}