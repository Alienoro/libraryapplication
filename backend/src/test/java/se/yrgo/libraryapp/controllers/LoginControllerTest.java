package se.yrgo.libraryapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jooby.Context;
import io.jooby.StatusCode;
import se.yrgo.libraryapp.dao.RoleDao;
import se.yrgo.libraryapp.dao.SessionDao;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.entities.forms.LoginData;
import se.yrgo.libraryapp.services.UserService;

class LoginControllerTest {
    private UserService userService;
    private RoleDao roleDao;
    private SessionDao sessionDao;
    private LoginController loginController;
    private Context context;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        roleDao = mock(RoleDao.class);
        sessionDao = mock(SessionDao.class);
        context = mock(Context.class);

        loginController = new LoginController(userService, roleDao, sessionDao);
    }

    @Test
    void testLoginSuccess() {

        String username = "hampus";
        String password = "password123";
        LoginData loginData = new LoginData("hampus", "password123");

        UserId fakeUserId = new UserId(1);
        UUID fakeSessionId = UUID.randomUUID();
        List<Role> fakeRoles = List.of(Role.USER);

        when(userService.validate(username, password)).thenReturn(Optional.of(fakeUserId));
        when(sessionDao.create(fakeUserId)).thenReturn(fakeSessionId);
        when(roleDao.get(fakeUserId)).thenReturn(fakeRoles);

        List<Role> result = loginController.login(context, null, loginData);

        assertThat(result).isEqualTo(fakeRoles);
        verify(context).setResponseCookie(any());
        verify(roleDao).get(fakeUserId);
    }
    @Test
    void testLoginFailureUnauthorized() {
        String username = "incorrectuser";
        String password = "incorrectpassword";
        LoginData loginData = new LoginData(username, password);

        when(userService.validate(username, password)).thenReturn(Optional.empty());

        List<Role> result = loginController.login(context, null, loginData);

        assertThat(result).isEmpty();
        verify(context).setResponseCode(StatusCode.UNAUTHORIZED);
        verifyNoInteractions(sessionDao, roleDao);
    }
}