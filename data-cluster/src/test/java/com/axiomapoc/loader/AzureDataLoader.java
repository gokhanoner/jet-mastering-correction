package com.axiomapoc.loader;


import com.axiomapoc.model.BiTemporalDoc;
import com.axiomapoc.model.MapKey;
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

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static com.axiomapoc.util.Constants.CORRECTION_MAP;
import static com.axiomapoc.util.Constants.DATA_CLUSTER_NAME;
import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;


public class AzureDataLoader {

    public static void main(String[] args) throws Exception {
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getGroupConfig().setName(DATA_CLUSTER_NAME);

            HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

            loadData(hz, args);
        } finally {
            HazelcastClient.shutdownAll();
        }

    }


    public static void loadData(HazelcastInstance hz, String[] args) throws Exception {
        try (DocumentClient client = new DocumentClient("https://instrumentpoc.documents.azure.com:443/",
                "fsvNaYuSWHlDkosSqu0OxkUlMbJZ7Qwfi5EWKlJ5LJ7F2pq42V8rWctIuciScqbewOeA9cHN5XuAbBHUd4TD5g==", ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Eventual)) {

            FeedOptions feedOptions = new FeedOptions();
            feedOptions.setEnableCrossPartitionQuery(true);
            feedOptions.setPageSize(10000);
            feedOptions.setMaxBufferedItemCount(10000);

            Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDateTime.class, new LDTConverter())
                            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                            .create();

            IMap<MapKey, BiTemporalDoc> map;

            int cnt = Integer.parseInt(args[0]);
            int pageStart = -1;
            int pageEnd = -1;
            if(args.length >= 2) {
                pageStart = Integer.parseInt(args[1]);
                if(args.length >= 3) {
                    pageEnd = Integer.parseInt(args[2]);
                }
            }

            switch (cnt) {
                case 1: map = hz.getMap(CORRECTION_MAP);break;
                case 2: map = hz.getMap(INSTRUMENT_MAP);break;
                default:throw new IllegalArgumentException();
            }

            String collectionLink = String.format("/dbs/%s/colls/%s", "ApiTest", map.getName());

            AtomicLong counter = new AtomicLong();

            String query = "SELECT * FROM R";

            System.out.printf("Writing to %s map\n", map.getName());

            int perPage = 100000;

            if(pageStart > -1) {
                int start = perPage * pageStart;
                query = query + " WHERE R.AxiomaDataId >= " + perPage * pageStart;
                if(pageEnd > -1) {
                    query = query + " AND R.AxiomaDataId < " + perPage * pageEnd;
                }
                System.out.println(query);
            }

            System.out.println("Querying");

            QueryIterable<Document> result = client.queryDocuments(collectionLink, query, feedOptions).getQueryIterable();

            System.out.println("Writing to Hazelcast");

            result
                    .forEach(e -> {
                        try {
                            BiTemporalDoc v = gson.fromJson(e.toJson(), BiTemporalDoc.class);
                            long l = counter.incrementAndGet();
                            map.putAsync(MapKey.of(v.getSource(), v.getAxiomaDataId(), v.getValidityRange(), v.getTransactionTime()), v);
                            if(l % 1000 == 0) {
                                System.out.println(l);
                            }
                        } catch (Throwable t) {
                            //NO-OP
                        }
                    });
            long size = counter.get();
            System.out.println("Completed -> " + size);

            while (map.size() < size) {
                Thread.sleep(5000);
            }
        }
    }
}
