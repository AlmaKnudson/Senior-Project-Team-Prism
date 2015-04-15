package app.lights.prism.com.prismlights;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;

public class ColorPickerViewGroup extends ViewGroup {


    private RectF currentBounds;
    private RectF currentInnerBounds;
    private int positionX;
    private int positionY;
    private float[] currentXYColor;
    private float[] tempXYColor;
    private ColorChangedListener colorChangedListener;
    private ColorPickerBackgroundView colorBackgroundView;
    private SelectorView selectorView;

    public final int halfSelectorWidth = PHUtilities.dpToPx(getResources().getDisplayMetrics().densityDpi, 30);

    public ColorPickerViewGroup(Context context) {
        super(context);
        construct(context);
    }

    public ColorPickerViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct(context);
    }

    public ColorPickerViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        colorBackgroundView.layout((int) currentBounds.left, (int) currentBounds.top, (int) currentBounds.right, (int) currentBounds.bottom);
//        int[] position = colorBackgroundView.getPositionFromColorBeforeDrawn(currentXYColor, new Rect((int) (currentBounds.left, (int) currentBounds.top, (int) currentBounds.right, (int) currentBounds.bottom));
//        positionX = position[0];
//        positionY = position[1];
        setPositionFromColor();
//        selectorView.layout(positionX - halfSelectorWidth, positionY - halfSelectorWidth, positionX + halfSelectorWidth, positionY + halfSelectorWidth);
        selectorView.layout(0, 0, 2*halfSelectorWidth, 2*halfSelectorWidth);
        translate();

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        currentBounds = new RectF(getPaddingLeft(), getPaddingTop(),
                width - getPaddingLeft() + getPaddingRight(),
                height + getPaddingTop() - getPaddingBottom());
        currentInnerBounds.set(currentBounds);
        currentInnerBounds.inset(halfSelectorWidth, halfSelectorWidth);
    }

    private void construct(Context context) {
        currentBounds = new RectF(0, 0, 0, 0);
        currentInnerBounds = new RectF(0, 0, 0, 0);
        currentXYColor = new float[]{0.4089552f, 0.5157895f};
        colorBackgroundView = new ColorPickerBackgroundView(context);
        selectorView = new SelectorView(context);
        colorBackgroundView.setPadding(halfSelectorWidth, halfSelectorWidth, halfSelectorWidth, halfSelectorWidth);
        setTempXYColor(currentXYColor);
        addView(colorBackgroundView);
        addView(selectorView);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0), resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    public void setColorChangedListener(ColorChangedListener colorChangedListener) {
        this.colorChangedListener = colorChangedListener;
    }


    private void setPositionFromColor() {
        int[] position = colorBackgroundView.getPositionFromColor(currentXYColor);
        setPositionX(position[0]);
        setPositionY(positionY = position[1]);
        //just becaue the colors aren't always in the right range
//        setTempXYColor(colorBackgroundView.getColorFromPosition(positionX, positionY));
    }

    private void setColorFromPosition(float x, float y) {
        setTempXYColor(colorBackgroundView.getColorFromPosition(x, y));
    }

    public void setColor(float[] xY) {
        if(HueBulbChangeUtility.colorsEqual(xY, currentXYColor)) {
            return;
        }
        currentXYColor = xY;
        setTempXYColor(xY);
        setPositionFromColor();
        translate();
    }

    private void setPositionX(int positionX) {
        this.positionX = positionX;
        if(positionX < currentInnerBounds.left) {
            this.positionX = (int) currentInnerBounds.left;
        }
        if(positionX > currentInnerBounds.right) {
            this.positionX = (int) currentInnerBounds.right;
        }
    }

    private void setPositionY(int positionY) {
        this.positionY = positionY;
        if(positionY < currentInnerBounds.top) {
            this.positionY = (int) currentInnerBounds.top;
        }
        if(positionY > currentInnerBounds.bottom) {
            this.positionY = (int) currentInnerBounds.bottom;
        }
    }

    private void setTempXYColor(float[] color) {
        selectorView.setColor(PHUtilities.colorFromXY(color, HueBulbChangeUtility.colorXYModelForHue));
        tempXYColor = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_CANCEL) {
            setTempXYColor(currentXYColor);
            setPositionFromColor();
            translate();
            return true;
        }
        setPositionX((int) event.getX());
        setPositionY((int) event.getY());
        setColorFromPosition(positionX, positionY);
        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            currentXYColor = tempXYColor;
            if(colorChangedListener != null) {
                colorChangedListener.onColorChanged(currentXYColor);
            }
        }
        translate();
        return true;
    }

    private void translate() {
        selectorView.setX(positionX - halfSelectorWidth);
        selectorView.setY(positionY - halfSelectorWidth);
        invalidate();
    }
}
