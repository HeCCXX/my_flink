package com.practice.util

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object MyKafkaUtil {

   val properties = new Properties()

   properties.setProperty("bootstrap.servers","hadoop1:9092")
   properties.setProperty("group.id","gmall")

   def getConsumer(topic:String): FlinkKafkaConsumer011[String] = {
      new FlinkKafkaConsumer011[String](topic,new SimpleStringSchema(),properties)
   }
}
