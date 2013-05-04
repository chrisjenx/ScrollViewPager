package uk.co.chrisjenx.scrollviewpager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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
    private static final String TAG = "ScrollViewPager";
    /**
     * Whats the shortest time it takes to animate.
     */
    private static int mMinAnimationDuration = 200;
    /**
     * How slow are we allowed to animate
     */
    private static int mMaxAnimationDuration = 1000;

    /**
     * Current Scroll Position + half the scroll view height will give the centre point of where the middle of the scroll view content is
     * in pixel
     *
     * @param scrollViewHeight Height of the Scroll View in pixels
     * @param currentScrollY   Current ScrollY value in pixels
     * @return ScrollY Position of the middle of the scroll view
     */
    static final int calculateScrollYMiddle(final int scrollViewHeight, final int currentScrollY)
    {
        //currScrollY + displayHeight / 2;
        return currentScrollY + scrollViewHeight / 2;
    }

    /**
     * ScrollView to try and intercept
     */
    protected final ScrollView mScrollView;
    /**
     * How far the view should overscroll by
     */
    protected final int mOverscrollDistance;
    protected final int mOverflingDistance;
    /**
     * ScrollContent, this will try to find its self if you don't define it
     */
    protected ViewGroup mScrollContent;
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
    /**
     * Set to true if we have found native scrollers to intercept
     */
    private boolean mNativeMode;
    /**
     * The Current Page that the ScrollView is sitting on.
     */
    private int mCurrentPage;


    public ScrollPagerWrapper(final ScrollView scrollView)
    {
        this(scrollView, null);
    }

    public ScrollPagerWrapper(final ScrollView scrollView, final ViewGroup scrollContent)
    {
        mScrollView = scrollView;
        mScrollContent = scrollContent;
        findScrollerFromScrollView(scrollView);
        if (Utils.SUPPORTS_GINGER)
        {
            final ViewConfiguration vc = ViewConfiguration.get(mScrollView.getContext());
            mOverflingDistance = vc.getScaledOverflingDistance();
            mOverscrollDistance = vc.getScaledOverflingDistance();
        }
        else
        {
            mOverflingDistance = 0;
            mOverscrollDistance = 0;
        }
        if (DEBUG)
            Log.d(TAG, String.format("ScrollView,[%h],Scroller[%h],OverScroller[%h]", scrollView, mScroller, mOverScroller));
    }

    public void fling(final int velocityY)
    {
        if (DEBUG) Log.d(TAG, String.format("FlingVelY[%d], ScrollerFinalY[%d]", velocityY, getScrollerFinalY()));
        calculateWhereFlingShouldStop();
    }

    public void computeScroll()
    {
        //Nothing at the moment
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
     * Fling the ScrollView to a certain page
     *
     * @param whichPage
     */
    public void snapToPage(final int whichPage)
    {
        snapToPage(whichPage, 0);
    }

    /**
     * Works out how many pages there on based off the content height
     *
     * @return
     */
    public int getPageCount()
    {
        return findScrollContent().getChildCount();
//        return Math.round((float) mContentView.getHeight() / (float) mScrollView.getHeight());
    }

    public int getCurrentPage()
    {
        return mCurrentPage;
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
        if (mOverScroller != null || mScroller != null)
            mNativeMode = true;
    }

    private ViewGroup findScrollContent()
    {
        return findScrollContent(mScrollView);
    }

    /**
     * If mScrollContent is defined it will return that otherwise will try and find it from the scrollview
     *
     * @param scrollView
     * @return null if it cant find the content, otherwise
     */
    private ViewGroup findScrollContent(final ScrollView scrollView)
    {
        if (mScrollContent != null) return mScrollContent;
        if (scrollView == null) return null;
        if (scrollView.getChildCount() > 0)
            if (scrollView.getChildAt(0) instanceof ViewGroup)
                return mScrollContent = (ViewGroup) scrollView.getChildAt(0);
        return null;
    }

    /**
     * Snap to the selected page, if you pass in a velocity it will try and reach that destination at the correct velocity speed
     *
     * @param whichPage
     * @param velocity
     */
    void snapToPage(int whichPage, final float velocity)
    {
        if (whichPage < 0) whichPage = 0;
        if (whichPage >= getPageCount()) whichPage = getPageCount() - 1;

        final boolean changingPages = whichPage != mCurrentPage;

        final int delta = findScrollContent().getChildAt(whichPage).getTop() - getCurrentScrollY();
        int time = mMaxAnimationDuration;
        if (velocity != 0)
            time = Math.min(mMaxAnimationDuration, Math.max(mMinAnimationDuration, (int) (delta / Math.abs(velocity))));


        mScroller.startScroll(0, getCurrentScrollY(), 0, delta, time);
    }

    /**
     * Looks at the current fling state of the stroller and will work out what the closest page to where the fling would naturally stop.
     * Then sets the finalY position to end there.
     */
    void calculateWhereFlingShouldStop()
    {
        final int initFinishY = getScrollerFinalY();
        int finishTopY = initFinishY;
        View child;
        int smallestDelta = getScrollHeight();
        int currentDelta;
        for (int i = 0; i < getPageCount(); i++)
        {
            child = findScrollContent().getChildAt(i);
            currentDelta = child.getTop() - initFinishY;
            if (DEBUG)
                Log.d(TAG, String.format("ClosestTop[%d],CurrTop[%d],ClosestDelta[%d],CurrDelta[%d]", finishTopY, child.getTop(), smallestDelta, currentDelta));
            if (Math.abs(currentDelta) < Math.abs(smallestDelta))
            {
                smallestDelta = currentDelta;
                finishTopY = child.getTop();
            }
        }
        setScrollerFinalY(finishTopY);
    }

    /**
     * Wrapped StartScroll, will start scroll on what ever scroller is available
     *
     * @param startX
     * @param startY
     * @param dx
     * @param dy
     * @param duration
     */
    private final void startScroll(int startX, int startY, int dx, int dy, int duration)
    {
        if (mOverScroller != null)
        {
            mOverScroller.startScroll(startX, startY, dx, dy, duration);
            postInvalidateOnAnimation();
            return;
        }
        if (mScroller != null)
        {
            mScroller.startScroll(startX, startY, dx, dy, duration);
            postInvalidateOnAnimation();
            return;
        }
    }

    /**
     * Works out the current page you are looking at. Or which is at least overlaps the middle of the scroll view
     *
     * @param scrollViewHeight Height of the scroll view
     * @param currentScrollY   The currentYScroll position
     * @return which the page is overlapping the centre of the scroll view
     */
    private final int calculateCurrentPage(final int scrollViewHeight, final int currentScrollY)
    {
        // Current page num.
        mCurrentPage = calculateScrollYMiddle(scrollViewHeight, currentScrollY) / scrollViewHeight;
        return mCurrentPage;
    }

    /**
     * Get the scrollview height
     *
     * @return height if avaliable otherwise 0
     */
    private final int getScrollHeight()
    {
        if (mScrollView != null)
            return mScrollView.getHeight();
        return 0;
    }

    /**
     * Get the working scrollers FinalY {@link android.widget.OverScroller#getFinalY()}
     *
     * @return 0 if no scroller is working, otherwise the attached scrollers final pos
     */
    private int getScrollerFinalY()
    {
        if (mOverScroller != null)
            return mOverScroller.getFinalY();
        if (mScroller != null)
            return mScroller.getFinalY();

        return 0;
    }

    private void setScrollerFinalY(final int y)
    {
        if (mOverScroller != null)
        {

        }
        else if (mScroller != null)
            mScroller.setFinalY(y);
    }

    /**
     * Will return the current y pos of the scroll view if it exisits otherwise defaults to 0
     *
     * @return
     */
    final int getCurrentScrollY()
    {
        if (mScrollView != null)
            return mScrollView.getScrollY();
        return 0;
    }

    /**
     * PostInvalidateOnAnimation on the ScrollView
     */
    private final void postInvalidateOnAnimation()
    {
        if (mScrollView != null)
            mScrollView.postInvalidateOnAnimation();
    }
}
