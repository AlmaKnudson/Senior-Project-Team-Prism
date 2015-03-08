package app.lights.prism.com.prismlights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;

public class ColorPickerBackgroundView extends View{


    private Rect currentBounds;
    private Paint currentColor;

    private static final double MIN_Y = 0.0503509;
    private static final double MAX_Y = 0.5157895;
    private static final double MID_Y = 0.3210526;

    private final double colorIncrement = PHUtilities.dpToPx(getResources().getDisplayMetrics().densityDpi, 8);
    public ColorPickerBackgroundView(Context context) {
        super(context);
        construct();
    }

    public ColorPickerBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public ColorPickerBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct();
    }

    private void construct() {
        currentBounds = new Rect(0, 0, 0, 0);
        currentColor = new Paint();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        currentBounds.set(getPaddingLeft(), getPaddingTop(),
                width - getPaddingRight(),
                height - getPaddingBottom());

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0), resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        currentColor.setStyle(Paint.Style.FILL);

        //(0.4089552, 0.5157895)
        //(0.1661765, 0.0503509)
        //(0.6731343, 0.3210526)
        for(int y = currentBounds.top; y < currentBounds.bottom; y+=colorIncrement) {
            double yPercentage =(double)(y - currentBounds.top)/ currentBounds.height();
            double currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
            double minX = getMinXFromY(currentY);
            double maxX = getMaxXFromY(currentY);
            for(int x = currentBounds.left; x < currentBounds.right; x+=colorIncrement) {
                double xPercentage = (double) (x - currentBounds.left) / currentBounds.width();
                float[] xYColor = new float[2];
                double currentX = (maxX - minX) * xPercentage + minX;
                xYColor[0] = (float) currentX;
                xYColor[1] = (float) currentY;
                currentColor.setColor(PHUtilities.colorFromXY(xYColor, HueBulbChangeUtility.colorXYModelForHue));
                canvas.drawRect(x, y, (int) (x + colorIncrement), (int) (y + colorIncrement), currentColor);
            }
        }
    }

    //y = mx + b
    //(0.4089552, 0.5157895)
    //(0.1661765, 0.0503509)
    // m = (0.51517895 - 0.0503509) / (0.4089552 - 0.1661765)
    // m = 1.9146162739976778
    // 0.05157895 = 1.9146162739976778 * 0.1661765 + b
    // b = 0.05157895 - 1.9146162739976778 * 0.1661765
    // y = 1.9146162739976778x + -0.2665852812559751
    // y + 0.2665852812559751 = 1.9146162739976778x
    // x = (y + 0.2665852812559751) / 1.9146162739976778
    private double getMinXFromY(double y) {

        return (y + 0.2665852812559751) / 1.9146162739976778;
    }

    private double getMaxXFromY(double y) {
        if(y > MID_Y) {
            //y = mx + b
            //(0.4089552, 0.5157895)
            //(0.6731343, 0.3210526)
            //m = (0.5157895 - 0.3210526) / (0.4089552 - 0.6731343)
            //m = -0.7371396904599948
            //0.5157895 = -0.7371396904599948 * (0.4089552) + b
            //b = 0.5157895 + 0.7371396904599948 * (0.4089552)
            // b = 0.8172466095400053
            // y = -0.7371396904599948x + 0.8172466095400053
            // y - 0.8172466095400053 = -0.7371396904599948x
            // x = (y - 0.8172466095400053) / -0.7371396904599948
            return (y - 0.8172466095400053) / -0.7371396904599948;
        }
        else {
            //y = mx + b
            //(0.1661765, 0.0503509)
            //(0.6731343, 0.3210526)
            //m = (0.0503509 - 0.3210526) / (0.1661765 - 0.6731343)
            //m = 0.533972847444107
            //0.0503509 = 0.533972847444107 * (0.1661765) + b
            //b =  -0.038382838883295654
            //y = 0.533972847444107x - 0.038382838883295654
            //x = (y + 0.038382838883295654) / 0.533972847444107
            return (y + 0.038382838883295654) / 0.533972847444107;
        }
    }

    public int[] getPositionFromColor(float[] currentXYColor) {
        double currentXColor = currentXYColor[0];
        double currentYColor = currentXYColor[1];
        double currentYPercentage = (currentYColor - MIN_Y) / (MAX_Y - MIN_Y);
        double currentY = (currentYPercentage * currentBounds.height()) + currentBounds.top;
        double minX = getMinXFromY(currentYColor);
        double maxX = getMaxXFromY(currentYColor);
        double currentXPercentage = (currentXColor - minX) / (maxX - minX);
        double currentX = (currentXPercentage * currentBounds.width()) + currentBounds.left;
        return new int[]{(int) Math.round(currentX), (int) Math.round(currentY)};
    }

    public float[] getColorFromPosition(float x, float y) {
        double yPercentage = (double)(y - currentBounds.top)/ currentBounds.height();
        double currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
        double minX = getMinXFromY(currentY);
        double maxX = getMaxXFromY(currentY);
        double xPercentage = (double)(x-currentBounds.left)/ currentBounds.width();
        double currentX = (maxX - minX) * xPercentage + minX;
        return new float[]{(float) currentX, (float) currentY};
    }
}
