package com.hcx

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}

/**
 * @Author HCX
 * @Description //TODO 离线处理的词频统计例程
 * @Date 16:38 2019-11-20
 *
 * @return
 * @exception
 **/
object WCAppBatch {
  def main(args: Array[String]): Unit = {
    //1 env
    //2 source
    //3 transform
    //4 sink
    val tool: ParameterTool = ParameterTool.fromArgs(args)

    val input: String = tool.get("input")
    val output: String = tool.get("output")
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //读取离线文件内容
    val ds: DataSet[String] = env.readTextFile(input)
    import org.apache.flink.api.scala._
    val aggDS: AggregateDataSet[(String, Int)] = ds.flatMap(_.split(" ")).map((_,1)).groupBy(0).sum(1)
//    aggDS.print()
    aggDS.writeAsText(output)
    env.execute()
  }
}
