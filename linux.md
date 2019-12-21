- parted -l  查看挂载和未挂载的硬盘

- kill -l  PID   以用户结束的方式杀死进程，但不一定成功，要考虑父进程和子进程，如果只杀死父进程，子进程变成僵尸进程，如果僵尸进程的父进程是init，则需要重启，清空进程表。（杀死init进程意味着关闭系统）

- fdisk -l 查看磁盘挂载情况

- mkfs.ext4  /dev/xxx  将xxx硬盘格式化为ext4文件系统

- mount -t ext4 -o rw /dev/xxx  /data   将xxx以ext4文件系统类型，读写权限挂载到data目录下。

- awk、sed、grep

  awk以列格式化文本

  awk '{print $1,$4}' test.log     取test.log文件中以空格分割列，取第一列和第四列。

  awk -F， '{print $1,$2}' test.log  使用，做分割，取第一列和第二列。

  awk -F ‘[ ,] ’ '{print $1,$2}' test.log   多个分隔符，先使用空格分隔，再使用，分隔，取第一列和第二列。

  awk -v a=1 'print $1,$1+a' test.log  将test..log中以空格分隔，取第一列+a，a的值为1。

​	awk -v a=\\"   'print a""$1""a' test.log  字符串拼接，拼接部分使用“”,两个双引号。

​	

​	grep 查找文件符合条件的字符串

​	grep -r nmask /etc/  查找/etc目录下包含nmask的文件

​	grep -v test test.log  过滤test.log中有test的字符串。



​	sed  (实际不改变文件内容，可以重定向到新文件)

​	sed -e 4a\hcx test.log  在第4行后插入一行，内容为hcx。

#### 参数

-e 以选项中指定的script来处理输入的文本文件。
-f 以选项中指定的script文件来处理输入的文本文件。
-h 显示帮助。
-n 仅显示script处理后的结果。
-V 显示版本信息。

#### 动作

a ：新增， a 的后面可以接字串，而这些字串会在下一行出现
i ：插入， i 的后面可以接字串，而这些字串会在上一行出现
c ：取代， c 的后面可以接字串，这些字串可以取代 n1,n2 之间的行
d ：删除
s ：取代，通常这个s的动作可以搭配正规表示法！如 s/old/new/g	



# crontab

文件格式

分 时 日 月 星期 要运行的命令

- 第1列分钟0～59
- 第2列小时0～23（0表示子夜）
- 第3列日1～31
- 第4列月1～12
- 第5列星期0～7（0和7表示星期天）
- 第6列要运行的命令



# 远程拷贝

scp  

-r  递归复制

```bash
scp localfile remoteuser@remote_ip:remote_file
scp localfile remote_ip:remote_file
```

rsync(只复制改变的文件)

-r  递归   -v  显示复制过程    -l   拷贝符号链接

```
rsync -rvl localfile remote_user@remote_ip:remote_file
```



管道

```shell
find t2/ -name '*.log' | xargs -I '{}' mv {} t1/

find t2/ -name '*.log' -exec mv {} t1/ \;
```





后台运行

```shell
nohup bin/elasticsearch > /dev/null 2>&1 &
```

\>dev/null  将输出重定向到null文件

2>&1  将标准出错重定向到标准输出

&  让该命令在后台执行



git

```shell
#将该文件夹初始化为版本库
git init   

#将readme.txt文件添加到仓库暂存区
git add readme.txt

#将readme.txt文件提交到版本库,并添加描述
git commit -m "this is first"

#查看当前文件夹中文件状态
git status

#多次提交后，查看提交log
git log

#回退版本,xxx为log中的commit id 前几位
git reset --hard xxx

#查看版本回退的head操作
git reflog

#校验这次修改，回退到上一次add或commit的状态
git checkout -- readme.txt

#删除文件,该文件提交或add过，可以通过checkout来恢复
git rm test.txt

#创建分支 branch   创建dev分支
git checkout -b dev

#合并某分支和当前分支
git merge dev

#删除分支
git branch -d dev

#将工作分支隐藏，操作另外一条分支的bug等等
git stash

#恢复隐藏分支
git stash apply      git  stash  drop
git stash pop   相当于上面两行

#打上标签
git tag xxx

#标签指定内容,加上说明文字
git tag -a v0.1 -m "version 0.1 released"  1094ab

#删除标签
git tag -d v0.1
```



远程仓库

设置免密登录，之后在github上设置rsa_pub；

添加远程仓库

```shell
git remote add origin https://github.com/HeCCXX/gitTest.git
```

推送内容到远程仓库

```shell
git push -u origin master	 
```

第一次加上 -u，还会将本地与远程仓库关联起来。

克隆远程仓库内容到本地

```shell
git clone git@github.com:HeCCXX/test
```

