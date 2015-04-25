package app.lights.prism.com.prismlights;

/**
 * Interface allowing the select bulb view to communicate with it's fragment
 * when the bulbs selected change
 */
public interface CheckedNumberChangedListener {
    public void onCheckedNumberChanged(int checkedNumber);
}
