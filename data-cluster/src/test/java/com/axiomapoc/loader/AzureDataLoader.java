package com.axiomapoc.loader;


import com.axiomapoc.model.BiTemporalDoc;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.QueryIterable;

import java.util.concurrent.atomic.AtomicLong;

import com.axiomapoc.model.MapKey;
import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;


public class AzureDataLoader {

    public static void main(String[] args) throws Exception {
        try {
            System.setProperty("hazelcast.enterprise.license.key", "HazelcastEnterpriseHD#2Nodes#maOHFiwR5YEcy1T6K7bJ0u290q21h9d19g00sX99C39399eG99Z9v0x9t9x0");

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getGroupConfig().setName("axioma-data");

            HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

            loadData(hz.getMap(INSTRUMENT_MAP));
            //loadData(hz.getMap(CORRECTION_MAP));
        } finally {
            HazelcastClient.shutdownAll();
        }

    }


    public static void loadData(IMap<MapKey, BiTemporalDoc> map) throws Exception {
        try (DocumentClient client = new DocumentClient("https://instrumentpoc.documents.azure.com:443/",
                "fsvNaYuSWHlDkosSqu0OxkUlMbJZ7Qwfi5EWKlJ5LJ7F2pq42V8rWctIuciScqbewOeA9cHN5XuAbBHUd4TD5g==", ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Session)) {

            FeedOptions feedOptions = new FeedOptions();
            feedOptions.setEnableCrossPartitionQuery(true);

            Gson gson = Converters.registerAll(new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)).create();

            String collectionLink = String.format("/dbs/%s/colls/%s", "ApiTest", map.getName());

            AtomicLong counter = new AtomicLong();

            System.out.println("Querying");
            QueryIterable<Document> result = client.queryDocuments(collectionLink, "SELECT * FROM r", feedOptions).getQueryIterable();

            System.out.println("Writing to Hazelcast");

            result
                    .forEach(e -> {
                        BiTemporalDoc v = gson.fromJson(e.toJson(), BiTemporalDoc.class);
                        //System.out.println(v);
                        long l = counter.incrementAndGet();
                        map.put(MapKey.of(v.getSource(), v.getAxiomaDataId(), v.getValidityRange(), v.getTransactionTime()), v);
                        if(l % 1000 == 0) System.out.println(l);
                    });

            long size = counter.get();
            System.out.println("Completed -> " + size);

            while (map.size() < size) {
                Thread.sleep(5000);
            }
        }
    }
}
