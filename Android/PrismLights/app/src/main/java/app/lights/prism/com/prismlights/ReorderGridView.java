package app.lights.prism.com.prismlights;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class ReorderGridView extends GridView {

    private int dragPosition;
    private OnItemShiftedListener onItemShiftedListener;

    public ReorderGridView(Context context) {
        super(context);
        construct();
    }

    public ReorderGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public ReorderGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    private void construct() {
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dragPosition = position;
//                int children = ReorderGridView.this.getChildCount();
//                for(int i = 0; i < children; i++) {
//                    //make children jiggle
//                }
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(null, shadowBuilder, null, 0);
                return false;
            }
        });
        setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(event.getAction() == DragEvent.ACTION_DROP) {
                    int position = pointToPosition((int) event.getX(), (int) event.getY());
                    if(position >= 0) {
                        if(position != dragPosition) {
                            if(onItemShiftedListener!= null) {
                                onItemShiftedListener.onItemShifted(dragPosition, position);
                            }
                        }
//                        int children = ReorderGridView.this.getChildCount();
//                        for(int i = 0; i < children; i++) {
//                            //make children stop jiggling
//                        }
                    }
                } else if(getBottom() - event.getY() < 50 && event.getX() < getRight() - (getWidth() / 4)) {
                    smoothScrollByOffset(50);
                } else if(event.getY() - getTop() < 50) {
                    smoothScrollByOffset(-50);
                }
                return true;
            }
        });
    }

    public void setOnItemShiftedListener(OnItemShiftedListener onItemShiftedListener) {
        this.onItemShiftedListener = onItemShiftedListener;
    }
}
