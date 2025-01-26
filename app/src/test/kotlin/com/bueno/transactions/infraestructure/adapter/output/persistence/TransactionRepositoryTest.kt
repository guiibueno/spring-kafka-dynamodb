package com.bueno.transactions.infraestructure.adapter.output.persistence

import com.bueno.transactions.application.port.output.persistence.TransactionOutputPort
import com.bueno.transactions.domain.entity.Authorization
import com.bueno.transactions.domain.entity.Authorization.AuthorizationReasons.AUTHORIZATION_REASON_REJECTED_TIMEOUT
import com.bueno.transactions.domain.entity.AuthorizationCode
import com.bueno.transactions.domain.entity.Transaction
import com.bueno.transactions.domain.entity.TransactionType
import com.bueno.transactions.domain.exception.IdempotencyException
import com.bueno.transactions.infraestructure.config.DynamoDbConfig
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClientExtension
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest(
    classes = [
        DynamoDbConfig::class,
        JacksonAutoConfiguration::class,
        KotlinModule::class,
        TransactionOutputPort::class,
        TransactionRepository::class
    ]
)
class TransactionRepositoryTest {
    companion object {
        var localstackImage: DockerImageName = DockerImageName.parse("localstack/localstack")

        @Container
        val container = LocalStackContainer(localstackImage)
            .withServices(LocalStackContainer.Service.DYNAMODB)

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun dynamoDbProp(registry: DynamicPropertyRegistry){
            registry.add("cloud.aws.region.name") { container.region }
            registry.add("dynamodb.config.endpoint") { container.endpoint }
        }
    }
    private val tableName = "transactions"

    @Autowired
    private lateinit var transactionOutputPort: TransactionOutputPort

    @Autowired
    private lateinit var dynamoDbClient: DynamoDbAsyncClient

    @BeforeEach
    fun initializeDb(){
        runBlocking { deleteTable() }
        runBlocking { createTable() }
        runBlocking { createTransaction() }
    }

    private suspend fun deleteTable() {
        try {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(tableName).build()).await()
        } catch (err: Exception){
            // DO NOTHING
        }
    }

    private suspend fun createTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder().tableName(tableName).attributeDefinitions(
                AttributeDefinition.builder().attributeName("id").attributeType("S").build()
            ).keySchema(
                KeySchemaElement.builder().attributeName("id").keyType("HASH").build()
            ).provisionedThroughput(
                ProvisionedThroughput.builder().readCapacityUnits(10).writeCapacityUnits(5).build()
            ).build()
        ).await()
    }

    private suspend fun createTransaction(){
        transactionOutputPort.save(transactionEntity)
    }

    private val transactionEntity = Transaction(
        id = UUID.randomUUID(),
        accountId = UUID.randomUUID(),
        type = TransactionType.DEBIT,
        value = BigDecimal.TEN,
        description = "Test",
        authorization = Authorization(
            code = AuthorizationCode.REJECTED,
            reason = AUTHORIZATION_REASON_REJECTED_TIMEOUT
        )
    )

    @Test
    fun `should save the transaction successfully`() {
        val transactionId = UUID.randomUUID()

        val authorization = Authorization(
            code = AuthorizationCode.REJECTED,
            reason = AUTHORIZATION_REASON_REJECTED_TIMEOUT
        )

        val transaction = Transaction(
            id = transactionId,
            accountId = UUID.randomUUID(),
            type = TransactionType.DEBIT,
            value = BigDecimal.TEN,
            description = "Test",
            authorization = authorization
        )

        val result = runBlocking { transactionOutputPort.save(transaction) }

        assertEquals(transactionId, result.id)
    }

    @Test
    fun `should throw idempotency exception when transaction already exists`() {
        val transaction = Transaction(
            id = transactionEntity.id,
            accountId = transactionEntity.accountId,
            type = transactionEntity.type,
            value = transactionEntity.value,
            description = transactionEntity.description,
            authorization = transactionEntity.authorization
        )

        val exception = assertThrows<IdempotencyException> {
            runBlocking { transactionOutputPort.save(transaction) }
        }

        assert(exception.id == transaction.id)
    }
}