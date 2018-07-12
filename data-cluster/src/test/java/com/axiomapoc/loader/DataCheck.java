package com.axiomapoc.loader;


import com.axiomapoc.model.BiTemporalDoc;
import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Set;

import com.axiomapoc.model.MapKey;
import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;


public class DataCheck {

    public static void main(String[] args) throws Exception {
        try {
            System.setProperty("hazelcast.enterprise.license.key", "HazelcastEnterpriseHD#2Nodes#maOHFiwR5YEcy1T6K7bJ0u290q21h9d19g00sX99C39399eG99Z9v0x9t9x0");

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getGroupConfig().setName("axioma-data");

            HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

            IMap<MapKey, BiTemporalDoc> map = hz.getMap(INSTRUMENT_MAP);

            Set<String> sources = map.aggregate(Aggregators.distinct("source"));
            System.out.println(sources);
        } finally {
            HazelcastClient.shutdownAll();
        }

    }

}
