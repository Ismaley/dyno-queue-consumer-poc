package com.sample.application.demo.configs.dynomite

import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.jedis.DynoJedisClient
import com.netflix.dyno.queues.DynoQueue
import com.netflix.dyno.queues.redis.RedisQueues
import com.netflix.dyno.queues.shard.DynoShardSupplier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(DynomiteConfiguration::class)
class DynoQueueConfig {

    @Value("\${dynomite.cluster.local.rack}")
    private lateinit var region: String
    @Value("\${dynomite.cluster.local.datacenter}")
    private lateinit var localDC: String

    @Bean
    fun callbackQueue(@Value("\${callbacks.queue.name}") queueName: String, redisQueue: RedisQueues) : DynoQueue {
        return redisQueue.get(queueName)
    }

    @Bean
    fun redisQueues(@Qualifier("dynoClient") client: DynoJedisClient, @Qualifier("readClient") readClient: DynoJedisClient, hostSupplier: HostSupplier) : RedisQueues {
       return RedisQueues(client, readClient, "callback", configureDynoShard(hostSupplier), 500, 500)
    }

    fun configureDynoShard(hostSupplier: HostSupplier) : DynoShardSupplier {

        return DynoShardSupplier(hostSupplier, "-rack1", localDC)
    }

}