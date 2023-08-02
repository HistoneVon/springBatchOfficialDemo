package com.example.springbatchofficialdemo.writer;

import com.example.springbatchofficialdemo.entity.Person;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/8/1 10:45
 */

public class PersonItemWriter implements ItemWriter<Person> {

    /*private static final String GET_PERSON = "select * from person where first_name = ?";*/
    private static final String INSERT_PERSON = "insert into person (first_name, last_name) values (?, ?)";
    /*private static final String UPDATE_PERSON = "update person set last_name = ? where first_name = ?";*/

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void write(List<? extends Person> persons) throws Exception {
        for (Person person : persons) {
            jdbcTemplate.update(INSERT_PERSON, person.getFirstName(), person.getLastName());
        }
        /*
        for (Person person : persons) { // 对每个Person对象
            // 根据指定的firstName获取Person
            List<Person> personList = jdbcTemplate.query(
                    GET_PERSON,
                    new Object[]{person.getFirstName()},
                    new RowMapper<Person>() {
                        @Override
                        public Person mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                            Person p = new Person();
                            p.setFirstName(resultSet.getString(1));
                            p.setLastName(resultSet.getString(2));
                            return p;
                        }
                    });

            if (!personList.isEmpty()) { // 如果SELECT取得一条记录, 则write()中更新数据库中对应记录的值
                jdbcTemplate.update(UPDATE_PERSON, person.getLastName(), person.getFirstName());
            } else { // 如果SELECT没有查询结果, 则write()执行INSERT将Person信息添加到数据库
                jdbcTemplate.update(INSERT_PERSON, person.getFirstName(), person.getLastName());
            }
        }
        */
    }
}
