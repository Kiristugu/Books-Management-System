package com.book.service;

import com.book.dao.LendDao;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class LendServiceWhiteBoxTest {

    private LendService lendService;
    private StubLendDao lendDao;

    @BeforeMethod
    public void setUp() {
        lendService = new LendService();
        lendDao = new StubLendDao();
        lendService.setLendDao(lendDao);
    }

    @Test
    public void bookLend_shouldReturnTrue_whenBothDaoUpdatesSucceed() {
        lendDao.bookLendOneRows = 1;
        lendDao.bookLendTwoRows = 1;

        boolean result = lendService.bookLend(5L, 1001);

        assertTrue(result);
    }

    @Test
    public void bookLend_shouldReturnFalse_whenInsertFails() {
        lendDao.bookLendOneRows = 0;
        lendDao.bookLendTwoRows = 1;

        boolean result = lendService.bookLend(6L, 1002);

        assertFalse(result);
    }

    @Test
    public void bookLend_shouldReturnFalse_whenStateUpdateFails() {
        lendDao.bookLendOneRows = 1;
        lendDao.bookLendTwoRows = 0;

        boolean result = lendService.bookLend(7L, 1003);

        assertFalse(result);
    }

    @Test
    public void bookReturn_shouldReturnTrue_whenBothDaoUpdatesSucceed() {
        lendDao.bookReturnOneRows = 1;
        lendDao.bookReturnTwoRows = 1;

        boolean result = lendService.bookReturn(8L);

        assertTrue(result);
    }

    @Test
    public void bookReturn_shouldReturnFalse_whenAnyDaoUpdateFails() {
        lendDao.bookReturnOneRows = 1;
        lendDao.bookReturnTwoRows = 0;

        boolean result = lendService.bookReturn(9L);

        assertFalse(result);
    }

    @Test
    public void bookReturn_shouldReturnFalse_whenFirstUpdateFails() {
        lendDao.bookReturnOneRows = 0;
        lendDao.bookReturnTwoRows = 1;

        boolean result = lendService.bookReturn(10L);

        assertFalse(result);
    }

    private static class StubLendDao extends LendDao {
        private int bookLendOneRows;
        private int bookLendTwoRows;
        private int bookReturnOneRows;
        private int bookReturnTwoRows;

        @Override
        public int bookLendOne(long bookId, int readerId) {
            return bookLendOneRows;
        }

        @Override
        public int bookLendTwo(long bookId) {
            return bookLendTwoRows;
        }

        @Override
        public int bookReturnOne(long bookId) {
            return bookReturnOneRows;
        }

        @Override
        public int bookReturnTwo(long bookId) {
            return bookReturnTwoRows;
        }
    }
}
