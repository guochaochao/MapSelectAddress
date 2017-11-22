package llss.com.www.mapselectaddress;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by user on 2017/11/16.
 * 1，进入页面首先定位，根据定位请求附近30条poi信息，在地图上标记第一条信息的mark
 * 2，搜索框中输入地址信息，点击搜索获取30条相关信息
 *
 */

public class MapAddressActivity extends BaseActivity implements
        PoiSearch.OnPoiSearchListener, AMap.OnMapClickListener {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.et_address)
    EditText etAddress;
    @BindView(R.id.tv_search)
    TextView tvSearch;
    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.rv_poi)
    RecyclerView rvPoi;
    private CommonAdapter mAdapter;
    private AMap aMap;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private LatLonPoint lp;
    private List<PoiItem> poiItems;// poi数据
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;
    private PoiResult poiResult; // poi返回的结果
    private List<String> mList;
    private LatLng latlng;
    private MarkerOptions markerOption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_map_address;
    }

    @Override
    protected void initView() {
        mList = new ArrayList<>();
        rvPoi.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new CommonAdapter<String>(context, R.layout.item_select_poi, mList) {
            @Override
            protected void convert(ViewHolder holder, final String item, int position) {
                TextView tv = holder.getView(R.id.tv_name);
                tv.setText(item);
                holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EventBus.getDefault().post(new MessageEvent(item));
                        finish();
                    }
                });

            }
        };
        rvPoi.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {
        init();
        initLocation();
        startLocation();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        lp = new LatLonPoint(latLng.latitude, latLng.longitude);
        doSearchQuery();
    }


    @OnClick({R.id.tv_back, R.id.tv_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_search:
                doSearchQuery1();
                break;
        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                lp = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
                Log.e("AmapErr", "定位成功");
                doSearchQuery();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    };


    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.setOnMapClickListener(this);
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latlng));
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .position(latlng)
                .draggable(true);
        aMap.addMarker(markerOption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }


    /**
     * 根据定位开始进行poi搜索
     */
    protected void doSearchQuery() {
        query = new PoiSearch.Query("", "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(30);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);// 设置查第一页
        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 1000, true));//
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    /**
     * 根据输入地址开始进行poi搜索
     */
    protected void doSearchQuery1() {
        query = new PoiSearch.Query(etAddress.getText().toString(), "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(30);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);// 设置查第一页
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiItemSearched(PoiItem arg0, int arg1) {

    }


    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
//                    poiItems.get(0).getAdName();
                    mList.clear();
                    for (PoiItem poiItem : poiItems) {
                        mList.add(poiItem.getTitle());
                    }
                    mAdapter.notifyDataSetChanged();
                    if (poiItems.size() != 0) {
                        latlng = new LatLng(poiItems.get(0).getLatLonPoint().getLatitude(), poiItems.get(0).getLatLonPoint().getLongitude());
                        addMarkersToMap();
                    }

                }
            } else {
                Toast.makeText(context, "无搜索结果", Toast.LENGTH_LONG);
            }
        } else {
            Toast.makeText(context, rcode, Toast.LENGTH_LONG);
        }
    }

}
