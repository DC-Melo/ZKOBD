package zk.obd.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import zk.obd.R;


public class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

    private Context mContext;
    private Paint dividerPaint;
    private int dividerHeight;
    public DividerGridItemDecoration(Context context)
    {
        mContext=context;
        dividerPaint = new Paint();
        dividerPaint.setColor(context.getResources().getColor(R.color.log_date));
        dividerHeight = context.getResources().getDimensionPixelSize(R.dimen.list_padding);

    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = dividerHeight;//类似加了一个bottom padding
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount - 1; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + dividerHeight;
            c.drawRect(left, top, right, bottom, dividerPaint);
        }
    }
}
