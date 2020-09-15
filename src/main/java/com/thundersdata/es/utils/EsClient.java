package com.thundersdata.es.utils;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class EsClient {

    public static RestHighLevelClient getClient(){

        // 创建httpHost对象
        HttpHost httpHost = new HttpHost("115.159.29.15",9200);

        RestClientBuilder restClientBuilder=RestClient.builder(httpHost);
        // 创建
        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        return client;
    }

}

