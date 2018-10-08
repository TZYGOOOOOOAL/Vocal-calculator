package edu.whut.taozeyuan.calculator;

import android.content.res.TypedArray;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

/*
    注意：在xml文件中button的ID用integer-array存放，不能直接利用getIntArray获取
        需要利用TypeArray解析，getResourceID获得ID值
 */
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener , View.OnLongClickListener ,TextToSpeech.OnInitListener {
    private ArrayList<Button> buttonArrayList;    //所有button存在list中
    private TypedArray buttonIdArray;
    private String[] buttonTextWordsArray;
    private String[] speakerTextWordsArray;
    private HashMap<Integer,String> buttonHashMap;
    private HashMap<String,String>  speakerHashMap;
    private LinkedList<String> inputLinkedList;
    private EditText inputEditText;
    private MyCalculator myCalculator;
    private TextToSpeech speaker;
    private double ans=0;
    private boolean calculateIsOver=false;
    private static boolean IS_ANSWER=true;
    private static boolean IS_NOT_ANSWER=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonArrayList=new ArrayList<>();    //所有button存在list中
        //buttonIdArray=getResources().getIntArray(R.array.button_id);
        buttonIdArray=getResources().obtainTypedArray(R.array.button_id);
        buttonTextWordsArray=getResources().getStringArray(R.array.button_words);
        speakerTextWordsArray=getResources().getStringArray(R.array.speaker_words);
        buttonHashMap=new HashMap<>();
        speakerHashMap=new HashMap<>();
        init();
        inputEditText=(EditText)findViewById(R.id.inputEditText);
        inputLinkedList=new LinkedList<>();
        speaker=new TextToSpeech(this,this);    //第一个参数Context，第二个TextToSpeech.OnInitListener
    }
    //初始化控件关联
    //初始化哈希表
    private void init() {
        for(int i=0;i<buttonIdArray.length();i++)
        {   //button关联，用ArrayList存放所有button
            Button button=(Button) findViewById(buttonIdArray.getResourceId(i,-1));
            button.setText(buttonTextWordsArray[i]);
            button.setOnClickListener(this);
            buttonArrayList.add(button);
            buttonHashMap.put(buttonIdArray.getResourceId(i,-1),buttonTextWordsArray[i]);
            speakerHashMap.put(buttonTextWordsArray[i],speakerTextWordsArray[i]);
        }
        buttonArrayList.get(0).setOnLongClickListener(this);    //清除建长按全清
    }

    @Override
    public void onClick(View v) {
        String input=buttonHashMap.get(v.getId());
        letUsSpeak(input,IS_NOT_ANSWER);      //语音播报
        if(!input.equals("C") && !input.equals("="))
        {
            if(calculateIsOver){
                //上一计算结束，重新输入
                inputEditText.setText("");
                inputLinkedList.clear();
            }
            inputEditText.append(input);
            inputLinkedList.add(input);
            calculateIsOver=false;
        }
        else if(input.equals("C") && !inputLinkedList.isEmpty())
        {
            calculateIsOver=false;
            inputLinkedList.removeLast();           //删除从链表中最后一个remove
            inputEditText.setText("");
            for(String str:inputLinkedList){        //foreach速度更快
                inputEditText.append(str);
            }
        }
        else if (input.equals("=") && !calculateIsOver)
        {
            String inputStr=inputEditText.getText().toString();
            myCalculator=new MyCalculator(inputStr,ans);
            try {
                ans=myCalculator.calculate();   //计算
            } catch (Exception e) {
                inputEditText.append("\n="+e.getMessage());
                return;
            }finally {
                calculateIsOver=true;               //标志计算结束
           }
            BigDecimal bigDecimalAns=new BigDecimal(""+ans);
            inputEditText.append("\n="+bigDecimalAns);
            letUsSpeak(""+bigDecimalAns,IS_ANSWER);
       }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId()==buttonIdArray.getResourceId(0,-1))
        {
            inputLinkedList.clear();               //输入链表全remove
            inputEditText.setText("");
        }
        return true;
    }

    //语音播报重写方法
    //设置语音语速和判断是否支持中文语音
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = speaker.setLanguage(Locale.CHINESE);
            speaker.setPitch(1.0f);         //音调
            speaker.setSpeechRate(1.0f);    //语速
            //语言包不支持或丢失
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language is not available.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Could not initialize TextToSpeech.", Toast.LENGTH_SHORT).show();
        }
    }

    private void letUsSpeak(String inputSpeakString ,boolean isAnswer){
        if(isAnswer){
            speaker.speak(inputSpeakString,TextToSpeech.QUEUE_ADD ,null);
        }else{
            //通过hashmap找到按键对应的中文发音
            String strToSpeak=speakerHashMap.get(inputSpeakString);
            //QUEUE_FLUSH清除当前语音任务，转而执行新的列队任务
            //QUEUE_ADD添加到队列后
            speaker.speak(strToSpeak,TextToSpeech.QUEUE_FLUSH ,null);
        }
    }
}
