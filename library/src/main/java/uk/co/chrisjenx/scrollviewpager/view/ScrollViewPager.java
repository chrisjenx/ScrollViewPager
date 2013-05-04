package uk.co.chrisjenx.scrollviewpager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import uk.co.chrisjenx.scrollviewpager.ScrollPagerWrapper;


/**
 * Created with Intellij with Android, BIZZBY product.
 * See licencing for usage of this code.
 * <p/>
 * User: chris
 * Date: 03/05/2013
 * Time: 20:41
 */
public class ScrollViewPager extends ScrollView
{
    protected final ScrollPagerWrapper mPagerWrapper;

    public ScrollViewPager(final Context context)
    {
        this(context, null);
    }

    public ScrollViewPager(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        mPagerWrapper = new ScrollPagerWrapper(this);
    }

    public ScrollViewPager(final Context context, final AttributeSet attrs, final int defStyle)
    {
        this(context, attrs);
    }

    @Override
    public void fling(final int velocityY)
    {
        // We want the scroll view to calculate the fling first so we can then "adjust it" later.
        super.fling(velocityY);
        mPagerWrapper.fling(velocityY);
    }

    @Override
    public void computeScroll()
    {
        // We want the scroll view to compute scroll so we can then "adjust" later.
        super.computeScroll();
        mPagerWrapper.computeScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev)
    {
        if (mPagerWrapper.onInterceptTouchEvent(ev))
            return true;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev)
    {
        if (mPagerWrapper.onTouchEvent(ev))
            return true;
        return super.onTouchEvent(ev);
    }
}
