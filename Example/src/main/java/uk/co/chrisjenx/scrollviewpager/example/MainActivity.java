package uk.co.chrisjenx.scrollviewpager.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import uk.co.chrisjenx.scrollviewpager.view.ScrollViewPager;

public class MainActivity extends Activity
{
    private ScrollViewPager mScrollView;
    private ViewGroup mScrollContent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        mScrollView = (ScrollViewPager) findViewById(R.id.scroll_view);
        mScrollContent = (ViewGroup) findViewById(R.id.content);
    }
}
