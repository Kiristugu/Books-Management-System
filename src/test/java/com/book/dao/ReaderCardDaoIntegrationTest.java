package com.book.dao;

import com.book.domain.ReaderCard;
import com.book.domain.ReaderInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(singleThreaded = true)
public class ReaderCardDaoIntegrationTest extends DaoIntegrationSupport {

    private ReaderCardDao readerCardDao;

    @BeforeMethod
    public void setUp() {
        readerCardDao = new ReaderCardDao();
        readerCardDao.setJdbcTemplate(createJdbcTemplate());
    }

    @Test
    public void getMatchCount_shouldReturnOne_whenCredentialsCorrect() {
        assertEquals(readerCardDao.getMatchCount(1501014101, "111111"), 1);
    }

    @Test
    public void findReaderByReaderId_shouldReturnReaderCard() {
        ReaderCard card = readerCardDao.findReaderByReaderId(1501014101);
        assertEquals(card.getReaderId(), 1501014101);
        assertEquals(card.getName(), "张华");
        assertEquals(card.getPasswd(), "111111");
    }

    @Test
    public void rePassword_shouldUpdateReaderPassword() {
        assertEquals(readerCardDao.rePassword(1501014101, "abc123"), 1);
        assertEquals(readerCardDao.getMatchCount(1501014101, "abc123"), 1);
    }

    @Test
    public void addReaderCard_shouldInsertReaderCardWithDefaultPassword() {
        ReaderInfo info = new ReaderInfo();
        info.setReaderId(1501014999);
        info.setName("新增读者");
        assertEquals(readerCardDao.addReaderCard(info), 1);
        ReaderCard card = readerCardDao.findReaderByReaderId(1501014999);
        assertEquals(card.getName(), "新增读者");
        assertEquals(card.getPasswd(), "111111");
    }

    @Test
    public void updateName_shouldModifyReaderName() {
        assertEquals(readerCardDao.updateName(1501014101, "新名字"), 1);
        assertEquals(readerCardDao.findReaderByReaderId(1501014101).getName(), "新名字");
    }
}
