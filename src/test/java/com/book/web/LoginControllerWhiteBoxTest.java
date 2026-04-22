package com.book.web;

import com.book.domain.Admin;
import com.book.domain.ReaderCard;
import com.book.service.LoginService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

@Test(singleThreaded = true)
public class LoginControllerWhiteBoxTest {

    private LoginController controller;
    private StubLoginService loginService;

    @BeforeMethod
    public void setUp() {
        controller = new LoginController();
        loginService = new StubLoginService();
        controller.setLoginService(loginService);
    }

    @Test
    public void loginCheck_shouldSetAdminSession_whenAdminCredentialsValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "1000");
        request.setParameter("passwd", "admin-pass");

        loginService.readerMatch = false;
        loginService.adminMatch = true;

        Object result = controller.loginCheck(request);
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) result;

        assertEquals(response.get("stateCode"), "1");
        assertEquals(response.get("msg"), "管理员登陆成功！");

        Object adminObj = request.getSession().getAttribute("admin");
        assertNotNull(adminObj);
        assertTrue(adminObj instanceof Admin);
        assertNull(request.getSession().getAttribute("readercard"));
    }

    @Test
    public void toLogin_shouldInvalidateSessionAndReturnIndex() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("k", "v");

        String view = controller.toLogin(request);

        assertEquals(view, "index");
    }

    @Test
    public void logout_shouldInvalidateSessionAndRedirectToLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("k", "v");

        String view = controller.logout(request);

        assertEquals(view, "redirect:/login.html");
    }

    @Test
    public void loginCheck_shouldSetReaderSession_whenReaderCredentialsValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "2000");
        request.setParameter("passwd", "reader-pass");

        loginService.readerMatch = true;
        loginService.adminMatch = false;
        loginService.readerCardToReturn = buildReaderCard(2000, "reader-pass", "Alice");

        Object result = controller.loginCheck(request);
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) result;

        assertEquals(response.get("stateCode"), "2");
        assertEquals(response.get("msg"), "读者登陆成功！");

        Object readerObj = request.getSession().getAttribute("readercard");
        assertNotNull(readerObj);
        assertTrue(readerObj instanceof ReaderCard);
        assertNull(request.getSession().getAttribute("admin"));
    }

    @Test
    public void loginCheck_shouldChooseAdminBranch_whenBothAdminAndReaderMatch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "1001");
        request.setParameter("passwd", "pass");

        loginService.readerMatch = true;
        loginService.adminMatch = true;

        Object result = controller.loginCheck(request);
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) result;

        assertEquals(response.get("stateCode"), "1");
        assertNotNull(request.getSession().getAttribute("admin"));
        assertNull(request.getSession().getAttribute("readercard"));
    }

    @Test
    public void loginCheck_shouldReturnFail_whenCredentialsInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3000");
        request.setParameter("passwd", "wrong");

        loginService.readerMatch = false;
        loginService.adminMatch = false;

        Object result = controller.loginCheck(request);
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) result;

        assertEquals(response.get("stateCode"), "0");
        assertEquals(response.get("msg"), "账号或密码错误！");
        assertNull(request.getSession().getAttribute("admin"));
        assertNull(request.getSession().getAttribute("readercard"));
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void loginCheck_shouldThrowException_whenIdIsNonNumeric() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "abc");
        request.setParameter("passwd", "whatever");

        controller.loginCheck(request);
    }

    @Test
    public void reAdminPasswdDo_shouldRedirectToLogin_whenSessionAdminMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.reAdminPasswdDo(
                request,
                "old",
                "new",
                "new",
                redirectAttributes
        );

        assertEquals(viewName, "redirect:/login.html");
        assertEquals(
                redirectAttributes.getFlashAttributes().get("error"),
                "登录已失效，请重新登录后再修改密码！"
        );
    }

    @Test
    public void reAdminPasswdDo_shouldReturnSuccess_whenOldPasswordCorrectAndUpdateSucceeds() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Admin admin = new Admin();
        admin.setAdminId(20170001);
        request.getSession().setAttribute("admin", admin);

        loginService.adminPasswd = "old";
        loginService.adminRepasswdResult = true;

        String viewName = controller.reAdminPasswdDo(
                request, "old", "new", "new", redirectAttributes
        );

        assertEquals(viewName, "redirect:/admin_repasswd.html");
        assertEquals(redirectAttributes.getFlashAttributes().get("succ"), "密码修改成功！");
        assertTrue(loginService.adminRepasswdCalled);
    }

    @Test
    public void reAdminPasswdDo_shouldReturnError_whenOldPasswordCorrectButUpdateFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Admin admin = new Admin();
        admin.setAdminId(20170001);
        request.getSession().setAttribute("admin", admin);

        loginService.adminPasswd = "old";
        loginService.adminRepasswdResult = false;

        String viewName = controller.reAdminPasswdDo(
                request, "old", "new", "new", redirectAttributes
        );

        assertEquals(viewName, "redirect:/admin_repasswd.html");
        assertEquals(redirectAttributes.getFlashAttributes().get("error"), "密码修改失败！");
    }

    @Test
    public void reAdminPasswdDo_shouldReturnError_whenOldPasswordWrong() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Admin admin = new Admin();
        admin.setAdminId(20170001);
        request.getSession().setAttribute("admin", admin);

        loginService.adminPasswd = "old";

        String viewName = controller.reAdminPasswdDo(
                request, "wrong", "new", "new", redirectAttributes
        );

        assertEquals(viewName, "redirect:/admin_repasswd.html");
        assertEquals(redirectAttributes.getFlashAttributes().get("error"), "旧密码错误！");
        assertFalse(loginService.adminRepasswdCalled);
    }

    @Test
    public void toAdminMain_shouldReturnAdminMainView() {
        ModelAndView mv = controller.toAdminMain(new MockHttpServletResponse());
        assertEquals(mv.getViewName(), "admin_main");
    }

    @Test
    public void toReaderMain_shouldReturnReaderMainView() {
        ModelAndView mv = controller.toReaderMain(new MockHttpServletResponse());
        assertEquals(mv.getViewName(), "reader_main");
    }

    @Test
    public void reAdminPasswd_shouldReturnAdminRepasswdPage() {
        ModelAndView mv = controller.reAdminPasswd();
        assertEquals(mv.getViewName(), "admin_repasswd");
    }

    @Test
    public void notFind_shouldReturn404Page() {
        assertEquals(controller.notFind(), "404");
    }

    private ReaderCard buildReaderCard(int readerId, String passwd, String name) {
        ReaderCard readerCard = new ReaderCard();
        readerCard.setReaderId(readerId);
        readerCard.setPasswd(passwd);
        readerCard.setName(name);
        return readerCard;
    }

    private static class StubLoginService extends LoginService {
        private boolean readerMatch;
        private boolean adminMatch;
        private ReaderCard readerCardToReturn;
        private String adminPasswd;
        private boolean adminRepasswdResult;
        private boolean adminRepasswdCalled;

        @Override
        public boolean hasMatchReader(int readerId, String passwd) {
            return readerMatch;
        }

        @Override
        public boolean hasMatchAdmin(int adminId, String password) {
            return adminMatch;
        }

        @Override
        public ReaderCard findReaderCardByUserId(int readerId) {
            return readerCardToReturn;
        }

        @Override
        public String getAdminPasswd(int id) {
            return adminPasswd;
        }

        @Override
        public boolean adminRePasswd(int adminId, String newPasswd) {
            adminRepasswdCalled = true;
            return adminRepasswdResult;
        }
    }
}
