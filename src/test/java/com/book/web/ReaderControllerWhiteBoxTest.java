package com.book.web;

import com.book.domain.ReaderCard;
import com.book.domain.ReaderInfo;
import com.book.service.LoginService;
import com.book.service.ReaderCardService;
import com.book.service.ReaderInfoService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

@Test(singleThreaded = true)
public class ReaderControllerWhiteBoxTest {

    private ReaderController controller;
    private StubReaderCardService readerCardService;
    private StubReaderInfoService readerInfoService;
    private StubLoginService loginService;

    @BeforeMethod
    public void setUp() {
        controller = new ReaderController();
        readerCardService = new StubReaderCardService();
        readerInfoService = new StubReaderInfoService();
        loginService = new StubLoginService();
        controller.setReaderCardService(readerCardService);
        controller.setReaderInfoService(readerInfoService);
        controller.setLoginService(loginService);
    }

    @Test
    public void readerRePasswdDo_shouldRedirectLogin_whenSessionMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.readerRePasswdDo(request, "old", "new", "new", redirect);

        assertEquals(view, "redirect:/login.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "登录已失效，请重新登录后再修改密码！");
    }

    @Test
    public void readerRePasswdDo_shouldFail_whenNewPasswordsNotSame() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(1001, "oldPass", "Tom"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.readerRePasswdDo(request, "oldPass", "new1", "new2", redirect);

        assertEquals(view, "redirect:/reader_repasswd.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "修改失败,两次输入的新密码不相同");
    }

    @Test
    public void readerRePasswdDo_shouldFail_whenOldPasswordWrong() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(1002, "correctOld", "Tom"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.readerRePasswdDo(request, "wrongOld", "newPass", "newPass", redirect);

        assertEquals(view, "redirect:/reader_repasswd.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "修改失败,原密码错误");
    }

    @Test
    public void readerRePasswdDo_shouldRefreshSession_whenPasswordUpdatedSuccessfully() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ReaderCard oldCard = buildReaderCard(1003, "oldPass", "Tom");
        request.getSession().setAttribute("readercard", oldCard);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        readerCardService.updatePasswdResult = true;
        loginService.readerCardToReturn = buildReaderCard(1003, "newPass", "Tom");

        String view = controller.readerRePasswdDo(request, "oldPass", "newPass", "newPass", redirect);

        assertEquals(view, "redirect:/reader_repasswd.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "密码修改成功！");
        assertEquals(readerCardService.lastReaderId, 1003);
        assertEquals(readerCardService.lastPasswd, "newPass");
        ReaderCard sessionCard = (ReaderCard) request.getSession().getAttribute("readercard");
        assertNotNull(sessionCard);
        assertEquals(sessionCard.getPasswd(), "newPass");
    }

    @Test
    public void readerRePasswdDo_shouldReturnFailureMessage_whenUpdateFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(1003, "oldPass", "Tom"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerCardService.updatePasswdResult = false;

        String view = controller.readerRePasswdDo(request, "oldPass", "newPass", "newPass", redirect);

        assertEquals(view, "redirect:/reader_repasswd.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "密码修改失败！");
    }

    @Test
    public void readerInfoEditDoReader_shouldRedirectLogin_whenSessionMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.readerInfoEditDoReader(
                request, "Tom", "M", "2000-01-01", "A", "110", redirect
        );

        assertEquals(view, "redirect:/login.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "登录已失效，请重新登录后再修改信息！");
    }

    @Test
    public void readerInfoEditDoReader_shouldUpdateCardNameAndInfo_whenNameChanged() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(2001, "p", "OldName"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        readerCardService.updateNameResult = true;
        readerInfoService.editReaderInfoResult = true;
        loginService.readerCardToReturn = buildReaderCard(2001, "p", "NewName");

        String view = controller.readerInfoEditDoReader(
                request, "NewName", "M", "2001-02-03", "Addr", "120", redirect
        );

        assertEquals(view, "redirect:/reader_info.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "信息修改成功！");
        assertEquals(readerCardService.updateNameCallCount, 1);
        assertEquals(readerCardService.lastUpdateNameReaderId, 2001);
        assertEquals(readerCardService.lastUpdateName, "NewName");
        assertEquals(readerInfoService.editReaderInfoCallCount, 1);
        ReaderCard sessionCard = (ReaderCard) request.getSession().getAttribute("readercard");
        assertEquals(sessionCard.getName(), "NewName");
    }

    @Test
    public void readerInfoEditDoReader_shouldOnlyUpdateInfo_whenNameUnchanged() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(2002, "p", "SameName"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        readerInfoService.editReaderInfoResult = true;
        loginService.readerCardToReturn = buildReaderCard(2002, "p", "SameName");

        String view = controller.readerInfoEditDoReader(
                request, "SameName", "F", "2002-03-04", "Addr2", "130", redirect
        );

        assertEquals(view, "redirect:/reader_info.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "信息修改成功！");
        assertEquals(readerCardService.updateNameCallCount, 0);
        assertEquals(readerInfoService.editReaderInfoCallCount, 1);
    }

    @Test
    public void allReaders_shouldReturnAdminReaderListPage() {
        ArrayList<ReaderInfo> readers = new ArrayList<ReaderInfo>();
        readers.add(new ReaderInfo());
        readerInfoService.readerInfosResult = readers;

        ModelAndView mv = controller.allBooks();
        assertEquals(mv.getViewName(), "admin_readers");
        assertSame(mv.getModel().get("readers"), readers);
    }

    @Test
    public void readerDelete_shouldShowSucc_whenDeleteSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("readerId", "2001");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.deleteReaderInfoResult = true;

        String view = controller.readerDelete(request, redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "删除成功！");
    }

    @Test
    public void readerDelete_shouldShowError_whenDeleteFail() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("readerId", "2001");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.deleteReaderInfoResult = false;

        String view = controller.readerDelete(request, redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "删除失败！");
    }

    @Test
    public void toReaderInfo_shouldRedirectLogin_whenSessionMissing() {
        ModelAndView mv = controller.toReaderInfo(new MockHttpServletRequest());
        assertEquals(mv.getViewName(), "redirect:/login.html");
    }

    @Test
    public void toReaderInfo_shouldReturnReaderInfoPage_whenSessionExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(3001, "p", "N"));
        ReaderInfo info = new ReaderInfo();
        info.setReaderId(3001);
        readerInfoService.getReaderInfoResult = info;

        ModelAndView mv = controller.toReaderInfo(request);
        assertEquals(mv.getViewName(), "reader_info");
        assertSame(mv.getModel().get("readerinfo"), info);
    }

    @Test
    public void readerInfoEdit_shouldReturnAdminEditPage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("readerId", "3002");
        ReaderInfo info = new ReaderInfo();
        readerInfoService.getReaderInfoResult = info;

        ModelAndView mv = controller.readerInfoEdit(request);
        assertEquals(mv.getViewName(), "admin_reader_edit");
        assertSame(mv.getModel().get("readerInfo"), info);
    }

    @Test
    public void readerInfoEditDo_shouldSuccess_whenNameChangedAndBothUpdatesSucceed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3003");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        ReaderCard old = buildReaderCard(3003, "p", "Old");
        loginService.readerCardToReturn = old;
        readerCardService.updateNameResult = true;
        readerInfoService.editReaderInfoResult = true;

        String view = controller.readerInfoEditDo(request, "New", "男", "2001-01-01", "A", "1", redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "读者信息修改成功！");
    }

    @Test
    public void readerInfoEditDo_shouldError_whenNameChangedButUpdateNameFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3004");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        loginService.readerCardToReturn = buildReaderCard(3004, "p", "Old");
        readerCardService.updateNameResult = false;
        readerInfoService.editReaderInfoResult = true;

        String view = controller.readerInfoEditDo(request, "New", "男", "2001-01-01", "A", "1", redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "读者信息修改失败！");
    }

    @Test
    public void readerInfoEditDo_shouldSuccess_whenNameUnchangedAndEditSucceeds() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3005");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        loginService.readerCardToReturn = buildReaderCard(3005, "p", "Same");
        readerInfoService.editReaderInfoResult = true;

        String view = controller.readerInfoEditDo(request, "Same", "男", "2001-01-01", "A", "1", redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "读者信息修改成功！");
    }

    @Test
    public void readerInfoEditDo_shouldError_whenNameUnchangedAndEditFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3005");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        loginService.readerCardToReturn = buildReaderCard(3005, "p", "Same");
        readerInfoService.editReaderInfoResult = false;

        String view = controller.readerInfoEditDo(request, "Same", "男", "2001-01-01", "A", "1", redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "读者信息修改失败！");
    }

    @Test
    public void readerInfoEditDo_shouldHandleInvalidBirthFormat_inNameChangedBranch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "3006");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        loginService.readerCardToReturn = buildReaderCard(3006, "p", "Old");
        readerCardService.updateNameResult = true;
        readerInfoService.editReaderInfoResult = true;

        String view = controller.readerInfoEditDo(request, "New", "男", "bad-date", "A", "1", redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "读者信息修改成功！");
    }

    @Test
    public void readerInfoAdd_shouldReturnAddPage() {
        ModelAndView mv = controller.readerInfoAdd();
        assertEquals(mv.getViewName(), "admin_reader_add");
    }

    @Test
    public void readerRePasswd_shouldReturnReaderRepasswdPage() {
        ModelAndView mv = controller.readerRePasswd();
        assertEquals(mv.getViewName(), "reader_repasswd");
    }

    @Test
    public void readerInfoAddDo_shouldSuccess_whenBothInsertSucceed() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.addReaderInfoResult = true;
        readerCardService.addReaderCardResult = true;

        String view = controller.readerInfoAddDo("N", "男", "2000-01-01", "A", "123", 4001, redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "添加读者信息成功！");
    }

    @Test
    public void readerInfoAddDo_shouldFail_whenAnyInsertFails() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.addReaderInfoResult = true;
        readerCardService.addReaderCardResult = false;

        String view = controller.readerInfoAddDo("N", "男", "2000-01-01", "A", "123", 4001, redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "添加读者信息失败！");
    }

    @Test
    public void readerInfoAddDo_shouldHandleInvalidBirthFormat() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.addReaderInfoResult = true;
        readerCardService.addReaderCardResult = true;

        String view = controller.readerInfoAddDo("N", "男", "not-date", "A", "123", 4002, redirect);
        assertEquals(view, "redirect:/allreaders.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "添加读者信息成功！");
    }

    @Test
    public void readerInfoEditReader_shouldRedirectLogin_whenSessionMissing() {
        ModelAndView mv = controller.readerInfoEditReader(new MockHttpServletRequest());
        assertEquals(mv.getViewName(), "redirect:/login.html");
    }

    @Test
    public void readerInfoEditReader_shouldReturnEditPage_whenSessionExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(5001, "p", "N"));
        ReaderInfo info = new ReaderInfo();
        readerInfoService.getReaderInfoResult = info;

        ModelAndView mv = controller.readerInfoEditReader(request);
        assertEquals(mv.getViewName(), "reader_info_edit");
        assertSame(mv.getModel().get("readerinfo"), info);
    }

    @Test
    public void readerInfoEditDoReader_shouldError_whenNameChangedButAnyUpdateFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(2003, "p", "OldName"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        readerCardService.updateNameResult = false;
        readerInfoService.editReaderInfoResult = true;
        loginService.readerCardToReturn = buildReaderCard(2003, "p", "NewName");

        String view = controller.readerInfoEditDoReader(
                request, "NewName", "M", "2001-02-03", "Addr", "120", redirect
        );

        assertEquals(view, "redirect:/reader_info.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "信息修改失败！");
    }

    @Test
    public void readerInfoEditDoReader_shouldError_whenNameUnchangedAndEditFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(2004, "p", "SameName"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        readerInfoService.editReaderInfoResult = false;

        String view = controller.readerInfoEditDoReader(
                request, "SameName", "F", "2002-03-04", "Addr2", "130", redirect
        );

        assertEquals(view, "redirect:/reader_info.html");
        assertEquals(redirect.getFlashAttributes().get("error"), "信息修改失败！");
    }

    @Test
    public void readerInfoEditDoReader_shouldHandleInvalidBirthFormat_inNameChangedBranch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("readercard", buildReaderCard(2005, "p", "OldName"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        readerCardService.updateNameResult = true;
        readerInfoService.editReaderInfoResult = true;
        loginService.readerCardToReturn = buildReaderCard(2005, "p", "NewName");

        String view = controller.readerInfoEditDoReader(
                request, "NewName", "M", "bad-date", "Addr", "120", redirect
        );

        assertEquals(view, "redirect:/reader_info.html");
        assertEquals(redirect.getFlashAttributes().get("succ"), "信息修改成功！");
    }

    private ReaderCard buildReaderCard(int readerId, String passwd, String name) {
        ReaderCard readerCard = new ReaderCard();
        readerCard.setReaderId(readerId);
        readerCard.setPasswd(passwd);
        readerCard.setName(name);
        return readerCard;
    }

    private static class StubReaderCardService extends ReaderCardService {
        private boolean updatePasswdResult;
        private boolean updateNameResult;
        private boolean addReaderCardResult;
        private int lastReaderId;
        private String lastPasswd;
        private int updateNameCallCount;
        private int lastUpdateNameReaderId;
        private String lastUpdateName;

        @Override
        public boolean updatePasswd(int readerId, String passwd) {
            this.lastReaderId = readerId;
            this.lastPasswd = passwd;
            return updatePasswdResult;
        }

        @Override
        public boolean updateName(int readerId, String name) {
            this.updateNameCallCount++;
            this.lastUpdateNameReaderId = readerId;
            this.lastUpdateName = name;
            return updateNameResult;
        }

        @Override
        public boolean addReaderCard(ReaderInfo readerInfo) {
            return addReaderCardResult;
        }
    }

    private static class StubReaderInfoService extends ReaderInfoService {
        private boolean editReaderInfoResult;
        private boolean deleteReaderInfoResult;
        private boolean addReaderInfoResult;
        private ArrayList<ReaderInfo> readerInfosResult = new ArrayList<ReaderInfo>();
        private ReaderInfo getReaderInfoResult = new ReaderInfo();
        private int editReaderInfoCallCount;

        @Override
        public boolean editReaderInfo(com.book.domain.ReaderInfo readerInfo) {
            this.editReaderInfoCallCount++;
            return editReaderInfoResult;
        }

        @Override
        public boolean deleteReaderInfo(int readerId) {
            return deleteReaderInfoResult;
        }

        @Override
        public ArrayList<ReaderInfo> readerInfos() {
            return readerInfosResult;
        }

        @Override
        public ReaderInfo getReaderInfo(int readerId) {
            return getReaderInfoResult;
        }

        @Override
        public boolean addReaderInfo(ReaderInfo readerInfo) {
            return addReaderInfoResult;
        }
    }

    private static class StubLoginService extends LoginService {
        private ReaderCard readerCardToReturn;

        @Override
        public ReaderCard findReaderCardByUserId(int readerId) {
            return readerCardToReturn;
        }
    }
}
