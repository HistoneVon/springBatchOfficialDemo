package com.example.springbatchofficialdemo.writer;

import com.example.springbatchofficialdemo.entity.Person;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/8/1 10:45
 */

public class PersonItemWriter implements ItemWriter<Person> {

    private static final String INSERT_PERSON = "insert into person (first_name, last_name) values (?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void write(List<? extends Person> persons) throws Exception {
        for (Person person : persons) {
            jdbcTemplate.update(INSERT_PERSON, person.getFirstName(), person.getLastName());
        }
    }
}
