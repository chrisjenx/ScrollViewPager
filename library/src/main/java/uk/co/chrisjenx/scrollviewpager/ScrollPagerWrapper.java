package uk.co.chrisjenx.scrollviewpager;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.OverScroller;
import android.widget.ScrollView;
import android.widget.Scroller;

import static uk.co.chrisjenx.scrollviewpager.Utils.findOverScroller;
import static uk.co.chrisjenx.scrollviewpager.Utils.findScroller;

/**
 * Created with Intellij with Android, BIZZBY product.
 * See licencing for usage of this code.
 * <p/>
 * User: chris
 * Date: 03/05/2013
 * Time: 20:50
 */
public class ScrollPagerWrapper
{
    private static final boolean DEBUG = true;
    private static final String TAG = "OldPager";
    /**
     * ScrollView to try and intercept
     */
    protected ScrollView mScrollView;
    /**
     * Depending on API Version we use #mScroller or #mOverScroller.
     * This is used API 4 - 8
     */
    protected Scroller mScroller;
    /**
     * Depending on API Version we use #mOverScroller or #mScroller.
     * This is the used API 9+
     */
    protected OverScroller mOverScroller;


    public ScrollPagerWrapper(final ScrollView scrollView)
    {
        findScrollerFromScrollView(scrollView);
        Log.d(TAG, String.format("Scroller[%h],OverScroller[%h]", mScroller, mOverScroller));

    }

    public void fling(final int velocityY)
    {
    }

    public void computeScroll()
    {
    }

    public boolean onTouchEvent(final MotionEvent ev)
    {
        return false;
    }

    public boolean onInterceptTouchEvent(final MotionEvent ev)
    {
        return false;
    }

    /**
     * Will find the valid Scroller for
     *
     * @param scrollView
     */
    private void findScrollerFromScrollView(final ScrollView scrollView)
    {
        if (scrollView == null) return;
        if (Utils.SUPPORTS_GINGER)
            mOverScroller = findOverScroller(scrollView);
        else
            mScroller = findScroller(scrollView);
    }
}
