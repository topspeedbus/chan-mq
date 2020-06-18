# chan-mq
rabbitmq demo

## 内容
- 集成rabbitmq消息丢失一套流程
- rabbitmq 基于死信队列的延迟实现




#单机版
##单机启动多个实例
	RABBITMQ_NODE_PORT=5672 RABBITMQ_NODENAME=rabbit1 rabbitmq-server -detached
	RABBITMQ_NODE_PORT=5673 RABBITMQ_NODENAME=rabbit2 rabbitmq-server -detached
	RABBITMQ_NODE_PORT=5674 RABBITMQ_NODENAME=rabbit3 rabbitmq-server -detached

> - 不用管： Warning: PID file not written; -detached was passed.
> - 查看运行进程：  ps aux | grep erl

## 停止在Erlang节点上运行的节点2和节点3 RabbitMQ Server 并清空（重置）它们的元数据
	rabbitmqctl -n rabbit1@localhost stop_app
	
	rabbitmqctl -n rabbit2@localhost stop_app
	
	rabbitmqctl -n rabbit1@localhost reset
	
	rabbitmqctl -n rabbit2@localhost reset

## 选出一个磁盘节点
	rabbitmqctl -n rabbit1 join_cluster rabbit@localhost
	rabbitmqctl -n rabbit1@localhost start_app

## 选出一个内存节点
	rabbitmqctl -n rabbit2 join_cluster --ram rabbit@localhost
	rabbitmqctl -n rabbit2@localhost start_app


---
#docker单机版
### Docker常用命令
> - 容器停止：docker stop 容器名称
> - 启动容器：docker start 容器名称
> - 删除容器：docker rm 容器名称
> - 删除镜像：docker rmi 镜像名称
> - 查看运行的所有容器：docker ps
> - 查看所有容器：docker ps -a
> - 容器复制文件到物理机：docker cp 容器名称:容器目录 物理机目录
> - 物理机复制文件到容器：docker cp 物理机目录 容器名称:容器目录

####进入Docker目录
简单的进入Docker容器的方法分为3种：

> 1. 使用attach
> - 使用SSH
> - 使用exec

##启动rabbit

	docker run -d --hostname localhost --name chanrabbit -p 15672:15672 -p 5672:5672 rabbitmq:3.8.5-management

- 参数说明：

> - -d 后台进程运行
> - hostname RabbitMQ主机名称
> - name 容器名称
> - -p port:port 本地端口:容器端口
> - -p 15672:15672 http访问端口
> - -p 5672:5672 amqp访问端口

##安装插件
 - wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.8.0/rabbitmq_delayed_message_exchange-3.8.0.ez
 - 进入容器内：
 > docker exec  -it containerId  /bin/bash
 
- 退出
> exit

- 复制插件到容器内
>  docker cp /root/rabbitmq_delayed_message_exchange-3.8.0.ez chanrabbit:/plugins

- 再次进入容器
- 执行命令让插件生效: 启动延时插件：
> rabbitmq-plugins enable rabbitmq_delayed_message_exchange

##启动多个
	docker run -d --hostname rabbit3 --name chanrabbit2  -p 5674:5672 --link chanrabbit:rabbit1 --link chanrabbit1:rabbit2 -e RABBITMQ_ERLANG_COOKIE='rabbitcookie' rabbitmq:3.8.5-management
	
	docker run -d --hostname rabbit2 --name chanrabbit1 -p 5673:5672 --link chanrabbit:rabbit1 -e RABBITMQ_ERLANG_COOKIE='rabbitcookie' rabbitmq:3.8.5-management

## 加入rabbitM节点到集群
- 设置主节点
> docker exec -it chanrabbit bash  
> rabbitmqctl stop_app  
> rabbitmqctl reset    
> rabbitmqctl start_app  
> exit

- 设置内存节点

> docker exec -it chanrabbit1 bash  
> rabbitmqctl stop_app  
> rabbitmqctl reset  
> rabbitmqctl join_cluster --ram rabbit@rabbit1  
> rabbitmqctl start_app  
> exit


- 设置磁盘节点

> docker exec -it chanrabbit1 bash  
> rabbitmqctl stop_app  
> rabbitmqctl reset  
> rabbitmqctl join_cluster rabbit@rabbit1    
> rabbitmqctl start_app  
> exit