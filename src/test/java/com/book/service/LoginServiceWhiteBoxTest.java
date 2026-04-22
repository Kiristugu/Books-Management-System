package com.book.service;

import com.book.dao.AdminDao;
import com.book.dao.ReaderCardDao;
import com.book.dao.ReaderInfoDao;
import com.book.domain.ReaderCard;
import com.book.domain.ReaderInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class LoginServiceWhiteBoxTest {

    private LoginService loginService;
    private StubReaderCardDao readerCardDao;
    private StubReaderInfoDao readerInfoDao;
    private StubAdminDao adminDao;

    @BeforeMethod
    public void setUp() {
        loginService = new LoginService();
        readerCardDao = new StubReaderCardDao();
        readerInfoDao = new StubReaderInfoDao();
        adminDao = new StubAdminDao();
        loginService.setReaderCardDao(readerCardDao);
        loginService.setReaderInfoDao(readerInfoDao);
        loginService.setAdminDao(adminDao);
    }

    @Test
    public void hasMatchReader_shouldReturnTrue_whenCountGreaterThanZero() {
        readerCardDao.matchCount = 1;
        assertTrue(loginService.hasMatchReader(1, "p"));
    }

    @Test
    public void hasMatchReader_shouldReturnFalse_whenCountIsZero() {
        readerCardDao.matchCount = 0;
        assertFalse(loginService.hasMatchReader(1, "p"));
    }

    @Test
    public void findReaderCardByUserId_shouldReturnDaoObject() {
        ReaderCard rc = new ReaderCard();
        rc.setReaderId(5);
        readerCardDao.readerCard = rc;
        assertSame(loginService.findReaderCardByUserId(5), rc);
    }

    @Test
    public void findReaderInfoByReaderId_shouldReturnDaoObject() {
        ReaderInfo ri = new ReaderInfo();
        ri.setReaderId(6);
        readerInfoDao.readerInfo = ri;
        assertSame(loginService.findReaderInfoByReaderId(6), ri);
    }

    @Test
    public void hasMatchAdmin_shouldReturnTrue_whenCountEqualsOne() {
        adminDao.matchCount = 1;
        assertTrue(loginService.hasMatchAdmin(1, "p"));
    }

    @Test
    public void hasMatchAdmin_shouldReturnFalse_whenCountNotOne() {
        adminDao.matchCount = 2;
        assertFalse(loginService.hasMatchAdmin(1, "p"));
    }

    @Test
    public void adminRePasswd_shouldReturnTrue_whenRowsPositive() {
        adminDao.rePasswordRows = 1;
        assertTrue(loginService.adminRePasswd(1, "n"));
    }

    @Test
    public void adminRePasswd_shouldReturnFalse_whenRowsZero() {
        adminDao.rePasswordRows = 0;
        assertFalse(loginService.adminRePasswd(1, "n"));
    }

    @Test
    public void getAdminPasswd_shouldReturnDaoValue() {
        adminDao.passwd = "abc";
        assertEquals(loginService.getAdminPasswd(1), "abc");
    }

    private static class StubReaderCardDao extends ReaderCardDao {
        private int matchCount;
        private ReaderCard readerCard = new ReaderCard();

        @Override
        public int getMatchCount(int readerId, String passwd) {
            return matchCount;
        }

        @Override
        public ReaderCard findReaderByReaderId(int userId) {
            return readerCard;
        }
    }

    private static class StubReaderInfoDao extends ReaderInfoDao {
        private ReaderInfo readerInfo = new ReaderInfo();

        @Override
        public ReaderInfo findReaderInfoByReaderId(int readerId) {
            return readerInfo;
        }
    }

    private static class StubAdminDao extends AdminDao {
        private int matchCount;
        private int rePasswordRows;
        private String passwd;

        @Override
        public int getMatchCount(int adminId, String password) {
            return matchCount;
        }

        @Override
        public int rePassword(int adminId, String newPasswd) {
            return rePasswordRows;
        }

        @Override
        public String getPasswd(int id) {
            return passwd;
        }
    }
}
