package com.practice.util

import java.util

import com.alibaba.fastjson.{JSON, JSONObject}
import com.practice.bean.StartupLog
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Requests
import org.json4s.{DefaultFormats}

import org.json4s.native.Serialization.write


/**
 * @Author HCX
 * @Description //TODO es工具类，获取es sink 实例
 * @Date 17:19 2019-11-20
 * @return
 * @exception
 **/

object MyEsUtil {

  val httpHosts = new util.ArrayList[HttpHost]()
  httpHosts.add(new HttpHost("hadoop1",9200,"http"))
  httpHosts.add(new HttpHost("hadoop2",9200,"http"))
  httpHosts.add(new HttpHost("hadoop3",9200,"http"))

  def getEsSink(indexName:String): ElasticsearchSink[StartupLog]  ={

    //ESFunction，在构造器中使用
    val esFunc: ElasticsearchSinkFunction[StartupLog] = new ElasticsearchSinkFunction[StartupLog] {
      override def process(t: StartupLog, runtimeContext: RuntimeContext, requestIndexer: RequestIndexer): Unit = {
          //导入隐式值
//          implicit val format = Serialization.formats(NoTypeHints)
//          val str: String = write(t)
        implicit val formats = DefaultFormats
        val str: String = write(t)
       val jsonObject: util.Map[String, String] = JSON.parseObject(str,classOf[util.Map[String,String]])
//        val jSONObject: JSONObject = JSON.parseObject(str)
          //增加插入操作，并添加到requestIndexer容器中
          val request: IndexRequest = Requests.indexRequest(indexName).`type`("_doc").source(jsonObject)
          requestIndexer.add(request)
      }

    }
    val sinBuilder: ElasticsearchSink.Builder[StartupLog] = new ElasticsearchSink.Builder[StartupLog](httpHosts,esFunc)
    //设置刷新前缓冲的最大动作量
    sinBuilder.setBulkFlushMaxActions(10)
    //将build生成的es_sink返回
    sinBuilder.build()
  }

}
