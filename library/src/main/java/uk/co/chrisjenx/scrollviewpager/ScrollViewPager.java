package uk.co.chrisjenx.scrollviewpager;

import android.support.v4.view.VelocityTrackerCompat;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

public class ScrollViewPager implements View.OnTouchListener
{
    private static final boolean DEBUG = true;
    private static final String TAG = "ScrollViewPager";
    // Velocity stuff
    private final int mMinVelocity;
    private final int mMaxVelocity;
    private final int mMinAnimationDuration = 400;
    private final int mMaxAnimationDuration = 1000;
    //Tracking
    private VelocityTracker mVelocityTracker;
    private float mInitialY = -1;
    private float mLastY = -1;
    // The class encapsulates scrolling.(Overshoot)
    private Scroller mScroller;
    // The task make scroll view scrolled.
    private Runnable task;
    private ScrollView mScrollView;
    private ViewGroup mContentView;
    //State
    private int mCurrentPage;
    private int mNextPage;


    public ScrollViewPager(final ScrollView scrollView, final ViewGroup contentView)
    {
        mScrollView = scrollView;
        mContentView = contentView;
        // Setup
        mScrollView.setOnTouchListener(this);
        mScroller = new Scroller(mScrollView.getContext(), new DecelerateInterpolator());
        task = new Runnable()
        {
            @Override
            public void run()
            {
                if (mScroller.computeScrollOffset())
                {
                    mScrollView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                }
                if (!mScroller.isFinished())
                {
                    mScrollView.post(this);
                }
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

        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        final int index = ev.getActionIndex();
        final int action = ev.getActionMasked();
        final int pointerId = ev.getPointerId(index);
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                // Stop scrolling calculation.
                // Stop scrolling animation.
                if (!mScroller.isFinished())
                {
                    mScroller.abortAnimation();
                    mScrollView.removeCallbacks(task);
                }

                //Get the intial touch points
                mLastY = mInitialY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                //Calculate current velocity
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                final float velocityY = VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId);
                if (DEBUG) Log.d(TAG, String.format("VelocityY [%f]", velocityY));


                final int pageCount = getPageCount();
                // The height of scroll view, in pixels
                final int displayHeight = mScrollView.getHeight();
                final int fullHeight = mContentView.getHeight();
                final int pageHeight = displayHeight;

                // The top of last page, in pixels.
                final int lastPageTopY = fullHeight - pageHeight;

                // Check not scrolling past first/last page
                if (getCurrentScrollY() < 0)
                    snapToPage(0, 0);
                else if (getCurrentScrollY() > lastPageTopY)
                    snapToPage(pageCount - 1, 0);
                else
                {
                    // The scrolled top position of scroll view, in pixels.
                    final int currScrollY = getCurrentScrollY();
                    // The scrolled middle position of scroll view, in pixels.
                    final int currScrollMiddleY = currScrollY + displayHeight / 2;
                    if (DEBUG)
                        Log.d(TAG, String.format("ScrollY [%d], ScrollMiddleY [%d]", currScrollY, currScrollMiddleY));

                    // Current page num.
                    final int currPage = currScrollMiddleY / displayHeight;

                    // Next page num.
                    final int nextPage = currPage;

                    // The top of next page, in pixels.
                    final int nextPageTop = nextPage * displayHeight;

                    if (DEBUG) Log.d(TAG, String.format("NextPageTop [%d]", nextPageTop));
                    if (Math.abs(velocityY) >= mMinVelocity)
                    {
                        final int pagePerSec = (int) Math.max(1f, FloatMath.floor(Math.abs(velocityY) / 1000f));
                        if (DEBUG) Log.d(TAG, String.format("PagesPerVel [%d]", pagePerSec));
                        if (velocityY < -mMinVelocity)
                            snapToPage(currPage + pagePerSec, Math.abs(velocityY));
                        else if (velocityY > mMinVelocity)
                            snapToPage(currPage - pagePerSec, Math.abs(velocityY));
                    }
                    // Start scrolling calculation.
//                    mScroller.fling(0, getCurrentScrollY(), 0, (int) -velocityY, 0, 0, 0, nextPageTop + pageHeight);
//                    mScroller.startScroll(0, currScrollY, 0, Math.min(lastPageTopY, nextPageTop) - currScrollY, mMinAnimationDuration);

                    // Start animation.
                    mScrollView.post(task);
                }
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null)
                {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                // consume(to stop fling)
                return true;
        }
        return false;
    }

    void snapToPage(int whichPage, final float velocity)
    {
        if (whichPage < 0) whichPage = 0;
        if (whichPage >= getPageCount()) whichPage = getPageCount() - 1;

        final boolean changingPages = whichPage != mCurrentPage;
        mNextPage = whichPage;

        final int delta = mContentView.getChildAt(whichPage).getTop() - getCurrentScrollY();
        int time = mMinAnimationDuration;
        if (velocity != 0)
        {
            time = Math.max(mMinAnimationDuration, (int) ((float) delta / velocity));
        }
        mScroller.startScroll(0, getCurrentScrollY(), 0, delta, time);
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
}