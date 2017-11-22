package llss.com.www.mapselectaddress;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class MainActivity extends BaseActivity {

    @BindView(R.id.btn_select)
    Button btnSelect;
    @BindView(R.id.tv_address)
    TextView tvAddress;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.btn_select)
    public void onClick() {
        startActivity(new Intent(context, MapAddressActivity.class));
    }

    @Subscribe
    public void onMoonEvent(MessageEvent addressEvent) {
        tvAddress.setText(addressEvent.getMessage());
    }
}
