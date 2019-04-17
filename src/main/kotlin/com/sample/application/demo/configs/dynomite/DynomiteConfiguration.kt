package com.sample.application.demo.configs.dynomite

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.dyno.connectionpool.impl.lb.AbstractTokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.jedis.DynoJedisClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(DynomiteHostSupplierConfiguration::class)
class DynomiteConfiguration {

    companion object {
        private const val HOSTS_DELIMITER: String = ","
        private const val HOST_DELIMITER: String = ":"
        private const val LOCAL_HACK_PROPERTY: String = "LOCAL_HACK"
        private const val LOCAL_DATACENTER_PROPERTY: String = "LOCAL_DATACENTER"
    }

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String
    @Value("\${dynomite.cluster.name}")
    private lateinit var clusterName: String
    @Value("\${dynomite.cluster.hosts}")
    private lateinit var clusterHosts: String
    @Value("\${dynomite.cluster.local.rack:#{null}}")
    private var localRack: String? = null
    @Value("\${dynomite.cluster.local.datacenter:#{null}}")
    private var localDatacenter: String? = null

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Bean
    fun dynoClient(
        hostSupplier: HostSupplier,
        hostTokenSupplier: HostTokenSupplier
    ): DynoJedisClient {
        setAffinitySystemProperties()

        val poolConfiguration = ConnectionPoolConfigurationImpl(clusterName)
            .setLocalDataCenter(localDatacenter)
            .setLocalRack(localRack)
            .withHostSupplier(hostSupplier)
            .withTokenSupplier(buildTokenMapSupplier(hostTokenSupplier.getHostTokens()))

        return DynoJedisClient.Builder()
            .withApplicationName(applicationName)
            .withDynomiteClusterName(clusterName)
            .withHostSupplier(hostSupplier)
            .withCPConfig(poolConfiguration)
            .build()
    }

    @Bean(name = ["readClient"])
    fun dynoReadClient(
        hostSupplier: HostSupplier,
        hostTokenSupplier: HostTokenSupplier
    ): DynoJedisClient {
        setAffinitySystemProperties()

        val poolConfiguration = ConnectionPoolConfigurationImpl(clusterName)
            .setLocalDataCenter(localDatacenter)
            .setLocalRack(localRack)
            .withHostSupplier(hostSupplier)
            .withTokenSupplier(buildTokenMapSupplier(hostTokenSupplier.getHostTokens()))

        return DynoJedisClient.Builder()
            .withApplicationName(applicationName)
            .withDynomiteClusterName(clusterName)
            .withHostSupplier(hostSupplier)
            .withCPConfig(poolConfiguration)
            .build()
    }

    private fun setAffinitySystemProperties() {
        if (localRack != null) {
            System.setProperty(LOCAL_HACK_PROPERTY, localRack)
        }
        if (localDatacenter != null) {
            System.setProperty(LOCAL_DATACENTER_PROPERTY, localDatacenter)
        }
    }

    private fun buildTokenHosts(hosts: List<HostToken>): List<TokenHost> {
        return hosts
            .map { hostToken ->
                TokenHost(
                    token = hostToken.token.toString(),
                    hostname = hostToken.host.hostName,
                    zone = hostToken.host.rack,
                    dc = hostToken.host.datacenter
                )
            }
    }

    private fun buildTokenMapSupplier(hosts: List<HostToken>): TokenMapSupplier {
        val tokenHosts = buildTokenHosts(hosts)
        val json = objectMapper.writeValueAsString(tokenHosts)

        return object : AbstractTokenMapSupplier() {
            override fun getTopologyJsonPayload(hostName: String): String {
                return json
            }

            override fun getTopologyJsonPayload(activeHosts: Set<Host>): String {
                return json
            }
        }
    }

    private data class TokenHost(
        val token: String,
        val hostname: String,
        val zone: String,
        val dc: String
    )

}
