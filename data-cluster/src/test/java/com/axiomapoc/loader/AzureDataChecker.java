package com.axiomapoc.loader;


import com.axiomapoc.model.BiTemporalDoc;
import com.axiomapoc.model.MapKey;
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


public class AzureDataChecker {

    public static void main(String[] args) throws Exception {
        try {
            loadData();

        } finally {
            HazelcastClient.shutdownAll();
        }

    }


    public static void loadData() throws Exception {
        try (DocumentClient client = new DocumentClient("https://instrumentpoc.documents.azure.com:443/",
                "fsvNaYuSWHlDkosSqu0OxkUlMbJZ7Qwfi5EWKlJ5LJ7F2pq42V8rWctIuciScqbewOeA9cHN5XuAbBHUd4TD5g==", ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Eventual)) {

            FeedOptions feedOptions = new FeedOptions();
            feedOptions.setEnableCrossPartitionQuery(true);
            feedOptions.setPageSize(10000);
            feedOptions.setMaxBufferedItemCount(10000);

            String collectionLink = String.format("/dbs/%s/colls/%s", "ApiTest", INSTRUMENT_MAP);

            System.out.println("Querying");

            client
                    .queryAggregateValues(collectionLink, "SELECT VALUE COUNT(1) FROM R WHERE R.AxiomaDataId >= 201000000 AND R.AxiomaDataId <= 201500000", feedOptions)
                    .forEach(System.out::println);
            }
    }
}
