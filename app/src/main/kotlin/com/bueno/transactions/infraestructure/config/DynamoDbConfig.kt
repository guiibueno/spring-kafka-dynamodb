package com.bueno.transactions.infraestructure.config

import com.bueno.transactions.domain.entity.Transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import java.net.URI
import java.time.Duration

@Configuration
class DynamoDbConfig {

    @Bean
    fun dynamoDbAsyncClient(
        @Value("\${cloud.aws.region.name:US-EAST-1}") awsRegion: String,
        @Value("\${dynamodb.config.endpoint:http://localhost:8000}") endpointUrl: String,
    ) : DynamoDbAsyncClient {
        val builder = DynamoDbAsyncClient.builder()
            .region(Region.of(awsRegion))
            .endpointOverride(URI.create(endpointUrl))
            .httpClient(
                NettyNioAsyncHttpClient
                    .builder()
                    .maxConcurrency(500)
                    .maxPendingConnectionAcquires(1000)
                    .connectionMaxIdleTime(Duration.ofSeconds(30))
                    .connectionTimeout(Duration.ofSeconds(30))
                    .connectionAcquisitionTimeout(Duration.ofSeconds(30))
                    .readTimeout(Duration.ofSeconds(30))
                    .build()
            )
            .credentialsProvider(DefaultCredentialsProvider.create())

        return builder.build()
    }

    @Bean
    fun dynamoDbEnhancedAsyncClient(dynamoDbAsyncClient: DynamoDbAsyncClient) : DynamoDbEnhancedAsyncClient {
        return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(dynamoDbAsyncClient).build()
    }

    @Bean
    fun transactionTable(
        dynamoDbEnhancedAsyncClient: DynamoDbEnhancedAsyncClient,
        @Value("\${dynamodb.tables.transaction.name}") tableName: String
    ) : DynamoDbAsyncTable<Transaction> {
        return dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromClass(Transaction::class.java))
    }
}