## 系统简介

VNDais 作业管理系统，用户类型为管理员、教师和学生，提供的功能也是围绕用户展开。



## 系统开发技术介绍

### 前端

- 飞冰(ICE)
- React
- React-router 4.X



### 后端

- Spring

- Spring MVC

- Mybatis

- MySql

- Maven

- Tomcat

- 其他

  - 参数合法性进行验证

    - Guava Preconditions

    结合 Guava Preconditions 自定义参数校验断言

  - 数据库连接池

    - Druid 

  - 日志组件

    - logback

  - 异常统一处理

    - 自定义 Exception
    - 全局 ExceptionHandler

  - 缓存

    - Redis

  - 单元测试框架

    - JUnit

  - 认证

    - JWT

  - API 规范

    - RESTful API 规范

- 完善

  - Mybatis 逆向工程

  - Shiro

  - 动态权限控制

    RBAC（Role-Based Access Control ）基于角色的访问控制

  - 整合富文本编辑器

  - 多模块构建【可选】

  - Nginx 负载均衡 + Tomcat 集群

- 优化

  - 慢 SQL 优化
  - 清洗脏数据

- ​



## 代码规范

**参考《阿里巴巴 Java 开发手册》**

### Links

[Java开发异常及日志](https://blog.csdn.net/CSDN_G_Y/article/details/78064937)



### 异常处理

- 返回给用户的异常信息能帮助定位问题才返回，不能就直接服务端记录日志，返回让用户稍后重试等信息
  - 返回给用户相关信息，通过提示信息，用户能自主解决问题
  - 返回给用户的处理过的异常信息（包含自定义状态码+服务端出错），服务端业务逻辑出错，通过用户得到的自定义状态码，能快速定位问题
- 执行或调用结果可能出现 null 的结果都必须先对结果进行非 null 判断再做处理
- try-catch 只针对不稳定代码（可能出现异常），而且尽可能区分异常类型进行相应的处理
- service cath 相关异常记录日志，然后再抛出自定义的 ServiceException（code，errorMessage）
- 注意事务回滚
- 不能在 finally 块中使用 return
- 捕获异常与抛异常，必须是完全匹配，或者捕获异常是抛异常的父类
- 返回 null 的情况必须要做说明
- 空指针异常的情况
  - 返回类型为基本数据类型，return包装数据类型的对象时，自动拆箱有可能产生NPE
  - 数据库的查询结果可能为null
  - 集合里的元素即使isNotEmpty，取出的数据元素也可能为null
  - 远程调用返回对象时，一律要求进行空指针判断，防止NPE
  - 对于Session中获取的数据，建议NPE检查，避免空指针
  - 级联调用obj.getA().getB().getC()；一连串调用，易产生NPE
- 定义时区分unchecked/ checked异常，避免直接抛出new RuntimeException()，更不允许抛出Exception或者Throwable，应使用有业务含义的自定义异常
- 如果 service 需要作为二方或三方库的话，就需要自定义 ServiceResult （封装isSuccess()方法、“错误码”、“错误简短信息）



### 日志处理

- 应用中不可直接使用日志系统（Log4j、Logback）中的API，而应依赖使用日志框架SLF4J中的API，使用门面模式的日志框架，有利于维护和各个类的日志处理方式统一

  ```
  import org.slf4j.Logger; 
  import org.slf4j.LoggerFactory;
  public class Abc {
  	private static final Logger logger = LoggerFactory.getLogger(Abc.class);  
  }
  ```

- 日志文件推荐至少保存15天，因为有些异常具备以“周”为频次发生的特点

- 记录日志采用占位符，代替字符串拼接

  ```
  logger.info("{},it's OK.","Hi");

  // 日志结果
  Hi,it's OK.
  ```

- 避免重复打印日志，浪费磁盘空间

  ```
  // log4j
  在log4j.xml中设置additivity=false。
  正例：<logger name="com.taobao.dubbo.config" additivity="false">

  // logback
  <root level="INFO" additivity="false">
    <appender-ref ref="DAYFILE"/>
  </root>
  ```



### 数据库

- 表达是与否概念的字段，必须使用is_xxx的方式命名，数据类型是unsignedtinyint（1表示是，0表示否）

- 表名、字段名必须使用小写字母或数字，禁止出现数字开头，禁止两个下划线中间只出现数字。数据库字段名的修改代价很大，因为无法进行预发布，所以字段名称需要慎重考虑

- 表名不使用复数名词

- 禁用保留字

- 主键索引名为pk_字段名；唯一索引名为uk_字段名；普通索引名则为idx_字段名

- 小数类型为decimal，禁止使用float和double

- 如果存储的字符串长度几乎相等，使用char定长字符串类型

- varchar是可变长字符串，不预先分配存储空间，长度不要超过5000，如果存储长度大于此值，定义字段类型为text，独立出来一张表，用主键来对应，避免影响其它字段索引效率

- 表必备三字段：id, gmt_create, gmt_modified

- 数据库名与应用名称尽量一致

- 如果修改字段含义或对字段表示的状态追加时，需要及时更新字段注释

- 字段允许适当冗余，以提高查询性能，但必须考虑数据一致

  冗余字段要求: 1、不是频繁修改的字段 2、不是varchar超长字段，更不能是text字段

- 单表行数超过500万行或者单表容量超过2GB，才推荐进行分库分表

- 合适的字符存储长度，不但节约数据库表空间、节约索引存储，更重要的是提升检索速度





## 后端开发过程

### 搭建开发环境

- get编辑器 IDEA 激活

  [link](https://sales.jetbrains.com/hc/zh-cn/articles/207154369-%E5%AD%A6%E7%94%9F%E6%8E%88%E6%9D%83%E7%94%B3%E8%AF%B7%E6%96%B9%E5%BC%8F)

- 安装 jdk 配置 java 环境

  - 1.7
  - 1.8

- tomcat【8.0】

  [教程 link](https://jingyan.baidu.com/article/a3761b2bf2ee681577f9aa42.html)

- mysql【5.7】

  [教程 link](https://jingyan.baidu.com/article/c1a3101e72fc9bde656debf7.html)

  ```
  1.Windows下

  启动服务
  mysqld --console　　
  或　　net start mysql　　
  关闭服务
  mysqladmin -uroot shudown　　
  或　　net stop mysql　　
   

  2.Linux下

  启动服务
  service mysql start　　　
  关闭服务
  service mysql stop　　
  重启服务
  service restart stop　
  ```

- maven【3.3.3】

  [link](https://jingyan.baidu.com/article/acf728fd68b4bef8e510a31c.html)



### 新建项目

打开idea ---> File ---> new ---> project ---> maven ---> create from archetype ---> maven-archetype-webapp

```
<groupId>com.menglin</groupId>
<artifactId>VNDais</artifactId>
```



### 搭建项目源码代码结构

```
├── java
│   └── com
│       └── menglin
│           ├── aop
│           │   └── xxx.java
│           ├── bo
│           │   └── xxx.java
│           ├── common
│           │   └── xxx.java
│           ├── controller
│           │   └── xxxController.java
│           ├── dao
│           │   └── TableNameDao.java
│           ├── dto
│           │   └── TableNameDto.java
│           ├── entity
│           │   └── TableName.java
│           ├── enum
│           │   └── xxxEnum.java
│           ├── exception
│           │   └── xxxException.java
│           ├── filter
│           │   └── xxxEnum.java
│           ├── redis
│           │   └── xxxEnum.java
│           ├── service
│           │   ├── xxxService.java
│           │   └── impl
│           │       └── xxxServiceImpl.java
│           └── util
│               └── xxxUtil.java
├── resources
│   ├── jdbc-druid-config.properties
│   ├── generatorConfig.xml
│   ├── logback.xml 
│   ├── mybatis-config.xml
│   ├── sqlMappers
│   │   └── TableMapper.xml
│   └── spring
│       ├── spring-context.xml
│       ├── spring-dao.xml
│       ├── spring-service.xml
│       └── spring-web.xml
└── webapp
    ├── index.jsp
    └── WEB-INF
        ├── dist
        │   └── index.html
        └── web.xml
```



### 配置 maven

- pom.xml

  设置项目基本信息并添加相关依赖

- settings.xml 的配置【可选】

  [link](http://www.cnblogs.com/jingmoxukong/p/6050172.html?utm_source=gold_browser_extension#settings.xml%E6%9C%89%E4%BB%80%E4%B9%88%E7%94%A8%EF%BC%9F)

```
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.menglin</groupId>
  <artifactId>VNDais</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>war</packaging>

  <name>VNDais Maven Webapp</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.7</maven.compiler.source>
    <maven.compiler.target>1.7</maven.compiler.target>
  </properties>
    <dependencies>
        <!-- 单元测试 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <!-- HTML 视图适配器依赖 -->
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>2.3.20</version>
        </dependency>

        <!-- 日志 -->
        <!-- 实现slf4j接口并整合 -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.1.1</version>
        </dependency>

        <!-- 数据库 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.37</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>c3p0</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.1.2</version>
        </dependency>

        <!-- MyBatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.3.0</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.2.3</version>
        </dependency>

        <!-- Servlet web -->
        <dependency>
            <groupId>taglibs</groupId>
            <artifactId>standard</artifactId>
            <version>1.1.2</version>
        </dependency>
        <dependency>
            <groupId>jstl</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.5.4</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
        </dependency>

        <!-- Spring -->
        <!-- 1.Spring核心 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <!-- 2.Spring DAO层 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <!-- 3.Spring web -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>4.1.7.RELEASE</version>
        </dependency>
        <!-- 4.Spring AOP -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>4.2.6.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>aopalliance</groupId>
            <artifactId>aopalliance</artifactId>
            <version>1.0</version>
        </dependency>
        <!--获取切面指向的方法相关信息-->
        <dependency>
            <groupId>org.apache.geronimo.bundles</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.6.8_2</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.8.9</version>
        </dependency>

        <!-- 数据库连接池druid -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.0.31</version>
        </dependency>

        <!-- 序列化fastjson -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.20</version>
        </dependency>

        <!-- spring-redis实现 -->
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-redis</artifactId>
            <version>1.6.2.RELEASE</version>
        </dependency>

        <!-- redis客户端jar -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.8.0</version>
        </dependency>

        <!-- Ehcache实现,用于参考 -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-ehcache</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- 分页插件 -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>4.0.0</version>
        </dependency>

        <!-- thymeleaf模版 -->
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring4</artifactId>
            <version>3.0.5.RELEASE</version>
        </dependency>

        <!-- Google Guava 工具类 -->
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
        </dependency>

        <!--好用的时间工具类-->
        <dependency>
            <groupId>joda-time</groupId>
            <artifactId>joda-time</artifactId>
            <version>2.9.3</version>
        </dependency>

        <!-- jwt -->
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>3.2.0</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>BookSystem_V3</finalName>
        <plugins>
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.3.2</version>
                <dependencies>
                    <dependency>
                        <groupId>mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                        <version>5.1.5</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <overwrite>true</overwrite>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



### 配置 DAO 层

添加 jdbc.properties

配置 mybatis-config.xml

配置 spring-dao.xml



### 配置 Service 层

配置 spring-service.xml



### 配置 Web 层

配置 spring-web.xml



### 配置 web.xml



### 其他工具配置

generatorConfig.xml



### 创建数据库和表



### 添加 entity、dao、table_sqlMap.xml

**使用 Mybatis 逆向工程**

**手动创建相关文件**

如: User.java、UserDao.java、user_sqlMap.xml



### 添加 dao 测试代码

新建 BaseTest.java

添加 tableNameDaoTest.java



### 添加 enums、exception、service

添加 ErrorStateEnum.java

添加 serviceException.java

添加 tableNameService.java、tableNameServiceImpl.java



### 添加 service 测试代码

添加 tableNameServiceTest.java



### 添加 ActionResult.java、controller

添加 XXXController.java



### 添加 controller 测试代码

添加 XXXControllerTest.java



### 添加并设置运行环境

本地服务器 tomcat + jrebel 热部署(可选)

mybatis 逆向工程



### 开发功能



### 注意

- dao 接口的方法中当参数大于或等于两个时，需要对参数进行注解

  ```
  int insertAppointment(@Param("bookId") long bookId, @Param("studentId") long studentId);

  // sqlMap.xml
  ```

- controller 和 serviceImpl 记得添加相应的注解 @controller 和 @service

- MyBatis + MySQL返回 insert / update 成功后的主键 id

  [link](http://www.cnblogs.com/han-1034683568/p/8305122.html)

- redis 缓存的类要实现序列化类 Serializable

  ```
  public class Task implements Serializable {
  	//...
  }
  ```

- 数据库写操作【涉及缓存更新策略】

  - 插入新纪录
    - 是否要求保证记录某些字段的唯一性，如需要则需要先校验记录的存在性（比如学生学号）
    - 新建一条记录，将必须参数设置为属性值
    - 调用数据库插入，并在插入后要获取相应的自增主键作为缓存 key 的一部分
    - 将新纪录添加到缓存
  - 修改记录
    - 先根据 id  查询到要修改的记录
    - 将相关要修改的参数设置为属性值
    - 修改数据库记录
    - 更新缓存记录
  - 删除记录
    - 删除缓存
    - 删除记录

- ​



## 相关配置文件

### pom.xml【Maven】

[build 标签配置1](https://blog.csdn.net/taiyangdao/article/details/52374125)

[build 标签配置2](https://www.cnblogs.com/whx7762/p/7911890.html)

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!--所有的 Maven 项目都必须配置这四个配置项-->
    <modelVersion>4.0.0</modelVersion>
    <!--groupId 指的是项目名的项目组，默认就是包名-->
    <groupId>com.wupengju.hello</groupId>
    <!--artifactId 指的是项目中的某一个模块，默认命名方式是"项目名-模块名"-->
    <artifactId>hello-web</artifactId>
    <!--version指的是版本，这里使用的是Maven的快照版本-->
    <version>SNAPSHOT-0.0.1</version>
    <!--可选配置-->
    <packaging>war</packaging>
    <name>hello Maven web</name>
    <url>http://maven.apache.org</url>
    
    <!--依赖配置-->
    <!--groupId、version、artifactId 能唯一定位到一个依赖包，scope 可选值:【compile-编译】、【test-测试】、【provided-由容器提供】、【runtime-运行时】-->
    <dependencies>
      <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>3.8.1</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.1.1</version>
      </dependency>
    </dependencies>
    
    <!--编译设置-->
    <build>
      <!--可选，未指定使用默认值，该配置相当于执行 mvn install-->
      <defaultGoal>install</defaultGoal>  
      <!--可选，未指定使用默认值，目标文件的存放目录，默认在 ${basedir}/target 目录-->
      <directory>${basedir}/target</directory>  
      <!--目标文件的名称，默认情况为 ${artifactId}-${version} -->
      <finalName>hello</finalName>
      <plugins>
        <plugin>
          <groupId>org.mybatis.generator</groupId>
          <artifactId>mybatis-generator-maven-plugin</artifactId>
          <version>1.3.2</version>
          <dependencies>
            <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>5.1.5</version>
            </dependency>
          </dependencies>
          <configuration>
            <overwrite>true</overwrite>
          </configuration>
        </plugin>
      </plugins>
    </build>
    
</project>
```



### mybatis-config.xml【Mybatis】

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <!-- 配置全局属性 -->
  <settings>
    <!-- 使用jdbc的getGeneratedKeys获取数据库自增主键值 -->
    <setting name="useGeneratedKeys" value="true" />
    <!-- 使用列别名替换列名 默认:true -->
    <setting name="useColumnLabel" value="true" />
    <!-- 开启驼峰命名转换:Table{create_time} -> Entity{createTime} -->
    <setting name="mapUnderscoreToCamelCase" value="true" />
  </settings>
  <typeAliases>
  	<package name="com.hisen.bean.entity"/>
  </typeAliases>
</configuration>
```



### spring-context.xml【Spring】

扫描系统其它依赖

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <!-- 扫描全局异常处理 -->
    <context:component-scan base-package="com.menglin.exception"/>
    <!-- 扫描 Redis  -->
    <context:component-scan base-package="com.menglin.redis"/>

</beans>
```



### spring-dao.xml【Spring】

```
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:p="http://www.springframework.org/schema/p"
  xmlns:tx="http://www.springframework.org/schema/tx"
  xmlns:context="http://www.springframework.org/schema/context"
  xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/tx
    http://www.springframework.org/schema/tx/spring-tx.xsd
    http://www.springframework.org/schema/context 
    http://www.springframework.org/schema/context/spring-context.xsd">
    
  <!-- 配置数据源 -->
  <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name=“url" value="jdbc:mysql://xx.xx.xx.xx:xx/db"/>                
    <!-- 改为你的地址即可 -->
    <property name="username" value="xxxx"/>
    <property name="password" value="xxxx"/>
  </bean>
  
  <!-- 配置事务管理 -->
  <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
  	<property name="dataSource" ref="dataSource"/>
  </bean>

  <!-- 配置 mybatis 的 sqlSessionFactory 对象 -->
  <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <!-- 注入数据源 -->
    <property name="dataSource" ref="dataSource"/>
    <!-- 自动扫描mappers包下的所有xml文件 -->
    <property name="mapperLocations" value="classpath:/mappers/*.xml"></property>
    <!-- mybatis 配置文件 -->
    <property name="configLocation" value="classpath:mybatis-config.xml"></property>
  </bean>

  <!-- 配置扫描 Dao 接口包，动态实现 Dao 接口，注入到 spring 容器中 -->
  <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <!-- 注入sqlSessionFactory -->
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    <!-- 给出需要扫描 Dao 接口包 -->
    <property name="basePackage" value="com.hisen.dao"/>
  </bean>

</beans>
```



### spring-service.xml【Spring】

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:context="http://www.springframework.org/schema/context"
  xmlns:tx="http://www.springframework.org/schema/tx"
  xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context.xsd
    http://www.springframework.org/schema/tx
    http://www.springframework.org/schema/tx/spring-tx.xsd">
    
  <!-- 扫描service包下所有使用注解的类型 -->
  <context:component-scan base-package="com.hisen.service" />

  <!-- 配置事务管理器 -->
  <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <!-- 注入数据源 -->
    <property name="dataSource" ref="dataSource" />
  </bean>

  <!-- 配置基于注解的声明式事务 -->
  <tx:annotation-driven transaction-manager="transactionManager" />
    
</beans>
```



### spring-web.xml【SpringMVC】

```
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:context="http://www.springframework.org/schema/context"
  xmlns:mvc="http://www.springframework.org/schema/mvc"
  xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context.xsd
    http://www.springframework.org/schema/mvc
    http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd">
    
  <!-- 视图解析器，配置HTML 显示ViewResolver -->
  <bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
    <property name="templateLoaderPath">
      <value>/WEB-INF/app/html</value>
    </property>
  </bean>
  <bean id="htmlviewResolver" class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">
    <property name="suffix" value=".html" />
    <property name="contentType" value="text/html;charset=UTF-8"></property>
    <property name="order" value="0"></property>
  </bean>

  <!-- 视图解析器，配置jsp 显示ViewResolver -->
  <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/WEB-INF/jsp/" />
    <property name="suffix" value=".jsp" />
    <!-- 不能用jstl的那个 -->
    <property name="viewClass" value="org.springframework.web.servlet.view.InternalResourceView"/> 
    <property name="contentType" value="text/html;charset=UTF-8"/>
    <property name="order" value="0"></property>
  </bean>

  <!-- 扫描 web 相关的bean，将控制器纳入Spring的管理 -->
  <context:component-scan base-package="com.hisen.controller" />
    
</beans>
```



### logback.xml【logback】

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds">

    <!-- 应用名称 -->
    <property name="APP_NAME" value="VNDais"/>
    <!-- 日志文件的保存路径 -->
    <property name="LOG_HOME" value="F:/Server/VNDais/logs/run"/>
    <!-- 文件日志的保存路径 -->
    <property name="FILE_LOG_HOME" value="${LOG_HOME}/file"/>
    <!-- 错误日志的保存路径 -->
    <property name="ERROR_FILE_LOG_HOME" value="${LOG_HOME}/error"/>
    <!-- 独立同步日志的保存路径 -->
    <property name="SYNC_FILE_LOG_HOME" value="${LOG_HOME}/sync"/>
    <!-- 日志输出格式 -->
    <property name="ENCODER_PATTERN"
              value="[ %-5level ] [ %date{yyyy-MM-dd HH:mm:ss.SSS} ] [ %thread ] %logger{96} [%line] - %msg%n"/>

    <!-- 控制台日志：输出全部日志到控制台 -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>${ENCODER_PATTERN}</Pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 文件日志：输出全部日志到文件 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily 配置日志所生成的目录以及生成文件名的规则 -->
            <fileNamePattern>${FILE_LOG_HOME}/${APP_NAME}.%d.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>

        <!--日志文件最大的大小-->
        <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
            <MaxFileSize>64MB</MaxFileSize>
        </triggeringPolicy>

        <prudent>false</prudent>

        <!-- encoder defaults to ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${ENCODER_PATTERN}</pattern>
            <charset>UTF-8</charset> <!-- 此处设置字符集 -->
        </encoder>
    </appender>

    <!-- 错误日志：输出全部错误日志到文件 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily 配置日志所生成的目录以及生成文件名的规则 -->
            <fileNamePattern>${ERROR_FILE_LOG_HOME}/${APP_NAME}.%d.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>

        <!--日志文件最大的大小-->
        <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
            <MaxFileSize>64MB</MaxFileSize>
        </triggeringPolicy>

        <prudent>false</prudent>

        <!-- encoder defaults to ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${ENCODER_PATTERN}</pattern>
            <charset>UTF-8</charset> <!-- 此处设置字符集 -->
        </encoder>

        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
    </appender>

    <!-- 同步日志：输出同步日志到文件 -->
    <appender name="SYNC_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily 配置日志所生成的目录以及生成文件名的规则 -->
            <fileNamePattern>${SYNC_FILE_LOG_HOME}/${APP_NAME}.%d.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>

        <!--日志文件最大的大小-->
        <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
            <MaxFileSize>64MB</MaxFileSize>
        </triggeringPolicy>

        <prudent>false</prudent>

        <!-- encoder defaults to ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${ENCODER_PATTERN}</pattern>
            <charset>UTF-8</charset> <!-- 此处设置字符集 -->
        </encoder>
    </appender>


    <!-- additivity 属性表示是否允许重复日志 -->
    <root level="INFO" additivity="false">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>

    <!-- 同步日志 -->
    <logger name="log.sync" level="DEBUG" addtivity="true">
        <appender-ref ref="SYNC_FILE"/>
    </logger>

    <!--mybatis log configure-->
    <logger name="com.apache.mybatis" level="TRACE"/>
    <logger name="java.sql.Connection" level="DEBUG"/>
    <logger name="java.sql.Statement" level="DEBUG"/>
    <logger name="java.sql.PreparedStatement" level="DEBUG"/>

</configuration>
```



### tableName_sqlMap.xml【Mybatis】

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- namespace 为对应的 Dao 类 -->
<mapper namespace="com.hisen.dao.UserInfoMapper">

<resultMap id="BaseResultMap" type="com.hisen.bean.entity.UserInfo">
  <id column="id" jdbcType="INTEGER" property="id" />
  <result column="name" jdbcType="VARCHAR" property="name" />
  <result column="gender" jdbcType="INTEGER" property="gender" />
  <result column="age" jdbcType="INTEGER" property="age" />
  <result column="pwd" jdbcType="VARCHAR" property="pwd" />
  <result column="user_state" jdbcType="INTEGER" property="userState" />
  <result column="create_time" jdbcType="DATE" property="createTime" />
  <result column="last_time" jdbcType="DATE" property="lastTime" />
  <result column="update_time" jdbcType="DATE" property="updateTime" />
  <result column="user_type" jdbcType="INTEGER" property="userType" />
</resultMap>

<sql id="Base_Column_List">
  id, name, gender, age, pwd, user_state, create_time, last_time, update_time, user_type
</sql>

<insert id="insert" parameterType="com.hisen.bean.entity.UserInfo">
  insert into user_info (id, name, gender, 
  age, pwd, user_state, create_time, last_time, update_time, user_type)
  values (#{id,jdbcType=INTEGER}, #{name,jdbcType=VARCHAR}, #{gender,jdbcType=INTEGER}, 
  #{age,jdbcType=INTEGER}, #{pwd,jdbcType=VARCHAR}, #{userState,jdbcType=INTEGER}, 
  #{createTime,jdbcType=DATE}, #{lastTime,jdbcType=DATE}, #{updateTime,jdbcType=DATE}, 
  #{userType,jdbcType=INTEGER})
</insert>

<delete id="deleteByPrimaryKey" parameterType="java.lang.Integer">
  delete from user_info
  where id = #{id,jdbcType=INTEGER}
</delete>

<update id="updateByPrimaryKey" parameterType="com.hisen.bean.entity.UserInfo">
  update user_info
  set name = #{name,jdbcType=VARCHAR},
      gender = #{gender,jdbcType=INTEGER},
      age = #{age,jdbcType=INTEGER},
      pwd = #{pwd,jdbcType=VARCHAR},
      user_state = #{userState,jdbcType=INTEGER},
      create_time = #{createTime,jdbcType=DATE},
      last_time = #{lastTime,jdbcType=DATE},
      update_time = #{updateTime,jdbcType=DATE},
      user_type = #{userType,jdbcType=INTEGER}
      where id = #{id,jdbcType=INTEGER}
</update>

<select id="selectByPrimaryKey" parameterType="java.lang.Integer" resultMap="BaseResultMap">
  select 
  <include refid="Base_Column_List" />
  from user_info
  where id = #{id,jdbcType=INTEGER}
</select>

</mapper>
```



### web.xml

```
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                      http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
  version="3.1" metadata-complete="true">
  <!-- 配置DispatcherServlet -->
  <servlet>
    <servlet-name>mvc-dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- 配置springMVC需要加载的配置文件
        spring-dao.xml,spring-service.xml,spring-web.xml
        Mybatis - > spring -> springmvc
     -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring/spring-*.xml</param-value>
    </init-param>
  </servlet>
  <servlet-mapping>
    <servlet-name>mvc-dispatcher</servlet-name>
    <!-- 默认匹配所有的请求 -->
    <url-pattern>/</url-pattern>
  </servlet-mapping>

  <!-- 配置 Druid 监控信息显示页面 -->
  <!-- 访问监控页面: 1. /druid/index.html 2. /druid（重定向到 /druid/index.html） -->
  <servlet>
    <servlet-name>DruidStatView</servlet-name>
    <servlet-class>com.alibaba.druid.support.http.StatViewServlet</servlet-class>
    <!-- 允许清空统计数据 -->
    <init-param>
      <param-name>resetEnable</param-name>
      <param-value>true</param-value>
    </init-param>
    <!-- 用户名 -->
    <init-param>
      <param-name>loginUsername</param-name>
      <param-value>druid</param-value>
    </init-param>
    <!-- 密码 -->
    <init-param>
      <param-name>loginPassword</param-name>
      <param-value>druid</param-value>
    </init-param>
    <!-- 是否重置所有监控记录 -->
    <init-param>
      <param-name>resetEnable</param-name>
      <param-value>false</param-value>
    </init-param>
    <!-- 设置允许访问监控页面的IP: 管理员电脑 IP -->
    <!--
    <init-param>-->
      <!--<param-name>allow</param-name>-->
      <!--<param-value>172.18.108.81</param-value>-->
    <!--</init-param>
    -->
  </servlet>
  <servlet-mapping>
    <servlet-name>DruidStatView</servlet-name>
    <url-pattern>/druid/*</url-pattern>
  </servlet-mapping>
  <filter>
    <filter-name>druidWebStatFilter</filter-name>
    <filter-class>com.alibaba.druid.support.http.WebStatFilter</filter-class>
    <init-param>
      <param-name>exclusions</param-name>
      <param-value>/public/*,*.js,*.css,/druid*,*.jsp,*.swf</param-value>
    </init-param>
    <init-param>
      <param-name>principalSessionName</param-name>
      <param-value>sessionInfo</param-value>
    </init-param>
    <init-param>
      <param-name>profileEnable</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>druidWebStatFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  <!-- CORS过滤器 start -->
  <filter>
    <filter-name>cors</filter-name>
    <filter-class>com.hisen.filter.CorsFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>cors</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  <!-- 错误页面配置 -->
  <error-page>
    <error-code>404</error-code>
    <location>/404.jsp</location>
  </error-page>
  <error-page>
    <error-code>500</error-code>
    <location>/500.jsp</location>
  </error-page>
</web-app>
```



## SSM 整合完整参考教程

[基础篇](http://www.cnblogs.com/han-1034683568/p/6634711.html#ssm1)

[优化篇](http://www.cnblogs.com/han-1034683568/p/6634711.html#ssm2)

[进阶篇](http://www.cnblogs.com/han-1034683568/p/6634711.html#ssm3)

[SSM 整合系统实现步骤参考](https://github.com/liyifeng1994/ssm/blob/master/README.md?ts=10#ssm)

[系统技术栈实现参考](https://github.com/megagao/production_ssm)

[对”优雅的SSM框架“进行完善（页面分离+Nginx负载均衡+Tomcat集群）](https://github.com/wosyingjun/beauty_ssm_cluster)
