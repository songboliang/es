package com.thundersdata.es.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.thundersdata.es.model.Person;
import com.thundersdata.es.utils.EsClient;
import org.elasticsearch.Build;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Demo {

    RestHighLevelClient client = EsClient.getClient();

    String index = "person";

    String type = "man";

    /**
     * 创建索引
     *
     * @throws IOException
     */
    @Test
    public void index()throws IOException {

        // 准备关于索引的settings
        Settings.Builder settings = Settings.builder()
                .put("number_of_shards",3)
                .put("number_of_replicas",1);
        // 准备关于索引的结构mappings
        XContentBuilder mappings = JsonXContent.contentBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("name")
                            .field("type","text")
                        .endObject()
                        .startObject("age")
                            .field("type","integer")
                        .endObject()
                        .startObject("birthday")
                            .field("type","date")
                            .field("format","yyyy-MM-dd")
                        .endObject()
                    .endObject()
                .endObject();

        // 将setting和mapping封装到一个Request对象( ElasticSearch7新特性 -Type类型取消)
        CreateIndexRequest request = new CreateIndexRequest(index).settings(settings).mapping(mappings);

        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println("reps:"+response.toString());

    }

    /**
     * 查询索引是否存在
     */
    @Test
    public void getIndex()throws IOException {

        GetIndexRequest request = new GetIndexRequest(index);

        request.indices();

        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(exists);

    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndex()throws IOException{

        DeleteIndexRequest request = new DeleteIndexRequest();

        request.indices(index);

        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);

        System.out.println(delete.isAcknowledged());

    }

    @Test
    public void addData() throws IOException {

        // 准备一个Json数据
        Person person = new Person(1,"张三",18,new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(person);

        // 准备一个request对象（手动指定id，elasticSearch7指定type是_doc）
        IndexRequest request = new IndexRequest(index,"_doc",person.getId().toString());
        request.source(json, XContentType.JSON);
        // 通过client对象执行添加
        IndexResponse index = client.index(request, RequestOptions.DEFAULT);
        // 输出返回结果
        System.out.println(index.getResult().toString());
    }

    /**
     * 修改文档
     * @throws IOException
     */
    @Test
    public void updateDoc() throws IOException {
        // 1.创建一个Map,指定需要修改的内容
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","李四");
        String docId = "1";
        // 2.创建request对象，封装数据
        UpdateRequest request = new UpdateRequest(index,docId);
        request.doc(map);
        // 3.通过client对象执行
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        // 4.输出返回结果
        System.out.println(update);
    }

    /**
     * 删除文档
     *
     * @throws IOException
     */
    @Test
    public void deleteDoc()throws IOException{
        DeleteRequest request = new DeleteRequest(index,"1");

        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);

        System.out.println(delete);
    }

    /**
     * 批量新增
     *
     * @throws IOException
     */
    @Test
    public void bulkDoc()throws IOException{

        Person person1 = new Person(14,"小红",6,new Date());
        Person person2 = new Person(15,"小芳",7,new Date());
        Person person3 = new Person(16,"小明",8,new Date());
        Person person4 = new Person(17,"小松",7,new Date());

        ObjectMapper objectMapper = new ObjectMapper();


        IndexRequest source = new IndexRequest(index,"_doc", person1.getId().toString()).source(objectMapper.writeValueAsString(person1), XContentType.JSON);
        IndexRequest source1 = new IndexRequest(index, "_doc",person2.getId().toString()).source(objectMapper.writeValueAsString(person2), XContentType.JSON);
        IndexRequest source2 = new IndexRequest(index, "_doc",person3.getId().toString()).source(objectMapper.writeValueAsString(person3), XContentType.JSON);
        IndexRequest source3 = new IndexRequest(index, "_doc",person4.getId().toString()).source(objectMapper.writeValueAsString(person4), XContentType.JSON);
        BulkRequest request = new BulkRequest();
        request.add(source);
        request.add(source1);
        request.add(source2);
        request.add(source3);

        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);

        System.out.println(bulk);
    }

    /**
     * 批量删除
     * @throws IOException
     */
    @Test
    public void bulkDelete()throws IOException{
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest(index,"1"));
        request.add(new DeleteRequest(index,"10"));
        request.add(new DeleteRequest(index,"11"));
        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk);
    }

}
