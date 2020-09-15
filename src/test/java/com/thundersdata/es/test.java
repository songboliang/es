package com.thundersdata.es;

import com.thundersdata.es.utils.EsClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;

public class test {

    @Test
    public void testConnect(){
        RestHighLevelClient client = EsClient.getClient();
        System.out.println("连接成功！");
    }

}
