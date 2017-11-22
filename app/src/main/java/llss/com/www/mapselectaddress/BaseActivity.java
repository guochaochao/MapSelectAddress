package llss.com.www.mapselectaddress;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import butterknife.ButterKnife;


/**
 * BaseActivity父类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private String TAG;
    public Intent intent;

    public Context context;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置App保持竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(getLayoutId());
        context = this;
        ButterKnife.bind(this);
        TAG = this.getClass().getSimpleName();
        initView();
        initData();


    }


    /**
     * Base基本类
     */
    public abstract int getLayoutId();
    /**
     * 设置toolbar
     * */

    /**
     * 设置initView
     */
    protected abstract void initView();

    protected abstract void initData();


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 点击空白处影藏输入法
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken(),
                        getApplicationContext());
            }

        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定是否需要隐藏
     */
    public static boolean isHideInput(View v, MotionEvent ev) {

        if (v != null && (v instanceof EditText)) {

            int[] l = {0, 0};

            v.getLocationInWindow(l);

            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left

                    + v.getWidth();

            if (ev.getX() > left && ev.getX() < right && ev.getY() > top

                    && ev.getY() < bottom) {

                return false;

            } else {

                return true;

            }

        }

        return false;

    }

    /**
     * 隐藏软键盘
     */
    public static void HideSoftInput(IBinder token, Context context) {

        if (token != null) {

            InputMethodManager manager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            manager.hideSoftInputFromWindow(token,

                    InputMethodManager.HIDE_NOT_ALWAYS);

        }

    }
}
