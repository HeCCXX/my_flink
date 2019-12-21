# Elasticsearch基础知识

Elasticsearch--面向文档型数据库（一条数据是一个文档，json格式）

与MySQL的对比：

|             MySQL             |               ES               |
| :---------------------------: | :----------------------------: |
| 需要建立table，并定义相应字段 | 将数据以json格式保存为一个文档 |

- 关系数据库-->数据库-->表-->行-->列（columns）


- ES-->索引（index）-->类型（type）-->文档（documents）-->字段（Fields）


下列几条简单的数据：

| ID   | Name | Age  |  Sex   |
| ---- | ---- | :--: | :----: |
| 1    | Kate |  24  | Female |
| 2    | John |  24  |  Male  |
| 3    | Bill |  29  |  Male  |

之后建立倒排索引：

- Name：


| Term | Posting List |
| ---- | ------------ |
| Kate | 1            |
| John | 2            |
| Bill | 3            |

- Age：


| Term | Posting List |
| ---- | ------------ |
| 24   | 1,2          |
| 29   | 3            |

- Sex：


| Term   | Posting List |
| ------ | ------------ |
| Female | 1            |
| Male   | 2,3          |

​        通过posting list这种索引方式进行查找，term Dictionary，类似字典查找，之后是term index，像字典里的索引页（用于处理term太多，放内存不显示）

# elasticsearch的安装（单机）

1. 安装jdk1.8，并配置环境变量，修改/etc/profile，最后source  /etc/profile使配置立即生效
2. 下载elasticsearch，并解压，修改文件夹中，config/elasticsearch.yml,把network.host和http.port前面注释取消，并将host改成本机IP地址。

# elasticsearch安装head插件

1. 安装nodejs

2. 安装git

   ```
   sudo yum install git
   ```

3. 下载及安装head插件

   ```bash
   git clone git://github.com/mobz/elasticsearch-head.git
   
   cd elasticsearch-head
   
   npm install
   
   npm run start
   
   open http://localhost:9100/
   ```

4. 配置elasticsearch允许head插件访问，修改config目录下的elasticsearch.yml,在最后加上以下内容：

   ```bash
   http.cors.enabled: true
   
   http.cors.allow-origin: "*"
   ```

5. 启动插件

   ```
   npm run start
   ```

   浏览器输入IP：9100，进入web界面，在连接处输入IP：9200，出现集群状态则配置完成。


# elasticsearch.yml内容：

```bash
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
# Before you set out to tweak and tune the configuration, make sure you
# understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please see the documentation for further information on configuration options:
# <http://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration.html>
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
# 集群名称，默认是elasticsearch
# cluster.name: my-application
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
# 节点名称，默认从elasticsearch-2.4.3/lib/elasticsearch-2.4.3.jar!config/names.txt中随机选择一个名称
# node.name: node-1
#
# Add custom attributes to the node:
# 
# node.rack: r1
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
# 可以指定es的数据存储目录，默认存储在es_home/data目录下
# path.data: /path/to/data
#
# Path to log files:
# 可以指定es的日志存储目录，默认存储在es_home/logs目录下
# path.logs: /path/to/logs
#
# ----------------------------------- Memory -----------------------------------
# Lock the memory on startup:
# 锁定物理内存地址，防止elasticsearch内存被交换出去,也就是避免es使用swap交换分区
# bootstrap.memory_lock: true
#
# 确保ES_HEAP_SIZE参数设置为系统可用内存的一半左右
# Make sure that the `ES_HEAP_SIZE` environment variable is set to about half the memory
# available on the system and that the owner of the process is allowed to use this limit.
# 
# 当系统进行内存交换的时候，es的性能很差
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
#
# 为es设置ip绑定，默认是127.0.0.1，也就是默认只能通过127.0.0.1 或者localhost才能访问
# es1.x版本默认绑定的是0.0.0.0 所以不需要配置，但是es2.x版本默认绑定的是127.0.0.1，需要配置
# Set the bind address to a specific IP (IPv4 or IPv6):
#
# network.host: 192.168.0.1
#
#
# 为es设置自定义端口，默认是9200
# 注意：在同一个服务器中启动多个es节点的话，默认监听的端口号会自动加1：例如：9200，9201，9202...
# Set a custom port for HTTP:
#
# http.port: 9200
#
# For more information, see the documentation at:
# <http://www.elastic.co/guide/en/elasticsearch/reference/current/modules-network.html>
#
# --------------------------------- Discovery ----------------------------------
#
# 当启动新节点时，通过这个ip列表进行节点发现，组建集群
# 默认节点列表：
# 127.0.0.1，表示ipv4的回环地址。
#	[::1]，表示ipv6的回环地址
#
# 在es1.x中默认使用的是组播(multicast)协议，默认会自动发现同一网段的es节点组建集群，
# 在es2.x中默认使用的是单播(unicast)协议，想要组建集群的话就需要在这指定要发现的节点信息了。
# 注意：如果是发现其他服务器中的es服务，可以不指定端口[默认9300]，如果是发现同一个服务器中的es服务，就需要指定端口了。
# Pass an initial list of hosts to perform discovery when new node is started:
# 
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
# discovery.zen.ping.unicast.hosts: ["host1", "host2"]
#
# 通过配置这个参数来防止集群脑裂现象 (集群总节点数量/2)+1
# Prevent the "split brain" by configuring the majority of nodes (total number of nodes / 2 + 1):
#
# discovery.zen.minimum_master_nodes: 3
#
# For more information, see the documentation at:
# <http://www.elastic.co/guide/en/elasticsearch/reference/current/modules-discovery.html>
#
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
# 一个集群中的N个节点启动后,才允许进行数据恢复处理，默认是1
# gateway.recover_after_nodes: 3
#
# For more information, see the documentation at:
# <http://www.elastic.co/guide/en/elasticsearch/reference/current/modules-gateway.html>
#
# ---------------------------------- Various -----------------------------------
# 在一台服务器上禁止启动多个es服务
# Disable starting multiple nodes on a single system:
#
# node.max_local_storage_nodes: 1
#
# 设置是否可以通过正则或者_all删除或者关闭索引库，默认true表示必须需要显式指定索引库名称
# 生产环境建议设置为true，删除索引库的时候必须显式指定，否则可能会误删索引库中的索引库。
# Require explicit names when deleting indices:
#
# action.destructive_requires_name: true
```

# elasticsearch集群配置

首先将目录中data目录下的node目录删除，否则集群建立会失败。

分别配置elasticsearch.yml

node-1需要修改下列配置：

```bash
cluster.name: my-application
node.name: node-1
network.host: 192.168.1.110
http.port: 9200
discovery.zen.ping.unicast.hosts: ["192.168.1.110"]
```

node-2需要修改下列配置：

```bash
cluster.name: my-application
node.name: node-2
network.host: 192.168.1.111
http.port: 9200
discovery.zen.ping.unicast.hosts: ["192.168.1.110"]
```

这里两台机器的cluster.name必须一致 这样才算一个集群

node.name节点名称每台取不同的名称，用来表示不同的集群节点

network.host配置成自己的局域网IP

http.port端口就固定9200

discovery.zen.ping.unicast.hosts主动发现节点我们都配置成110节点IP。

配置完成后，重启es服务，head插件查看配置成功：![1541841565632](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1541841565632.png)

# elasticsearch写流程

### put基本流程

新建、索引、删除请求都是写操作，必须在主分片上面完成之后才能被复制到相关的副本。

![1542098425195](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542098425195.png)

写操作可能会发生三个节点:协调节点,主分片所在节点,副本分片所在节点
下面从这三个节点上发生的流程分别梳理。

### put详细流程

#### 协调节点流程

​	协调节点负责创建索引，转发请求到主分片节点，等待响应，回复客户端。

​	创建索引请求被发送到master，知道收到其Response之后，进入写doc操作主逻辑。在master执行完创建索引流程，将新的clusterState发布完毕后才会放回Response。默认情况下，master发布clusterState的Request收到半数以上节点的Response，认为发布成功，负责写数据的节点会先走一遍内容路由的过程，以处理没有收到最新clusterState的情况。

内容路由过程：

1. 加载映射：加载请求指定的type，或使用默认映射。
2. 检查别名：如果是索引别名，做一些检查工作，有以下之一的将做失败处理：
   1. 关联了一个以上的索引
   2. 别名设置了routing，且与请求的不一致
   3. 请求参数中的routing指定了多个​	
3. 获取主分片路由：之后判断是否自动生成doc id，计算shardid，获取到主分片路由信息，如果主分片处于不可用状态，将进行重试，充实的触发时机为收到新的clusterState，或者1分钟超时。
4. 转发请求：主分片确定后，将请求发送到主分片所在节点，等待其Response，协调过程至此完毕，主节点可能在本地也可能在其他节点，如果在本地，不会产生网络请求，通过函数调用到相应的处理模块。	

#### 主分片所在节点处理流程

​	主节点所在节点负责在本地写主分片，转发写副本分片请求，等待响应，回复协调节点。

​	在上一个流程中，目标是发送到网络中其他节点的，节点收到这个请求后，首先将内容路由的流程重新走一遍。因为在索引创建的过程放回后，并非集群的所有节点都有了最新的clusteState，写操作到这个节点上就会写失败。

- 检查写一致性

  默认的一致性策略为半数以上。写操作之前，涉及到要写的shard，可用shard数过半时，才执行写操作。

- 写主分片

- index还是create

  如果putAPI指定op_type=create，或者自动生成ID，会进入create过程，否则进入index过程。如果是自动生成id，不检查数据版本号。

- 写doc流程

  1. 加锁
  2. 获取版本号
  3. 检查数据版本
  4. 写入lucene，通过版本号判断doc师傅已存在，调用lucene的add或update接口写入数据。
  5. 写translog
  6. 写结束，写入完毕，释放锁

- 处理可能refresh，检查请求中是否有refresh设置，决定是否刷盘。

- 转发写副本请求，为要写的副本shard准备一个列表，循环处理每个shard，跳过unassigned的，向每个目标节点发送请求，等待响应，都是异步的。在等待response的过程中，本节点发出多少request，就要等待多少个response，无论成功与否，直到超时，最后给主分片节点消息，主分片节点返回消息，告知哪些成功，哪些失败。

#### 副本分片节点流程

1. bulk流程

   ​	客户端肖Node1发送bulk请求。Node1为每个节点创建一个批量请求，并将这些请求并行转发到每个包含主分片的节点主机。主分片一个接一个按顺序执行每个操作。当每个操作成功时，主分片并行转发新文档到副本分片，然后执行下一个操作。一旦所有的副本分片报告所有操作成功，该节点将向协调节点报告成功，协调节点将这些响应收集整理并返回给客户端。

   ![1542102396034](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542102396034.png)

   - 创建索引

     ​	BulkRequest是接口收到的原始bulk请求列表，遍历BulkRequest，去除重复索引名称，存到indicesAndTypes，然后从中遍历，异步创建索引，等待所有索引创建完毕后进入下一步，如果某个index创建失败，将把位于此index的所有请求做失败处理，而其他创建成功的index上的请求会进入下一环节。

   - 合并请求

     ​	遍历BulkRequest请求，为每个请求中计算shardid，重新组织成以shardid为单位的结构。

   - 协调处理每个shard上的请求

     ​	作为协调节点，异步并行发出以shard为单位的请求，循环执行execute，等待响应，每个响应也是以shard为单位的，如果某个shard的响应中部分doc写失败，将异常信息填充到response中，整体请求做成功处理。

   - 主分片所在节点处理逻辑

     ​	作为主分片所在节点，顺序执行主分片的每个写操作，如果某个操作失败，不会重试，不会将位于此shard上的全部请求做失败处理，而是将失败的条目标记原因，进行下一条处理，待主分片所有操作处理完毕，执行写副本，并行向其他节点发出写副本请求，等待响应。

2. 异常流程

   ​	在一个shard上执行的一些操作可能会产生IO异常之类的情况。

### 结论

1. 集群red时，写操作到达未损坏的分片是可以正常处理的
2. 对索引别名执行写操作，别名只关联了一个索引的情况下是可以正常处理的
3. 自动创建索引是阻塞的过程，创建完成后才会继续后面的流程
4. 关于写一致性，如果activeshard不足，不会等待，而是直接失败
5. 只有内容路由阶段才有重试机制，磁盘写失败等不会重试，而是直接失败，之后进入shard迁移流程

# ES测试

​	利用es-data-generator向es集群中写入模拟生成的车辆数据，并通过head插件对数据，索引等进行查看和数据的查询。（本次测试使用集群配置，但只使用一台es节点）

​	软件环境：ubuntu 16.04

​	elasticsearch版本：2.1.2

步骤如下：

1. 安装es

   解压pci-elasticsearch-2.1.2.tar.gz，并修改根目录下的elasticsearch.yml文件，修改IP地址，端口号，	集群名称等属性。	(安装和yml内容详情可参考之前内容)

2. 启动es集群

   命令bin/elasticsearch ，来启动es

3. 写入数据

   利用es-data-genetator，模拟生成车辆数据，并发送给es集群。修改集群IP，clustername，	并设置好每天产生的数据量和每次提交数据量。

   <font color=red size=5>注意：</font>

   ​	在本次测试中，使用的是集群配置，但只使用了一台es节点，相当于单机使用，需要注意的是关于写一致性，如果activeshard不足时是直接失败，写入失败，在之前介绍中写到：默认的一致性策略为半数以上。写操作之前，涉及到要写的shard，可用shard数过半时，才执行写操作。所以在elasticsearch.yml配置文件中，我们需要将副本数调为1，刚好可用分片过半，可以执行写操作。否则，需要开足够的es节点，满足写一致性的要求，过半可用分片。	

4. head插件查看

   输入http://192.168.79.129:9200/_plugin/head，查看新建立的索引，可通过head插件来进行添加索引，添加、修改、删除文档等操作。

   ```bash
   #添加索引
   PUT http://192.168.79.129:9200/xxx/        #xxx为索引名称，用户自定义
   
   #添加文档
   POST http://192.168.79.129:9200/xxx/a/b/       #xxx为索引名称，a为类别，b为id，id可自动生成
   
   #修改文档
   POST http://192.168.79.129:9200/xxx/a/b/  {"XX":"XX",....}   #{}中为需要修改的内容
   
   #删除文档
   DELETE 
   
   #查询所有映射关系
   GET http://192.168.79.129:9200/xxx/    #xxx为索引名称
   
   #查询所有数据
   GET http://192.168.79.129:9200/xxx/a/_search/   #xxx为索引名称  a为类型
   
   #分页查询数据
   POST http://192.168.79.129:9200/xxx/a/_search/  {"from":0,"size":2}  #从0位置开始查询接下来2个的文档
   
   #排序查询
   POST http://192.168.79.129:9200/xxx/a/_search/  {"sort":{{"xx":{"order":"desc"}}}}  #返回以xx倒序排序的结果
   
   #数据列过滤查询
   POST http://192.168.79.129:9200/xxx/a/_search/  {"_source":{"include":["xx1","xx2"]}}  #返回查询结果，结果过滤剩下xx1和xx2列的数据
   
   #简单条件查询
   POST http://192.168.79.129:9200/xxx/a/_search/  {"query":{"match":{"xx":"x"}}}  #查询匹配xx域含有x的结果
   
   #组合多条件查询
   #elasticsearch  提供bool 来实现需求
   #主要参数：
   #must  ：   文档必须匹配这些条件才能被包含进来
   #must_not   :  文档必须不匹配这些条件才能被包含进来
   #should   ：   如果满足这些语句中的任意语句，将增加_score,否则，无任何影响。主要用于修正每个文档的相关性得分
   #filter  ：  必须匹配，但它以不评分、过滤模式来进行。
   POST http://192.168.79.129:9200/xxx/a/_search/  {"query":{"bool":{"must":{"match":{"xx1":"x"}},"filter":{"range":{"xx2":{"lte":"xx3"}}}}}}   #查询结果中，xx1必须含有x字段，并过滤，xx2必须小于xx3。
   ```

5. 路由优化查询

   ​	ES的路由机制决定了拥有相同路由属性的文档，一定会被分配到同一个分片上，无论是主分片还是副本。在查询的时候，一旦指定了规定的路由属性，ES就可以直接到相应的分片所在的机器上进行搜索。

   ​	在进行查询时，可以使用以下命令来指定相应的分片：

   ​	GET http://192.168.79.129:9200/xxx/a/_search?routing=xxxxxxxx

   ​	当有其他参数需要提交时，可以使用post提交表单：

   ​	POST  http://192.168.79.129:9200/xxx/a/_search?routing=xxxxxxxx   {"xx":"xxx"}

   ​	如果routing的类型是string ，则需要加单引号，如：‘xxxxxxx’，如果routing的类型是org.elasticsearch.cluster.routing.ByDateHashFunction，则不需要加单引号，但输入的日期格式需满足Date的YYYYMMDD格式。

查询例子：

​	导入到es的是2018年9月份的车辆信息，查询号牌号码为粤D309D9的车辆的出现情况。由下图基本查询结果可以看到，该车牌号的车辆在9月12号和25号出现过。

![1542179993467](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542179993467.png)



下面使用复合查询

POST  http://172.25.21.225:9200/car_detect_indice_v1_1809/_search?routing=20180912,20180925 

{"query":{"bool":{"must":[{"term":{"HPHM":"粤D309D9"}}],"must_not":[],"should":[]}},"from":0,"size":10,"sort":[],"aggs":{}}

![1542180638450](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542180638450.png)

可以看到查询结果和简单查询步骤的一样，且总共的文档数目为8个。表示查询出车牌为粤D309D9的车辆的记录在2018-09-12和2018-09-25这两天中出现了8次。

如果不规定routing的话也可以查询出指定内容，但当数据量过大或出现灾况的时候，检索速度会有明显的下降。当对设置了路由属性的索引进行查询时，设置routing相当于一个条件。

# elasticsearch-sql使用

公司用的elasticsearch-2.1.2，所以找到版本对应的elasticsearch-sql安装即可。

安装命令：

```bash
./bin/plugin install https://github.com/NLPchina/elasticsearch-sql/releases/download/2.1.2.0/elasticsearch-sql-2.1.2.0.zip 
```

重启elasticsearch服务后，web前端访问http://localhost:9200/_plugin/sql/进入图像界面。



2.x和5.x的区别：

在5.x版本，需要通过以下命令来安装elasticsearch-sql。

```bash
cd site-server
npm install express --save
node node-server.js      #启动elasticsearch-sql
```

在上面第三步启动服务前，可以修改site-server目录下的site_configuration.json文件，配置端口号，之后启动。可以通过IP：端口号访问web图像界面。

也可以通过url的方式进行sql查询，例如http://localhost:9200/_sql?sql=select * from indexName limit 10 ，查询结果会以json格式展示在页面。如下图：

![1542268862552](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542268862552.png)



查询例子：

查询出每个车牌号在18年8月出现的总次数。

SELECT HPHM,COUNT(*) FROM car_detect_indice_1808 GROUP BY HPHM；

![1542269311046](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542269311046.png)

<font color=red size=5>注意事项</font>

​	出现上述原因是由于在上传数据到es集群时，没有设置索引信息的相应字段类型的index属性，我们直接上传数据，每个字段都是默认值，type属性string，默认使用全文搜索，index属性为analyzed（即输入字段中部分词就能检索到该条数据）通过elasticsearch的默认分词器将string类型的字段进行分词。

​	当我们规定数据字段需要关键词搜索时，需要将index属性设置为not_analyzed，即必须使用数据整个值进行搜索，当使用es-sql时，便不会将字段分词进行处理。

<font color=red size=5>2.x和5.x以上的String分词对比</font>

​	在es  2.x版本，当我们需要全文搜索和关键词搜索时，需要修改字段的映射。关键词搜索时，应该设置为not_analyzed，必须输入完整字段内容才能完成搜索；全文搜索，应该设置为analyzed，（或者不设置，默认就是elasticsearch的内置分词器），当输入字段的部分内容，模糊搜索。并且在es 2.x版本，文本字段的type属性为string，当设置全文搜索和关键字搜索两个不同用例使用相同字段类型时会导致问题。所以在es 5.0版本移除了string类型，将string类型分为两种新类型：text应用于全文搜索（会进行分词），keyword应用于关键字搜索。（当搜索时，用到聚合时会聚合整个字段值）

​	5.x版本还兼容string类型，如果默认analyzed，则转换为text，如果指定了not_analyzed，则转换为keyword。官方介绍6.x版本后将不再兼容string。

### es-sql字段值聚合解决办法：

#### 一、通过java API

首先创建空索引库:

```java
client.admin().indices().prepareCreate("film").execute().actionGet();
```

接下来提交mapping设置：

```java
XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("title").field("type","string").field("store","yes").field("index","not_analyzed").endObject()
                .startObject("publishDate").field("type","date").endObject()
                .startObject("content").field("type","string").endObject()
                .startObject("director").field("type","string").field("index","not_analyzed").endObject()
                .startObject("price").field("type","float").endObject()
                .endObject()
                .endObject();
        PutMappingRequest source = Requests.putMappingRequest("film").type("dongzuo").source(mapping);
        client.admin().indices().putMapping(source).actionGet();
```

上述创建一个电影的索引，类型为动作类，设置动作类的相关属性。创建完index后，可以通过head插件查看具体的索引信息。如下图：

![1542353853588](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542353853588.png)

​	可见，电影名称和导演字段类型为string，且not_analyzed，可用于关键字查询，不将这两个字段进行分词。

默认设置的索引信息如下图，进行聚合时会出现问题，会将string分词分别进行聚合处理。可与上图做对比。

![1542354043216](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542354043216.png)

#### 二、通过手动创建索引

手动创建索引步骤和一相同，首先创建一个空索引库。

![1542354405914](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542354405914.png)



接下来添加索引字段信息，字段内容如和一相同。

![1542355604083](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542355604083.png)

查看索引信息，和通过java API创建的一样，如下图：

![1542355647492](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542355647492.png)

### analyzed和not_analyzed测试对比

分别导入相同数据到设置了not_analyzed的index和默认analyzed的index中，再通过elasticsearch-sql或head插件进行聚合查询。在本次测试找那个，分别导入8条数据到两个index中，具体数据浏览如下图：

<font color=red>film为设置了not_analyzed，film2为默认analyzed</font>

![1542355946103](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542355946103.png)



查询sql语句：

```sql
SELECT title,count(*) FROM film group by title；
```

查询结果如下图：

film的结果：![1542356134052](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542356134052.png)



film2的查询结果：![1542356164422](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542356164422.png)

# es-sql支持的语句

- SQL Statements   (SQL 语句)
  1. select
  2. where
  3. order by
  4. group by
  5. limit  （默认是200条）
- Conditions  （条件查询）
  1. like
  2. and   or
  3. distinct
  4. in
  5. between
  6. now（）
- Basic  aggregations   （基本聚合）
  1. avg（）
  2. count（）
  3. max（）
  4. sum（）
  5. min（）
- SQL  Fields   （SQL字段）
  1. include（‘d*’）  包含所有字段以d开头
  2. exclude（‘age’）   包含所有字段，除了age
  3. include（‘*name’），exclude（'lastname'）  包含所有字段以name结尾，除了lastname。

### 聚合分类：

1. 字段聚合

   ```sql
   SELECT COUNT(*) FROM ACCOUNT GROUP BY gender
   SELECT COUNT(*) FROM ACCOUNT GROUP BY gender,age
   ```

2. 多元聚合

   ```sql
   SELECT * FROM account GROUP BY (gender)，（age）
   SELECT * FROM account GROUP BY (gender, state),(age)
   SELECT * FROM account GROUP BY (gender, state, age),(state),(age)
   ```

   括号内每个字段都有它自己的集合；

   括号中的每个字段列表都是自己的子聚合聚合。

3. 范围聚合

   ```sql
   SELECT COUNT(age) FROM bank GROUP BY range(age, 20,25,30,35,40)
   ```

   提交你规定的范围的字段名。

4. date histogram 聚合

   ```sql
   SELECT online FROM online GROUP BY date_histogram(field='insert_time','interval'='1d','alias'='yourAlias')
   ```

   日期直方图聚合，提交字段名，间隔，alias是可选的。

5. 日期范围聚合

   ```sql
   SELECT online FROM online GROUP BY date_range('alias'='yourAlias',field='insert_time','format'='yyyy-MM-dd' ,'2014-08-18','2014-08-17','now-8d','now-7d','now-6d','now')
   ```

   提交相应的字段和特殊格式化的间隔，alias是可选的。

# es-sql不支持的语句

1. 子查询
2. EXISTS/NOT EXISTS语句
3. UNION/UNION ALL  语句
4. CASE  WHEN   THEN  ELSE
5. contains()函数
6. range（）函数
7. INSERT语句
8. UPDATE语句
9. DELETE语句

![1542621488637](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542621488637.png)







# curl  es

用户手册：https://www.elastic.co/guide

/_cat   提供了一系列查询elasticsearch集群状态的接口。

/_cluster  查询修改集群状态

/_nodes  查询节点状态信息

/_search  查询操作，-XGET  -XPOST	

/_mapping  获取索引内容信息



