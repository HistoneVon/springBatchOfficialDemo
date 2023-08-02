package com.example.springbatchofficialdemo;

import com.example.springbatchofficialdemo.entity.Person;
import com.example.springbatchofficialdemo.listener.JobCompletionNotificationListener;
import com.example.springbatchofficialdemo.processor.PersonItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("data/sample-data.csv")) // 读取数据的来源，这里表示在类路径的resources目录下的sample-data.csv文件
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

}
