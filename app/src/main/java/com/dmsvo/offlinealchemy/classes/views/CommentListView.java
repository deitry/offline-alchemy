package com.dmsvo.offlinealchemy.classes.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.dmsvo.offlinealchemy.R;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class CommentListView extends ListView {
    private android.view.ViewGroup.LayoutParams params;
    private int oldCount = 0;

    public CommentListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void updateHeight()
    {
        oldCount = 0;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int cnt = getCount();
        if (cnt != oldCount)
        {
            params = getLayoutParams();
            int hei = calculateHeight();
            params.height = hei;
            setLayoutParams(params);
            requestLayout();
        }

        super.onDraw(canvas);
    }

    void drawLines(Canvas canvas, int dif)
    {
        Paint pnt = new Paint();
        pnt.setColor(Color.BLACK);
        pnt.setStyle(Paint.Style.STROKE);
        pnt.setStrokeWidth(4);

        int incr = 0;
        while (incr < getLayoutParams().height)
        {
            incr += 100;
            canvas.drawLine(0, incr, 200, incr, pnt);
        }
    }

    public int calculateHeight()
    {
        ListAdapter listAdapter = this.getAdapter();
        if (listAdapter == null) return 1;

        int cnt = getCount();
        int listWidth = this.getMeasuredWidth();

        int totalHeight = 0;

        for (int i = 0; i < cnt; i++)
        {
            View mView = listAdapter.getView(i, null, this);

//            int subTotal = 0;
//            ListView responses = mView.findViewById(R.id.responsesList);
//            //responses.invalidateViews();
//            ListAdapter subAd = responses.getAdapter();
//            if (subAd != null) {
//                for (int j = 0; j < subAd.getCount(); j++)
//                {
//                    View subView = subAd.getView(j, null, responses);
////                    int subWidth = responses.getMeasuredWidth();
//                    subView.measure(
//                            MeasureSpec.makeMeasureSpec(listWidth-10, MeasureSpec.EXACTLY),
//                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//
//                    subTotal += subView.getMeasuredHeight();
//                }
//            }

            mView.measure(
                    MeasureSpec.makeMeasureSpec(listWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight()
//                + subTotal
            ;
        }

        oldCount = cnt;
        params = getLayoutParams();

        totalHeight +=
                + (this.getDividerHeight() * (listAdapter.getCount() - 1));
        setLayoutParams(params);
        requestLayout();

        return totalHeight;
    }
}
