# Trade Order Service

这是一个基础的Java项目，实现了交易订单服务的各种功能，包括负载均衡、缓存、限流、超时重试、幂等等。

## 项目结构

```
trade-order-service
│   README.md
│   ...（其他项目文件）
│
└───trade-order-gateway
│   │   ...（trade-order-gateway模块源代码）
│   
└───trade-order-api
    │   ...（trade-order-api模块源代码）
```

## 启动

要运行该项目，请按照以下步骤：

1. 克隆项目仓库：

   ```
   git clone ssh://git@120.92.88.48:8022/yinyuao/trade-order-service.git
   ```

2. 确保已安装必要的依赖项：

  - JDK 1.8
  - Maven

3. 进入项目目录：

   ```
   cd trade-order-service
   ```

4. 构建项目：

   ```
   mvn clean package -Dmaven.test.skip=true -Plocal
   ```

5. 启动本地开发服务器：

   ```
   sh ./bin/start_server.sh -e=local
   ```

   若要部署到生产环境，请使用以下命令：

   ```
   sh ./bin/start_server.sh -e=prod
   ```

## API接口

### 1. 查询订单详情

- URL：`http://127.0.0.1:8088/online/queryOrderInfo?id=7`
- 方法：GET
- 参数：
  - `id`：订单ID（整数）

- 响应：
  ```
  {
      "code": 200,
      "msg": "ok",
      "requestId": "3d6c2579-7dd5-48c0-830f-900c928b26a5",
      "descr": "0.00毫秒",
      "data": {
          "upsteam": "campus.query1.ksyun.com:8089",
          "id": 7,
          "priceValue": 12165.7542,
          "user": {
              "username": "user9574",
              "email": "user9574@example.com",
              "phone": "10000902619",
              "address": "深圳市解放路保利城2号"
          },
          "region": {
              "code": "Bengbu",
              "name": "蚌埠"
          },
          "configs": {
              "itemNo": "TRANSFER-CDN",
              "itemName": "CDN流量",
              "unit": "GB",
              "value": 773
          }
      }
  }
  ```

### 2. 根据ID查询机房名称

- URL：`http://127.0.0.1:8088/online/queryRegionName?regionId=45`
- 方法：GET
- 参数：
  - `regionId`：机房ID（整数）

- 响应：
  ```
  {
      "code": 200,
      "msg": "ok",
      "requestId": "3d6c2579-7dd5-48c0-830f-900c928b26a5",
      "descr": "118.00毫秒",
      "data": "唐山"
  }
  ```

### 3. 订单优惠券抵扣

- URL：`http://127.0.0.1:8088/online/voucher/deduct`
- 方法：POST
- 请求体：
  ```
  {
    "orderId": 1,
    "voucherNo": "TEST123456",
    "amount": 10.0000
  }
  ```
- 响应：
  ```
  {
    "code": 200,
    "requestId": "6ebc37ee-b46b-43c9-ac2d-c2117b149b27",
    "msg": "ok"
  }
  ```

### 4. 获取上游服务器信息

- URL：`/online/listUpstreamInfo`
- 方法：GET
- 响应：
  ```
  {
    "code": 200,
    "msg": "ok",
    "requestId": "6ebc37ee-b46b-43c9-ac2d-c2117b149b27",
    "data": ["campus.query1.ksyun.com", "campus.query2.ksyun.com"]
  }
  ```

## 限流

API接口 `/online/listUpstreamInfo` 的限流为每秒最多5个请求。如果超过限制，响应如下：

```
{
  "code": 429,
  "requestId": "6ebc37ee-b46b-43c9-ac2d-c2117b149b27",
  "msg": "对不起，系统压力过大，请稍后再试！"
}
```

## 负载均衡

在`GatewayService`类中，实现了负载均衡器。根据路由规则，从接口URL列表中随机选择一个URL进行请求转发。具体代码实现参见`GatewayService`中的相关方法。

### 请求转发

`GatewayService`类中的`forwardingGet`方法和`forwardingPost`方法分别用于GET请求和POST请求的转发。这些方法使用`RestTemplate`发送HTTP请求并接收响应结果。

### 随机选择URL

`GatewayService`类中的`random`方法实现了随机算法，从配置的接口URL列表中随机选择一个URL进行负载均衡。

## 二级缓存

该项目实现了二级缓存功能，包含了`MemoryCache`和`RedisCache`两种缓存实现，并通过`TwoLevelCache`类将它们组合成一个二级缓存系统。

### MemoryCache

`MemoryCache`类是内存缓存的实现，使用`HashMap`作为缓存数据存储容器。在构造方法中指定最大缓存容量和缓存过期时间。当缓存容量达到最大限制时，将会自动清除过期的缓存项，并在仍然缓存已满时清除最旧的缓存项。

### RedisCache

`RedisCache`类是Redis缓存的实现，使用`Jedis`作为Redis客户端。它通过与Redis服务器通信，将缓存项存储到Redis中，并能够从Redis中获取缓存项。

### TwoLevelCache

`TwoLevelCache`类将`MemoryCache`和`RedisCache`组合成一个二级缓存系统。它通过首先从一级缓存(`MemoryCache`)获取缓存项，如果一级缓存没有命中，则从二级缓存(`RedisCache`)获取缓存项，并将缓存项存储到一级缓存中，以便下次能够更快地获取。

### 使用方法

要使用二级缓存，首先需要创建`MemoryCache`和`RedisCache`实例，并将它们传递给`TwoLevelCache`的构造方法。然后，通过`TwoLevelCache`的`get`和`put`方法来获取和存储缓存项。

例如，在`GatewayService`中，可以将二级缓存用于存储接口响应数据，以提高请求的处理速度和减轻后端服务的压力。在调用接口前，先从二级缓存中查询数据，如果缓存中不存在，则发起实际的接口请求，并将响应数据存储到缓存中，以便后续查询时可以直接获取。

### 注意事项

在使用二级缓存时，需要根据实际情况来设置缓存的最大容量和过期时间。合理的设置可以提高缓存命中率，提升系统性能。

### 示例代码

```java

MemoryCache<String, Object> memoryCache = new MemoryCache<>(1000, 60000); // 最大容量1000，过期时间60000毫秒// R
RedisCache<String, Object> redisCache = new RedisCache<>(jedisPool);

// 创建TwoLevelCache实例
TwoLevelCache<String, Object> twoLevelCache = new TwoLevelCache<>(memoryCache, redisCache);

// 存储缓存项
twoLevelCache.put("key1", "value1");

// 获取缓存项
Object value = twoLevelCache.get("key1");
```

###  项目使用

使用Spring Boot的配置类和初始化类来配置并初始化缓存

#### CacheConfig
通过Spring配置类，定义了一个twoLevelCache的Bean。这个Bean是一个TwoLevelCache的实例，由MemoryCache和RedisCache组合而成。MemoryCache用于一级缓存，RedisCache用于二级缓存。jedisPool是通过@Autowired注入的，用于创建RedisCache实例所需的JedisPool。

#### CacheInitializer
实现了ApplicationRunner接口的初始化类。在Spring Boot应用启动时，run方法会被执行，用于初始化缓存。在该示例代码中，它从指定的url获取远程地区数据列表，并将这些数据列表存储到缓存中。具体地，它使用RemoteRequestUtils.getRemoteData方法获取远程数据，然后使用objectMapper将数据映射为List<RegionDo>对象，并将其存储到Redis缓存中。

```java
@Override
public void run(ApplicationArguments args) {
    // 获取远程地区数据列表
    Map<String, Object> regionMap = RemoteRequestUtils.getRemoteData(url, null, "online", "region", "list");
    // 将地区数据列表映射为List<RegionDo>对象
    List<RegionDo> list = objectMapper.convertValue(regionMap.get("data"), new TypeReference<List<RegionDo>>() {});

    Jedis jedis = jedisPool.getResource();
    try {
        String key = url + "/online/region";
        String jsonList = objectMapper.writeValueAsString(list);
        jedis.set(key, jsonList);
    } catch (JsonProcessingException e) {
        e.printStackTrace();
    }
}
```

#### 接口
每个查询接口都会先去查询二级缓存，若缓存没有再去数据库查找数据。

## 注意事项

在最终部署前，请确保`local`、`uat`和`prod`环境下的YAML配置结构保持一致，以避免出现遗漏配置的问题。

## 联系方式

如有任何问题或疑虑，请联系：

- 殷钰奥：[807362114@qq.com]
- 项目仓库：[[yuao yin / trade-order-service · GitLab](http://120.92.88.48/yinyuao/trade-order-service)]