导入数据到mysql过程中遇到的问题

- load导入数据，导出数据

```mysql
#导入本地数据到xxx表中，字段以，分隔，每一行以回车分隔。

load data local infile 'C:/out.txt' into table xxx fields terminated by ',' lines terminated by '\n';

#导出数据到本地,将xxx表的数据导出到out.txt文件中，字段，行分隔
select * into outfile "D:/out.txt" fields terminated by "," lines terminated by "\n" from xxx;
```

- 字段格式化

```mysql
#DATE_FORMAT()函数
DATE_FORMAT(date,format)

eggs:
DATE_FORMAT('960802131400','%Y-%m-%d %T')
output:
1996-08-02 13:14:00
```

| 格式 | 描述                                           |
| ---- | ---------------------------------------------- |
| %a   | 缩写星期名                                     |
| %b   | 缩写月名                                       |
| %c   | 月，数值                                       |
| %D   | 带有英文前缀的月中的天                         |
| %d   | 月的天，数值(00-31)                            |
| %e   | 月的天，数值(0-31)                             |
| %f   | 微秒                                           |
| %H   | 小时 (00-23)                                   |
| %h   | 小时 (01-12)                                   |
| %I   | 小时 (01-12)                                   |
| %i   | 分钟，数值(00-59)                              |
| %j   | 年的天 (001-366)                               |
| %k   | 小时 (0-23)                                    |
| %l   | 小时 (1-12)                                    |
| %M   | 月名                                           |
| %m   | 月，数值(00-12)                                |
| %p   | AM 或 PM                                       |
| %r   | 时间，12-小时（hh:mm:ss AM 或 PM）             |
| %S   | 秒(00-59)                                      |
| %s   | 秒(00-59)                                      |
| %T   | 时间, 24-小时 (hh:mm:ss)                       |
| %U   | 周 (00-53) 星期日是一周的第一天                |
| %u   | 周 (00-53) 星期一是一周的第一天                |
| %V   | 周 (01-53) 星期日是一周的第一天，与 %X 使用    |
| %v   | 周 (01-53) 星期一是一周的第一天，与 %x 使用    |
| %W   | 星期名                                         |
| %w   | 周的天 （0=星期日, 6=星期六）                  |
| %X   | 年，其中的星期日是周的第一天，4 位，与 %V 使用 |
| %x   | 年，其中的星期一是周的第一天，4 位，与 %v 使用 |
| %Y   | 年，4 位                                       |
| %y   | 年，2 位                                       |

- 插入数据乱码问题

```java
jdbc:mysql://127.0.0.1:3306/test?Unicode=true&characterEncoding=utf8
```

- 解决服务时区问题

```java
The server time zone value '�й���׼ʱ��' is unrecognized or represents more than one time zone. You must configure either the server or JDBC driver (via the serverTimezone configuration
```

```java
#在连接字符串后面加上,UTC是统一标准世界时间
?serverTimezone=UTC

#结合字符编码
jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

- 大数据写入或更新失败问题

```
设置max_allowed_packet参数
显示当前的max_allowed_packet参数的值
show VARIABLES like '%max_allowed_packet%'

#在mysql命令行中运行
set global max_allowed_packet=1024*1024*16
#注意：重启mysql服务，该值会还原成默认的初始值
```

