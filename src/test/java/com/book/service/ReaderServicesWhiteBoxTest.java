package com.book.service;

import com.book.dao.ReaderCardDao;
import com.book.dao.ReaderInfoDao;
import com.book.domain.ReaderInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class ReaderServicesWhiteBoxTest {

    private ReaderInfoService readerInfoService;
    private ReaderCardService readerCardService;
    private StubReaderInfoDao readerInfoDao;
    private StubReaderCardDao readerCardDao;

    @BeforeMethod
    public void setUp() {
        readerInfoService = new ReaderInfoService();
        readerCardService = new ReaderCardService();
        readerInfoDao = new StubReaderInfoDao();
        readerCardDao = new StubReaderCardDao();
        readerInfoService.setReaderInfoDao(readerInfoDao);
        readerCardService.setReaderCardDao(readerCardDao);
    }

    @Test
    public void readerInfos_shouldReturnDaoList() {
        ArrayList<ReaderInfo> list = new ArrayList<ReaderInfo>();
        list.add(new ReaderInfo());
        readerInfoDao.allReaders = list;
        assertSame(readerInfoService.readerInfos(), list);
    }

    @Test
    public void deleteReaderInfo_shouldMapDaoRowsToBoolean() {
        readerInfoDao.deleteRows = 1;
        assertTrue(readerInfoService.deleteReaderInfo(1));
        readerInfoDao.deleteRows = 0;
        assertFalse(readerInfoService.deleteReaderInfo(1));
    }

    @Test
    public void editReaderInfo_shouldMapDaoRowsToBoolean() {
        readerInfoDao.editRows = 1;
        assertTrue(readerInfoService.editReaderInfo(new ReaderInfo()));
        readerInfoDao.editRows = 0;
        assertFalse(readerInfoService.editReaderInfo(new ReaderInfo()));
    }

    @Test
    public void addReaderInfo_shouldMapDaoRowsToBoolean() {
        readerInfoDao.addRows = 1;
        assertTrue(readerInfoService.addReaderInfo(new ReaderInfo()));
        readerInfoDao.addRows = 0;
        assertFalse(readerInfoService.addReaderInfo(new ReaderInfo()));
    }

    @Test
    public void getReaderInfo_shouldReturnDaoObject() {
        ReaderInfo info = new ReaderInfo();
        info.setReaderId(8);
        readerInfoDao.oneReader = info;
        assertSame(readerInfoService.getReaderInfo(8), info);
    }

    @Test
    public void readerCardService_shouldMapAllDaoBooleanBranches() {
        ReaderInfo ri = new ReaderInfo();
        readerCardDao.addRows = 1;
        assertTrue(readerCardService.addReaderCard(ri));
        readerCardDao.addRows = 0;
        assertFalse(readerCardService.addReaderCard(ri));

        readerCardDao.passRows = 1;
        assertTrue(readerCardService.updatePasswd(1, "n"));
        readerCardDao.passRows = 0;
        assertFalse(readerCardService.updatePasswd(1, "n"));

        readerCardDao.nameRows = 1;
        assertTrue(readerCardService.updateName(1, "x"));
        readerCardDao.nameRows = 0;
        assertFalse(readerCardService.updateName(1, "x"));
    }

    private static class StubReaderInfoDao extends ReaderInfoDao {
        private ArrayList<ReaderInfo> allReaders = new ArrayList<ReaderInfo>();
        private int deleteRows;
        private ReaderInfo oneReader = new ReaderInfo();
        private int editRows;
        private int addRows;

        @Override
        public ArrayList<ReaderInfo> getAllReaderInfo() {
            return allReaders;
        }

        @Override
        public int deleteReaderInfo(int readerId) {
            return deleteRows;
        }

        @Override
        public ReaderInfo findReaderInfoByReaderId(int readerId) {
            return oneReader;
        }

        @Override
        public int editReaderInfo(ReaderInfo readerInfo) {
            return editRows;
        }

        @Override
        public int addReaderInfo(ReaderInfo readerInfo) {
            return addRows;
        }
    }

    private static class StubReaderCardDao extends ReaderCardDao {
        private int addRows;
        private int passRows;
        private int nameRows;

        @Override
        public int addReaderCard(ReaderInfo readerInfo) {
            return addRows;
        }

        @Override
        public int rePassword(int readerId, String newPasswd) {
            return passRows;
        }

        @Override
        public int updateName(int readerId, String name) {
            return nameRows;
        }
    }
}
