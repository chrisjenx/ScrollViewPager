package uk.co.chrisjenx.scrollviewpager.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ScrollView;
import uk.co.chrisjenx.scrollviewpager.ScrollViewPager;

public class MainActivity extends Activity
{
    private ScrollViewPager mPager;
    private ScrollView mScrollView;
    private ViewGroup mScrollContent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollContent = (ViewGroup) findViewById(R.id.content);

        mPager = new ScrollViewPager(mScrollView, mScrollContent);
    }
}
