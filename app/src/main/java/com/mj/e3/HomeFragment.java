package com.mj.e3;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.mj.e3.databinding.FragmentHomeBinding;

import java.util.Objects;

import javax.microedition.khronos.egl.EGLDisplay;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.mj.e3.HomeVM.litterIndex;
import static com.mj.e3.HomeVM.nextWord;
import static com.mj.e3.HomeVM.tipTimes;
import static com.mj.e3.MainActivity.editor;
import static com.mj.e3.MainActivity.play;
import static com.mj.e3.MainActivity.sharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
public class HomeFragment extends Fragment {

    private static int wordPointer;
    @SuppressLint("StaticFieldLeak")
    private static FragmentHomeBinding fragmentHomeBinding;
    static private String rightLitter;
    @SuppressLint("StaticFieldLeak")
    static private TextView inputBox;
    private TextView showBox;
    static String stingIcon;//图标字符串,绘文字表情
    static String translateURL;
    static String dictionaryURL;
    @SuppressLint("StaticFieldLeak")
    private static WebView webView;
    private String searchEngine = "http://m.baidu.com/s?wd=####&ie=UTF-8";
    private static String[] wordArray;
    static String checkedWord;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private String cleanWord;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        HomeVM homeVM = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(HomeVM.class);
        fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        fragmentHomeBinding.setData(homeVM);
        fragmentHomeBinding.setLifecycleOwner(getActivity());//委托activity来管理生命周期
        return fragmentHomeBinding.getRoot();//返回根节点
    }


    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        webView = fragmentHomeBinding.webView;
        webView.setScrollY((int) sharedPreferences.getFloat(getString(R.string.key_web_roll_position), 350.0f));
        stingIcon = sharedPreferences.getString(getString(R.string.emojiOfShowBox), "\uD83D\uDC80");//安卓初始化showBox里的绘文字表情
        translateURL = sharedPreferences.getString(getString(R.string.key_translateURL), "没有设置####");
        dictionaryURL = sharedPreferences.getString(getString(R.string.key_dictionaryURL), "没有设置");
        wordPointer = sharedPreferences.getInt(getString(R.string.key_wordPointer), 0);
        fragmentHomeBinding.showBox.setText(sharedPreferences.getString(getString(R.string.key_showBox),""));
        fragmentHomeBinding.preShowBox.setText(sharedPreferences.getString(getString(R.string.key_preShowBox),""));
        fragmentHomeBinding.inputBox.setText(sharedPreferences.getString(getString(R.string.key_input_box),""));
        wordArray = makeWordArray();
        nextWord.setValue(sharedPreferences.getString(getString(R.string.key_input_box2),wordArray[wordPointer]));
        litterIndex.setValue(Integer.valueOf(sharedPreferences.getString(getString(R.string.key_letter_index),"0")));
        tipTimes.setValue(Integer.valueOf(sharedPreferences.getString(getString(R.string.key_times),"1")));
        checkedWord = getWordByWordPointer(wordPointer);

        Log.d(TAG, "onActivityCreated: " + checkedWord);
        WebSettings webSettings = webView.getSettings();

        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);


        inputBox = fragmentHomeBinding.inputBox;
        showBox = fragmentHomeBinding.showBox;

        fragmentHomeBinding.times.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int times = Integer.valueOf(s.toString());
                if (times > 1){
                    fragmentHomeBinding.tableLayout.setBackgroundResource(R.color.深键盘区背景颜色);
                }
                else {
                    fragmentHomeBinding.tableLayout.setBackgroundResource(R.color.键盘区背景颜色);
                }
            }
        });

        //region 背景box监听器
        fragmentHomeBinding.preShowBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                fragmentHomeBinding.ScrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putString(getString(R.string.key_preShowBox),fragmentHomeBinding.preShowBox.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion 背景box监听器




        //region showBox 的监听器 ,会在里面对输入的字母进行判断
        fragmentHomeBinding.showBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                new Handler().post(() -> {
                    fragmentHomeBinding.ScrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                });
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putString(getString(R.string.key_showBox),fragmentHomeBinding.showBox.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
//endregion ,,

        //region 翻译本句Button的单击事件监听器
        fragmentHomeBinding.btnWeb3.setOnClickListener(v -> disposeSentence());
        //endregion Button翻译本句的单击事件监听器

        //region more的单击事件
        fragmentHomeBinding.more.setOnClickListener(v -> {
            //显示
            if (fragmentHomeBinding.tableRow3.getVisibility() == View.GONE) {
                fragmentHomeBinding.tableRow3.setVisibility(View.VISIBLE);
            }
            //隐藏
            else {
                fragmentHomeBinding.tableRow3.setVisibility(View.GONE);
            }
        });
        //endregion more的单击事件

        //region share1 单击事件
        fragmentHomeBinding.share1.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, checkedWord);
            startActivity(shareIntent);
        });
        //endregion


        //region share 单机事件
        fragmentHomeBinding.share.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, checkedWord);
//切记需要使用Intent.createChooser，否则会出现别样的应用选择框，您可以试试
            shareIntent = Intent.createChooser(shareIntent, "分享当前单词到");
            startActivity(shareIntent);
        });
        //endregion share 单击事件

        //region 大小屏按钮单击事件监听器
        fragmentHomeBinding.shareInstance.setOnClickListener(v -> {

        });
        //endregion Button翻译本句的单击事件监听器


        //region webView的触摸监听器,用于设置WebView默认滚动到的高度
        webView.setOnTouchListener((v, event) -> {
            //注意这里存入的不是event的属性,而是webView当前的属性
            editor.putFloat(getString(R.string.key_web_roll_position), fragmentHomeBinding.webView.getScrollY());
            editor.commit();
            return false;
        });
        //endregion webView的触摸监听器

        // region 字母索引监听器 自动加字符
        fragmentHomeBinding.litterIndex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //对当前字符进行判断,或对当前单词长度进行判断
                if (String.valueOf(checkedWord.charAt(litterIndex.getValue())).matches("[^a-zA-Z’ ]")||checkedWord.length()<=3) {//如果当前字符不是字母,就自动加上屏幕
                    rightLitter = String.valueOf(checkedWord.charAt(litterIndex.getValue()));
                    onScreen();
                }
            }
        });
//endregion 字母索引监听器

        //region litterIndex 监听器 ,及时保存litterIndex的值
        fragmentHomeBinding.litterIndex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //顺便也把字母索引的值在这里存储吧
                editor.putString(getString(R.string.key_letter_index),fragmentHomeBinding.litterIndex.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        //region times 的监听器,及时保存好times
        fragmentHomeBinding.times.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //顺便也把times放进去
                editor.putString(getString(R.string.key_times),fragmentHomeBinding.times.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        //region inputBox状态监听器
        fragmentHomeBinding.inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //存好inputBox的状态
                editor.putString(getString(R.string.key_input_box),inputBox.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        //region 新单词监听器,也就是inputBox2的查词
        fragmentHomeBinding.inputBox2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                //有不同的撇号,直撇号,和弯撇号
                cleanWord = checkedWord.replaceAll("[^a-zA-Z’`'-]", "");//删除不相关字符

                //播放当前词
                play(cleanWord, TextToSpeech.QUEUE_FLUSH);

                //记录当前词汇索引
                editor.putInt(getString(R.string.key_wordPointer), wordPointer);
                //记录inputBox2的文本
                editor.putString(getString(R.string.key_input_box2),checkedWord);
                editor.commit();

                //网页查词
//                webView.loadUrl("http://m.youdao.com/dict?le=eng&q=" + cleanWord);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //preShowBox加词
                fragmentHomeBinding.preShowBox.append(checkedWord);
                //showBox加词
                if (wordPointer>0){
                    fragmentHomeBinding.showBox.append(wordArray[wordPointer-1]);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//endregion 新单词监听器

        //region webView的相关设置
        webView.setWebViewClient(new WebViewClient() {
            //防止在网页view里点击和会跳转到其它的浏览器
            //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return true;
            }

            //网页刚加载的时候执行的页面
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {//网页页面开始加载的时候
                super.onPageStarted(view, url, favicon);
            }

            //页面加载结束后要进行的操作
            @Override
            public void onPageFinished(WebView view, String url) {//网页加载结束的时候

                //webView滚一下,按照存储的那个值去读取数据,然后滚动到对应的那个位置
                fragmentHomeBinding.webView.setScrollY((int) sharedPreferences.getFloat(getString(R.string.key_web_roll_position), 350.0f));

            }

        });
        //endregion webView的相关设置

        //region 播放全文按钮的监听器
        fragmentHomeBinding.playAll.setOnClickListener(new View.OnClickListener() {
            boolean isReading = true;

            @Override
            public void onClick(View v) {

                if (isReading) {
                    isReading = false;
                    play(showBox.getText().toString(), TextToSpeech.QUEUE_FLUSH);
                } else {
                    isReading = true;
                    play("stopped", TextToSpeech.QUEUE_FLUSH);
                }
            }
        });
        //endregion 播放全文按钮的监听器

        // region 搜索按钮的单击事件
        fragmentHomeBinding.search.setOnClickListener(v -> {
            String s = searchEngine.replace("####", checkedWord);
            webView.loadUrl(s);

        });
        //endregion 搜索按钮的单击事件

        //region OnTouchListener,滑块,目前处于已经无用状态
        fragmentHomeBinding.drawable.setOnTouchListener((v, event) -> true);
        //endregion OnTouchListener 的监听器

        // region       times的监听器
        fragmentHomeBinding.times.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion times的监听器

        //region 26个字母button的监听器
        fragmentHomeBinding.q.setOnClickListener(v -> judge("[qwas]"));
        fragmentHomeBinding.w.setOnClickListener(v -> judge("[wqeasd]"));
        fragmentHomeBinding.e.setOnClickListener(v -> judge("[wersdf]"));
        fragmentHomeBinding.r.setOnClickListener(v -> judge("[ertdfg]"));
        fragmentHomeBinding.t.setOnClickListener(v -> judge("[rtyfgh]"));
        fragmentHomeBinding.y.setOnClickListener(v -> judge("[tyughj]"));
        fragmentHomeBinding.u.setOnClickListener(v -> judge("[yuihjk]"));
        fragmentHomeBinding.i.setOnClickListener(v -> judge("[uiojkl]"));
        fragmentHomeBinding.o.setOnClickListener(v -> judge("[iopkl]"));
        fragmentHomeBinding.p.setOnClickListener(v -> judge("[opl]"));
        fragmentHomeBinding.a.setOnClickListener(v -> judge("[qwaszx]"));
        fragmentHomeBinding.s.setOnClickListener(v -> judge("[qweasdzxc]"));
        fragmentHomeBinding.d.setOnClickListener(v -> judge("[wersdfxcv]"));
        fragmentHomeBinding.f.setOnClickListener(v -> judge("[ertdfgcv]"));
        fragmentHomeBinding.g.setOnClickListener(v -> judge("[rtyfghvbn]"));
        fragmentHomeBinding.h.setOnClickListener(v -> judge("[tyughjbnm]"));
        fragmentHomeBinding.j.setOnClickListener(v -> judge("[uihjkbn]"));
        fragmentHomeBinding.k.setOnClickListener(v -> judge("[iopjklnm’]"));
        fragmentHomeBinding.l.setOnClickListener(v -> judge("[opklm’]"));
        fragmentHomeBinding.z.setOnClickListener(v -> judge("[aszx]"));
        fragmentHomeBinding.x.setOnClickListener(v -> judge("[asdzxc]"));
        fragmentHomeBinding.c.setOnClickListener(v -> judge("[sdfxcv]"));
        fragmentHomeBinding.v.setOnClickListener(v -> judge("[dfcvb]"));
        fragmentHomeBinding.b.setOnClickListener(v -> judge("[hjvbn]"));
        fragmentHomeBinding.n.setOnClickListener(v -> judge("[jkbnm]"));
        fragmentHomeBinding.m.setOnClickListener(v -> judge("[klnmv]"));
        fragmentHomeBinding.pieHao.setOnClickListener(v -> judge("[’']"));
        fragmentHomeBinding.space.setOnClickListener(v -> judge("[ ]"));
        fragmentHomeBinding.tong.setOnClickListener(v -> judge("[a-z’ ]"));
//endregion 三个 button 的监听器

        //region 重新开始按钮监听器
        fragmentHomeBinding.restart.setOnClickListener(v -> restart());
        //endregion 重新开始按钮监听器




        //region 提示按钮监听器
        fragmentHomeBinding.btnTip.setOnClickListener(v -> {
            //播放一下
            play(checkedWord, TextToSpeech.QUEUE_FLUSH);

            //查一下当前词
            webView.loadUrl(dictionaryURL.replaceAll("####", checkedWord));

            //显示一下后面的提示页,在字母输入正确后自动关闭
            fragmentHomeBinding.inputBox2.setVisibility(View.VISIBLE);

            //实现查词
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, checkedWord);
            startActivity(shareIntent);

            //times也要加一
            tipTimes.setValue(tipTimes.getValue() + 1);
        });
        //endregion


    }

    //region 分类处理
    private static void judge(String regex) {
        rightLitter = String.valueOf(checkedWord.charAt(litterIndex.getValue()));
        String rightLitterLowerCase = rightLitter.toLowerCase();
        if (rightLitterLowerCase.matches(regex)) {
            onScreen();
        } else {
            noOnScreen();
        }
    }

    //endregion 分类处理
    //region 加新文的方法
    @SuppressWarnings("SingleStatementInBlock")
    static private void onScreen() {
        //新字母上屏
        inputBox.append(rightLitter);

        //检查背景页是关闭的还是开启的,如果是开着的就把它关闭
//        fragmentHomeBinding.preShowBox.setVisibility(View.INVISIBLE);
        fragmentHomeBinding.inputBox2.setVisibility(View.INVISIBLE);


        //输入的字母正确时震动40毫秒
        VibrateUtil.vibrate(activity, 20);//0秒后震动40毫秒

        //region 分支aa,不是最后一个字母,
        //noinspection SingleStatementInBlock
        if (litterIndex.getValue() < (checkedWord.length() - 1)) {//小于最大索引
            litterIndex.setValue(litterIndex.getValue() + 1);
        }
        //endregion

        //region 分支ab 是最后一个字母的对应的两种情况
        else if (litterIndex.getValue() == checkedWord.length() - 1) {
            //region 分支aba,但不是最后一个单词时,归零字母索引,换新词,还是复旧词
            if (wordPointer < wordArray.length - 1) {

                //归零字母索引
                litterIndex.setValue(0);


                inputBox.setText("");

                //region tipTimes = 1 时
                //分支aba a,兽血为0,换新词,给新血,加空格,变蓝色
                if (tipTimes.getValue() == 1) {


                    //换新词
                    wordPointer++;
                    checkedWord = wordArray[wordPointer];//更新当前词=============================分界线===============================
                    nextWord.setValue(checkedWord);
                    //给新血
                    tipTimes.setValue(1);//输错一个字母就加一,输对所有字母才减一,换新词给一,初始给一
                }
                //endregion

                //region tipTimes != 1 时
                //分支aba b, 不换新词,减血一滴,然后重来
                else {
                    //减血
                    tipTimes.setValue(tipTimes.getValue() - 1);//输错一个字母就加一,输对所有字母才减一,换新词给一,初始给一

                    play(checkedWord, TextToSpeech.QUEUE_ADD);

                }
                //endregion
            }
            //endregion 分支aba,但不是最后一个单词时,归零字母索引,换新词,还是复旧词
            //region 分支abb,是最后一个单词
            else {

                //归零字母索引
                litterIndex.setValue(0);
                wordPointer = 0;
                checkedWord = wordArray[wordPointer];//更新当前词
            }
            //endregion 分支abb,是最后一个单词
        }
        //endregion 是最后一个字母的两种情况
    }
//endregion

    //region 输错字母时调用的方法
    private static void noOnScreen() {
        //震动反馈,输入的字母错误时,长震动一下,给100毫秒,使用的是其中一个重载函数
        VibrateUtil.vibrate(activity, 100);

        //显示一下用于提示的inputBox2
        fragmentHomeBinding.inputBox2.setVisibility(View.VISIBLE);

        //怪物增血
        tipTimes.setValue(tipTimes.getValue() + 1);

        //查询输错的词
        webView.loadUrl(dictionaryURL.replaceAll("####", checkedWord));


        //获取剩余字母后朗读,以空格分割,
        String remainingLetters = checkedWord.substring(litterIndex.getValue()).replaceAll("", " ");
        play(remainingLetters, TextToSpeech.QUEUE_FLUSH);

    }
//endregion

    //region disposeSentence() 处理最后的句子
    static private void disposeSentence() {

        //对全文进行分句
        String s = fragmentHomeBinding.preShowBox.getText().toString();
        Log.d(TAG, "disposeSentence: " + s);
        String[] strings = s.split("[,.?!:;\n] ");
        int i = strings.length - 1;


        //处理句子,并在webView里显示翻译
        webView.loadUrl(translateURL.replaceAll("####", strings[i]));

    }
    //endregion

    // region getWordByWordPointer()
    private String getWordByWordPointer(int index) {
        return wordArray[index];
    }
    //endregion getWordByWordPointer()

    //region makeWordArray() 用String制作字符串数组
    private static String[] makeWordArray() {

        @NonNull String ori = sharedPreferences.getString
                (activity.getString(R.string.used_text_key),
                        activity.getString(R.string.default_text));

        //英文替换
        ori = ori.replaceAll(",",",####");
        ori = ori.replaceAll("\\.",".####");
        ori = ori.replaceAll("\\?","?####");
        ori = ori.replaceAll("!","!####");
        ori = ori.replaceAll(";",";####");
        ori = ori.replaceAll(":",":####");
        ori = ori.replaceAll("\\)",")####");
        ori = ori.replaceAll(",",",####");

        //中文替换
        ori = ori.replaceAll("。","。####");
        ori = ori.replaceAll("？","？####");
        ori = ori.replaceAll("！","！####");
        ori = ori.replaceAll("；","；####");
        ori = ori.replaceAll("：","：####");
        ori = ori.replaceAll("）","）####");
        ori = ori.replaceAll("，","，####");

        //空格替换
        ori = ori.replaceAll("\\s", "####");
        Log.d(TAG, "makeWordArray1: " + ori);

        //把所有的#字符替换成空格加三个###的形式
        ori = ori.replaceAll("#+"," ###");
        Log.d(TAG, "makeWordArray2: " + ori);

        //用###分割!
        return ori.split("###");//单词边界或一个或多个空白字符
    }
    //endregion 用String制作字符串数组


    //region 重来
    static void restart() {
        makeWordArray();//必须 前面
        wordPointer = 0;
        checkedWord = wordArray[wordPointer];
        tipTimes.setValue(1);
        fragmentHomeBinding.preShowBox.setText("");//置空
        fragmentHomeBinding.showBox.setText("");//置空
        litterIndex.setValue(0);
        judge(".");
    }
    //endregion

}