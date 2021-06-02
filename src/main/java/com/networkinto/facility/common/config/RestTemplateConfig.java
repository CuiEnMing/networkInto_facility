package com.networkinto.facility.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * RestTemplate Config
 *
 * @author Hu Jingling
 * @version 1.0
 * @since 2020-08-04
 */
@Configuration
public class RestTemplateConfig {
    /**
     *  配置 RestTemplate
     * @param factory
     * @return
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        // 创建一个 httpCilent 简单工厂
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时
        factory.setConnectTimeout(15000);
        // 设置读取超时
        factory.setReadTimeout(5000);
        return factory;
    }

}