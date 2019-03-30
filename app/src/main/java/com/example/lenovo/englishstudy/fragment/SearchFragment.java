package com.example.lenovo.englishstudy.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lenovo.englishstudy.R;
import com.example.lenovo.englishstudy.Util.HttpUtil;
import com.example.lenovo.englishstudy.Util.Utility;
import com.example.lenovo.englishstudy.bean.WordMeanig;
import com.example.lenovo.englishstudy.bean.WordSuggest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private SearchView mSearchView;
    private ListView mListView;
    private ArrayList<String> mString = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.searchfragment, container, false);
        mSearchView = view.findViewById(R.id.searchView);
        mListView = view.findViewById(R.id.lv_searchList);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mString);
        mListView.setAdapter(adapter);
        // mListView.setTextFilterEnabled(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    // Log.d("1234", "1");
                    // requestWordSuggest(s);
                    // Log.d("1234", "2");
                    // mListView.setFilterText(s);
                    // //changeSearch(mListView);
                    adapter.notifyDataSetChanged();
                    mListView.setSelection(0);
                    requestWordSuggest(s);
                } else {
                    mListView.clearTextFilter();
                }
                return false;
            }
        });
        return view;
    }

    public void requestWordSuggest(final String word) {
        String wordSuggestUrl = "http://dict.youdao.com/suggest?q=" + word +
                "&le=eng&num=15&ver=2.0&doctype=json&keyfrom=mdict.7.2.0.android&model=honor&mid=5.6.1&imei=659135764921685&vendor=wandoujia&screen=1080x1800&ssid=superman&abtest=2";
        HttpUtil.sendHttpRequest(wordSuggestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取单词联想失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WordSuggest wordSuggest = Utility.handleWordSuggestResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (wordSuggest != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                            editor.putString("wordSuggest", responseText);
                            editor.apply();
                            showWordSuggest(wordSuggest);
                        } else {
                            Toast.makeText(getContext(), "获取单词联想失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void showWordSuggest(WordSuggest wordSuggest) {
        if (!wordSuggest.getResult().getMsg().equals("success")) {
            mString.clear();
            mString.add("查无此词");
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
        } else {
            mString.clear();
            for (WordSuggest.DataBean.EntriesBean entriesBean: wordSuggest.getData().getEntries()) {
                mString.add(entriesBean.getEntry() + "    " + entriesBean.getExplain());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
        }
    }

    /**
     * 通过反射设置mTextFilter属性达到隐藏弹框
     * @param listView ListView实例
     */
    private void changeSearch(ListView listView) {
        try {
            Field field = listView.getClass().getSuperclass().getDeclaredField("mTextFilter");
            field.setAccessible(true);
            EditText searchAutoComplete = (EditText) field.get(listView);
            // searchAutoComplete.setTextColor(getResources().getColor(android.R.color.transparent));
            // searchAutoComplete.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            searchAutoComplete.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
