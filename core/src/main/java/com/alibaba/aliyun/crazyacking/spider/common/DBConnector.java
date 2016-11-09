package com.alibaba.aliyun.crazyacking.spider.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;

public class DBConnector {
    private static final Logger logger = LoggerFactory.getLogger(DBConnector.class.getName());
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWD;
    public static String DB_NAME;
    public static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    private DBConnector() {
    }

    public static Connection getConnection() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(DB_URL, DB_USER, DB_PASSWD);
        Connection conn = null;
        try {
            dataSource.setDriverClassName(DB_DRIVER);
            conn = dataSource.getConnection();
        } catch (Exception e) {
            logger.error("", e);
        }
        return conn;
    }
}
