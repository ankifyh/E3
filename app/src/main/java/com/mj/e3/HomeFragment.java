package com.mj.e3;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.mj.e3.HomeVM.litterIndex;
import static com.mj.e3.HomeVM.nextWord;
import static com.mj.e3.HomeVM.tipTimes;
import static com.mj.e3.HomeVM.wordPointer;
import static com.mj.e3.MainActivity.editor;
import static com.mj.e3.MainActivity.play;
import static com.mj.e3.MainActivity.sharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings({"SingleStatementInBlock", "ConstantConditions", "JavaDoc"})
public class HomeFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static FragmentHomeBinding fragmentHomeBinding;
    static private String rightLitter;
    @SuppressLint("StaticFieldLeak")
    static private TextView inputBox;
    private TextView showBox;
    static String stingIcon;//图标字符串,绘文字表情
    static String translateURL;
    private static WebView webView;
    private String searchEngine = "http://m.baidu.com/s?wd=####&ie=UTF-8";
    private static String[] wordArray;
    static String checkedWord;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;


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
        fragmentHomeBinding.webScrollLine.setGuidelineBegin((int) sharedPreferences.getFloat(getString(R.string.webBoxH), 300.0f));//安卓初始化webBox高度
        stingIcon = sharedPreferences.getString(getString(R.string.emojiOfShowBox), "\uD83D\uDC80");//安卓初始化showBox里的绘文字表情
        translateURL = sharedPreferences.getString(getString(R.string.key_translateURL),"没有设置####");
        wordArray = makeWordArray();
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


        //region 背景box监听器
        fragmentHomeBinding.preShowBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                disposeSentence();
            }
        });
        //endregion 背景box监听器


        //region webView的触摸监听器,用于设置WebView默认滚动到的高度
        webView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //注意这里存入的不是event的属性,而是webView当前的属性
                editor.putFloat(getString(R.string.key_web_roll_position), fragmentHomeBinding.webView.getScrollY());
                editor.commit();
                return false;
            }
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
                //对当前字符进行判断
                if (String.valueOf(checkedWord.charAt(litterIndex.getValue())).matches("[^a-zA-Z’]")) {//如果当前字符不是字母,就自动加上屏幕
                    rightLitter = String.valueOf(checkedWord.charAt(litterIndex.getValue()));
                    onScreen();
                }

            }
        });
//endregion 字母索引监听器

        //region 新单词监听器,查词
        fragmentHomeBinding.inputBox2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                //有不同的撇号,直撇号,和弯撇号
                String cleanWord = checkedWord.replaceAll("[^a-zA-Z’`'-]", "");//删除不相关字符




                //网页查词
//                webView.loadUrl("http://m.youdao.com/dict?le=eng&q=" + cleanWord);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
        fragmentHomeBinding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = searchEngine.replace("####", checkedWord);
                webView.loadUrl(s);

            }
        });
        //endregion 搜索按钮的单击事件

        //region OnTouchListener,用来滑动修改"帘子"高度的监听器
        fragmentHomeBinding.drawableTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editor.putFloat(getString(R.string.webBoxH), event.getRawY());
                editor.commit();
                if (event.getRawY() > fragmentHomeBinding.line3.getBottom() && event.getRawY() < fragmentHomeBinding.line10.getBottom()) {
                    fragmentHomeBinding.webScrollLine.setGuidelineBegin((int) event.getRawY());
                }
                return true;
            }
        });
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

        fragmentHomeBinding.q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[qwas]");
            }
        });

        fragmentHomeBinding.w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[wqeasd]");
            }
        });


        fragmentHomeBinding.e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[wersdf]");
            }
        });
        fragmentHomeBinding.r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[ertdfg]");
            }
        });
        fragmentHomeBinding.t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[rtyfgh]");
            }
        });

        fragmentHomeBinding.y.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[tyughj]");
            }
        });
        fragmentHomeBinding.u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[yuihjk]");
            }
        });
        fragmentHomeBinding.i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[uiojkl]");
            }
        });
        fragmentHomeBinding.o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[iopkl]");
            }
        });
        fragmentHomeBinding.p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[opl]");
            }
        });
        fragmentHomeBinding.a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[qwaszx]");
            }
        });
        fragmentHomeBinding.s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[qweasdzxc]");
            }
        });
        fragmentHomeBinding.d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[wersdfxcv]");
            }
        });
        fragmentHomeBinding.f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[ertdfgcv]");
            }
        });
        fragmentHomeBinding.g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[rtyfghvbn]");
            }
        });
        fragmentHomeBinding.h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[tyughjbnm]");
            }
        });
        fragmentHomeBinding.j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[uihjkbn]");
            }
        });
        fragmentHomeBinding.k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[iopjklnm’]");
            }
        });
        fragmentHomeBinding.l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[opklm’]");
            }
        });
        fragmentHomeBinding.z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[aszx]");
            }
        });
        fragmentHomeBinding.x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[asdzxc]");
            }
        });
        fragmentHomeBinding.c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[sdfxcv]");
            }
        });
        fragmentHomeBinding.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[dfcv ]");
            }
        });
        fragmentHomeBinding.space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[v b]");
            }
        });
        fragmentHomeBinding.b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[hj bn]");
            }
        });
        fragmentHomeBinding.n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[jkbnm]");
            }
        });
        fragmentHomeBinding.m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[klnmv’]");
            }
        });
        fragmentHomeBinding.pieHao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[klm’]");
            }
        });
        fragmentHomeBinding.tong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judge("[a-z’]");
            }
        });
//endregion 三个 button 的监听器

        //region 重新开始按钮监听器
        fragmentHomeBinding.restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });
        //endregion 重新开始按钮监听器

        //region show Box2 的监听器 ,会在里面对输入的字母进行判断
        fragmentHomeBinding.showBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        fragmentHomeBinding.ScrollViewRight.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    }
                });
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
//endregion ,,

        //region 提示按钮监听器
        fragmentHomeBinding.btnTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放一下
                play(checkedWord, TextToSpeech.QUEUE_FLUSH);
                //显示一下后面的提示页,在字母输入正确后自动关闭
                fragmentHomeBinding.preShowBox.setVisibility(View.VISIBLE);
                fragmentHomeBinding.inputBox2.setVisibility(View.VISIBLE);
            }
        });
        //endregion

        //region 朗读全文的监听器
        fragmentHomeBinding.readSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disposeSentence();
            }
        });
        //endregion 加新文的方法

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
        fragmentHomeBinding.preShowBox.setVisibility(View.INVISIBLE);
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

                    if (wordPointer  >= 1){
                        fragmentHomeBinding.showBox.append(wordArray[wordPointer-1] + " ");
                    }
                    fragmentHomeBinding.preShowBox.append(checkedWord + " ");
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

        //检查背景页是关闭的还是开启的,如果是关闭的就把它打开
        fragmentHomeBinding.preShowBox.setVisibility(View.VISIBLE);
        fragmentHomeBinding.inputBox2.setVisibility(View.VISIBLE);

        //怪物增血
        tipTimes.setValue(tipTimes.getValue() + 1);

        //读正确的字母
        play(String.valueOf(checkedWord.charAt(litterIndex.getValue())), TextToSpeech.QUEUE_FLUSH);

    }
//endregion

    //region disposeSentence() 处理最后的句子
    static private void disposeSentence() {

        //对全文进行分句
        String s = fragmentHomeBinding.preShowBox.getText().toString();
        Log.d(TAG, "disposeSentence: " + s);
        String[] strings = s.split("[,.?!:;\n] ");
        int i = strings.length - 1;

        //播放最后索引的那个句子
        Log.d(TAG, "disposeSentence: " + strings[i]);
        play(strings[i], TextToSpeech.QUEUE_FLUSH);


        //处理句子,并在webView里显示翻译
        webView.loadUrl(translateURL.replaceAll("####",strings[i]));

    }
    //endregion

    // region getWordByWordPointer()
    private String getWordByWordPointer(int index) {
        return wordArray[index];
    }
    //endregion getWordByWordPointer()

    //region makeWordArray() 用String制作字符串数组
    private static String[] makeWordArray() {

        @NonNull String ori = sharedPreferences.getString(activity.getString(R.string.used_text_key), "现在使用的是默认文本: \n" + activity.getString(R.string.default_text));
        ori = ori.replaceAll(",", ", ");
        ori = ori.replaceAll("\\.", ". ");
        ori = ori.replaceAll("\\?", "? ");
        ori = ori.replaceAll("!", "! ");
        ori = ori.replaceAll(";", "; ");
        ori = ori.replaceAll(":", ": ");
        //以下是中文符号
        ori = ori.replaceAll("，", "， ");
        ori = ori.replaceAll("。", "。 ");
        ori = ori.replaceAll("？", "？ ");
        ori = ori.replaceAll("；", "； ");
        ori = ori.replaceAll("：", "： ");
        ori = ori.replaceAll("！", "！ ");
        //清除多余空格
        ori = ori.replaceAll(" +", " ");
        Log.d(TAG, "makeWordArray: " + ori);
        return ori.split(" ");//单词边界或一个或多个空白字符
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