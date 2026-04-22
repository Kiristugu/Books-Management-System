package com.book.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

abstract class DaoIntegrationSupport {

    protected JdbcTemplate createJdbcTemplate() {
        String dbName = "booktest_" + System.nanoTime();
        String url = "jdbc:h2:mem:" + dbName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(url);
        ds.setUsername("sa");
        ds.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        initSchema(jdbcTemplate);
        seedData(jdbcTemplate);
        return jdbcTemplate;
    }

    private void initSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE admin (admin_id INT PRIMARY KEY, password VARCHAR(15))");
        jdbcTemplate.execute("CREATE TABLE book_info (" +
                "book_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL, author VARCHAR(50) NOT NULL, publish VARCHAR(30) NOT NULL, " +
                "ISBN VARCHAR(13) NOT NULL, introduction CLOB, language VARCHAR(10) NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, pubdate DATE, class_id INT, pressmark INT, state SMALLINT)");
        jdbcTemplate.execute("CREATE TABLE reader_card (" +
                "reader_id INT PRIMARY KEY, name VARCHAR(16) NOT NULL, passwd VARCHAR(15) NOT NULL DEFAULT '111111', card_state TINYINT DEFAULT 1)");
        jdbcTemplate.execute("CREATE TABLE reader_info (" +
                "reader_id INT PRIMARY KEY, name VARCHAR(16) NOT NULL, sex VARCHAR(2), birth DATE, address VARCHAR(50), telcode VARCHAR(11) NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE lend_list (" +
                "sernum BIGINT AUTO_INCREMENT PRIMARY KEY, book_id BIGINT NOT NULL, reader_id INT NOT NULL, lend_date DATE, back_date DATE)");
    }

    private void seedData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO admin(admin_id,password) VALUES (?,?)", 20170001, "111111");
        jdbcTemplate.update("INSERT INTO book_info(book_id,name,author,publish,ISBN,introduction,language,price,pubdate,class_id,pressmark,state) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                10000001L, "BookA", "AuthorA", "PubA", "1234567890123", "IntroA", "CN", 35.00, java.sql.Date.valueOf("2017-06-01"), 9, 13, 1);
        jdbcTemplate.update("INSERT INTO book_info(book_id,name,author,publish,ISBN,introduction,language,price,pubdate,class_id,pressmark,state) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                10000002L, "BookB", "AuthorB", "PubB", "1234567890124", "IntroB", "EN", 45.00, java.sql.Date.valueOf("2018-01-01"), 6, 5, 1);
        jdbcTemplate.update("INSERT INTO reader_card(reader_id,name,passwd,card_state) VALUES (?,?,?,?)", 1501014101, "张华", "111111", 1);
        jdbcTemplate.update("INSERT INTO reader_card(reader_id,name,passwd,card_state) VALUES (?,?,?,?)", 1501014102, "王小伟", "111111", 1);
        jdbcTemplate.update("INSERT INTO reader_info(reader_id,name,sex,birth,address,telcode) VALUES (?,?,?,?,?,?)",
                1501014101, "张华", "男", java.sql.Date.valueOf("1995-06-10"), "天津市", "12345678900");
        jdbcTemplate.update("INSERT INTO reader_info(reader_id,name,sex,birth,address,telcode) VALUES (?,?,?,?,?,?)",
                1501014102, "王小伟", "男", java.sql.Date.valueOf("1996-02-01"), "北京市", "12345678909");
        jdbcTemplate.update("INSERT INTO lend_list(sernum,book_id,reader_id,lend_date,back_date) VALUES (?,?,?,?,?)",
                2015040143L, 10000001L, 1501014101, java.sql.Date.valueOf("2017-06-15"), null);
        jdbcTemplate.update("INSERT INTO lend_list(sernum,book_id,reader_id,lend_date,back_date) VALUES (?,?,?,?,?)",
                2015040144L, 10000002L, 1501014102, java.sql.Date.valueOf("2017-06-16"), java.sql.Date.valueOf("2017-09-01"));
    }
}
