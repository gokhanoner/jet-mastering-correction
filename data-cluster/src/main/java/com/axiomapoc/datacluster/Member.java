package com.axiomapoc.datacluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapAttributeConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;

import java.io.File;

import static com.axiomapoc.util.Constants.*;

public class Member {

    public static void main(String[] args) {
        System.setProperty("hazelcast.phone.home.enabled", "false");
        System.setProperty("hazelcast.enterprise.license.key","HazelcastEnterpriseHD#2Nodes#maOHFiwR5YEcy1T6K7bJ0u290q21h9d19g00sX99C39399eG99Z9v0x9t9x0");

        Config config = new Config();
        config.getGroupConfig().setName(DATA_CLUSTER_NAME);
        config.getMapConfig(CORRECTION_MAP).getHotRestartConfig().setEnabled(true);
        config.getMapConfig(INSTRUMENT_MAP).getHotRestartConfig().setEnabled(true);

        config.getMapConfig(INSTRUMENT_MAP)
                .addMapAttributeConfig(new MapAttributeConfig("bitempidx", "com.axiomapoc.index.BiTempValExtractor"));

        config.getMapConfig(INSTRUMENT_MAP).addMapIndexConfig(new MapIndexConfig("bitempidx", true));

        config.getMapConfig(INSTRUMENT_MAP).addMapIndexConfig(new MapIndexConfig("transactionTime", true));
        config.getMapConfig(INSTRUMENT_MAP).addMapIndexConfig(new MapIndexConfig("validityRange.validFrom", true));
        config.getMapConfig(INSTRUMENT_MAP).addMapIndexConfig(new MapIndexConfig("validityRange.validTo", true));
        
        config.getHotRestartPersistenceConfig().setEnabled(true).setBaseDir(new File(args[0]));

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        System.out.println(hz.getMap(CORRECTION_MAP).size());
        System.out.println(hz.getMap(INSTRUMENT_MAP).size());
    }
}
