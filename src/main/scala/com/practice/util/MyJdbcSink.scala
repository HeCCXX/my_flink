package com.practice.util


import java.sql.{Connection, PreparedStatement}
import java.util.Properties

import com.alibaba.druid.pool.DruidDataSourceFactory
import javax.sql.DataSource
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
/**
 * @Author HCX
 * @Description //TODO 自定义mysql  jdbc sink
 * @Date 10:39 2019-11-26
 *
 * @return
 * @exception
 **/

class MyJdbcSink(sql:String) extends RichSinkFunction[Array[Any]]{

  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://hadoop1:3306/gmall?useSSL=false"
  val username = "root"
  val password = "123456"
  val maxActive = "20"
  var connection: Connection = null

  //连接数据库
  override def open(parameters: Configuration): Unit = {
    val properties = new Properties()
    properties.put("driverClassName",driver)
    properties.put("url",url)
    properties.put("username",username)
    properties.put("password",password)
    properties.put("maxActive",maxActive)
    val datasource: DataSource = DruidDataSourceFactory.createDataSource(properties)
    connection = datasource.getConnection()
  }

  //反复调用
  override def invoke(value: Array[Any]): Unit = {
    val ps: PreparedStatement = connection.prepareStatement(sql)
    println(value.mkString(","))
    for (i <- 0 until value.length) {
      ps.setObject(i+1,value(i))
    }
    ps.executeUpdate()
  }

  override def close(): Unit = {
    if (connection != null){
      connection.close()
    }
  }
}
