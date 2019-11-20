package com.pracitice

import java.util.Properties

object MyKafkaUtil {

   val properties = new Properties()

  properties.setProperty("bootstrap.servers","hadoop1:9092")

}
