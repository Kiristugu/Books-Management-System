package com.book.web;

import com.book.domain.Book;
import com.book.service.BookService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

@Test(singleThreaded = true)
public class BookControllerWhiteBoxTest {

    private BookController controller;
    private StubBookService bookService;

    @BeforeMethod
    public void setUp() {
        controller = new BookController();
        bookService = new StubBookService();
        controller.setBookService(bookService);
    }

    @Test
    public void addBookDo_shouldPassLengthValidation_whenNameLengthEqualsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setName(repeat("A", 50));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.addBookResult = true;

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书添加成功！");
        assertNotNull(bookService.lastAddedBook);
        assertEquals(bookService.lastAddedBook.getName().length(), 50);
    }

    @Test
    public void addBookDo_shouldReject_whenNameExceedsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setName(repeat("N", 51));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/book_add.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "书名过长，最多 50 个字符。");
    }

    @Test
    public void addBookDo_shouldReject_whenAuthorExceedsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setAuthor(repeat("X", 51));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/book_add.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "作者过长，最多 50 个字符。");
    }

    @Test
    public void addBookDo_shouldReject_whenPublishExceedsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setPublish(repeat("P", 31));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/book_add.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "出版社过长，最多 30 个字符。");
    }

    @Test
    public void addBookDo_shouldReject_whenLanguageExceedsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setLanguage(repeat("L", 11));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/book_add.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "语言字段过长，最多 10 个字符。");
    }

    @Test
    public void bookEditDo_shouldReject_whenIsbnExceedsBoundary() {
        BookAddCommand cmd = buildValidCommand();
        cmd.setIsbn("12345678901234");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "10");

        String view = controller.bookEditDo(request, cmd, redirect);

        assertEquals(view, "redirect:/updatebook.html?bookId=10");
        assertEquals(redirect.getFlashAttributes().get("error"), "ISBN 过长，最多 13 个字符。");
    }

    @Test
    public void addBookDo_shouldReturnFailMessage_whenServiceAddFails() {
        BookAddCommand cmd = buildValidCommand();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.addBookResult = false;

        String view = controller.addBookDo(cmd, redirect);

        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书添加失败！");
    }

    @Test
    public void queryBookDo_shouldReturnBooksView_whenMatchExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ArrayList<Book> books = new ArrayList<Book>();
        books.add(new Book());
        bookService.matchBookResult = true;
        bookService.queryBookResult = books;

        ModelAndView mv = controller.queryBookDo(request, "Java");

        assertEquals(mv.getViewName(), "admin_books");
        assertSame(mv.getModel().get("books"), books);
    }

    @Test
    public void queryBookDo_shouldReturnError_whenNoMatch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        bookService.matchBookResult = false;

        ModelAndView mv = controller.queryBookDo(request, "NotExists");

        assertEquals(mv.getViewName(), "admin_books");
        assertEquals(mv.getModel().get("error"), "没有匹配的图书");
    }

    @Test
    public void readerQueryBook_shouldReturnReaderQueryPage() {
        ModelAndView mv = controller.readerQueryBook();
        assertEquals(mv.getViewName(), "reader_book_query");
    }

    @Test
    public void readerQueryBookDo_shouldRedirectWithBooks_whenMatchExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        ArrayList<Book> books = new ArrayList<Book>();
        books.add(new Book());
        bookService.matchBookResult = true;
        bookService.queryBookResult = books;

        String view = controller.readerQueryBookDo(request, "Java", redirect);

        assertEquals(view, "redirect:/reader_querybook.html");
        assertSame(redirect.getFlashAttributes().get("books"), books);
    }

    @Test
    public void readerQueryBookDo_shouldRedirectWithError_whenNoMatch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.matchBookResult = false;

        String view = controller.readerQueryBookDo(request, "None", redirect);

        assertEquals(view, "redirect:/reader_querybook.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "没有匹配的图书！");
    }

    @Test
    public void allBook_shouldReturnAdminBooksView() {
        ArrayList<Book> books = new ArrayList<Book>();
        books.add(new Book());
        bookService.allBooksResult = books;

        ModelAndView mv = controller.allBook();
        assertEquals(mv.getViewName(), "admin_books");
        assertSame(mv.getModel().get("books"), books);
    }

    @Test
    public void deleteBook_shouldReturnSuccess_whenDeleteAffectedOneRow() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "10");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.deleteBookRows = 1;

        String view = controller.deleteBook(request, redirect);
        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书删除成功！");
    }

    @Test
    public void deleteBook_shouldReturnError_whenDeleteFailed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "10");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.deleteBookRows = 0;

        String view = controller.deleteBook(request, redirect);
        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "图书删除失败！");
    }

    @Test
    public void addBook_shouldReturnAddPage() {
        ModelAndView mv = controller.addBook(new MockHttpServletRequest());
        assertEquals(mv.getViewName(), "admin_book_add");
    }

    @Test
    public void bookEdit_shouldLoadBookAndReturnEditPage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "77");
        Book b = new Book();
        b.setBookId(77L);
        bookService.bookToReturn = b;

        ModelAndView mv = controller.bookEdit(request);
        assertEquals(mv.getViewName(), "admin_book_edit");
        assertSame(mv.getModel().get("detail"), b);
    }

    @Test
    public void bookEditDo_shouldReturnSuccess_whenServiceEditSucceeds() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "90");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.editBookResult = true;

        String view = controller.bookEditDo(request, buildValidCommand(), redirect);
        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书修改成功！");
    }

    @Test
    public void bookEditDo_shouldReturnError_whenServiceEditFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "90");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        bookService.editBookResult = false;

        String view = controller.bookEditDo(request, buildValidCommand(), redirect);
        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "图书修改失败！");
    }

    @Test
    public void bookDetail_shouldReturnAdminDetailPage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "77");
        Book b = new Book();
        b.setBookId(77L);
        bookService.bookToReturn = b;

        ModelAndView mv = controller.bookDetail(request);
        assertEquals(mv.getViewName(), "admin_book_detail");
        assertSame(mv.getModel().get("detail"), b);
    }

    @Test
    public void readerBookDetail_shouldReturnReaderDetailPage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "78");
        Book b = new Book();
        b.setBookId(78L);
        bookService.bookToReturn = b;

        ModelAndView mv = controller.readerBookDetail(request);
        assertEquals(mv.getViewName(), "reader_book_detail");
        assertSame(mv.getModel().get("detail"), b);
    }

    @Test
    public void getBook_shouldPreserveBookIdDataFlow_fromControllerToService() {
        long expectedBookId = 77L;
        Book returned = new Book();
        returned.setBookId(expectedBookId);
        returned.setName("DataFlow");
        bookService.bookToReturn = returned;

        Book book = bookService.getBook(expectedBookId);

        assertEquals(bookService.lastGetBookId, expectedBookId);
        assertEquals(book.getBookId(), expectedBookId);
        assertEquals(book.getName(), "DataFlow");
    }

    private BookAddCommand buildValidCommand() {
        BookAddCommand cmd = new BookAddCommand();
        cmd.setName("Clean Code");
        cmd.setAuthor("Author");
        cmd.setPublish("Publisher");
        cmd.setIsbn("1234567890123");
        cmd.setIntroduction("intro");
        cmd.setLanguage("CN");
        cmd.setPrice(new BigDecimal("88.00"));
        cmd.setPubdate("2026-04-22");
        cmd.setClassId(1);
        cmd.setPressmark(1);
        cmd.setState(1);
        return cmd;
    }

    private String repeat(String s, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    private static class StubBookService extends BookService {
        private boolean matchBookResult;
        private ArrayList<Book> queryBookResult = new ArrayList<Book>();
        private ArrayList<Book> allBooksResult = new ArrayList<Book>();
        private boolean addBookResult;
        private int deleteBookRows;
        private Book bookToReturn;
        private Book lastAddedBook;
        private long lastGetBookId;
        private boolean editBookResult;

        @Override
        public boolean matchBook(String searchWord) {
            return matchBookResult;
        }

        @Override
        public ArrayList<Book> queryBook(String searchWord) {
            return queryBookResult;
        }

        @Override
        public boolean addBook(Book book) {
            this.lastAddedBook = book;
            return addBookResult;
        }

        @Override
        public ArrayList<Book> getAllBooks() {
            return allBooksResult;
        }

        @Override
        public int deleteBook(long bookId) {
            return deleteBookRows;
        }

        @Override
        public Book getBook(Long bookId) {
            this.lastGetBookId = bookId;
            return bookToReturn;
        }

        @Override
        public boolean editBook(Book book) {
            return editBookResult;
        }
    }
}
