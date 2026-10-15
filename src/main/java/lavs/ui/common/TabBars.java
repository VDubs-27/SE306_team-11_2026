package lavs.ui.common;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Makes a pill-style tab bar behave well: tabs share the width evenly (so the bar fills the card and
 * doesn't shift when the bold selected tab changes), the card fits the open tab, and tabs can show a count.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class TabBars {

    /** Horizontal padding of the tab header area (app.css). */
    private static final double TAB_BAR_PADDING = 24;
    /** Horizontal padding inside each tab (app.css). */
    private static final double TAB_PADDING = 16;

    private TabBars() {
    }

    /** Sets up even widths and sizing to the open tab; call once the tabs are in place. */
    public static void setUp(TabPane tabPane) {
        tabPane.widthProperty().addListener((obs, oldWidth, width) -> spreadTabs(tabPane, width.doubleValue()));
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, tab) -> sizeToTab(tabPane, tab));
        sizeToTab(tabPane, tabPane.getSelectionModel().getSelectedItem());
    }

    /** Puts a small count pill beside the tab name. */
    public static void setCount(Tab tab, long count) {
        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("tab-count");
        tab.setGraphic(countLabel);
    }

    /**
     * A TabPane is as tall as its tallest tab, even when that tab isn't showing. Only the selected
     * tab's content counts towards the size, so the card fits whichever tab is open. Uses the tab
     * passed in, because each tab's own isSelected() hasn't caught up yet when selection changes.
     */
    private static void sizeToTab(TabPane tabPane, Tab selected) {
        for (Tab tab : tabPane.getTabs()) {
            tab.getContent().setManaged(tab == selected);
        }
    }

    /** Gives every tab the same width, so the bar fills the card and doesn't shift on selection. */
    private static void spreadTabs(TabPane tabPane, double width) {
        // Half a pixel spare, so rounding never pushes the last tab into the overflow menu
        double tabWidth = (width - TAB_BAR_PADDING - 0.5) / tabPane.getTabs().size() - TAB_PADDING;
        tabPane.setTabMinWidth(tabWidth);
        tabPane.setTabMaxWidth(tabWidth);
    }
}
