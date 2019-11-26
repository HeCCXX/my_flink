package com.practice.app

import com.alibaba.fastjson.JSON
import com.practice.bean.StartupLog
import com.practice.util.{MyEsUtil, MyJdbcSink, MyKafkaUtil, MyRedisUtil}
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, SplitStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.flink.streaming.connectors.redis.RedisSink
/**
 * @Author HCX
 * @Description //TODO 例程主函数，试验  source、reduce、split、connect、union、sink（kafka、redis）
 * @Date 16:41 2019-11-20
 *
 * @return
 * @exception
 **/
object StartupApp {

  def main(args: Array[String]): Unit = {
    val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    //设置kafka   source 从kafka中读取数据
    val kafkaConsumer: FlinkKafkaConsumer011[String] = MyKafkaUtil.getConsumer("GMALL_STARTUP")
    val dstream: DataStream[String] = environment.addSource(kafkaConsumer)
//    dstream.print()


    //  reduce 分组聚合，会记录上一次的状态，进行累加
    val startuplogDstream: DataStream[StartupLog] = dstream.map(JSON.parseObject(_,classOf[StartupLog]))

    val kvDstream: DataStream[(String, Int)] = startuplogDstream.map(startuplog => (startuplog.ch, 1)).keyBy(0).reduce {
      (key, value) => (key._1, key._2 + value._2)
    }
//    kvDstream.print().setParallelism(1)

    //split 将一个流切分为两个或者多个流   split流后使用select对切分的流进行使用
    val splitDstream: SplitStream[StartupLog] = startuplogDstream.split {
      startuplog =>
        var flags: List[String] = null
        if ("appstore".equals(startuplog.ch)) {
          flags = List(startuplog.ch)
        } else {
          flags = List("other")
        }
        flags
    }
    val appstoreDstream: DataStream[StartupLog] = splitDstream.select("appstore")
    val otherDstream: DataStream[StartupLog] = splitDstream.select("other")
//    appstoreDstream.print().setParallelism(1)
//    otherDstream.print().setParallelism(1)

    //connect  coMap   和union  对两个流进行合并，区别在于union需要对两个类型相同的流进行合并,connect 可以通过coMap中函数调整
    val connDstream: ConnectedStreams[StartupLog, StartupLog] = appstoreDstream.connect(otherDstream)
    val allDstream: DataStream[String] = connDstream.map((log1:StartupLog) => log1.ch,(log2:StartupLog)=> log2.ch)
//    allDstream.print()

    //两个类型一样的流进行合并
    val unionDstream: DataStream[StartupLog] = appstoreDstream.union(otherDstream)
//    unionDstream.print()

    //添加sink  kafka  sink
    val kafkaProducer: FlinkKafkaProducer011[String] = MyKafkaUtil.getProducer("channel_sum")

    val sinkDstream: DataStream[String] = kvDstream.map(chCount => chCount._1 + ":" + chCount._2)
//    sinkDstream.print()
//    sinkDstream.addSink(kafkaProducer)

    // redis  sink
    val redisSink: RedisSink[(String, String)] = MyRedisUtil.getRedisSink()
    val redisSinkDS: DataStream[(String, String)] = kvDstream.map(chcount => (chcount._1, chcount._2.toString))
//    redisSinkDS.print()
//    redisSinkDS.addSink(redisSink)

    val esSink: ElasticsearchSink[StartupLog] = MyEsUtil.getEsSink("test_esink")
//    startuplogDstream.print()
//    startuplogDstream.addSink(esSink)

    //  自定义mysql sink  将数据插入到mysql表中
    val jdbcSink = new MyJdbcSink("insert into hcx_startup values(?,?,?,?,?)")
    startuplogDstream.map(startuplog => Array(startuplog.mid,startuplog.uid,startuplog.ch,startuplog.area,startuplog.ts)).addSink(jdbcSink)


    environment.execute()
  }

}
