package com.nantian.ad;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.nantian.utils.HLog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class LogView 
{
    private LinearLayout mView;

    private Context mContext;

    private TextView contextText;

    private Button btnDetele;

    // 增加一个EditText 和一个 Button 来对HLog的 内容作过滤 zWX204232 2014.2.7
    private EditText filterText;

    private ToggleButton filterBtn;

    private Button btnClr;

    private AsyncTask<String, Void, SpannableStringBuilder> filterTask = new FilterTask();

    public LogView(Context mContext)
    {

        this.mContext = mContext;
        mView = new LinearLayout(mContext);
        android.widget.LinearLayout.LayoutParams linear_lp  = new android.widget.LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mView.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollview = new ScrollView(mContext);
        		

        contextText = new TextView(mContext);
        scrollview.addView(contextText);
        mView.addView(scrollview,linear_lp);
        filterText = new EditText(mContext);
        filterBtn = new ToggleButton(mContext);
        btnDetele = new Button(mContext);
        btnClr = new Button(mContext);
        /**
        btnDetele = (Button) mView.findViewById(R.id.log_view_btn_detele);
        
        // 增加一个EditText 和一个 Button 来对HLog的 内容作过滤 zWX204232 2014.2.7

        filterBtn.setTextOn("filter");
        filterBtn.setTextOff("no filter");
        btnClr = (Button) mView.findViewById(R.id.btnClr);
        btnClr.
        initListener();
        */
    }

    private void initListener()
    {
        btnDetele.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                HLog.delFile();
                readLog();
				
			}
        });


        filterText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // TODO Auto-generated method stub
                if (TextUtils.isEmpty(s))
                {
                    btnClr.setVisibility(View.GONE);
                }
                else
                {
                    btnClr.setVisibility(View.VISIBLE);
                }
                refreshContextText();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // TODO Auto-generated method stub

            }

        });

        filterBtn.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                refreshContextText();
            }
        });

        btnClr.setOnClickListener(new OnClickListener()
        {

            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                filterText.setText("");
            }
        });

    }

    private void refreshContextText()
    {
        // editText没有输入，直接输出
        if (filterText.getText().length() == 0)
        {
            contextText.setText(HLog.readLog());
        }
        else
        {
            // contextText.setText(getFilterText(HLog.readLog()));
            // new AsyncTask<String, Void, SpannableStringBuilder>()
            // {
            //
            // protected SpannableStringBuilder doInBackground(String... params)
            // {
            // // TODO Auto-generated method stub
            // return getFilterText(params[0]);
            // }
            //
            // @Override
            // protected void onPostExecute(SpannableStringBuilder result)
            // {
            // // TODO Auto-generated method stub
            // contextText.setText(result);
            // }
            //
            // }.execute(HLog.readLog());

            if (filterTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                filterTask.cancel(true);
                HLog.v("asyncTask", "refreshContextText filterTask.isCancelled() = " + filterTask.isCancelled());

            }
            filterTask = new FilterTask();
            filterTask.execute(HLog.readLog());
        }
    }

    // 增加一个EditText 和一个 Button 来对HLog的 内容作过滤 zWX204232 2014.2.7
    private SpannableStringBuilder getFilterText(String oriText)
    {
        int start, end;
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        BufferedReader br = new BufferedReader(new StringReader(oriText));
        try
        {
            String line = br.readLine();
            String line2LowCase;
            String filterStr = filterText.getText().toString().toLowerCase();
            while (line != null)
            {
                if (filterTask.isCancelled())
                {
                    HLog.v("asyncTask", "filterTask.isCancelled() = " + filterTask.isCancelled());
                    break;
                }
                // 把读取到的line 的内容 和 filterText.getText()全部转化成小写
                line2LowCase = line.toLowerCase();

                // 包含关键字
                if (line2LowCase.contains(filterStr))
                {
                    // 包含的字体红色显示
                    start = ssb.length() + line2LowCase.indexOf(filterStr);
                    end = start + filterStr.length();

                    ssb.append(line + "\n");
                    // 字体变红，粗体，放大1.5倍
                    ssb.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    ssb.setSpan(new AbsoluteSizeSpan((int) (contextText.getTextSize() * 1.5)), start, end,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                // 不包含关键字
                else
                {
                    if (filterBtn.isChecked())
                    {
                        // filterBtn.isChecked() == true, 不包含关键字的line，不添加到新的text中
                    }
                    else
                    {
                        // filterBtn.isChecked() == false, 包含关键字的line，不添加到新的text中
                        ssb.append(line + "\n");
                    }
                }
                line = br.readLine();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ssb;
    }

    private void readLog()
    {
        contextText.setText("");
        contextText.setText(HLog.readLog());
    }



    public View getLayoutView()
    {
        // readLog();
        //
        filterText.clearFocus();
        refreshContextText();
        return mView;
    }

    public class FilterTask extends AsyncTask<String, Void, SpannableStringBuilder>
    {

        protected SpannableStringBuilder doInBackground(String... params)
        {
            // TODO Auto-generated method stub
            return getFilterText(params[0]);
        }

        @Override
        protected void onPostExecute(SpannableStringBuilder result)
        {
            // TODO Auto-generated method stub
            contextText.setText(result);
        }

        @Override
        protected void onCancelled(SpannableStringBuilder result)
        {
            // TODO Auto-generated method stub
            super.onCancelled(result);
            HLog.v("asyncTask", "cancell the asyncTask result");
        }

        @Override
        protected void onCancelled()
        {
            // TODO Auto-generated method stub
            super.onCancelled();
            HLog.v("asyncTask", "cancell the asyncTask");
        }

    }
}
