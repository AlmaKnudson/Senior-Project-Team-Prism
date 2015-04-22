package app.lights.prism.com.prismlights;

/**
 * A listener for determining if the edit button should be shown when a specific
 * fragment is shown in the viewpager on the real home screen
 */
public interface EditButtonPresentListener {
    /**
     * Called by the inner fragments when the edit button should change visibility
     * @param shown true if the button should be shown, false otherwise
     */
    public void editButtonPresent(boolean shown);
}
