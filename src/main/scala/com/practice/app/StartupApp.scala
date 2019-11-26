package com.practice.app

import com.alibaba.fastjson.JSON
import com.practice.bean.StartupLog
import com.practice.util.{MyEsUtil, MyJdbcSink, MyKafkaUtil, MyRedisUtil}
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, KeyedStream, SplitStream, StreamExecutionEnvironment, WindowedStream}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.{GlobalWindow, TimeWindow}
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
//    startuplogDstream.map(startuplog => Array(startuplog.mid,startuplog.uid,startuplog.ch,startuplog.area,startuplog.ts)).addSink(jdbcSink)


    /**
     * TimeWindow   滚动窗口、滑动窗口、会话窗口
     *    滚动窗口：时间对齐，窗口长度固定、没有重叠 ，默认根据 Processing Time进行窗口的划分
     *    滑动窗口：时间对齐，窗口长度固定、有重叠
     *    会话窗口：时间无对齐
     */
    val kStream: KeyedStream[(String, Int), Tuple] = startuplogDstream.map(startuplog => (startuplog.ch,1)).keyBy(0)
    //每10秒统计一次各个渠道的计数
    val windowStream: WindowedStream[(String, Int), Tuple, TimeWindow] = kStream.timeWindow(Time.seconds(10))
    val sumStream: DataStream[(String, Int)] = windowStream.sum(1)
//    sumStream.print()

    //每5秒统计最近10秒的各个渠道的计数
    val windowsSlideStream: WindowedStream[(String, Int), Tuple, TimeWindow] = kStream.timeWindow(Time.seconds(10),Time.seconds(5))
    val slideSumStream: DataStream[(String, Int)] = windowsSlideStream.sum(1)
//    slideSumStream.print()

    //每当某一个key的个数达到2的时候，触发计算，计算最近该key最近10个元素的内容
    val countStream: WindowedStream[(String, Int), Tuple, GlobalWindow] = kStream.countWindow(10,2)
    val countSumStream: DataStream[(String, Int)] = countStream.sum(1)
//    countSumStream.print()

    /**
     * Flink的流式处理中，绝大部分的业务都会使用eventTime，一般只在eventTime无法使用时，才会被迫使用ProcessingTime或者IngestionTime
     * 如果要使用EventTime，那么需要引入EventTime的时间属性，引入方式如下
     * environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
     *
     * watermark
     */

    //实例见EventTimeApp.scala


    environment.execute()
  }

}
