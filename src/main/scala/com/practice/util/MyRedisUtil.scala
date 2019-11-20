package com.practice.util

import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

/**
 * @Author HCX
 * @Description //TODO redis工具类，实现redis实例的获取
 * @Date 16:42 2019-11-20
 *
 * @return
 * @exception
 **/

object MyRedisUtil {

  private val config: FlinkJedisPoolConfig = new FlinkJedisPoolConfig.Builder().setHost("hadoop1").setPort(6379).build()

  def getRedisSink(): RedisSink[(String,String)] ={
    new RedisSink[(String,String)](config,new MyRedisMapper)

  }

  class MyRedisMapper extends RedisMapper[(String,String)]{
    override def getCommandDescription: RedisCommandDescription = {
      new RedisCommandDescription(RedisCommand.HSET,"channel_count")
    }

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
