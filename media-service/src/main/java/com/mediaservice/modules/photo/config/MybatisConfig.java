package com.mediaservice.modules.photo.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.commonlibrary.config.MyMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }
}