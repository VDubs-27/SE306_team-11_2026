package lavs.ui.shell;

import lavs.logic.service.LoginService;
import lavs.logic.service.Session;

/**
 * The logic services the screens use, built once when the app starts and handed out by the
 * navigator. More join as they're written, such as the customer services.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record Services(Session session, LoginService login) {
}
