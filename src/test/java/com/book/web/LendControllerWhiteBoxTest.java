package com.book.web;

import com.book.domain.Book;
import com.book.domain.Lend;
import com.book.domain.ReaderCard;
import com.book.service.BookService;
import com.book.service.LendService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test(singleThreaded = true)
public class LendControllerWhiteBoxTest {

    private LendController controller;
    private StubLendService lendService;
    private StubBookService bookService;

    @BeforeMethod
    public void setUp() {
        controller = new LendController();
        lendService = new StubLendService();
        bookService = new StubBookService();
        controller.setLendService(lendService);
        controller.setBookService(bookService);
    }

    @Test
    public void bookLendDo_shouldRedirectAllBooks_whenLendSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "8");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        lendService.bookLendResult = true;

        String view = controller.bookLendDo(request, redirect, 101);

        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书借阅成功！");
        assertEquals(lendService.lastBookLendBookId, 8L);
        assertEquals(lendService.lastBookLendReaderId, 101);
    }

    @Test
    public void bookLendDo_shouldStillReturnSuccessMessage_whenLendFails_currentBehavior() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "9");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        lendService.bookLendResult = false;

        String view = controller.bookLendDo(request, redirect, 102);

        assertEquals(view, "redirect:/allbooks.html");
        // current code defect: false branch still writes success message
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书借阅成功！");
    }

    @Test
    public void bookReturn_shouldSetSuccessMessage_whenReturnSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "11");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        lendService.bookReturnResult = true;

        String view = controller.bookReturn(request, redirect);

        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "图书归还成功！");
        assertEquals(lendService.lastBookReturnBookId, 11L);
    }

    @Test
    public void bookReturn_shouldSetErrorMessage_whenReturnFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "12");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        lendService.bookReturnResult = false;

        String view = controller.bookReturn(request, redirect);

        assertEquals(view, "redirect:/allbooks.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "图书归还失败！");
    }

    @Test
    public void myLend_shouldRedirectLogin_whenReaderCardMissingInSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ModelAndView mv = controller.myLend(request);

        assertEquals(mv.getViewName(), "redirect:/login.html");
    }

    @Test
    public void myLend_shouldReturnReaderLendList_whenReaderCardExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ReaderCard rc = new ReaderCard();
        rc.setReaderId(2001);
        request.getSession().setAttribute("readercard", rc);

        ArrayList<Lend> lends = new ArrayList<Lend>();
        lends.add(new Lend());
        lendService.myLendListResult = lends;

        ModelAndView mv = controller.myLend(request);

        assertEquals(mv.getViewName(), "reader_lend_list");
        assertSame(mv.getModel().get("list"), lends);
        assertEquals(lendService.lastMyLendReaderId, 2001);
    }

    @Test
    public void lendList_shouldReturnAdminLendListView() {
        ArrayList<Lend> lends = new ArrayList<Lend>();
        lends.add(new Lend());
        lendService.lendListResult = lends;

        ModelAndView mv = controller.lendList();

        assertEquals(mv.getViewName(), "admin_lend_list");
        assertSame(mv.getModel().get("list"), lends);
    }

    @Test
    public void bookLend_shouldShowBookDetail_whenBookIdProvided() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("bookId", "66");
        Book book = new Book();
        book.setBookId(66L);
        book.setName("Borrow Me");
        bookService.bookToReturn = book;

        ModelAndView mv = controller.bookLend(request);

        assertEquals(mv.getViewName(), "admin_book_lend");
        assertSame(mv.getModel().get("book"), book);
        assertEquals(bookService.lastGetBookId, 66L);
    }

    private static class StubLendService extends LendService {
        private boolean bookLendResult;
        private boolean bookReturnResult;
        private ArrayList<Lend> lendListResult = new ArrayList<Lend>();
        private ArrayList<Lend> myLendListResult = new ArrayList<Lend>();
        private long lastBookLendBookId;
        private int lastBookLendReaderId;
        private long lastBookReturnBookId;
        private int lastMyLendReaderId;

        @Override
        public boolean bookLend(long bookId, int readerId) {
            this.lastBookLendBookId = bookId;
            this.lastBookLendReaderId = readerId;
            return bookLendResult;
        }

        @Override
        public boolean bookReturn(long bookId) {
            this.lastBookReturnBookId = bookId;
            return bookReturnResult;
        }

        @Override
        public ArrayList<Lend> myLendList(int readerId) {
            this.lastMyLendReaderId = readerId;
            return myLendListResult;
        }

        @Override
        public ArrayList<Lend> lendList() {
            return lendListResult;
        }
    }

    private static class StubBookService extends BookService {
        private Book bookToReturn;
        private long lastGetBookId;

        @Override
        public Book getBook(Long bookId) {
            this.lastGetBookId = bookId;
            return bookToReturn;
        }
    }
}
