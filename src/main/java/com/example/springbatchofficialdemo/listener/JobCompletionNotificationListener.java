package com.example.springbatchofficialdemo.listener;

import com.example.springbatchofficialdemo.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        log.info(Objects.requireNonNull(jdbcTemplate.getDataSource()).toString());
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