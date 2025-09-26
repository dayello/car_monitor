# 统一网络
设置完成后，各个服务之间就能进行网络通讯。

## 第一步：创建一个公用网络
```shell
docker network create --subnet=192.169.0.0/16 mynetwork
```

## 第二步：docker-compose.yml 指定使用网络： mynetwork
```shell
# 文件开头设置
networks:
  mynetwork:
    external: true
```

## 第三步： 每个服务单独指定网络，设置IP
```shell
services:
  服务名称:
    networks:
      mynetwork:
        ipv4_address: 192.169.20.1
```

## 第四步： 设置统一的 hosts
```shell
# 1. 编辑 public/script/hosts
# 2. 编辑 docker-compose.yml 设置卷
services:
  服务名称:
    volumes:
      - ../../public/script/hosts:/etc/hosts
```

## 服务统一配置项
```shell
    networks:
      mynetwork:
        ipv4_address: 192.169.xxx.xxx
    environment:
      - JAVA_HOME=/opt/jdk
      - PATH=/opt/jdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
    volumes:
      - ../../public/code/jdk-17.0.13+11:/opt/jdk
      - ../../public/script/hosts:/etc/hosts
      - ../../public/download:/data/download
      - ./script:/data/script
```

# public 说明
```shell
public/code/jdk-17.0.13+11  x86 系统
public/code/jdk-11.0.26+4 arm64 系统
```

# services

# 免密登录
## 1. 先在一台容器安装生成私钥和公钥
````shell
# 修改root的密码
echo root:123456 | chpasswd
# 安装openssh
yum install -y openssh-server openssh-clients \
&& yum install -y wget \
&& ssh-keygen -t rsa -N '' -q -f /etc/ssh/ssh_host_rsa_key \
&& ssh-keygen -t ecdsa -N '' -q -f /etc/ssh/ssh_host_ecdsa_key \
&& ssh-keygen -t ed25519 -N '' -q -f /etc/ssh/ssh_host_ed25519_key \
&& /usr/sbin/sshd \
&& ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa \
&& cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
&& chmod 600 ~/.ssh/authorized_keys
# 尝试登录自己这台容器，生成 known_hosts
ssh localhost
````
### 2.保留私钥，公钥等文件到本地
### 3.通过 volumes: 方式，使所有的容器都使用相同的：id_rsa , id_rsa.pub, authorized_keys, known_hosts
### 4.修改 known_hosts， 将所有的容器host和ip， 做修改 