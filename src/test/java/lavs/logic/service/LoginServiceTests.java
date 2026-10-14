package lavs.logic.service;

import java.io.IOException;
import java.util.List;
import lavs.logic.adapter.ConstantsConnection;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.model.FieldError;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.loan.Loan;
import mainframe.client.Status;
import mainframe.client.StubConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoginServiceTests {

    private final ConstantsConnection connection = new ConstantsConnection()
            .category("LoanRateType", "Fixed", "Floating");
    private final LoginService service = new LoginService(new MainframeAdapter(connection));

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

        LoginResult result = new LoginService(new MainframeAdapter(counting)).login(blank, blank);

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertEquals(List.of(
                        new FieldError("userName", "User name is required."),
                        new FieldError("password", "Password is required.")), result.fieldErrors()),
                () -> Assertions.assertNull(result.errorMessage()),
                () -> Assertions.assertEquals(0, counting.calls)
        );
    }

    @Test
    void blankUserNameOnlyReportsUserName() {
        CountingConnection counting = new CountingConnection();

        LoginResult result = new LoginService(new MainframeAdapter(counting)).login(" ", "secret");

        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(
                        new FieldError("userName", "User name is required.")), result.fieldErrors()),
                () -> Assertions.assertEquals(0, counting.calls)
        );
    }

    @Test
    void blankPasswordOnlyReportsPassword() {
        CountingConnection counting = new CountingConnection();

        LoginResult result = new LoginService(new MainframeAdapter(counting)).login("alice", "");

        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(
                        new FieldError("password", "Password is required.")), result.fieldErrors()),
                () -> Assertions.assertEquals(0, counting.calls)
        );
    }

    @Test
    void credentialsArePassedToMainframe() {
        CountingConnection counting = new CountingConnection();
        counting.setupSuccessfulLoginResult("token", "alice", 42);

        new LoginService(new MainframeAdapter(counting)).login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, counting.calls),
                () -> Assertions.assertEquals("alice", counting.userName),
                () -> Assertions.assertEquals("secret", counting.password)
        );
    }

    @Test
    void secondLoginReplacesCurrentUser() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);
        service.login("alice", "secret");
        connection.setupSuccessfulLoginResult("token2", "bob", 43);

        service.login("bob", "secret");

        Assertions.assertEquals("bob", service.getCurrentUser());
    }

    @Test
    void failedLoginKeepsExistingSession() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);
        service.login("alice", "secret");
        connection.setupFailedLoginResult(3, "Incorrect password");

        LoginResult result = service.login("bob", "wrong");

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertEquals("alice", service.getCurrentUser())
        );
    }

    @Test
    void canLogInAgainAfterLogout() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);
        service.login("alice", "secret");
        service.logout();

        LoginResult result = service.login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertTrue(result.successful()),
                () -> Assertions.assertTrue(service.isLoggedIn())
        );
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
        "3, Unknown user name 'alice', The user name or password is incorrect.",
        "3, Incorrect password, The user name or password is incorrect.",
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
        LoginService failing = new LoginService(new MainframeAdapter(new StubConnection() {
            @Override
            public Status Login(String username, String password) throws IOException {
                throw new IOException("boom");
            }
        }));

        LoginResult result = failing.login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertEquals("Could not reach the mainframe. Check the connection and try again.",
                        result.errorMessage()),
                () -> Assertions.assertFalse(failing.isLoggedIn())
        );
    }

    @Test
    void logoutClearsCurrentUserAndConnection() {
        CountingConnection counting = new CountingConnection();
        counting.setupSuccessfulLoginResult("token", "alice", 42);
        LoginService loggedIn = new LoginService(new MainframeAdapter(counting));
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
    void successfulLoginLoadsConstants() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);

        service.login("alice", "secret");

        Assertions.assertEquals(List.of(Loan.RateType.FIXED, Loan.RateType.FLOATING),
                service.getConstants().options(ConstantCategory.RATE_TYPE));
    }

    @Test
    void noConstantsBeforeLogin() {
        Assertions.assertSame(Constants.EMPTY, service.getConstants());
    }

    @Test
    void failedLoginDoesNotLoadConstants() {
        connection.setupFailedLoginResult(3, "Incorrect password");

        service.login("alice", "wrong");

        Assertions.assertAll(
                () -> Assertions.assertSame(Constants.EMPTY, service.getConstants()),
                () -> Assertions.assertTrue(connection.constantRequests().isEmpty())
        );
    }

    @ParameterizedTest
    @CsvSource({
        "3, Invalid transaction number, '" + LoginService.CONSTANTS_FAILED_MESSAGE + "'",
        "10, Unavailable, Could not reach the mainframe. Check the connection and try again."
    })
    void constantsFailureFailsLoginAndLogsOut(int errorCode, String serverMessage, String expected) {
        CountingConnection counting = new CountingConnection();
        counting.setupSuccessfulLoginResult("token", "alice", 42);
        counting.category("LoanRateType", "Fixed").failItemList(errorCode, serverMessage);
        LoginService failing = new LoginService(new MainframeAdapter(counting));

        LoginResult result = failing.login("alice", "secret");

        Assertions.assertAll(
                () -> Assertions.assertEquals(expected, result.errorMessage()),
                () -> Assertions.assertFalse(failing.isLoggedIn()),
                () -> Assertions.assertSame(Constants.EMPTY, failing.getConstants()),
                () -> Assertions.assertEquals(1, counting.logouts)
        );
    }

    @Test
    void logoutClearsConstants() {
        connection.setupSuccessfulLoginResult("token", "alice", 42);
        service.login("alice", "secret");

        service.logout();

        Assertions.assertSame(Constants.EMPTY, service.getConstants());
    }

    @Test
    void requiresAdapter() {
        Assertions.assertThrows(NullPointerException.class, () -> new LoginService(null));
    }

    private static final class CountingConnection extends ConstantsConnection {
        private int calls;
        private int logouts;
        private String userName;
        private String password;

        @Override
        public Status Login(String username, String password) throws IOException {
            calls++;
            this.userName = username;
            this.password = password;
            return super.Login(username, password);
        }

        @Override
        public void Logout() {
            logouts++;
            super.Logout();
        }
    }
}
