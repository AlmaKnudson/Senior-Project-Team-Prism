package app.lights.prism.com.prismlights;

/**
 * An interface allowing the color picker to communicate when it's color has been changed by the user
 */
public interface ColorChangedListener {
    public void onColorChanged(float[] newColor);
}
