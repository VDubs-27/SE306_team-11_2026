package lavs.ui.shell;

/** Implemented by any controller that needs to move between screens. */
public interface NavigatorAware {
    void setNavigator(Navigator navigator);
}