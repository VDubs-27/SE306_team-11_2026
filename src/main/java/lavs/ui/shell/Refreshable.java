package lavs.ui.shell;

/**
 * A screen that can go out of date while another is open on top of it, such as search results after a
 * customer is edited. The navigator calls refresh() when the user comes back to it with Back. Screens
 * that hold what the user is part-way through, like the wizards, must not implement this.
 */
public interface Refreshable {

    /** Reads again whatever on the screen may have changed. */
    void refresh();
}