package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestOnClickListener;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ViewTest {
    private View view;

    @Before public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        view = new View(null);
    }

    @Test
    public void testHasEmptyLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        assertThat(layoutParams, notNullValue());
    }

    @Test
    public void layout_shouldAffectWidthAndHeight() throws Exception {
        assertThat(view.getWidth(), equalTo(0));
        assertThat(view.getHeight(), equalTo(0));

        view.layout(100, 200, 303, 404);
        assertThat(view.getWidth(), equalTo(303 - 100));
        assertThat(view.getHeight(), equalTo(404 - 200));
    }

    @Test
    public void shouldFocus() throws Exception {
        final Transcript transcript = new Transcript();

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                transcript.add(hasFocus ? "Gained focus" : "Lost focus");
            }
        });

        assertFalse(view.isFocused());
        assertFalse(view.hasFocus());
        transcript.assertNoEventsSoFar();

        view.requestFocus();
        assertTrue(view.isFocused());
        assertTrue(view.hasFocus());
        transcript.assertEventsSoFar("Gained focus");

        view.clearFocus();
        assertFalse(view.isFocused());
        assertFalse(view.hasFocus());
        transcript.assertEventsSoFar("Lost focus");
    }

    @Test
    public void shouldNotBeFocusableByDefault() throws Exception {
        assertFalse(view.isFocusable());

        view.setFocusable(true);
        assertTrue(view.isFocusable());
    }

    @Test
    public void shouldKnowIfThisOrAncestorsAreVisible() throws Exception {
        assertTrue(shadowOf(view).derivedIsVisible());
        
        ViewGroup grandParent = new LinearLayout(null);
        ViewGroup parent = new LinearLayout(null);
        grandParent.addView(parent);
        parent.addView(view);

        grandParent.setVisibility(View.GONE);

        assertFalse(shadowOf(view).derivedIsVisible());
    }

    @Test
    public void shouldInflateMergeRootedLayoutAndNotCreateReferentialLoops() throws Exception {
        LinearLayout root = new LinearLayout(null);
        root.inflate(new Activity(), R.layout.inner_merge, root);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            assertNotSame(root, child);
        }
    }

    @Test
    public void checkedClick_shouldClickOnView() throws Exception {
        TestOnClickListener clickListener = new TestOnClickListener();
        view.setOnClickListener(clickListener);
        shadowOf(view).checkedPerformClick();

        assertTrue(clickListener.clicked);
    }

    @Test(expected= RuntimeException.class)
    public void checkedClick_shouldThrowIfViewIsNotVisible() throws Exception {
        ViewGroup grandParent = new LinearLayout(null);
        ViewGroup parent = new LinearLayout(null);
        grandParent.addView(parent);
        parent.addView(view);
        grandParent.setVisibility(View.GONE);

        shadowOf(view).checkedPerformClick();
    }

    @Test(expected= RuntimeException.class)
    public void checkedClick_shouldThrowIfViewIsDisabled() throws Exception {
        view.setEnabled(false);
        shadowOf(view).checkedPerformClick();
    }

}
