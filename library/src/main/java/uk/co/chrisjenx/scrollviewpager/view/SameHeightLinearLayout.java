package uk.co.chrisjenx.scrollviewpager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Fixed orientation LinearLayout that measures its children to all be the same height of the scroll view.
 * Similar to the way that the ViewPager measures its children to the width of it's self.
 * <p/>
 * This creates effective pages of content based off of this height.
 * <p/>
 * The child layout_height has no effect as this layout will set it onMeasure.
 * <p/>
 * MarginSupport is untested
 */
public class SameHeightLinearLayout extends LinearLayout
{
    public SameHeightLinearLayout(final Context context)
    {
        this(context, null);
    }

    public SameHeightLinearLayout(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public SameHeightLinearLayout(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int parentHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int parentWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        final int childHeightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
        final int childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidth, widthMeasureSpec);

        int measuredHeight = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View view = getChildAt(i);
            if (view.getVisibility() != GONE)
            {
                view.measure(childWidthSpec, childHeightSpec);
                measuredHeight += parentHeight;
            }
        }
        setMeasuredDimension(parentWidth, measuredHeight);
    }
}
