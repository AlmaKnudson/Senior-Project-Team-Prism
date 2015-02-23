package app.lights.prism.com.prismlights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;


//not going to bother with onMeasure because I won't need it in my viewgroup
public class SelectorView extends View {

    private RectF currentBounds;
    private Paint currentColor;
    private int color;

    public SelectorView(Context context) {
        super(context);
        currentBounds = new RectF(0, 0, 0, 0);
        currentColor = new Paint();
        currentColor.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        currentBounds = new RectF(getPaddingLeft(), getPaddingTop(),
                width - getPaddingLeft() + getPaddingRight(),
                height + getPaddingTop() - getPaddingBottom());
        currentBounds.inset(5, 5);

    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        currentColor.setStyle(Paint.Style.STROKE);
        currentColor.setStrokeWidth(7.0f);
        currentColor.setColor(Color.WHITE);
        canvas.drawOval(currentBounds, currentColor);
        currentColor.setColor(color);
        currentColor.setStyle(Paint.Style.FILL);
        canvas.drawOval(currentBounds, currentColor);
    }

}
