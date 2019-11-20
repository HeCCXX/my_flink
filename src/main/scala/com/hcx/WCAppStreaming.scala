package com.hcx

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object WCAppStreaming {
  def main(args: Array[String]): Unit = {
      val tool: ParameterTool = ParameterTool.fromArgs(args)

    val input: String = tool.get("input")
    val port: Int = tool.get("output").toInt


      val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val ds: DataStream[String] = env.socketTextStream("hadoop1",7777)
    import org.apache.flink.api.scala._
    val Dstream: DataStream[(String, Int)] = ds.flatMap(_.split(" ")).filter(_.nonEmpty).map((_,1)).keyBy(0).sum(1)


    Dstream.print()

    env.execute()

  }

}
