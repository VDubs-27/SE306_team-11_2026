package lavs.ui.shell;

import lavs.logic.service.LoginService;
import lavs.logic.service.Session;

/**
 * The logic services the screens use, built once when the app starts and handed out by the
 * navigator. Every call to the mainframe goes through one of these.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record Services(Session session, LoginService login, CustomerServices customer, LoanServices loan) {
}
