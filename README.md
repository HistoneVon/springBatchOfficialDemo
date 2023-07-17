# Spring Batch学习及Demo项目

> Author: histonevon@zohomail.com
>
> Date: 2023/07/14

* 本文档配套仓库：https://github.com/HistoneVon/springBatchOfficialDemo
* 原文地址：https://histonevon.top/archives/spring-batch-demo
* 本文档基于Spring官方文档：[Getting Started | Creating a Batch Service (spring.io)](https://spring.io/guides/gs/batch-processing/)
* Spring Batch官方仓库：https://github.com/spring-projects/spring-batch
* 官方提供的本Demo项目：https://github.com/spring-guides/gs-batch-processing

[TOC]

## 准备工作

* 由于官方文档数据库基于HyperSQL，故先使用HyperSQL，后边再使用Oracle
* Spring Batch架构图，来自官网

![Figure 1.1: Spring Batch Layered Architecture](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/spring-batch-layers.png)

## 创建项目并准备数据

* 使用Spring Initializr创建项目

    * 选择Maven
    * 添加Dependencies：`Spring Batch` `HyperSQL Database`
    * Generate生成项目

* 创建`src/main/resources/sample-data.csv`文件，内容如下

  ```csv
  Jill,Doe
  Joe,Doe
  Justin,Doe
  Jane,Doe
  John,Doe
  ```

  ![image-20230717100645320](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717100645320.png)

* Spring无需自定义即可处理`csv`文件

* 创建`src/main/resources/schema-all.sql`用于存储数据的表，如下

  ```sql
  DROP TABLE people IF EXISTS;
  
  CREATE TABLE people
  (
      person_id  BIGINT IDENTITY NOT NULL PRIMARY KEY,
      first_name VARCHAR(20),
      last_name  VARCHAR(20)
  );
  ```

  ![image-20230717101919628](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717101919628.png)

* 官网：Spring Boot runs `schema-@@platform@@.sql` automatically during startup. `-all` is the default for all platforms.（命名规则）

## 编写批处理程序

### 创建Business类

* `src/main/java/com/example/springbatchofficialdemo/Person.java`

```java
package com.example.springbatchofficialdemo;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 10:16
 */

public class Person {

    private String lastName;
    private String firstName;

    public Person() {
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName;
    }

}

```

### 创建中间处理器

* 中间处理器（Intermediate Processor）
* 批处理常见流程：**引入数据，转换数据，然后将其传输到其他位置**
* 本例转换为：**将名字转换为大写**
* `src/main/java/com/example/springbatchofficialdemo/Person.java`
* 官网：The input and output types need not be the same. In fact, after one source of data is read, sometimes the application’s data flow needs a different data type.（输入输出类型不必相同）

```java
package com.example.springbatchofficialdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 10:32
 */

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
```

### 配置批处理作业

* 官网：将实际的批处理作业放在一起
* Spring Batch提供了许多实用程序类，可以减少编写自定义代码的需要，专注于业务逻辑
* 要配置作业，必须首先创建一个Spring类，带有`@Configuration`注解
* `src/main/java/com/example/springbatchofficialdemo/BatchConfiguration.java`，如以下示例所示
* 此示例使用基于内存的数据库，这意味着，完成后，数据将消失
* 类中以`@bean`定义读取器、处理器和写入器

```java
package com.example.springbatchofficialdemo;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 11:06
 */

@Configuration
public class BatchConfiguration {

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv")) // 读取数据的来源，这里表示在类路径的resources目录下的sample-data.csv文件
                .delimited() // 指定每行的数据字段的分割符为 ,（默认）
                .names("firstName", "lastName") // 将分割的字段映射到 firstName 和 lastName 属性字段
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }}) // 这些分割的属性字段对应的类
                // 使用 {{}} 的方式来将初始化的 BeanWrapperFieldSetMapper 调用 setTargetType 方法
                // 可能是一个比较简洁的方式，但这种方式可能会导致内存泄漏
                // java双大括号语法：https://www.cnblogs.com/vipstone/p/12937582.html
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) { // 由于这里的写出是写入的数据库中，因此采用 JdbcBatchItemWriter 的实现类进行写出
        return new JdbcBatchItemWriterBuilder<Person>() // 以构建者模式的方式创建 JdbcBatchItemWriter 实例对象
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>() // 提供执行相关 SQL 需要的参数
                )
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)") // 写入数据库中具体执行的 SQL
                .dataSource(dataSource) // 设置数据源，这个对象可以手动创建，但是一般在配置相关的 datasource 属性之后，Spring 会自动生成这个类型的 Bean
                .build();
    }
}

```

* 如果使用官方的代码可能会有如下警告⚠️`Redundant array creation for calling varargs method`
* 即：用于调用可变参数方法而创建的数组是冗余的
* 这是由于Java中的可变参数是使用数组实现的，我们可以直接向函数（如此处的`names`）直接传递多个符合数据类型的参数，也可以显式声明一个相应数据类型的数组（有时候这样看起来会更加清晰）

![image-20230717134838188](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717134838188.png)

![image-20230717135158659](./Spring Batch学习及Demo项目.assets/image-20230717135158659.png)

![image-20230717134919331](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717134919331.png)

* 此代码块定义输入、处理、输出
* `reader()`创建一个`ItemReader`，它查找一个名为`sample-data.csv`的文件，使用足够的信息解析每一行记录并将其转换为一个`Person`对象
* `processor()`创建一个`PersonItemProcessor`实例，用于将数据转换为大写
* `writer(DataSource)`创建一个`ItemWriter`，此处针对JDBC目标，并自动获取一个由`@EnableBatchProcessing`创建的数据源的副本，包括插入单个`Person`且由Java Bean属性驱动的SQL语句
* 向`src/main/java/com/example/springbatchofficialdemo/BatchConfiguration.java`加入以下代码，配置步骤

```java
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) { // Step 类是批处理任务的执行单元
        // return new StepBuilder("step1")
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(10) // 这个Step一次处理的数据的数量，前缀<I, O>泛型表示的含义与Item Process中的一致，因此这里两个泛型都设置为Person
                .reader(reader()) // 数据的读取部分
                .processor(processor()) // 数据的读取部分
                .writer(writer) // 写出部分，由于 writer 需要注入 DataSource 对象，因此直接作为一个注入参数参数并使用会更加方便；
                // 当然，reader 和 process 也可以通过注入参数的方式直接使用，因为它们都被定义成了 Spring 中的 Bean
                .build();
    }
```

* `step1`方法定义单个步骤，作业（Job）是从步骤构建的，其中每个步骤都可能涉及读取器、处理器和写入器

### 任务执行监听器

* 批处理配置的最后一步是在作业完成时获得通知
* `src/main/java/com/example/springbatchofficialdemo/JobCompletionNotificationListener.java`
* `JobCompletionNotificationListener`侦听作业何时为`BatchStatus.COMPLETE`，然后使用`JdbcTemplate`检查结果

```java
package com.example.springbatchofficialdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 14:50
 */

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) { // 执行任务之前的一些操作
        log.info("JOB READY! Look at this");
    }

    @Override
    public void afterJob(JobExecution jobExecution) { // 在任务执行完成之后执行的一些操作，这里是执行完成之后查询写入到数据库中的结果
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("JOB FINISHED! Time to verify the results");

            jdbcTemplate.query("SELECT first_name, last_name FROM people",
                    (rs, row) -> new Person(
                            rs.getString(1),
                            rs.getString(2))
            ).forEach(person -> log.info("Found <{{}}> in the database.", person));
        }
    }
}
```

### 创建Job

* 批处理的最顶层的抽象便是 `Job`，`Job` 是一个批处理任务，现在整合上文的内容，创建一个 `Job`
* 在`src/main/java/com/example/springbatchofficialdemo/BatchConfiguration.java`加入以下代码，配置作业

```java
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        // return new JobBuilder("importUserJob")
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer()) // 增加这个 Job 的参数信息
                .listener(listener) // 添加之前创建的任务执行监听器，使得在任务开始和结束时执行相应的操作
                .flow(step1) // 添加上文定义的 step1 处理
                .end() // 添加上文定义的 step1 处理
                .build();
    }
```

* 如果想要添加多个 Step，那么可以按照下面的方式进行添加

```java
@Bean(name = "importUserJob")
public Job importUserJob(JobCompletionNotificationListener listener) {
    return jobBuilderFactory
        .get("importUserJob")
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .start(step1) // 定义的 step1
        .next(step2) // 定义的 step2
        .build();
}
```

* 值得注意的是，由于上文定义的任务执行监听器监听的是任务（即 `Job`） 的状态，因此当添加多个 `Step` 时，只有在完成最后的 `Step` 之后才会触发这个事件监听

### 执行程序

* 一定要在运行入口添加`@EnableBatchProcessing`注解
    * 这个注解的作用，和Spring家庭中的`@Enable*`系列注解功能很类似，顾名思义，就是让我们可以运行Spring Batch。
    * 在配置类上打上这个注解，Spring会自动帮我们生成一系列与Spring Batch运行有关的bean，并交给Spring容器管理，而当我们需要这些beans时，只需要用一个@Autowired就可以实现注入了。
    * 如果不添加，则会找不到`jobBuilderFactory`和`stepBuilderFactory`

```java
package com.example.springbatchofficialdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author histonevon
 */
@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchOfficialDemoApplication {

    public static void main(String[] args) throws Exception {
        System.exit(SpringApplication.exit(SpringApplication.run(SpringBatchOfficialDemoApplication.class, args)));
    }

}

```

#### Maven打包

* 本文创建一个单机程序而非一个Web项目来演示，将程序打包成jar包（命令行或直接使用IDEA）

```shell
./mvnw clean package
```

![image-20230717162256278](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717162256278.png)

![image-20230717162436381](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717162436381.png)

![image-20230717162448137](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717162448137.png)

* 打出的包不能运行，试过查看`pom.xml`中已经配置主类，暂不知为何

![image-20230717164932038](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717164932038.png)

![image-20230717165032725](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717165032725.png)

#### 直接运行

* 直接运行`SpringBatchOfficialDemoApplication`也可以

![image-20230717161638967](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230717161638967.png)

```shell
/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/bin/java -XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true -Dmanagement.endpoints.jmx.exposure.include=* -javaagent:/Users/histonevon/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/231.9225.16/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=58973:/Users/histonevon/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/231.9225.16/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk/Contents/Home/jre/lib/rt.jar:/Users/histonevon/Workspace/JavaProjects/springBatchOfficialDemo/target/classes:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot-starter-batch/2.6.13/spring-boot-starter-batch-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot-starter/2.6.13/spring-boot-starter-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot/2.6.13/spring-boot-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot-autoconfigure/2.6.13/spring-boot-autoconfigure-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot-starter-logging/2.6.13/spring-boot-starter-logging-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/ch/qos/logback/logback-classic/1.2.11/logback-classic-1.2.11.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/ch/qos/logback/logback-core/1.2.11/logback-core-1.2.11.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/apache/logging/log4j/log4j-to-slf4j/2.17.2/log4j-to-slf4j-2.17.2.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/apache/logging/log4j/log4j-api/2.17.2/log4j-api-2.17.2.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/slf4j/jul-to-slf4j/1.7.36/jul-to-slf4j-1.7.36.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/yaml/snakeyaml/1.29/snakeyaml-1.29.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/boot/spring-boot-starter-jdbc/2.6.13/spring-boot-starter-jdbc-2.6.13.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/com/zaxxer/HikariCP/4.0.3/HikariCP-4.0.3.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/batch/spring-batch-core/4.3.7/spring-batch-core-4.3.7.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/com/fasterxml/jackson/core/jackson-databind/2.13.4.2/jackson-databind-2.13.4.2.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/com/fasterxml/jackson/core/jackson-annotations/2.13.4/jackson-annotations-2.13.4.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/com/fasterxml/jackson/core/jackson-core/2.13.4/jackson-core-2.13.4.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/io/micrometer/micrometer-core/1.8.11/micrometer-core-1.8.11.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/hdrhistogram/HdrHistogram/2.1.12/HdrHistogram-2.1.12.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/latencyutils/LatencyUtils/2.0.3/LatencyUtils-2.0.3.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/javax/batch/javax.batch-api/1.0/javax.batch-api-1.0.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/batch/spring-batch-infrastructure/4.3.7/spring-batch-infrastructure-4.3.7.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/retry/spring-retry/1.3.4/spring-retry-1.3.4.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-aop/5.3.23/spring-aop-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-beans/5.3.23/spring-beans-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-context/5.3.23/spring-context-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-expression/5.3.23/spring-expression-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-tx/5.3.23/spring-tx-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/hsqldb/hsqldb/2.5.2/hsqldb-2.5.2.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-core/5.3.23/spring-core-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-jcl/5.3.23/spring-jcl-5.3.23.jar:/Users/histonevon/Workspace/Environment/LocalMavenRepo/org/springframework/spring-jdbc/5.3.23/spring-jdbc-5.3.23.jar com.example.springbatchofficialdemo.SpringBatchOfficialDemoApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.6.13)

2023-07-17 16:14:42.197  INFO 56757 --- [           main] c.e.s.SpringBatchOfficialDemoApplication : Starting SpringBatchOfficialDemoApplication using Java 1.8.0_321 on fengchalindeAir with PID 56757 (/Users/histonevon/Workspace/JavaProjects/springBatchOfficialDemo/target/classes started by histonevon in /Users/histonevon/Workspace/JavaProjects/springBatchOfficialDemo)
2023-07-17 16:14:42.201  INFO 56757 --- [           main] c.e.s.SpringBatchOfficialDemoApplication : No active profile set, falling back to 1 default profile: "default"
2023-07-17 16:14:42.886  INFO 56757 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2023-07-17 16:14:43.150  INFO 56757 --- [           main] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Driver does not support get/set network timeout for connections. (feature not supported)
2023-07-17 16:14:43.152  INFO 56757 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2023-07-17 16:14:43.328  INFO 56757 --- [           main] o.s.b.c.r.s.JobRepositoryFactoryBean     : No database type set, using meta data indicating: HSQL
2023-07-17 16:14:43.378  INFO 56757 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : No TaskExecutor has been set, defaulting to synchronous executor.
2023-07-17 16:14:43.479  INFO 56757 --- [           main] c.e.s.SpringBatchOfficialDemoApplication : Started SpringBatchOfficialDemoApplication in 1.579 seconds (JVM running for 2.155)
2023-07-17 16:14:43.483  INFO 56757 --- [           main] o.s.b.a.b.JobLauncherApplicationRunner   : Running default command line with: []
2023-07-17 16:14:43.521  INFO 56757 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [FlowJob: [name=importUserJob]] launched with the following parameters: [{run.id=1}]
2023-07-17 16:14:43.534  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : JOB READY! Look at this
2023-07-17 16:14:43.534  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : HikariDataSource (HikariPool-1)
2023-07-17 16:14:43.545  INFO 56757 --- [           main] o.s.batch.core.job.SimpleStepHandler     : Executing step: [step1]
2023-07-17 16:14:43.567  INFO 56757 --- [           main] c.e.s.PersonItemProcessor                : Converting (firstName: Jill, lastName: Doe) into (firstName: JILL, lastName: DOE)
2023-07-17 16:14:43.567  INFO 56757 --- [           main] c.e.s.PersonItemProcessor                : Converting (firstName: Joe, lastName: Doe) into (firstName: JOE, lastName: DOE)
2023-07-17 16:14:43.567  INFO 56757 --- [           main] c.e.s.PersonItemProcessor                : Converting (firstName: Justin, lastName: Doe) into (firstName: JUSTIN, lastName: DOE)
2023-07-17 16:14:43.567  INFO 56757 --- [           main] c.e.s.PersonItemProcessor                : Converting (firstName: Jane, lastName: Doe) into (firstName: JANE, lastName: DOE)
2023-07-17 16:14:43.567  INFO 56757 --- [           main] c.e.s.PersonItemProcessor                : Converting (firstName: John, lastName: Doe) into (firstName: JOHN, lastName: DOE)
2023-07-17 16:14:43.574  INFO 56757 --- [           main] o.s.batch.core.step.AbstractStep         : Step: [step1] executed in 28ms
2023-07-17 16:14:43.581  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : JOB FINISHED! Time to verify the results
2023-07-17 16:14:43.583  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : Found <{firstName: JILL, lastName: DOE}> in the database.
2023-07-17 16:14:43.583  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : Found <{firstName: JOE, lastName: DOE}> in the database.
2023-07-17 16:14:43.583  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : Found <{firstName: JUSTIN, lastName: DOE}> in the database.
2023-07-17 16:14:43.583  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : Found <{firstName: JANE, lastName: DOE}> in the database.
2023-07-17 16:14:43.583  INFO 56757 --- [           main] c.e.s.JobCompletionNotificationListener  : Found <{firstName: JOHN, lastName: DOE}> in the database.
2023-07-17 16:14:43.586  INFO 56757 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [FlowJob: [name=importUserJob]] completed with the following parameters: [{run.id=1}] and the following status: [COMPLETED] in 49ms
2023-07-17 16:14:43.593  INFO 56757 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2023-07-17 16:14:43.594  INFO 56757 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

Process finished with exit code 0

```

## 参考文献

* [Getting Started | Creating a Batch Service (spring.io)](https://spring.io/guides/gs/batch-processing/)
* [Spring Batch 的基本使用 - FatalFlower - 博客园 (cnblogs.com)](https://www.cnblogs.com/FatalFlower/p/15483940.html)
* [永远不要使用双花括号初始化实例，否则就会OOM！ - 磊哥|www.javacn.site - 博客园 (cnblogs.com)](