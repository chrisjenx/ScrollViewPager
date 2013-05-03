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
public class ScollViewPager extends ScrollView
{
    protected final ScrollPagerWrapper mPagerWrapper;

    public ScollViewPager(final Context context)
    {
        this(context, null);
    }

    public ScollViewPager(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        mPagerWrapper = new ScrollPagerWrapper(this);
    }

    public ScollViewPager(final Context context, final AttributeSet attrs, final int defStyle)
    {
        this(context, attrs);
    }

    @Override
    public void fling(final int velocityY)
    {
        super.fling(velocityY);
        mPagerWrapper.fling(velocityY);
    }

    @Override
    public void computeScroll()
    {
        super.computeScroll();
        mPagerWrapper.computeScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev)
    {
        return mPagerWrapper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev)
    {
        return mPagerWrapper.onTouchEvent(ev) || super.onTouchEvent(ev);
    }
}
