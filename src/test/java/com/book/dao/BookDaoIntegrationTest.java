package com.book.dao;

import com.book.domain.Book;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class BookDaoIntegrationTest extends DaoIntegrationSupport {

    private BookDao bookDao;

    @BeforeMethod
    public void setUp() {
        bookDao = new BookDao();
        bookDao.setJdbcTemplate(createJdbcTemplate());
    }

    @Test
    public void matchAndQueryBook_shouldReturnExpectedResults() {
        assertTrue(bookDao.matchBook("BookA") > 0);
        ArrayList<Book> books = bookDao.queryBook("BookA");
        assertEquals(books.size(), 1);
        assertEquals(books.get(0).getName(), "BookA");
    }

    @Test
    public void getAllBooks_shouldReturnSeedData() {
        assertTrue(bookDao.getAllBooks().size() >= 2);
    }

    @Test
    public void addBook_shouldInsertRow() {
        Book book = new Book();
        book.setName("NewBook");
        book.setAuthor("NA");
        book.setPublish("NP");
        book.setIsbn("1234567890999");
        book.setIntroduction("N");
        book.setLanguage("CN");
        book.setPrice(new BigDecimal("19.99"));
        book.setPubdate(new Date());
        book.setClassId(1);
        book.setPressmark(1);
        book.setState(1);
        assertEquals(bookDao.addBook(book), 1);
        assertTrue(bookDao.matchBook("NewBook") > 0);
    }

    @Test
    public void getEditDeleteBook_shouldWork() {
        Book b = bookDao.getBook(10000001L);
        assertEquals(b.getName(), "BookA");

        b.setName("BookA-Edit");
        assertEquals(bookDao.editBook(b), 1);
        assertEquals(bookDao.getBook(10000001L).getName(), "BookA-Edit");

        assertEquals(bookDao.deleteBook(10000002L), 1);
    }
}
