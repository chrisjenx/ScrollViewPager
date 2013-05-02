ScrollViewPager
===============

ScrollView Pager that makes a ScrollView act like it has pages.

*Jar: [v1.0.3](https://github.com/chrisjenx/ScrollViewPager/blob/master/library-1.0.3-SNAPSHOT.jar?raw=true)*

Notes
-----

So what you may notice is that unlike a `ViewPager`, this is designed to maintain a scroll view feel but will finish on the closest page.

So what you may notices that it feels less 'snappy' then the ViewPager, I am looking at adding modes in the future.

**Important**: The sub views to the ScrollView must be the same height, luckily for you i have provided a linear layout for the time being; `SameHeightLinearLayout`.
(Not abiding by this is completely untested)

Version
-------

**v1.0.3** - Not really stable at all. Very Initial Release.

This is still very beta, and I am not pushing to Central until I know that is pretty stable, so please feel free to clone and test and send pull requests/sugestions.

ChangeLog
---------
 - 1.0.1 Fixed the linear layout measuring childrens widths wrong, does a very fast layout pass.
 - 1.0.2 Fixed potential NP error with velocity tracker
 - 1.0.3 Fixed pre API-11 issue.

Example Usage
=============

First off see the example app structure, very very simple.

Otherwise here you go:

Activity/Fragment:

```java
{
  //...
  mScrollView = (ScrollView) findViewById(R.id.scroll_view);
  mScrollContent = (ViewGroup) findViewById(R.id.content);

  mPager = new ScrollViewPager(mScrollView, mScrollContent);
}
```

Layout:

```xml
<ScrollView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/scroll_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">

    <uk.co.chrisjenx.scrollviewpager.view.SameHeightLinearLayout
            android:id="@+id/content"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical">

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_blue_light">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_green_light">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_orange_light">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_red_light">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_blue_dark">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_green_dark">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_orange_dark">
        </FrameLayout>

        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@android:color/holo_red_dark">
        </FrameLayout>

    </uk.co.chrisjenx.scrollviewpager.view.SameHeightLinearLayout>
</ScrollView>
```
