package uk.co.chrisjenx.scrollviewpager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;
import android.widget.ScrollView;
import android.widget.Scroller;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
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
    private static int mMinAnimationDuration = 400;
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
    private final int mMinVelocity;
    private final int mMaxVelocity;
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
    /**
     * Temp final y where to stop
     */
    private int mFinalY;


    public ScrollPagerWrapper(final ScrollView scrollView)
    {
        this(scrollView, null);
    }

    public ScrollPagerWrapper(final ScrollView scrollView, final ViewGroup scrollContent)
    {
        mScrollView = scrollView;
        mScrollContent = scrollContent;
        findScrollerFromScrollView(scrollView);
        final ViewConfiguration vc = ViewConfiguration.get(mScrollView.getContext());
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        if (Utils.SUPPORTS_GINGER)
        {
            mOverflingDistance = vc.getScaledOverflingDistance();
            mOverscrollDistance = vc.getScaledOverscrollDistance();
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
        mFinalY = calculateWhereFlingShouldStop();
        setScrollerFinalY(mFinalY); // Only works for API9<
        if (DEBUG)
            Log.d(TAG, String.format("FlingVelY[%d], ScrollerFinalY[%d], NewFinalY[%d]", velocityY, getScrollerFinalY(), mFinalY));
    }

    public void computeScroll()
    {
        //Nothing at the moment
        if (mFinalY == Integer.MAX_VALUE) return;

        if (mFinalY != getScrollerFinalY() && willNotPassFinalY(getScrollerStartY(), getScrollerFinalY(), mFinalY))
        {
            abortAnimation();
            final int delta = mFinalY - getCurrentScrollY();
            final int newVel = min(mMaxVelocity, max(mMinVelocity, abs(delta * 10)));
            if (delta > 0)
                fling(0, getCurrentScrollY(), 0, newVel, 0, 0, 0, mFinalY);
            else
                fling(0, getCurrentScrollY(), 0, -newVel, 0, 0, mFinalY, mScrollContent.getHeight());
            if (DEBUG)
                Log.i(TAG, String.format("Delta[%d],mFinal[%d],newVel[%d],FlingY[%d]", delta, mFinalY, newVel, getScrollerFinalY()));
//            startScroll(0, getCurrentScrollY(), 0, delta, calculateDuration(mFinalY - getScrollerStartY(), getScrollerCurrVelocity()));
            postInvalidateOnAnimation();
        }
        if (mFinalY != getScrollerFinalY() && hasPassedFinalY(getScrollerStartY(), mFinalY, getScrollerCurrentY()))
            notifyVerticalEdgeReached(getScrollerCurrentY(), mFinalY, mOverscrollDistance);
    }

    public boolean onTouchEvent(final MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                //Reset mFinalY
                mFinalY = Integer.MAX_VALUE;
        }
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

    int calculateDuration(float delta, float velocity)
    {
        if (delta == 0 || velocity == 0) return mMinAnimationDuration;
        final int duration = (int) Math.max(mMinAnimationDuration, (abs(delta) / abs(velocity)) * 1000);
        if (DEBUG) Log.d(TAG, String.format("Duration[%d]", duration));
        return duration;
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
            time = min(mMaxAnimationDuration, Math.max(mMinAnimationDuration, (int) (delta / abs(velocity))));


        mScroller.startScroll(0, getCurrentScrollY(), 0, delta, time);
    }

    /**
     * Looks at the current fling state of the stroller and will work out what the closest page to where the fling would naturally stop.
     *
     * @return the final y pos that the scroller should stop on
     */
    int calculateWhereFlingShouldStop()
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
                Log.v(TAG, String.format("ClosestTop[%d],CurrTop[%d],ClosestDelta[%d],CurrDelta[%d]", finishTopY, child.getTop(), smallestDelta, currentDelta));
            if (abs(currentDelta) < abs(smallestDelta))
            {
                smallestDelta = currentDelta;
                finishTopY = child.getTop();
            }
        }
        return finishTopY;
    }

    /**
     * Works out if we have scrolled passed our desired page location
     *
     * @param startY       Scroller StartingY
     * @param finalY       Where we want the Scroller to stop
     * @param currentCalcY the Scroller currently calculated Y pos
     * @return
     */
    private boolean hasPassedFinalY(final int startY, final int finalY, final int currentCalcY)
    {
        if (finalY < startY)//Negative scrolling (up)
            return currentCalcY <= finalY; // Is currentY above finalY
        else
        { //Scrolling down
            return currentCalcY >= finalY; //Is currentY below finalY
        }
    }

    private boolean willNotPassFinalY(int startY, int currFinalY, int targetFinalY)
    {
        if (currFinalY < startY) return targetFinalY < currFinalY || targetFinalY >= startY;
        else if (currFinalY > startY)
            return targetFinalY > currFinalY || targetFinalY <= startY;
        return false;
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

    private void abortAnimation()
    {
        if (mOverScroller != null)
            mOverScroller.abortAnimation();
        if (mScroller != null)
            mScroller.abortAnimation();
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

    /**
     * This only works for preAPI9, worth modifying the on Scroll or calcScroll to make sure this is carried out
     *
     * @param y
     */
    private void setScrollerFinalY(final int y)
    {
        if (mOverScroller != null)
            mFinalY = y;
        if (mScroller != null)
            mScroller.setFinalY(y);
    }

    /**
     * Tell the over scroller that we have reached an edge or a top of a page..
     *
     * @param startY
     * @param finalY
     * @param overY
     */
    private void notifyVerticalEdgeReached(final int startY, final int finalY, final int overY)
    {
        if (mOverScroller != null)
            mOverScroller.notifyVerticalEdgeReached(startY, finalY, overY);
    }

    /**
     * Fling the scroller
     */
    private void fling(final int startX, final int startY, final int velocityX, final int velocityY, final int minX, final int maxX, final int minY, final int maxY)
    {
        if (mOverScroller != null)
            mOverScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        else if (mScroller != null)
            mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    /**
     * Get the current y calcualted by the scroller
     *
     * @return
     */
    private int getScrollerCurrentY()
    {
        if (mOverScroller != null)
            return mOverScroller.getCurrY();
        else if (mScroller != null)
            return mScroller.getCurrY();
        return 0;
    }

    /**
     * get the starting point of the Scroller!
     *
     * @return
     */
    private int getScrollerStartY()
    {
        if (mOverScroller != null)
            return mOverScroller.getStartY();
        else if (mScroller != null)
            return mScroller.getStartY();
        return 0;
    }

    private float getScrollerCurrVelocity()
    {
        if (mOverScroller != null)
            return mOverScroller.getCurrVelocity();
        if (mScroller != null)
            return mScroller.getCurrVelocity();
        return 0;
    }

    /**
     * Will return the current y pos of the scroll view if it exists otherwise defaults to 0
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
