package com.book.service;

import com.book.dao.BookDao;
import com.book.domain.Book;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class BookServiceWhiteBoxTest {

    private BookService bookService;
    private StubBookDao bookDao;

    @BeforeMethod
    public void setUp() {
        bookService = new BookService();
        bookDao = new StubBookDao();
        bookService.setBookDao(bookDao);
    }

    @Test
    public void queryBook_shouldReturnDaoResult() {
        ArrayList<Book> list = new ArrayList<Book>();
        list.add(new Book());
        bookDao.queryBookResult = list;

        ArrayList<Book> result = bookService.queryBook("java");

        assertSame(result, list);
    }

    @Test
    public void getAllBooks_shouldReturnDaoResult() {
        ArrayList<Book> list = new ArrayList<Book>();
        list.add(new Book());
        bookDao.allBooksResult = list;

        ArrayList<Book> result = bookService.getAllBooks();

        assertSame(result, list);
    }

    @Test
    public void deleteBook_shouldReturnDaoRows() {
        bookDao.deleteRows = 1;
        assertEquals(bookService.deleteBook(10L), 1);
    }

    @Test
    public void matchBook_shouldReturnTrue_whenCountPositive() {
        bookDao.matchCount = 2;
        assertTrue(bookService.matchBook("x"));
    }

    @Test
    public void matchBook_shouldReturnFalse_whenCountZero() {
        bookDao.matchCount = 0;
        assertFalse(bookService.matchBook("x"));
    }

    @Test
    public void addBook_shouldReturnTrue_whenDaoRowsPositive() {
        bookDao.addRows = 1;
        assertTrue(bookService.addBook(new Book()));
    }

    @Test
    public void addBook_shouldReturnFalse_whenDaoRowsZero() {
        bookDao.addRows = 0;
        assertFalse(bookService.addBook(new Book()));
    }

    @Test
    public void getBook_shouldReturnDaoBook() {
        Book b = new Book();
        b.setBookId(99L);
        bookDao.bookResult = b;

        Book result = bookService.getBook(99L);
        assertSame(result, b);
    }

    @Test
    public void editBook_shouldReturnTrue_whenDaoRowsPositive() {
        bookDao.editRows = 1;
        assertTrue(bookService.editBook(new Book()));
    }

    @Test
    public void editBook_shouldReturnFalse_whenDaoRowsZero() {
        bookDao.editRows = 0;
        assertFalse(bookService.editBook(new Book()));
    }

    private static class StubBookDao extends BookDao {
        private ArrayList<Book> queryBookResult = new ArrayList<Book>();
        private ArrayList<Book> allBooksResult = new ArrayList<Book>();
        private int deleteRows;
        private int matchCount;
        private int addRows;
        private Book bookResult = new Book();
        private int editRows;

        @Override
        public ArrayList<Book> queryBook(String sw) {
            return queryBookResult;
        }

        @Override
        public ArrayList<Book> getAllBooks() {
            return allBooksResult;
        }

        @Override
        public int deleteBook(long bookId) {
            return deleteRows;
        }

        @Override
        public int matchBook(String searchWord) {
            return matchCount;
        }

        @Override
        public int addBook(Book book) {
            return addRows;
        }

        @Override
        public Book getBook(Long bookId) {
            return bookResult;
        }

        @Override
        public int editBook(Book book) {
            return editRows;
        }
    }
}
