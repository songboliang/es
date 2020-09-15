# es
elasticSearch学习

1.索引基本操作

PUT /person
{
  "settings": {
    "number_of_shards": 5,   <创建分片>
    "number_of_replicas": 1  <创建副本分片>
  }
}

2.查看索引信息
GET /person

3.删除索引
DELETE /person

1.5 创建索引并指定数据结构 (ElasticSearch7 新特性 不需要为索引指定类型，默认_doc)
PUT /book
{
  "settings": {
      #备份数
    "number_of_replicas": 1,
      #分片数
   	"number_of_shards": 5
  },
    #指定数据结构
  "mappings": {
    #指定类型 Type
    # 文件存储的Field属性名
      "properties": {
        "name": {
          "type": "text",
          "analyzer": "ik_max_word",
    #   指定当前的Field可以作为查询的条件
          "index": true
        },
        "authoor": {
          "type": "keyword"
        },
        "onsale": {
          "type": "date",
          "format": "yyyy-MM-dd"
        }
      }
  }
}

1.6 文档的操作

    文档在ES服务中的唯一标志，_index, _type, _id 三个内容为组合，来锁定一个文档，操作抑或是修改

1.6.1 新建文档

`自动生成id`

PUT /book
{
  "name": "西游记",
  "authoor": "刘明",
  "onsale": "2020-12-11"
}

`指定ID`

PUT /book/1
{
  "name": "三国演义",
  "authoor": "小明",
  "onsale": "2020-12-11"
}

1.6.2 修改文档

`覆盖式修改`
POST /book/1
{
  "name": "三国演义",
  "authoor": "小明",
  "onsale": "2020-12-11"
}


doc修改方式（更推荐）
POST /book/1/_update
{
  "doc": {
    "name": "极品家丁"
  }
}
 
#先锁定文档，_update  修改需要的字段即可

1.6.3 删除文档

DELETE /book/1

#2. java操作ElaticSearch

导入依赖
        
        <!-- 要和elasticSearch版本一致-->
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>7.5.1</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.5.1</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.4</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.10.2</version>
        </dependency>


2.1.2 创建测试类，连接ES

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

2.2 java创建索引

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
}

2.3 检查索引是否存在

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

2.4 删除索引

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

2.5 操作文档

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




















