package com.axiomapoc.jobengine;

import com.axiomapoc.model.BiTemporalDoc;
import com.axiomapoc.model.MapKey;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.axiomapoc.util.Constants.*;

public class JobEngine {

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.phone.home.enabled", "false");
        System.setProperty("hazelcast.logging.type", "log4j");

        ClientConfig dataClientConfig = loadClientConfigProps();

        EntryListenerConfig cfg = new EntryListenerConfig();
        cfg.setLocal(true).setIncludeValue(true).setImplementation(new JobListener());

        JetConfig jetConfig = new JetConfig();
        jetConfig.getHazelcastConfig().getGroupConfig().setName(JOB_CLUSTER_NAME);

        jetConfig.getHazelcastConfig().getMapConfig(JOB_MAP).addEntryListenerConfig(cfg);
        jetConfig.getHazelcastConfig().getNetworkConfig().setPort(6701);
        jetConfig.getHazelcastConfig().getManagementCenterConfig().setEnabled(true).setUrl("http://localhost:8080/hazelcast-mancenter");


        JetInstance jetInstance = Jet.newJetInstance(jetConfig);

        JobProcessor.init(jetInstance, dataClientConfig);
        loadData(jetInstance, dataClientConfig);
    }

    private static void loadData(JetInstance jetInstance, ClientConfig dataClientConfig) {
        //Load Data
        IMap<MapKey, BiTemporalDoc> map = HazelcastClient.newHazelcastClient(dataClientConfig).getMap(INSTRUMENT_MAP);

        IMap<MapKey, BiTemporalDoc> jMap = jetInstance.getMap(INSTRUMENT_MAP);

        map.entrySet().forEach(e-> jMap.put(e.getKey(), e.getValue()));

        System.out.println("finished");
    }

    private static ClientConfig loadClientConfigProps() throws IOException {
        Properties prop = new Properties();
        try(InputStream is = JobEngine.class.getClassLoader().getResourceAsStream("data-cluster.properties")) {
            prop.load(is);
        }

        String[] addressses = prop.getProperty("addresses").split(",");

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName(prop.getProperty("name"));
        clientConfig.getNetworkConfig().addAddress(addressses);

        return clientConfig;
    }
}
