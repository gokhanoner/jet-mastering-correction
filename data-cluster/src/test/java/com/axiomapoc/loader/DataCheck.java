package com.axiomapoc.loader;


import com.axiomapoc.index.BiTemporalIdx;
import com.axiomapoc.model.ValidityRange;
import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.proxy.ClientMapProxy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;
import static com.hazelcast.query.Predicates.*;


public class DataCheck {

    public static void main(String[] args) throws Exception {
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getGroupConfig().setName("axioma-data");

            HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

            LocalDateTime asAtDate = LocalDateTime.of(1993, 1, 1, 0,0);


            Collection<Object> map1 = hz.getMap(INSTRUMENT_MAP)
                    .values(lessEqual("bitempidx", new BiTemporalIdx(asAtDate)));


            map1.forEach(System.out::println);

            Collection<Object> map = hz.getMap(INSTRUMENT_MAP)
                    .values(equal("currency", "USD"));


            hz.getMap(INSTRUMENT_MAP)
                    .aggregate(Aggregators.distinct("source")).forEach(System.out::println);
            System.out.println("\n");


            Predicate predicate = and(lessEqual("validityRange.validFrom", asAtDate),
                            greaterThan("validityRange.validTo", asAtDate),
                            lessEqual("transactionTime", asAtDate));




            //hz.getMap(INSTRUMENT_MAP).entrySet().forEach(e -> hz.getMap("4444").putAsync(e.getKey(), e.getValue()));

            hz.getMap(INSTRUMENT_MAP).addIndex("validityRange.validFrom", true);
            hz.getMap(INSTRUMENT_MAP).addIndex("validityRange.validTo", true);
            hz.getMap(INSTRUMENT_MAP).addIndex("transactionTime", true);

            long a = System.nanoTime();

            hz.getPartitionService().getPartitions().forEach(i -> {
                ((ClientMapProxy)hz.getMap(INSTRUMENT_MAP)).iterator(1000, i.getPartitionId(), Projections.singleAttribute("this"), predicate).forEachRemaining(System.out::println);

            });

            long b = System.nanoTime();
            //map.forEach(System.out::println);
            System.out.println((b - a) / 10e6);

            a = System.nanoTime();
            hz.getMap("4444").values(predicate).forEach(System.out::println);
            b = System.nanoTime();
            System.out.println((b - a) / 10e6);

            System.out.println(map.size());

            //Set<String> sources = map.aggregate(Aggregators.distinct("currency"));
            //System.out.println(sources);
        } finally {
            HazelcastClient.shutdownAll();
        }

    }

}
