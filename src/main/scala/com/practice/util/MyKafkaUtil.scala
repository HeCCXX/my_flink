package com.practice.util

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}

/**
 * @Author HCX
 * @Description //TODO kafka工具类，获取生产者和消费者实例
 * @Date 16:42 2019-11-20
 *
 * @return
 * @exception
 **/

object MyKafkaUtil {

   val properties = new Properties()

   properties.setProperty("bootstrap.servers","hadoop1:9092")
   properties.setProperty("group.id","gmall")

   def getConsumer(topic:String): FlinkKafkaConsumer011[String] = {
      new FlinkKafkaConsumer011[String](topic,new SimpleStringSchema(),properties)
   }

   def getProducer(topic:String): FlinkKafkaProducer011[String] ={
      new FlinkKafkaProducer011[String]("hadoop1:9092",topic,new SimpleStringSchema())
   }
}
