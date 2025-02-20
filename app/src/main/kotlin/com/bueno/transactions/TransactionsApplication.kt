package com.bueno.transactions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TransactionsApplication

fun main(args: Array<String>) {
	runApplication<TransactionsApplication>(*args)
}
