package com.example.springbatchofficialdemo.reader;

import com.example.springbatchofficialdemo.entity.Person;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/8/1 09:42
 * @description 根据csv文件内容创建Person对象
 */

public class PersonFieldSetMapper implements FieldSetMapper<Person> {
    /**
     *
     * @param fieldSet lineMapper将一行数据解析为单独的字段，构建一个FieldSet（包含命名好的字段），该对象被传递给mapFieldSet
     * @return com.example.springbatchofficialdemo.reader.PersonFieldSetMapper
     * @throws BindException
     */
    @Override
    public Person mapFieldSet(FieldSet fieldSet) throws BindException {
        Person person = new Person();
        person.setFirstName(fieldSet.readString("firstName"));
        person.setLastName(fieldSet.readString("lastName"));
        return person;
    }
}
