package com.book.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(singleThreaded = true)
public class AdminDaoIntegrationTest extends DaoIntegrationSupport {

    private AdminDao adminDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeMethod
    public void setUp() {
        jdbcTemplate = createJdbcTemplate();
        adminDao = new AdminDao();
        adminDao.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void getMatchCount_shouldReturnOne_whenPasswordMatches() {
        assertEquals(adminDao.getMatchCount(20170001, "111111"), 1);
    }

    @Test
    public void rePassword_shouldUpdatePassword() {
        assertEquals(adminDao.rePassword(20170001, "222222"), 1);
        assertEquals(adminDao.getPasswd(20170001), "222222");
    }

    @Test
    public void getPasswd_shouldReturnCurrentPassword() {
        assertEquals(adminDao.getPasswd(20170001), "111111");
    }
}
