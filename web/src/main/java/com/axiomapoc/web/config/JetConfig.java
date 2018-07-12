package com.axiomapoc.web.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.axiomapoc.util.Constants.*;

@Configuration
public class JetConfig {

    @Bean
    ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName(JOB_CLUSTER_NAME);
        clientConfig.getGroupConfig().setPassword("jet-pass");
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:6701");
        clientConfig.getNetworkConfig().setConnectionAttemptLimit(0);
        return clientConfig;
    }

    @Bean
    JetInstance jetInstance(ClientConfig clientConfig) {
        return Jet.newJetClient(clientConfig);
    }
}
