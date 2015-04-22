package app.lights.prism.com.prismlights;

/**
 * Allows the RealHomeFragment to set the edit button present listener on its inner fragments
 * if they allow it
 */
public interface EditButtonPresentCaller {

    /**
     * Allows the RealHomeFragment to set the edit button present listener on its inner fragments
     * if they allow it
     */
    public void setEditButtonPresentListener(EditButtonPresentListener listener);

    /**
     * Allows the RealHomeFragment to check on the edit button state when the view is selected
     * @return true if it should be present, false otherwise
     */
    public boolean shouldEditButtonBePresent();
}
