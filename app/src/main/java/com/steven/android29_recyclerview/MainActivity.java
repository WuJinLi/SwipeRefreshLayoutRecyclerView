package com.steven.android29_recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.steven.android29_recyclerview.adapter.QiushiAdapter;
import com.steven.android29_recyclerview.decoration.DividerItemDecoration;
import com.steven.android29_recyclerview.helper.OkHttpClientHelper;
import com.steven.android29_recyclerview.model.QiushiModel;
import com.steven.android29_recyclerview.utils.Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Context mContext = this;
    private ProgressBar progressBar_main = null;
    private SwipeRefreshLayout swipeRefreshLayout_main;
    private RecyclerView recyclerView_main;
    private QiushiAdapter adapter = null;
    private List<QiushiModel.ItemsEntity> totalLlist = new ArrayList<>();
    private int curPage = 1;
    private int lastVisibleItemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        loadNetworkData();
    }

    private void loadNetworkData() {
        String url = String.format(Constant.URL_LATEST, curPage);
        OkHttpClientHelper.getDataAsync(mContext, url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String jsonString = body.string();
                        QiushiModel model = jsonStringToModel(jsonString);
                        final List<QiushiModel.ItemsEntity> list = model.getItems();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressBar_main.isShown()) {
                                    progressBar_main.setVisibility(View.GONE);
                                }
                                if (curPage == 1) {
                                    adapter.reloadListView(list, true);
                                } else {
                                    adapter.reloadListView(list, false);
                                }
                                //设置刷新的图标消失
                                swipeRefreshLayout_main.setRefreshing(false);
                            }
                        });
                    }
                }

            }
        }, "qiushi_latest");
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar_main = (ProgressBar) findViewById(R.id.progressBar_main);
        swipeRefreshLayout_main = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout_main);
        recyclerView_main = (RecyclerView) findViewById(R.id.recyclerView_main);


   // 新建头部
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.mipmap.ic_launcher);

        RelativeLayout headerLayout = new RelativeLayout(mContext);
        headerLayout.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        //recyclerView_main.

        //设置item具有相同的高度，设置此方法可以提升加载效率
        recyclerView_main.setHasFixedSize(true);
        //设置布局管理器
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false);
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext , 2);
        recyclerView_main.setLayoutManager(linearLayoutManager);
        //添加item之间的分割线
        recyclerView_main.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL_LIST));
        //recyclerView_main.addItemDecoration(new DividerGridItemDecoration(mContext));

        //设置item的动画效果
        recyclerView_main.setItemAnimator(new DefaultItemAnimator());

        adapter = new QiushiAdapter(mContext, totalLlist);
        //设置适配器
        recyclerView_main.setAdapter(adapter);

        //设置SwipeRefreshLayout的颜色方案
        swipeRefreshLayout_main.setColorSchemeColors(Color.parseColor("#ff0000"), Color.GREEN,
                Color.BLUE);

        //设置SwipeRefreshLayout的监听器，实现下拉刷新
        swipeRefreshLayout_main.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                curPage = 1;
                loadNetworkData();
            }
        });

        //上拉加载下一页
        recyclerView_main.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (lastVisibleItemPosition + 1 == adapter.getItemCount() && newState ==
                        RecyclerView.SCROLL_STATE_IDLE) {
                    curPage++;
                    loadNetworkData();
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, "xxxx", Toast.LENGTH_SHORT).show();
                recyclerView_main.smoothScrollToPosition(0);
            }
        });

    }

    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.imageView_backtotop:
                //实现RecyclerView的置顶功能
                recyclerView_main.scrollToPosition(0);
                break;
        }
    }

    private QiushiModel jsonStringToModel(String jsonString) {
        Gson gson = new Gson();
        QiushiModel model =
                gson.fromJson(jsonString, new TypeToken<QiushiModel>() {
                }.getType());
        return model;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpClientHelper.cancelCall("qiushi_latest");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(mContext, "Searching...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Toast.makeText(mContext, "Share...", Toast.LENGTH_SHORT).show();
                recyclerView_main.smoothScrollToPosition(0);
                break;
            case android.R.id.home:
                /*Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);*/
                recyclerView_main.smoothScrollToPosition(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
