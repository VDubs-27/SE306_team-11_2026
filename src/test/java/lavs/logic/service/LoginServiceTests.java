package lavs.logic.service;

import java.io.IOException;
import java.util.List;
import lavs.logic.model.FieldError;
import mainframe.client.Status;
import mainframe.client.StubConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoginServiceTests {

    private final StubConnection connection = new StubConnection();
    private final LoginService service = new LoginService(connection);

    @Test
    void successfulLoginRecordsCurrentUser() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);

        LoginResult result = service.login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertTrue(result.successful()),
                () -> Assertions.assertTrue(service.isLoggedIn()),
                () -> Assertions.assertEquals("alice", service.getCurrentUser())
        );
    }

    @Test
    void notLoggedInBeforeLogin() {
        Assertions.assertAll(
                () -> Assertions.assertFalse(service.isLoggedIn()),
                () -> Assertions.assertNull(service.getCurrentUser())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankFieldsAreRejectedWithoutCallingMainframe(String blank) {
        CountingConnection counting = new CountingConnection();

        LoginResult result = new LoginService(counting).login(blank, blank);

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertEquals(List.of(
                        new FieldError("userName", "User name is required."),
                        new FieldError("password", "Password is required.")), result.fieldErrors()),
                () -> Assertions.assertNull(result.errorMessage()),
                () -> Assertions.assertEquals(0, counting.calls)
        );
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
        "3, Invalid credentials, Invalid credentials",
        "3, '', The user name or password is incorrect.",
        "3, NULL, The user name or password is incorrect.",
        "10, Unavailable, Could not reach the mainframe. Check the connection and try again."
    })
    void failedLoginReportsMessage(int errorCode, String serverMessage, String expected) {
        connection.setupFailedLoginResult(errorCode, serverMessage);

        LoginResult result = service.login("alice", "wrong");

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertTrue(result.fieldErrors().isEmpty()),
                () -> Assertions.assertEquals(expected, result.errorMessage()),
                () -> Assertions.assertFalse(service.isLoggedIn())
        );
    }

    @Test
    void ioExceptionIsReportedAsNetworkFailure() {
        LoginService failing = new LoginService(new StubConnection() {
            @Override
            public Status Login(String username, String password) throws IOException {
                throw new IOException("boom");
            }
        });

        LoginResult result = failing.login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertEquals(LoginService.NETWORK_FAILURE_MESSAGE, result.errorMessage()),
                () -> Assertions.assertFalse(failing.isLoggedIn())
        );
    }

    @Test
    void logoutClearsCurrentUserAndConnection() {
        CountingConnection counting = new CountingConnection();
        counting.setupSuccessfulLoginResult("token", "alice", 42);
        LoginService loggedIn = new LoginService(counting);
        loggedIn.login("alice", "secret");

        loggedIn.logout();

        Assertions.assertAll(
                () -> Assertions.assertFalse(loggedIn.isLoggedIn()),
                () -> Assertions.assertNull(loggedIn.getCurrentUser()),
                () -> Assertions.assertEquals(1, counting.logouts)
        );
    }

    @Test
    void logoutWhenNotLoggedInIsSafe() {
        service.logout();

        Assertions.assertFalse(service.isLoggedIn());
    }

    @Test
    void requiresConnection() {
        Assertions.assertThrows(NullPointerException.class, () -> new LoginService(null));
    }

    private static final class CountingConnection extends StubConnection {
        private int calls;
        private int logouts;

        @Override
        public Status Login(String username, String password) throws IOException {
            calls++;
            return super.Login(username, password);
        }

        @Override
        public void Logout() {
            logouts++;
            super.Logout();
        }
    }
}
