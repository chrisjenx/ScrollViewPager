package uk.co.chrisjenx.scrollviewpager;

import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

public class OldPager
{
    private static final boolean DEBUG = false;
    private static final String TAG = "OldPager";
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


    public OldPager(final ScrollView scrollView, final ViewGroup contentView)
    {
        mScrollView = scrollView;
        mContentView = contentView;
        // Setup
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

//    @Override
//    public boolean onTouch(final View v, final MotionEvent ev)
//    {
//        if (v != mScrollView) return false;
//
//        final int index = MotionEventCompat.getActionIndex(ev);
//        final int action = MotionEventCompat.getActionMasked(ev);
//        final int pointerId = MotionEventCompat.getPointerId(ev, index);
//
//        //Start Tracking
//        if (mVelocityTracker == null)
//            mVelocityTracker = VelocityTracker.obtain();
//
//        switch (action)
//        {
//            case MotionEvent.ACTION_DOWN:
//                //Reset everything
//                resetPager();
//
//                // Add motion
//                mVelocityTracker.addMovement(ev);
//
//                //Get the initial touch points
//                mLastY = mInitialY = ev.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                // Add motion
//                mVelocityTracker.addMovement(ev);
//                mLastY = ev.getY();
//                break;
//            case MotionEvent.ACTION_UP:
//                // Add motion
//                mVelocityTracker.addMovement(ev);
//                //Calculate current velocity
//                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
//                final float velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
//                if (DEBUG) Log.d(TAG, String.format("VelocityY [%f]", velocityY));
//
//
//                // The height of scroll view, in pixels
//                final int scrollHeight = mScrollView.getHeight();
//                final int fullHeight = mContentView.getHeight();
//                final int pageHeight = scrollHeight;
//
//                // The top of last page, in pixels.
//                final int lastPageTopY = fullHeight - pageHeight;
//                // The scrolled top position of scroll view, in pixels.
//                final int currScrollY = getCurrentScrollY();
//                //The current page we are looking at upon letting go of the scroll view
//                mCurrentPage = calculateCurrentPage(scrollHeight, currScrollY);
//                if (DEBUG) Log.v(TAG, String.format("CurrentPage [%d]", mCurrentPage));
//
//
//                mIsScrolling = true;
//                // Check not scrolling past first/last page
//                if (getCurrentScrollY() < 0)
//                    snapToPage(0, 0);
//                else if (getCurrentScrollY() > lastPageTopY)
//                    snapToPage(getPageCount() - 1, 0);
//                else
//                {
//
////                    // The scrolled middle position of scroll view, in pixels.
////                    final int currScrollMiddleY = calculateScrollYMiddle(scrollHeight, currScrollY);
////                    if (DEBUG)
////                        Log.d(TAG, String.format("ScrollY [%d], ScrollMiddleY [%d]", currScrollY, currScrollMiddleY));
//
//                    // Next page num.
////                    final int nextPage = mCurrentPage; // The top of next page, in pixels.
////                    final int nextPageTop = nextPage * scrollHeight;
////                    if (DEBUG) Log.d(TAG, String.format("NextPageTopY [%d]", nextPageTop));
//
//                    if (Math.abs(velocityY) < mMinVelocity)
//                    {
//                        snapToPage(mCurrentPage, velocityY);
//                    }
//                    else if (Math.abs(velocityY) >= mMinVelocity)
//                    {
//                        mIsScrolling = true;
//                        // Start scrolling calculation.
//                        mScroller.fling(0, getCurrentScrollY(), 0, (int) -velocityY, 0, 0, 0, fullHeight - pageHeight);
//                        calculateWhereFlingShouldStops();
//                        mScrollView.post(mScrollRunnable);
//                    }
//
////                    mScroller.startScroll(0, currScrollY, 0, Math.min(lastPageTopY, nextPageTop) - currScrollY, mMinAnimationDuration);
//                }
//                resetVelocityTracker();
//                // Consume touch event
//                return true;
//            case MotionEvent.ACTION_CANCEL:
//                resetVelocityTracker();
//                resetPager();
//                break;
//        }
//        return false;
//    }

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


}