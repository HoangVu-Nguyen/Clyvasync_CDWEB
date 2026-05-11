package com.relationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
// Nếu bạn có MyBatis Plus trong common, hãy import nó để exclude
// import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {
                "com.relationservice",
                "com.commonlibrary",
                "com.commoncore"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class RelationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RelationServiceApplication.class, args);
    }
}