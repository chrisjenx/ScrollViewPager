package uk.co.chrisjenx.scrollviewpager;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

public class ScrollViewPager implements View.OnTouchListener
{
    private static final boolean DEBUG = false;
    private static final String TAG = "ScrollViewPager";
    // Velocity stuff
    private final int mMinVelocity;
    private final int mMaxVelocity;
    private final int mMinAnimationDuration = 400;
    //Tracking
    private VelocityTracker mVelocityTracker;
    private float mInitialY = -1;
    private float mLastY = -1;
    // The class encapsulates scrolling.(Overshoot)
    private Scroller mScroller;
    // The mScrollRunnable make scroll view scrolled.
    private Runnable mScrollRunnable;
    private ScrollView mScrollView;
    private ViewGroup mContentView;
    //State
    private int mCurrentPage = 0;
    private boolean mIsScrolling = false;


    public ScrollViewPager(final ScrollView scrollView, final ViewGroup contentView)
    {
        mScrollView = scrollView;
        mContentView = contentView;
        // Setup
        mScrollView.setOnTouchListener(this);
        mScroller = new Scroller(mScrollView.getContext(), new DecelerateInterpolator(1.f));
        // The Scroller Runnable
        mScrollRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (mScroller.computeScrollOffset())
                    mScrollView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                // If its not finished make sure we try and calculate the next time to run
                if (!mScroller.isFinished())
                    mScrollView.post(this);
                else
                    mIsScrolling = false;
            }
        };

        //View Configuration
        final ViewConfiguration vc = ViewConfiguration.get(scrollView.getContext());
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent ev)
    {
        if (v != mScrollView) return false;

        final int index = MotionEventCompat.getActionIndex(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, index);

        //Start Tracking
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                //Reset everything
                resetPager();

                // Add motion
                mVelocityTracker.addMovement(ev);

                //Get the initial touch points
                mLastY = mInitialY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Add motion
                mVelocityTracker.addMovement(ev);
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                // Add motion
                mVelocityTracker.addMovement(ev);
                //Calculate current velocity
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                final float velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
                if (DEBUG) Log.d(TAG, String.format("VelocityY [%f]", velocityY));


                // The height of scroll view, in pixels
                final int scrollHeight = mScrollView.getHeight();
                final int fullHeight = mContentView.getHeight();
                final int pageHeight = scrollHeight;

                // The top of last page, in pixels.
                final int lastPageTopY = fullHeight - pageHeight;
                // The scrolled top position of scroll view, in pixels.
                final int currScrollY = getCurrentScrollY();
                //The current page we are looking at upon letting go of the scroll view
                mCurrentPage = calculateCurrentPage(scrollHeight, currScrollY);
                if (DEBUG) Log.v(TAG, String.format("CurrentPage [%d]", mCurrentPage));


                mIsScrolling = true;
                // Check not scrolling past first/last page
                if (getCurrentScrollY() < 0)
                    snapToPage(0, 0);
                else if (getCurrentScrollY() > lastPageTopY)
                    snapToPage(getPageCount() - 1, 0);
                else
                {

//                    // The scrolled middle position of scroll view, in pixels.
//                    final int currScrollMiddleY = calculateScrollYMiddle(scrollHeight, currScrollY);
//                    if (DEBUG)
//                        Log.d(TAG, String.format("ScrollY [%d], ScrollMiddleY [%d]", currScrollY, currScrollMiddleY));

                    // Next page num.
//                    final int nextPage = mCurrentPage; // The top of next page, in pixels.
//                    final int nextPageTop = nextPage * scrollHeight;
//                    if (DEBUG) Log.d(TAG, String.format("NextPageTopY [%d]", nextPageTop));

                    if (Math.abs(velocityY) < mMinVelocity)
                    {
                        snapToPage(mCurrentPage, velocityY);
                    }
                    else if (Math.abs(velocityY) >= mMinVelocity)
                    {
                        mIsScrolling = true;
                        // Start scrolling calculation.
                        mScroller.fling(0, getCurrentScrollY(), 0, (int) -velocityY, 0, 0, 0, fullHeight - pageHeight);
                        calculateWhereFlingShouldStops();
                        mScrollView.post(mScrollRunnable);
                    }

//                    mScroller.startScroll(0, currScrollY, 0, Math.min(lastPageTopY, nextPageTop) - currScrollY, mMinAnimationDuration);
                }
                resetVelocityTracker();
                // Consume touch event
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetVelocityTracker();
                resetPager();
                break;
        }
        return false;
    }

    /**
     * Looks at the current fling state of the stroller and will work out what the closest page to where the fling would naturally stop.
     * Then sets the finalY position to end there.
     */
    void calculateWhereFlingShouldStops()
    {
        final int initFinishY = mScroller.getFinalY();
        int finishTopY = initFinishY;
        View child;
        int smallestDelta = mScrollView.getHeight();
        int currentDelta;
        for (int i = 0; i < getPageCount(); i++)
        {
            child = mContentView.getChildAt(i);
            currentDelta = child.getTop() - initFinishY;
            if (DEBUG)
                Log.d(TAG, String.format("ClosestTop[%d],CurrTop[%d],ClosestDelta[%d],CurrDelta[%d]", finishTopY, child.getTop(), smallestDelta, currentDelta));
            if (Math.abs(currentDelta) < Math.abs(smallestDelta))
            {
                smallestDelta = currentDelta;
                finishTopY = child.getTop();
            }
        }
        mScroller.setFinalY(finishTopY);
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

        final int delta = mContentView.getChildAt(whichPage).getTop() - getCurrentScrollY();
        int time = mMinAnimationDuration;
        if (velocity != 0)
            time = Math.max(mMinAnimationDuration, (int) ((float) delta / Math.abs(velocity)));

        mScroller.startScroll(0, getCurrentScrollY(), 0, delta, time);
        mScrollView.post(mScrollRunnable); // Start animation.
    }

    /**
     * Will completely clear state and stop any scrolling
     */
    void resetPager()
    {
        if (!mScroller.isFinished())
        {
            mScroller.abortAnimation();
            mScrollView.removeCallbacks(mScrollRunnable);
        }
        mIsScrolling = false;
//        resetVelocityTracker();
    }

    void resetVelocityTracker()
    {
        mInitialY = mLastY = -1;
        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    final void calculateNextPage()
    {
        //                        if (velocityY < -mMinVelocity)
//                            snapToPage(mCurrentPage + 1, velocityY);
//                        else if (velocityY > mMinVelocity)
//                            snapToPage(mCurrentPage - 1, velocityY);
    }

    /**
     * Works out how many pages there on based off the content height
     *
     * @return
     */
    public int getPageCount()
    {
        return mContentView.getChildCount();
//        return Math.round((float) mContentView.getHeight() / (float) mScrollView.getHeight());
    }

    final int getCurrentScrollY()
    {
        return mScrollView.getScrollY();
    }


    /**
     * Works out the current page you are looking at. Or which is at least overlaps the middle of the scroll view
     *
     * @param scrollViewHeight Height of the scroll view
     * @param currentScrollY   The currentYScroll position
     * @return which the page is overlapping the centre of the scroll view
     */
    final int calculateCurrentPage(final int scrollViewHeight, final int currentScrollY)
    {
        // Current page num.
        mCurrentPage = calculateScrollYMiddle(scrollViewHeight, currentScrollY) / scrollViewHeight;
        return mCurrentPage;
    }

    /**
     * Current Scroll Position + half the scroll view height will give the centre point of where the middle of the scroll view content is
     * in pixel
     *
     * @param scrollViewHeight Height of the Scroll View in pixels
     * @param currentScrollY   Current ScrollY value in pixels
     * @return ScrollY Position of the middle of the scroll view
     */
    final int calculateScrollYMiddle(final int scrollViewHeight, final int currentScrollY)
    {
        //currScrollY + displayHeight / 2;
        return currentScrollY + scrollViewHeight / 2;
    }

    public int getCurrentPage()
    {
        return mCurrentPage;
    }

}