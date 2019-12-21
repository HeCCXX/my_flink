# flume  sink  HDFS

组件：flume-1.8、hadoop-2.6.0

目的：flume消费kafka数据，以时间戳的形式创建文件，保存到hdfs

kafka-hdfs.conf配置内容：

```bash
kafka-hdfs.sources = s1
kafka-hdfs.sinks = k1
kafka-hdfs.channels = c1

#设置kafka源
kafka-hdfs.sources.s1.type = org.apache.flume.source.kafka.KafkaSource
#一批次写入通道的消息最大数
kafka-hdfs.sources.s1.batchSize = 10000
#kafka集群
kafka-hdfs.sources.s1.kafka.bootstrap.servers = 172.25.21.4:9099,172.25.21.5:9099,17
2.25.21.6:9099
#订阅的topics，可定义多个
kafka-hdfs.sources.s1.kafka.topics = testFace,testFace2
#消费者组id
kafka-hdfs.sources.s1.kafka.consumer.group.id=test-consumer-group
#设置sink类型
kafka-hdfs.sinks.k1.type = hdfs
#设置为hdfs目录，文件存储位置
kafka-hdfs.sinks.k1.hdfs.path = hdfs://172.25.21.4:8020/home/hcx/flume/%Y%m%d%H

#文件前缀
kafka-hdfs.sinks.k1.hdfs.filePrefix = logs-
kafka-hdfs.sinks.k1.hdfs.round = true
#开启时间上的舍弃，每60分钟创建一个文件夹
kafka-hdfs.sinks.k1.hdfs.roundValue = 60
kafka-hdfs.sinks.k1.hdfs.roundUnit = minute
#使用本地时间戳
kafka-hdfs.sinks.k1.hdfs.useLocalTimeStamp=true
kafka-hdfs.sinks.k1.hdfs.writeFormat = Text
#文件类型
kafka-hdfs.sinks.k1.hdfs.fileType = DataStream
#数据条数10000，将临时文件滚动
kafka-hdfs.sinks.k1.hdfs.rollCount = 10000
#临时文件滚动时间，600秒 |  5分钟
kafka-hdfs.sinks.k1.hdfs.rollInterval = 600
#每批次刷新到hdfs的events数量
kafka-hdfs.sinks.k1.hdfs.batchSize = 10000
#临时文件滚动文件大小
kafka-hdfs.sinks.k1.hdfs.rollSize = 52428800
#sink操作hdfs的线程数
kafka-hdfs.sinks.k1.hdfs.threadsPoolSize = 20


kafka-hdfs.channels.c1.type = memory
kafka-hdfs.channels.c1.capacity = 1000000
kafka-hdfs.channels.c1.transactionCapacity = 10000

kafka-hdfs.sources.s1.channels=c1
kafka-hdfs.sinks.k1.channel =c1
```

# <font color=red size=5>flume error总结：</font>

1、时间戳

```java
java.lang.NullPointerException: Expected timestamp in the Flume event headers, but it was null
        at com.google.common.base.Preconditions.checkNotNull(Preconditions.java:204)
        at org.apache.flume.formatter.output.BucketPath.replaceShorthand(BucketPath.java:228)
        at org.apache.flume.formatter.output.BucketPath.escapeString(BucketPath.java:432)
        at org.apache.flume.sink.hdfs.HDFSEventSink.process(HDFSEventSink.java:380)
        at org.apache.flume.sink.DefaultSinkProcessor.process(DefaultSinkProcessor.java:68)
        at org.apache.flume.SinkRunner$PollingRunner.run(SinkRunner.java:147)
        at java.lang.Thread.run(Thread.java:744)
```

原因没有设置时间戳，添加sink属性，useLocalTimeStamp=true。

2、HDFS权限不够，无法写

```java
org.apache.hadoop.security.AccessControlException: Permission denied: user=kafka, access=WRITE, inode="/test/flume/16-09-19/events-.1474268726127.tmp":hadoop:supergroup:drwxr-xr-x
        at org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker.check(FSPermissionChecker.java:319)
        at org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker.check(FSPermissionChecker.java:292)  
```

HDFS权限不够，要授权。  hadoop  fs  -chmod 777 -R  test/

3、hdfs文件系统错误

```java
java.io.IOException: No FileSystem for scheme: hdfs
        at org.apache.hadoop.fs.FileSystem.getFileSystemClass(FileSystem.java:2644)
        at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2651)
```

把hadoop-hdfs-2.6.0.jar复制到flume/bin目录下

4、HDFS IO  ERROR

检查端口和IP，排查拒绝连接的原因。

# SINKS.HDFS配置说明：

- **channel**
- **type**

hdfs

- **path**

写入hdfs的路径，需要包含文件系统标识，比如：hdfs://namenode/flume/webdata/

可以使用flume提供的日期及%{host}表达式。

- filePrefix

默认值：FlumeData

写入hdfs的文件名前缀，可以使用flume提供的日期及%{host}表达式。

- fileSuffix

写入hdfs的文件名后缀，比如：.lzo .log等。

- inUsePrefix

临时文件的文件名前缀，hdfs sink会先往目标目录中写临时文件，再根据相关规则重命名成最终目标文件；

- inUseSuffix

默认值：.tmp

临时文件的文件名后缀。

- rollInterval

默认值：30

hdfs sink间隔多长将临时文件滚动成最终目标文件，单位：秒；

如果设置成0，则表示不根据时间来滚动文件；

> 注：滚动（roll）指的是，hdfs sink将临时文件重命名成最终目标文件，并新打开一个临时文件来写入数据；

- rollSize

默认值：1024

当临时文件达到该大小（单位：bytes）时，滚动成目标文件；

如果设置成0，则表示不根据临时文件大小来滚动文件；

- rollCount

默认值：10

当events数据达到该数量时候，将临时文件滚动成目标文件；

如果设置成0，则表示不根据events数据来滚动文件；

- idleTimeout

默认值：0
当目前被打开的临时文件在该参数指定的时间（秒）内，没有任何数据写入，则将该临时文件关闭并重命名成目标文件；

- batchSize

默认值：100

每个批次刷新到HDFS上的events数量；

- codeC

文件压缩格式，包括：gzip, bzip2, lzo, lzop, snappy

- fileType

默认值：SequenceFile

文件格式，包括：SequenceFile, DataStream,CompressedStream

当使用DataStream时候，文件不会被压缩，不需要设置hdfs.codeC;

当使用CompressedStream时候，必须设置一个正确的hdfs.codeC值；

- maxOpenFiles

默认值：5000

最大允许打开的HDFS文件数，当打开的文件数达到该值，最早打开的文件将会被关闭；

- minBlockReplicas

默认值：HDFS副本数

写入HDFS文件块的最小副本数。

该参数会影响文件的滚动配置，一般将该参数配置成1，才可以按照配置正确滚动文件。

- writeFormat

写sequence文件的格式。包含：Text, Writable（默认）

- callTimeout

默认值：10000

​       执行HDFS操作的超时时间（单位：毫秒）；

- threadsPoolSize

默认值：10

hdfs sink启动的操作HDFS的线程数。

- rollTimerPoolSize

默认值：1

hdfs sink启动的根据时间滚动文件的线程数。

- kerberosPrincipal

HDFS安全认证kerberos配置；

- kerberosKeytab

HDFS安全认证kerberos配置；

- proxyUser

代理用户

- round

默认值：false

是否启用时间上的”舍弃”，这里的”舍弃”，类似于”四舍五入”，后面再介绍。如果启用，则会影响除了%t的其他所有时间表达式；

- roundValue

默认值：1

时间上进行“舍弃”的值；

- roundUnit

默认值：seconds

时间上进行”舍弃”的单位，包含：second,minute,hour

 

示例：

a1.sinks.k1.hdfs.path = /flume/events/%y-%m-%d/%H%M/%S

a1.sinks.k1.hdfs.round = true

a1.sinks.k1.hdfs.roundValue = 10

a1.sinks.k1.hdfs.roundUnit = minute

当时间为2015-10-16 17:38:59时候，hdfs.path依然会被解析为：

/flume/events/20151016/17:30/00

因为设置的是舍弃10分钟内的时间，因此，该目录每10分钟新生成一个。

- timeZone

默认值：Local Time

时区。

- useLocalTimeStamp

默认值：flase

是否使用当地时间。

- closeTries

默认值：0

hdfs sink关闭文件的尝试次数；

如果设置为1，当一次关闭文件失败后，hdfs sink将不会再次尝试关闭文件，这个未关闭的文件将会一直留在那，并且是打开状态。

设置为0，当一次关闭失败后，hdfs sink会继续尝试下一次关闭，直到成功。

- retryInterval

默认值：180（秒）

hdfs sink尝试关闭文件的时间间隔，如果设置为0，表示不尝试，相当于于将hdfs.closeTries设置成1.

- serializer

默认值：TEXT

序列化类型。其他还有：avro_event或者是实现了EventSerializer.Builder的类名。





# kafka-sink-hdfs



- 创建topic：

```
bin/kafka-topics.sh --zookeeper 172.25.21.4:2189,172.25.21.5:2189,172.25.21.6:2189/kafka --create --replication-factor 2 --partitions 3 --topic testFace3
```

- 删除topic：

```dos
bin/kafka-topics.sh --zookeeper 172.25.21.4:2189,172.25.21.5:2189,172.25.21.6:2189/kafka --delete --topic testFace3
```

- 消费者消费

```
bin/kafka-console-consumer.sh --zookeeper 172.25.21.4:2189,172.25.21.5:2189,172.25.21.6:2189/kafka --topic testFace
```

- 生产者

```
bin/kafka-console-producer.sh --broker-list 172.25.21.4:9099,172.25.21.5:9099,172.25.21.6:9099 --topic testFace
```



# 测试

测试过程kafka-hdfs.conf配置文件内容：（每10分钟创建一个滚动文件夹，数据文件每50M刷新一次）

```bash
kafka-hdfs.sources = s1
kafka-hdfs.sinks = k1
kafka-hdfs.channels = c1

#设置kafka源
kafka-hdfs.sources.s1.type = org.apache.flume.source.kafka.KafkaSource
#一批次写入通道的消息最大数
kafka-hdfs.sources.s1.batchSize = 10000
#kafka集群
kafka-hdfs.sources.s1.kafka.bootstrap.servers = 172.25.21.4:9099,172.25.21.5:9099,17
2.25.21.6:9099
#订阅的topics，可定义多个
kafka-hdfs.sources.s1.kafka.topics = testFace,testFace2
#消费者组id
kafka-hdfs.sources.s1.kafka.consumer.group.id=test-consumer-group
#设置sink类型
kafka-hdfs.sinks.k1.type = hdfs
#设置为hdfs目录，文件存储位置
kafka-hdfs.sinks.k1.hdfs.path = hdfs://172.25.21.4:8020/home/hcx/flume/%Y%m%d%H%M

#文件前缀
kafka-hdfs.sinks.k1.hdfs.filePrefix = logs-
kafka-hdfs.sinks.k1.hdfs.round = true
#开启时间上的舍弃，没10分钟创建一个文件夹
kafka-hdfs.sinks.k1.hdfs.roundValue = 10
kafka-hdfs.sinks.k1.hdfs.roundUnit = minute
#使用本地时间戳
kafka-hdfs.sinks.k1.hdfs.useLocalTimeStamp=true
kafka-hdfs.sinks.k1.hdfs.writeFormat = Text
#文件类型
kafka-hdfs.sinks.k1.hdfs.fileType = DataStream
kafka-hdfs.sinks.k1.hdfs.rollCount = 10000
kafka-hdfs.sinks.k1.hdfs.rollInterval = 600
kafka-hdfs.sinks.k1.hdfs.batchSize = 10000
kafka-hdfs.sinks.k1.hdfs.rollSize = 52428800
kafka-hdfs.sinks.k1.hdfs.threadsPoolSize = 20


kafka-hdfs.channels.c1.type = memory
kafka-hdfs.channels.c1.capacity = 1000000
kafka-hdfs.channels.c1.transactionCapacity = 10000

kafka-hdfs.sources.s1.channels=c1
kafka-hdfs.sinks.k1.channel =c1
```

利用上述flume配置，将数据从kafka消费发送到hdfs指定目录，设置文件类型为DataStream，以Text格式，且每隔10分钟创建一个文件夹用于存放刷入数据。

### 测试1：

在部分设置默认情况下，每个文件写入内容10条，且每隔30秒将临时文件刷新为目标文件。默认情况下，当临时文件大小到达1024 bytes后，将临时文件刷新为目标文件。

### 改进：

在之前的flume配置文件中添加一下内容，规定10000条数据时，将临时文件刷新为目标文件；每隔5分钟刷新一次文件，每批次刷新到HDFS上的events数量改为1000，每个文件设置为50M。

```bash
kafka-hdfs.sinks.k1.hdfs.rollCount = 10000
kafka-hdfs.sinks.k1.hdfs.rollInterval = 300
kafka-hdfs.sinks.k1.hdfs.batchSize = 1000
kafka-hdfs.sinks.k1.hdfs.rollSize = 51200
```

## 异常总结：

<font color=red Size =4>注意：当发送数据量较大，会出现OOM，此时需要调整java_opts的内存设置，防止内存不足引发的异常。</font>

flume启动时的默认最大的堆内存大小是20M，实际环境中数据量较大时，很容易出现OOM问题，在flume的基础配置文件conf下的flume-env.sh中添加

export JAVA_OPTS="-Xms2048m -Xmx2048m -Xss256k -Xmn1g -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-UseGCOverheadLimit"

并且在flume启动脚本flume-ng中，修改JAVA_OPTS="-Xmx20m"为JAVA_OPTS="-Xmx2048m"

将堆内存的阈值跳转到了2G，实际生产环境中可以根据具体的硬件情况作出调整。



<font color=red size=5>注意事项</font>

由于上述调整java_opts，当堆内存的阈值设置过大后，就会出现内存不足，无法运行java环境。日志文件内容如下图，可以通过减少系统内存负载，减少java堆大小等等来解决。可根据实际生产环境来合理的调整。

![1542942932064](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542942932064.png)



另外，当文件系统的磁盘大小可用磁盘很少后，会发生hdfs进入安全模式，namenode无法正常启动。以致于在flume  sink 写入hdfs的时候无法创建写入数据，出现如下图的异常。可以通过清理挂载点日志，清理磁盘内容，释放出足够磁盘后，可重启namenode。

![1542871148034](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542871148034.png)

![1542944024078](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542944024078.png)



### 测试2：

模拟生成车辆数据，发送到kafka，flume消费kafka数据，写入hdfs。规定的是每10分钟滚动一次文件夹，当数据写入文件的大小为50M进行一次刷新目标文件，sink启动的hdfs线程数设置为20。

![1542953105793](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542953105793.png)



![1542944981608](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542944981608.png)

测试结果图如上图，各个目标文件在每个10分钟的文件夹中。

测试过程中，写入速度主要取决于数据写入kafka集群的写入速度，单线程数据写入kafka集群完成数据的hdfs落地，平均完成速度为3500条/分钟。

写入完成后，会有临时文件在设置的时间完成writer  callback 将临时文件重命名目标文件，保存至hdfs。在flume进程INFO信息中可以看到，如下图。

![1542945569061](C:\Users\HCX\AppData\Roaming\Typora\typora-user-images\1542945569061.png)

### 测试3：

模拟数据写入不同的topic中，本次使用2个topic，flume中配置sources消费topic为这两个topic。（测试3中开启两个进程分别向testFace和testFace2这两个topic中写入与测试2中相同数量的数据）

写入HDFS的平均速度为4200条/分钟，当开启两个进程将数据并行写入kafka集群时，在一定程度上加大了数据的生产速度，数据写到hdfs的平均速度有一定的提升。