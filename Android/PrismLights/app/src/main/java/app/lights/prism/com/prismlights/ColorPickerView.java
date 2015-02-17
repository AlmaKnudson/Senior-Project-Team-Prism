package app.lights.prism.com.prismlights;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;

public class ColorPickerView extends View {


    private Rect currentBounds;
    private Rect currentInnerBounds;
    private RectF selectorRect;
    private Paint currentColor;
    private Bitmap cached;
    private int positionX;
    private int positionY;
    private float[] currentXYColor;
    private float[] tempXYColor;
    private ColorChangedListener colorChangedListener;

    private static double MIN_Y = 0.0503509;
    private static double MAX_Y = 0.5157895;
    private static double MID_Y = 0.3210526;

    public ColorPickerView(Context context) {
        super(context);
        construct();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct();
    }

    private void construct() {
        currentBounds = new Rect(0, 0, 0, 0);
        currentInnerBounds = new Rect(0, 0, 0, 0);
        currentColor = new Paint();
        currentColor.setFlags(Paint.ANTI_ALIAS_FLAG);
        selectorRect = new RectF(0f, 0f, 0f, 0f);
        currentXYColor = new float[]{0.4089552f, 0.5157895f};
        tempXYColor = currentXYColor;
        setPositionFromColor();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        currentBounds.set(getPaddingLeft(), getPaddingTop(),
                width - getPaddingLeft() + getPaddingRight(),
                height + getPaddingTop() - getPaddingBottom());
        currentInnerBounds.set(currentBounds);
        currentInnerBounds.inset(40, 40);
        setPositionFromColor();
        cached = null;

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0), resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    public void setColorChangedListener(ColorChangedListener colorChangedListener) {
        this.colorChangedListener = colorChangedListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(cached == null) {
            currentColor.setStyle(Paint.Style.FILL);
            cached = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas forCache = new Canvas(cached);

            //(0.4089552, 0.5157895)
            //(0.1661765, 0.0503509)
            //(0.6731343, 0.3210526)
            for(int y = currentInnerBounds.top; y < currentInnerBounds.bottom; y+=5) {
                double yPercentage =(double)(y - currentInnerBounds.top)/ currentInnerBounds.height();
                double currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
                double minX = getMinXFromY(currentY);
                double maxX = getMaxXFromY(currentY);
                for(int x = currentInnerBounds.left; x < currentInnerBounds.right; x+=5) {
                    double xPercentage = (double)(x-currentInnerBounds.left)/ currentInnerBounds.width();
                    float[] xYColor = new float[2];
                    double currentX = (maxX - minX) * xPercentage + minX;
                    xYColor[0] = (float) currentX;
                    xYColor[1] = (float) currentY;
                    currentColor.setColor(PHUtilities.colorFromXY(xYColor, HueBulbChangeUtility.colorXYModelForHue));
                    forCache.drawRect(x, y, x+5, y+5, currentColor);
                }
            }
//            currentColor.setColor(Color.WHITE);
//            currentColor.setStyle(Paint.Style.STROKE);
//            currentColor.setStrokeWidth(5.0f);
        }
        canvas.drawBitmap(cached, 0, 0, currentColor);
        selectorRect.set(positionX - 30, positionY - 30, positionX + 30, positionY + 30);
        currentColor.setStyle(Paint.Style.STROKE);
        currentColor.setStrokeWidth(7.0f);
        currentColor.setColor(Color.WHITE);
        canvas.drawOval(selectorRect, currentColor);
        currentColor.setColor(PHUtilities.colorFromXY(tempXYColor, HueBulbChangeUtility.colorXYModelForHue));
        currentColor.setStyle(Paint.Style.FILL);
        canvas.drawOval(selectorRect, currentColor);
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

    private void setPositionFromColor() {
        double currentXColor = currentXYColor[0];
        double currentYColor = currentXYColor[1];
        double currentYPercentage = (currentYColor - MIN_Y) / (MAX_Y - MIN_Y);
        double currentY = (currentYPercentage * currentInnerBounds.height()) + currentInnerBounds.top;
        double minX = getMinXFromY(currentYColor);
        double maxX = getMaxXFromY(currentYColor);
        double currentXPercentage = (currentXColor - minX) / (maxX - minX);
        double currentX = (currentXPercentage * currentInnerBounds.width()) + currentInnerBounds.left;
        setPositionX((int) Math.round(currentX));
        setPositionY((int) Math.round(currentY));
    }

    private void setColorFromPosition(float x, float y) {
        double yPercentage = (double)(y - currentInnerBounds.top)/ currentInnerBounds.height();
        double currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
        double minX = getMinXFromY(currentY);
        double maxX = getMaxXFromY(currentY);
        double xPercentage = (double)(x-currentInnerBounds.left)/ currentInnerBounds.width();
        double currentX = (maxX - minX) * xPercentage + minX;
        tempXYColor = new float[]{(float) currentX, (float) currentY};

    }

    public void setColor(float[] xY) {
        if(HueBulbChangeUtility.colorsEqual(xY, currentXYColor)) {
            return;
        }
        currentXYColor = xY;
        tempXYColor = xY;
        setPositionFromColor();
        invalidate();
    }

    private void setPositionX(int positionX) {
        this.positionX = positionX;
        if(positionX < currentInnerBounds.left) {
            this.positionX = currentInnerBounds.left;
        }
        if(positionX > currentInnerBounds.right) {
            this.positionX = currentInnerBounds.right;
        }
    }

    private void setPositionY(int positionY) {
        this.positionY = positionY;
        if(positionY < currentInnerBounds.top) {
            this.positionY = currentInnerBounds.top;
        }
        if(positionY > currentInnerBounds.bottom) {
            this.positionY = currentInnerBounds.bottom;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_CANCEL) {
            tempXYColor = currentXYColor;
            setPositionFromColor();
            invalidate();
            return true;
        }
        setPositionX((int) event.getX());
        setPositionY((int) event.getY());
        setColorFromPosition(positionX, positionY);
        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            currentXYColor = tempXYColor;
            colorChangedListener.onColorChanged(currentXYColor);
        }
        invalidate();
        return true;
    }
}
