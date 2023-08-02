package com.example.springbatchofficialdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 10:16
 */

@Data // getter setter toString
@AllArgsConstructor // 全参构造
@NoArgsConstructor // 无参构造
public class Person {

    private String lastName;
    private String firstName;

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName;
    }

}
