package com.sample.application.demo.configs.dynomite

import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

interface HostTokenSupplier {
    fun getHostTokens() : List<HostToken>
}

@Configuration
class DynomiteHostSupplierConfiguration {

    companion object {
        private const val HOSTS_DELIMITER: String = ","
        private const val HOST_DELIMITER: String = ":"
    }

    @Value("\${dynomite.cluster.hosts}")
    private lateinit var clusterHosts: String

    @Bean
    fun getHostTokenSupplier() : HostTokenSupplier {
        return object : HostTokenSupplier {
            override fun getHostTokens(): List<HostToken> {
                return buildHosts(clusterHosts)
            }
        }
    }

    @Bean
    fun getHostSupplier() : HostSupplier {
        return HostSupplier { getHostTokenSupplier().getHostTokens().map { hostToken -> hostToken.host } }
    }

    private fun buildHosts(clusterHosts: String): List<HostToken> {
        return clusterHosts
            .split(HOSTS_DELIMITER)
            .map(this::buildHost)
    }

    private fun buildHost(host: String): HostToken {
        val hostInfo = host.split(HOST_DELIMITER)
        val hostName = hostInfo[0]
        val port = hostInfo[1].toInt()
        val rack = hostInfo[2]
        val datacenter = hostInfo[3]
        val token = hostInfo[4].toLong()
        return HostToken(token, Host(hostName, null, port, rack, datacenter, Host.Status.Up))
    }
}