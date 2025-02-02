package com.bueno.transactions.infraestructure.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConfig {

    @Bean
    fun batchTransactionRequestListenerBean(
        @Value("\${spring.kafka.listener.ack-mode:AUTO}") ackMode: String,
        @Value("\${spring.kafka.consumer.max.poll.records:500}") maxPoolRecords: Int,
        @Value("\${spring.kafka.consumer.fetch.max.wait.ms:500}") fetchMaxWaitMs: Int,
        @Value("\${spring.kafka.consumer.fetch.min.bytes:1}") fetchMinBytes: Int,
        @Value("\${spring.kafka.consumer.enable.auto.commit:true}") autoCommit: Boolean,
        consumerFactory: ConsumerFactory<String?, String?>
    ): ConcurrentKafkaListenerContainerFactory<String?, String?> {
        val factory = ConcurrentKafkaListenerContainerFactory<String?, String?>()

        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = maxPoolRecords
        configProps[ConsumerConfig.FETCH_MIN_BYTES_CONFIG] = fetchMinBytes
        configProps[ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG] = fetchMaxWaitMs
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = autoCommit.toString()
        consumerFactory.updateConfigs(configProps)

        factory.setConcurrency(2)
        factory.consumerFactory = consumerFactory
        factory.containerProperties.pollTimeout = 3000
        factory.isBatchListener = true
        factory.containerProperties.ackMode = ContainerProperties.AckMode.valueOf(ackMode)

        return factory
    }

}