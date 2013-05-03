package uk.co.chrisjenx.scrollviewpager;

import android.view.MotionEvent;
import android.widget.ScrollView;

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

    protected ScrollView mScrollView;

    public ScrollPagerWrapper(final ScrollView scrollView)
    {
    }

    public void fling(final int velocityY)
    {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void computeScroll()
    {
        //To change body of created methods use File | Settings | File Templates.
    }

    public boolean onTouchEvent(final MotionEvent ev)
    {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public boolean onInterceptTouchEvent(final MotionEvent ev)
    {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }
}
