package com.practice.app

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment, WindowedStream}
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

import scala.collection.mutable


/**
 * Flink的流式处理中，绝大部分的业务都会使用eventTime，一般只在eventTime无法使用时，才会被迫使用ProcessingTime或者IngestionTime
 * 如果要使用EventTime，那么需要引入EventTime的时间属性，引入方式如下
 * environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
 *
 * watermark
 */
object EventTimeApp {
  def main(args: Array[String]): Unit = {
    val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    environment.setParallelism(1)

    val dstream: DataStream[String] = environment.socketTextStream("hadoop1",7777)

    import org.apache.flink.api.scala._
    val textWithTsDstream: DataStream[(String, Long, Int)] = dstream.map { text =>
      val arr: Array[String] = text.split(" ")
      (arr(0), arr(1).toLong, 1)
    }
    val textEventTimeDstream: DataStream[(String, Long, Int)] = textWithTsDstream.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[(String, Long, Int)](Time.milliseconds(4000)) {
      override def extractTimestamp(t: (String, Long, Int)): Long = {
        return t._2
      }
    })
    val textKeyStream: KeyedStream[(String, Long, Int), Tuple] = textEventTimeDstream.keyBy(0)
    textKeyStream.print("textkey:")

    val windowStream: WindowedStream[(String, Long, Int), Tuple, TimeWindow] = textKeyStream.window(TumblingEventTimeWindows.of(Time.seconds(4)))

    val groupDstream: DataStream[mutable.HashSet[Long]] = windowStream.fold(new mutable.HashSet[Long]()) {
      case (set, (key, ts, count)) =>
        set += ts
    }
    groupDstream.print("window::::").setParallelism(1)

    environment.execute()

  }

}
