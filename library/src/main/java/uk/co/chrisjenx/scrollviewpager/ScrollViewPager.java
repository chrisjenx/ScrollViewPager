package uk.co.chrisjenx.scrollviewpager;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Scroller;

public class ScrollViewPager implements View.OnTouchListener
{
    //Tracking
    private VelocityTracker mVelocityTracker;
    private float mInitialY = -1;
    private float mLastY = -1;
    // The class encapsulates scrolling.(Overshoot)
    private Scroller scroller;
    // The task make scroll view scrolled.
    private Runnable task;
    private ScrollView mScrollView;
    private ViewGroup mContentView;


    public ScrollViewPager(final ScrollView scrollView, final ViewGroup contentView)
    {
        mScrollView = scrollView;
        mScrollView.setOnTouchListener(this);
        mContentView = contentView;
        scroller = new Scroller(mScrollView.getContext());
        task = new Runnable()
        {
            @Override
            public void run()
            {
                scroller.computeScrollOffset();
                mScrollView.scrollTo(0, scroller.getCurrY());

                if (!scroller.isFinished())
                {
                    mScrollView.post(this);
                }
            }
        };
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event)
    {
        if (v != mScrollView) return false;

        // Stop scrolling calculation.
        scroller.forceFinished(true);
        // Stop scrolling animation.
        mScrollView.removeCallbacks(task);

        final int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mLastY = mInitialY = event.getY();
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.addMovement(event);
                // The height of scroll view, in pixels
                final int displayHeight = mScrollView.getHeight();
                // The top of content view, in pixels.
                final int contentTop = mContentView.getPaddingTop();
                // The top of content view, in pixels.
                final int contentBottom = mContentView.getHeight() - mContentView.getPaddingBottom();
                // The top of last page, in pixels.
                final int lastPageTop = contentBottom - displayHeight;

                // The scrolled top position of scroll view, in pixels.
                final int currScrollY = getCurrentScrollY();
                // The scrolled middle position of scroll view, in pixels.
                final int currScrollMiddleY = currScrollY + displayHeight / 2 - contentTop;

                // Current page num.
                final int currPage = currScrollMiddleY / displayHeight;

                // Next page num.
                final int nextPage = currPage;

                // The top of next page, in pixels.
                final int nextPageTop = contentTop + nextPage * displayHeight;

                // Start scrolling calculation.
                scroller.startScroll(0, currScrollY, 0, Math.max(Math.min(lastPageTop, nextPageTop), contentTop) - currScrollY, 500);

                // Start animation.
                mScrollView.post(task);

                // consume(to stop fling)
                return true;
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }
        return false;
    }

    final int getCurrentScrollY()
    {
        return mScrollView.getScrollY();
    }

}