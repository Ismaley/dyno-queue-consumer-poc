package com.sample.application.demo.consumer

import com.netflix.dyno.queues.DynoQueue
import com.netflix.dyno.queues.Message
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TestConsumer(private val queue: DynoQueue) : CommandLineRunner {



    override fun run(vararg args: String?) {
        val ids = queue.push(listOf(Message("id", "hello"), Message("id2", "hello"), Message("id3", "hello")))
        println("published $ids")

        Thread.sleep(500)
        println("queue now has ${queue.size()} elements")

        val messages =  queue.pop(10, 1, TimeUnit.SECONDS)
        println("Popped $messages with size ${messages.size}")
        messages.forEach { queue.ack(it.id) }
        Thread.sleep(3000)
        println("queue now has ${queue.size()} elements")
        println("finishing")
    }
}