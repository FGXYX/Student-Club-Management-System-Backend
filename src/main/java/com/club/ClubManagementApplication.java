package com.club;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

// 如果有数据源问题，可以尝试排除一些自动配置
@SpringBootApplication
// @SpringBootApplication(exclude = {
//     DataSourceAutoConfiguration.class,
//     HibernateJpaAutoConfiguration.class
// })
public class ClubManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClubManagementApplication.class, args);
    }
}