package com.example.springbatchofficialdemo;

import lombok.Data;

/**
 * @author Histone Von fengclchn@outlook.com
 * @createdBy histonevon
 * @date 2023/7/17 10:16
 */

@Data
public class Person {

    private String lastName;
    private String firstName;

    public Person() {
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName;
    }

}
