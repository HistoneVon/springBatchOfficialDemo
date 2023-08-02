package com.example.springbatchofficialdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author histonevon
 */

@EnableBatchProcessing
@Slf4j
public class SpringBatchOfficialDemoApplication {

    public static void main(String[] args)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        String[] springConfig = {
                "applicationContext.xml",
                "jobs/csv2mysql-job.xml"
        };
        ApplicationContext context = new ClassPathXmlApplicationContext(springConfig);
        JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
        Job job = (Job) context.getBean("simpleFileImportJob");
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
        if (log.isInfoEnabled()) {
            log.info(String.format("Exit Status : %s", jobExecution.getStatus()));
        }
    }

}
