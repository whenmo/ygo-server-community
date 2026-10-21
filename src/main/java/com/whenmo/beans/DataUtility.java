package com.whenmo.beans;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.WebApplicationContext;

// Spring 資料庫配置類別，負責建立 DataSource 與 JdbcTemplate Bean
@Configuration
public class DataUtility {

    // 從 application.properties 注入 JDBC 屬性
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // 建立 DataSource Bean，生命週期為 Singleton（整個應用程式共用同一實例）
    //回應是介面interface 由spring提供實作類別 產生物件實例(介面多型化)
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) //設定Singleton 獨一 共用的物件
    //@RequestScope //設定RequestScope 每個HTTP請求建立一個實例
    DataSource dataSource() {
        System.out.println("建立 DataSource Bean 實例"); // 用於測試 Bean 的建立時機
        //資料庫如果伺服器類型 往往會有Database api 支援一個DataSource類別 產生個體物件
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverClassName); // 設定 SQLite 驅動程式
        ds.setUrl(url);                          // 設定資料庫連線路徑
        return ds; //註冊到網站系統引擎去
    }

    // 建立 JdbcTemplate Bean，生命週期為 Request Scope（每個 HTTP 請求各自建立一個實例）
    // proxyMode = TARGET_CLASS：注入代理物件，避免啟動期（無 Request）就嘗試建立實例
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource); // 注入共用的 DataSource 建立 JdbcTemplate
    }
}
