package app.lights.prism.com.prismlights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


import com.philips.lighting.hue.sdk.utilities.PHUtilities;

import java.util.ArrayList;
import java.util.List;

public class SingleFavoriteView extends View{
    private Rect currentBounds;
    private Paint currentColor;
    private List<Integer> colors;
    private Path clipPath;

    private final float clipPathRounding = PHUtilities.dpToPx(getResources().getDisplayMetrics().densityDpi, 10);
    private final float rectangleStrokeWidth = PHUtilities.dpToPx(getResources().getDisplayMetrics().densityDpi, 10);

    public SingleFavoriteView(Context context) {
        super(context);
        construct();
    }

    public SingleFavoriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public SingleFavoriteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct();
    }

    private void construct() {
        currentBounds = new Rect(0, 0, 0, 0);
        currentColor = new Paint();
        currentColor.setFlags(Paint.ANTI_ALIAS_FLAG);
        colors = new ArrayList<Integer>();
        colors.add(Color.BLACK);
        clipPath = new Path();
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        currentBounds.set(getPaddingLeft(), getPaddingTop(),
                width - getPaddingRight(),
                height - getPaddingBottom());
        clipPath.reset();
        RectF clippingRect = new RectF(currentBounds);
        clipPath.addRoundRect(clippingRect, clipPathRounding, clipPathRounding, Path.Direction.CW);

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0), resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    public void setColors(List<Integer> colors) {
        if(colors.size() > 0) {
            this.colors = colors;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.clipPath(clipPath);
        currentColor.setStyle(Paint.Style.FILL);
        int rectWidth = Math.round((float) currentBounds.width() / colors.size());
        int currentX = currentBounds.left;
        for(Integer color: colors) {
            int nextX = currentX + rectWidth;
            currentColor.setColor(color);
            canvas.drawRect(currentX, currentBounds.top, nextX, currentBounds.bottom, currentColor);
            currentX = nextX;
        }
        currentColor.setStyle(Paint.Style.STROKE);
        currentColor.setColor(Color.WHITE);
        currentColor.setStrokeWidth(rectangleStrokeWidth);
        canvas.drawPath(clipPath, currentColor);

    }
}
