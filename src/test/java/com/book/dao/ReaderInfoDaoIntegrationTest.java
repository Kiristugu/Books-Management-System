package com.book.dao;

import com.book.domain.ReaderInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class ReaderInfoDaoIntegrationTest extends DaoIntegrationSupport {

    private ReaderInfoDao readerInfoDao;

    @BeforeMethod
    public void setUp() {
        readerInfoDao = new ReaderInfoDao();
        readerInfoDao.setJdbcTemplate(createJdbcTemplate());
    }

    @Test
    public void getAllReaderInfo_shouldReturnSeedData() {
        ArrayList<ReaderInfo> list = readerInfoDao.getAllReaderInfo();
        assertTrue(list.size() >= 2);
    }

    @Test
    public void findReaderInfoByReaderId_shouldReturnReader() {
        ReaderInfo info = readerInfoDao.findReaderInfoByReaderId(1501014101);
        assertEquals(info.getName(), "张华");
        assertEquals(info.getAddress(), "天津市");
    }

    @Test
    public void editReaderInfo_shouldUpdateRow() {
        ReaderInfo info = new ReaderInfo();
        info.setReaderId(1501014101);
        info.setName("张华改");
        info.setSex("男");
        info.setBirth(new Date());
        info.setAddress("新地址");
        info.setTelcode("18888888888");
        assertEquals(readerInfoDao.editReaderInfo(info), 1);
        ReaderInfo updated = readerInfoDao.findReaderInfoByReaderId(1501014101);
        assertEquals(updated.getName(), "张华改");
        assertEquals(updated.getAddress(), "新地址");
    }

    @Test
    public void addAndDeleteReaderInfo_shouldWork() {
        ReaderInfo info = new ReaderInfo();
        info.setReaderId(1501014998);
        info.setName("新增");
        info.setSex("女");
        info.setBirth(new Date());
        info.setAddress("地址");
        info.setTelcode("17777777777");
        assertEquals(readerInfoDao.addReaderInfo(info), 1);
        assertEquals(readerInfoDao.deleteReaderInfo(1501014998), 1);
    }
}
