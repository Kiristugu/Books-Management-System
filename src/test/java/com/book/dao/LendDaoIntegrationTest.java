package com.book.dao;

import com.book.domain.Lend;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class LendDaoIntegrationTest extends DaoIntegrationSupport {

    private LendDao lendDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeMethod
    public void setUp() {
        jdbcTemplate = createJdbcTemplate();
        lendDao = new LendDao();
        lendDao.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void bookLend_shouldInsertLendAndUpdateBookState() {
        assertEquals(lendDao.bookLendOne(10000002L, 1501014101), 1);
        assertEquals(lendDao.bookLendTwo(10000002L), 1);
        Integer state = jdbcTemplate.queryForObject(
                "SELECT state FROM book_info WHERE book_id = ?", new Object[]{10000002L}, Integer.class);
        assertEquals(state.intValue(), 0);
    }

    @Test
    public void bookReturn_shouldUpdateBackDateAndBookState() {
        assertEquals(lendDao.bookReturnOne(10000001L), 1);
        assertEquals(lendDao.bookReturnTwo(10000001L), 1);
        ArrayList<Lend> list = lendDao.myLendList(1501014101);
        assertTrue(list.size() >= 1);
        assertNotNull(list.get(0).getBackDate());
    }

    @Test
    public void lendListAndMyLendList_shouldReturnRows() {
        assertTrue(lendDao.lendList().size() >= 2);
        assertTrue(lendDao.myLendList(1501014101).size() >= 1);
    }
}
