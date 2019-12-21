# 长时间 FGC 问题分析

问题定位：应该是：

bulk 队列暴增问题

netty 内存池累积问题

修改方式：

- bulk 队列不要过大，他们占用的内存会成为 GC 的负担
- 单个 bulk 请求体的数据量不要太大，官方建议大约5-15mb
- 写入端的 bulk 请求超时需要足够长，建议60s 以上
- 写入端尽量将数据轮询打到不同节点

具体修改配置文件中内容：

1. 调整bulk队列大小：（按情况适当调小）

threadpool.bulk.queue_size: 3000  

  2.调整buffer缓冲 （可以调整到30%）

indices.memory.index_buffer_size: 20%                   # 每个节点写入索引数据时可以使用的内存buffer缓冲

3.索引刷新时间  （可按情况调整）

index.refresh_interval: 30s    