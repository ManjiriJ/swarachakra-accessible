package game.Typing;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class CustomTouchListener implements OnTouchListener {

    private CustomKeyboard viewHandle;
    private float x,y;
    private float width, height;
    public Vibrator myVib;
    private AudioManager am;
    public TextToSpeech tts, tts1;
    int COUNT_DOWN_TIME=1000;
    int l,k;
    float touchdX=0,touchdY=0,touchdX1=0,touchdY1=0,distance=0,distance1=0;
    boolean touch_flag = false, multi_touch_flag = false, action_down_flag = false, multi_touch = false,tri_touch=false,navigator_flag=false;
    int flag[][] = new int[11][7];
    int flag1[] = new int[10];
    boolean action_downflag2[] = {false,false,false};
    int P_CHECK_FLAGS[][] =  new int[11][7];
    boolean PREV_COORDINATE=false;
    int THRESHOLD_DIST = 50,THRESHOLD_DIST_LAST_ROW = 60;
    boolean PROXIMITY_CHECK=false;
    boolean vowelBoolean = false;

    private String TAG = "production";


    public void setHandle(CustomKeyboard cb){
        viewHandle = cb;
    }

    public float getX(){

        return x;
    }
    public float getY(){
        return y;
    }

    public void initTTS(){
        tts = new TextToSpeech(viewHandle.mHostActivity, onInit);
        tts1 = new TextToSpeech(viewHandle.mHostActivity, onInit1);
    }

    public void speakOut_pitch(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        tts1.setPitch((float) 0.8);
        tts1.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    public void speakOut(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        tts.setPitch(1);
        tts.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    public void speakOut(String keyCodelabel,float pitch) {
//        if(!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())){
        if(pitch == 0.8){
            tts.setPitch((float) 0.8);
        }else{
            tts.setPitch(pitch);
        }
        tts.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void key_touch(int r, int s, String label,int vibrate_time){
        Log.d(TAG,"On key touch");
        if (flag[r][s] == 0) {
            speakOut(label);
            myVib.vibrate(vibrate_time);
            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                public void onTick(long millisUntilFinished) {
                    PROXIMITY_CHECK =true;
                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    //Done timer time out
                    Log.i("Countdown Timer:","TimeOut");
                }
            }.start();
            //Intialising the flag value of this column to 1 and rest of the keys to 0
            assign_flag(r,s);
            Log.d("key", "1");

        }
    }

    public void assign_flag(int r,int s){
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == r && j == s) {
                    flag[i][j] = 1;
                } else {
                    flag[i][j] = 0;
                }
            }
        }
    }
    public void proximity_check(int r,int s){
        if (P_CHECK_FLAGS[r][s] == 0) {

            //check any other p_check_flags are intialised to 1
            for (int i = 0; i < 11; i++) {
                for (int j = 0; j < 7; j++) {
                    if (P_CHECK_FLAGS[i][j] == 1) {
                        PREV_COORDINATE = true;
                    }
                }
            }

            //if there is no other flags intilaise then set a new starting point
            if (PREV_COORDINATE == false) {
                for (int i = 0; i < 11; i++) {
                    for (int j = 0; j < 7; j++) {
                        if (i == r && j == s) {
                            P_CHECK_FLAGS[i][j] = 1;
                        }
                    }
                }
                touchdX = x;
                touchdY = y;
            }


        }

        //differnce b/w two points
        touchdX1 = x - touchdX;
        touchdY1 = y - touchdY;
        distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

        if (distance > THRESHOLD_DIST_LAST_ROW) {
            PROXIMITY_CHECK = false;
            //setting all the flags to 0
            for (int i = 0; i < 11; i++) {
                for (int j = 0; j < 7; j++) {
                    P_CHECK_FLAGS[i][j] = 0;
                }
            }
            //setting prev cordinate to false
            PREV_COORDINATE = false;
        }
        Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
        Log.d("proximitydistance", String.valueOf(distance));

    }
    public String getLastword(){

        View focusCurrent = viewHandle.mHostActivity.getWindow().getCurrentFocus();
        EditText edittext = (EditText) focusCurrent;
        String s = edittext.getText().toString();
        int selectionEnd = edittext.getSelectionEnd();
        selectionEnd = edittext.getSelectionEnd();
        String text = edittext.getText().toString();
        if (selectionEnd >= 0) {
            // gives you the substring from start to the current cursor
            // position
            text = text.substring(0, selectionEnd);
//                            speakOut_pitch(text);
        }
        String delimiter = " ";
        int lastDelimiterPosition = text.lastIndexOf(delimiter);
        String lastWord = lastDelimiterPosition == -1 ? text :
                text.substring(lastDelimiterPosition + delimiter.length());
        Log.d(TAG,"Last word:"+lastWord);
        return lastWord;
    }

    public void log_chars(char c){

//        //append & delete code for FT
        viewHandle.et1 = (EditText) viewHandle.mHostActivity.findViewById(R.id.editText1);
        viewHandle.et1.append(c+"");
        viewHandle.et1.getText().delete(viewHandle.et1.getText().length() - 1,
                viewHandle.et1.getText().length());


    }

    TextToSpeech.OnInitListener onInit = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                final Locale loc = new Locale("hin", "IND");
                int result = tts.setLanguage(loc);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(TAG, "1:This Language is not supported");
                    Toast.makeText(viewHandle.getContext(), "Your default text to speech engine does not support Hindi. Please download Hindi locale for current TTS or switch to a text to speech engine that supports Hindi locale.",Toast.LENGTH_LONG).show();
                } else {

//                    speakOut(keyCodelabel);
                    Log.d(TAG, "1: This Language is supported");
                }

            } else {
                Log.d(TAG, "1:Initilization Failed!");
            }
        }
    };
    //Text to Speech
    TextToSpeech.OnInitListener  onInit1 = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                final Locale loc = new Locale("hin", "IND");
                int result1 = tts.setLanguage(loc);
                if (result1 == TextToSpeech.LANG_MISSING_DATA
                        || result1 == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(TAG, "2:This Language is not supported");
                    Toast.makeText(viewHandle.getContext(), "Your default text to speech engine does not support Hindi. Please download Hindi locale for current TTS or switch to a text to speech engine that supports Hindi locale.",Toast.LENGTH_LONG).show();

                } else {

//                    speakOut(keyCodelabel);
                    Log.d(TAG, "2:This Language is supported");
                }

            } else {
                Log.d(TAG, "2:Initilization Failed!");
            }
        }
    };


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int pointerIndex = event.getActionIndex();
//            int liveregion = v.getAccessibilityLiveRegion();

        //total height of keyonboard = 1545 & width of a row = 1080
        //height of row = 15.45 ,  width of a first row keys = 180 &&  width of next row keys = 216
//            Log.d("height of keyboard",Integer.toString(mKeyboardView.getHeight()));
//            Log.d("width of keyboard",Integer.toString(mKeyboardView.getWidth()));

        Log.d("(Y)", Integer.toString((int) event.getRawY()));

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

//            Log.d("Multitouch","intialised");
        if (event.getPointerCount() > 2)
            multi_touch = false;
        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
                action_down_flag = true;
                viewHandle.action_up  = false;
                Log.d("touch", "single touch");
                x = (int) event.getX();
                y = (int) event.getY();
                int curkey = viewHandle.keyCodeClicked;
                Display display = viewHandle.mHostActivity.getWindowManager().getDefaultDisplay();
                Point size1 = new Point();
                display.getSize(size1);
                width = viewHandle.mKeyboardView.getWidth();
                height = viewHandle.mKeyboardView.getHeight();
//                    Log.d("width,height",width+","+height);

                Log.d("keycodeclicked", Integer.toString(curkey));

                myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
                am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);

                if(y <2*height/9){
                    if(x>5*width/6 && x < width){
                        key_touch(0,6,"delete",50);
                    }
                }
                Log.d("height",String.valueOf(height));
                if (y < height/9 ) {
                    if (x < width/6) {
                        if (flag[1][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0915\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0915\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "\u0930\u094D\u0915";
                                speakOut("\u0930\u094D\u0915");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0915\u093C";
                                speakOut("\u0915"+"nukta");


                            } else {
                                speakOut("\u0915");
                                viewHandle.keyCodelabel = "\u0915";


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.d("proximity","Check is true for 1 0");
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[1][0]==1){
                                        speakOut("काम");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 1 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            myVib.vibrate(500);

                            Log.d("keycodelabel", viewHandle.keyCodelabel);
                            Log.d("key", "1");
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[1][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0916\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0916\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "\u0930\u094D\u0916";
                                speakOut("\u0930\u094D\u0916");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0916\u093C";
                                speakOut("\u0916"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0916";
                                Log.d("keycodelabel", viewHandle.keyCodelabel);
                                speakOut("\u0916");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK= true;
                                    Log.d("proximity","Check is true for 1 1");
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[1][1]==1){
                                        speakOut("खुशी");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("timer", "aftertimer");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 1 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            myVib.vibrate(50);
                            Log.d("key", "2");
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[1][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0917\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0917\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "\u0930\u094D\u0917";
                                speakOut("\u0930\u094D\u0917");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0917\u093C";
                                speakOut("\u0917"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0917";
                                speakOut("\u0917");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[1][2]==1){
                                        speakOut("गंगा");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("timer","aftertimer");

                            Log.d("key", "3");
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 1 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[1][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0918\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0918\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "\u0930\u094D\u0918";
                                speakOut("र्\u0918");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "घ";
                                speakOut("घ");


                            } else {
                                viewHandle.keyCodelabel = "\u0918";
                                speakOut("\u0918");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[1][3]==1){
                                        speakOut("घंटा");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            Log.d("timer", "aftertimer");

//     keyCodelabel = "\u0918";
//                                speakOut("\u0918");
                            Log.d("key", "4");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 1 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[1][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0919\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0919\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0919";
                                speakOut("र्\u0919");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ङ";
                                speakOut("ङ");

                            } else {
                                viewHandle.keyCodelabel = "\u0919";
                                speakOut("\u0919");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            Log.d("timer","aftertimer");

//                                keyCodelabel = "\u0919";
//                                speakOut("\u0919");
                            myVib.vibrate(500);

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 1 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");

                        }
                    } else if (x > 5 * width/6 && x < 6 * width/6) {
//                                speakOut("delete");
//                                CountDownTimer timer = new CountDownTimer(5000 /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {
//
//                                    public void onTick(long millisUntilFinished) {
//                                        PROXIMITY_CHECK=true;
//
//                                    }
//
//                                    public void onFinish() {
//                                        //Done timer time out.
//                                        Log.i("Countdown Timer:","TimeOut");
//                                        long_backspace();
//                                    }
//                                }.start();
//
//                                //Intialising the flag value of this column to 1 and rest of the keys to 0
//                                for (int i = 0; i < 11; i++) {
//                                    for (int j = 0; j < 7; j++) {
//                                        if (i == 0 && j == 6) {
//                                            flag[i][j] = 1;
//                                        } else {
//                                            flag[i][j] = 0;
//                                        }
//                                    }
//                                }
//                                myVib.vibrate(50);
                    }

                } else if (y >  height/9 && y < 2 * height/9) {
                    if (x < width/6) {
                        if (flag[2][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091A\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091A\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091A";
                                speakOut("र्\u091A");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "च";
                                speakOut("च");

                            } else {
                                viewHandle.keyCodelabel = "\u091A";
                                speakOut("\u091A");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[2][0]==1){
                                        PROXIMITY_CHECK=true;
                                        speakOut("चम्मच");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();


//                                keyCodelabel = "\u091A";
//                                speakOut(keyCodelabel);


                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 2 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[2][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091B\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091B\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091B";
                                speakOut("र्\u091B");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "छ";
                                speakOut("छ");

                            } else {
                                viewHandle.keyCodelabel = "\u091B";
                                speakOut("\u091B");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[2][1]==1){
                                        speakOut("छतरी");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();


//                                keyCodelabel = "\u091B";
//                                speakOut("\u091B");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 2 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[2][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091C\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091C\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091C";
                                speakOut("र्\u091C");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u091C़";
                                speakOut("\u091C"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u091C";
                                speakOut("\u091C");

                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[2][2]==1){
                                        speakOut("ज़मीन");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u091C";
//                                speakOut("\u091C");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 2 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[2][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091D\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091D\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091D";
                                speakOut("र्\u091D");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "झ";
                                speakOut("झ");

                            } else {
                                viewHandle.keyCodelabel = "\u091D";
                                speakOut("\u091D");

                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[2][3]==1){
                                        speakOut("झन्डा");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
// /                                keyCodelabel = "\u091D";
//                                speakOut("\u091D");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 2 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[2][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091E\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091E";
                                speakOut("र्\u091E");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ञ";
                                speakOut("ञ");

                            } else {
                                viewHandle.keyCodelabel = "\u091E";
                                speakOut("\u091E");

                            }
//                                keyCodelabel = "\u091E";
//                                speakOut("\u091E");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 2 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }

                            Log.d("key", "5");
                            myVib.vibrate(50);
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
//                                speakOut("delete");
//                                CountDownTimer timer = new CountDownTimer(5000 /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {
//
//                                    public void onTick(long millisUntilFinished) {
//                                        PROXIMITY_CHECK=true;
//
//                                    }
//
//                                    public void onFinish() {
//                                        //Done timer time out.
//                                        Log.i("Countdown Timer:","TimeOut");
//                                        long_backspace();
//                                    }
//                                }.start();
//
//                                //Intialising the flag value of this column to 1 and rest of the keys to 0
//                                for (int i = 0; i < 11; i++) {
//                                    for (int j = 0; j < 7; j++) {
//                                        if (i == 0 && j == 6) {
//                                            flag[i][j] = 1;
//                                        } else {
//                                            flag[i][j] = 0;
//                                        }
//                                    }
//                                }
//                                myVib.vibrate(50);

                    }
                } else if (y > 2 * height/9 && y < 3 * height/9) {
                    if (x < width/6) {
                        if (flag[3][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091F\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091F\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091F";
                                speakOut("र्\u091F");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ट";
                                speakOut("ट");


                            } else {
                                viewHandle.keyCodelabel = "\u091F";
                                speakOut("\u091F");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[3][0]==1){
                                        speakOut("टमाटर");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u091F";
//                                speakOut("\u091F");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 3 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[3][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0920\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0920\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0920";
                                speakOut("र्\u0920");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ठ";
                                speakOut("ठ");

                            } else {
                                viewHandle.keyCodelabel = "\u0920";
                                speakOut("\u0920");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[3][1]==1){
                                        speakOut("ठप्पा");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u0920";
//                                speakOut("\u0920");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 3 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[3][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0921\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0921\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0921";
                                speakOut("र्\u0921");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0921़";
                                speakOut("\u0921"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0921";
                                speakOut("\u0921");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[3][2]==1){
                                        speakOut("डमरू");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u0921";
//                                speakOut("\u0921");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 3 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[3][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0922\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0922\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0922";
                                speakOut("र्\u0922");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0922़";
                                speakOut("\u0922"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0922";
                                speakOut("\u0922");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[3][3]==1){
                                        speakOut("ढोलक");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u0922";
//                                speakOut("\u0922");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 3 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[3][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0923\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0923\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0923";
                                speakOut("र्\u0923");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ण";
                                speakOut("ण");

                            } else {
                                viewHandle.keyCodelabel = "\u0923";
                                speakOut("\u0923");
                            }
//                                keyCodelabel = "\u0923";
//                                speakOut("\u0923");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 3 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("key", "5");
                            myVib.vibrate(50);
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        //speakOut("ं");
                        speakOut("अनुस्वार");
                        viewHandle.keyCodelabel = "\u0902";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 0) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }
                } else if (y > 3 * height/9 && y < 4 * height/9) {
                    if (x < width/6) {
                        if (flag[4][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0924\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0924\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0924";
                                speakOut("र्\u0924");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "त";
                                speakOut("त");


                            } else {
                                viewHandle.keyCodelabel = "\u0924";
                                speakOut("\u0924");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[4][0]==1){
                                        speakOut("तरबूज़");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
//                                keyCodelabel = "\u0924";
//                                speakOut("\u0924");

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 4 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[4][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0925\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0925\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0925";
                                speakOut("र्\u0925");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "थ";
                                speakOut("थ");


                            } else {
                                viewHandle.keyCodelabel = "\u0925";
                                speakOut("\u0925");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[4][1]==1){
                                        speakOut("थरमस");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 4 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[4][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0926\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0926\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0926";
                                speakOut("र्\u0926");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "द";
                                speakOut("द");

                            } else {
                                viewHandle.keyCodelabel = "\u0926";
                                speakOut("\u0926");

                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[4][2]==1){
                                        speakOut("दवात");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

//                                keyCodelabel = "\u0926";

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 4 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
//                                speakOut("\u0926");
                            myVib.vibrate(500);
                            Log.d("key", "3");
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[4][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0927\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0927\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0927";
                                speakOut("र्\u0927");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ध";
                                speakOut("ध");

                            } else {
                                viewHandle.keyCodelabel = "\u0927";
                                speakOut("\u0927");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[4][3]==1){
                                        speakOut("धनुष");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 4 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[4][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0928\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0928\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0928";
                                speakOut("र्\u0928");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0928़";
                                speakOut("\u0928"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0928";
                                speakOut("\u0928");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[4][4]==1){
                                        speakOut("नल");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 4 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");
                            myVib.vibrate(50);
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        //speakOut("ः");
                        speakOut("विसर्ग");
                        viewHandle.keyCodelabel = "\u0903";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 1) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }
                } else if (y > 4 * height/9 && y < 5 * height/9) {
                    if (x < width/6) {
                        if (flag[5][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092A\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092A\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092A";
                                speakOut("र्\u092A");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "प";
                                speakOut("प");

                            } else {
                                viewHandle.keyCodelabel = "\u092A";
                                speakOut("\u092A");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[5][0]==1){
                                        speakOut("पतंग");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 5 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[5][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092B\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092B\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092B";
                                speakOut("र्\u092B");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u092B़";
                                speakOut("\u092B"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u092B";
                                speakOut("\u092B");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[5][1]==1){
                                        speakOut("फल");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 5 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[5][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092C\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092C\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092C";
                                speakOut("र्\u092C");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ब";
                                speakOut("ब");

                            } else {
                                viewHandle.keyCodelabel = "\u092C";
                                speakOut("\u092C");


                            } CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[5][2]==1){
                                        speakOut("बतख");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 5 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[5][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092D\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092D\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092D";
                                speakOut("र्\u092D");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "भ";
                                speakOut("भ");
                            } else {
                                viewHandle.keyCodelabel = "\u092D";
                                speakOut("\u092D");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[5][3]==1){
                                        speakOut("भालू");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 5 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[5][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092E\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092E";
                                speakOut("र्\u092E");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "म";
                                speakOut("म");


                            } else {
                                viewHandle.keyCodelabel = "\u092E";
                                speakOut("\u092E");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[5][4]==1){
                                        speakOut("मछली");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 5 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");
                            myVib.vibrate(50);
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        //speakOut("ँ");
                        speakOut("चंद्रबिंदु");
                        viewHandle.keyCodelabel = "\u0901";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 2) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }

                } else if (y > 5 * height/9 && y < 6 * height/9) {
                    if (x < width/6) {
                        if (flag[6][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u092F\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092F\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092F";
                                speakOut("र्\u092F");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u092F़";
                                speakOut("\u092F"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u092F";
                                speakOut("\u092F");
                            }


                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 6 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[6][0]==1){
                                        speakOut("यज्ञ");
                                    }

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[6][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0930\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0930";
                                speakOut("र्\u0930");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0930़";
                                speakOut("\u0930"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0930";
                                speakOut("\u0930");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[6][1]==1) {
                                        speakOut("रस्सी");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 6 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[6][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0932\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0932\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0932";
                                speakOut("र्\u0932");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "\u0932़";
                                speakOut("\u0932"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0932";
                                speakOut("\u0932");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[6][2]==1) {
                                        speakOut("लडका");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 6 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[6][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0935\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0935\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0935";
                                speakOut("र्\u0935");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "व";
                                speakOut("व");

                            } else {
                                viewHandle.keyCodelabel = "\u0935";
                                speakOut("\u0935");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[6][3]==1){
                                        speakOut("वन");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 6 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[6][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0936\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0936\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0936";
                                speakOut("र्\u0936");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "श";
                                speakOut("श");


                            } else {
                                viewHandle.keyCodelabel = "\u0936";
                                speakOut("\u0936");


                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[6][4]==1){
                                        speakOut("शहर");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 6 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");
                            myVib.vibrate(50);
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        speakOut("Nukta");
                        viewHandle.keyCodelabel="";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 3) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }
                } else if (y > 6 * height/9 && y < 7 * height/9) {
                    if (x < width/6) {
                        if (flag[7][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0937\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0937\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0937";
                                speakOut("र्\u0937");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ष";
                                speakOut("ष");


                            } else {
                                viewHandle.keyCodelabel = "\u0937";
                                speakOut("\u0937");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[7][0] == 1) {
                                        speakOut("षद्कोण");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();


                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 7 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(50);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[7][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0938\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0938\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0938";
                                speakOut("र्\u0938");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "स";
                                speakOut("स");


                            } else {
                                viewHandle.keyCodelabel = "\u0938";
                                speakOut("\u0938");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[7][1] == 1){
                                        speakOut("सेब");}
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 7 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }

                            }
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[7][2] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0939\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0939\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0939";
                                speakOut("र्\u0939");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ह";
                                speakOut("ह");

                            } else {
                                viewHandle.keyCodelabel = "\u0939";
                                speakOut("\u0939");
                            }


                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 7 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[7][2] == 1) {
                                        speakOut("हरिन");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[7][3] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0924\u094D\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0924\u094D\u0930\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0924\u094D\u0930";
                                speakOut("र्\u0924\u094D\u0930");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "त्र";
                                speakOut("त्र");

                            } else {
                                viewHandle.keyCodelabel = "\u0924\u094D\u0930";
                                speakOut("\u0924\u094D\u0930");
                            }


                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 7 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[7][3] == 1){
                                        speakOut("त्रिशुल");}
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[7][4] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0915\u094D\u0937\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0915\u094D\u0937\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0915\u094D\u0937";
                                speakOut("र्\u0915\u094D\u0937");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "क्ष";
                                speakOut("क्ष");


                            } else {
                                viewHandle.keyCodelabel = "\u0915\u094D\u0937";
                                speakOut("\u0915\u094D\u0937");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[7][4]==1){
                                        speakOut("क्षत्रिय");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 7 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");
                            myVib.vibrate(50);
                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        speakOut("Trakaar");
                        viewHandle.keyCodelabel ="";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 4) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }
                } else if (y > 7 * height/9 && y < 8 * height/9) {
                    if (x < width/6) {
                        if (flag[8][0] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u091C\u094D\u091E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091C\u094D\u091E\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091C\u094D\u091E";
                                speakOut("र्\u091C\u094D\u091E");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "ज्ञ";
                                speakOut("ज्ञ");


                            } else {
                                viewHandle.keyCodelabel = "\u091C\u094D\u091E";
                                speakOut("\u091C\u094D\u091E");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK=true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[8][0]==1){
                                        speakOut("ज्ञान");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 8 && j == 0) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "1");
                            myVib.vibrate(500);
                        }
                    } else if (x > width/6 && x < 2 * width/6) {
                        if (flag[8][1] == 0) {
                            if (viewHandle.flag2[1]) {
                                speakOut("\u0936\u094D\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0936\u094D\u0930\u094D\u0930";


                            } else if (viewHandle.flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0930\u094D\u0936";
                                speakOut("र्\u0930\u094D\u0936");


                            } else if (viewHandle.flag2[0]) {
                                viewHandle.keyCodelabel = "श्र";
                                speakOut("श्र");

                            } else {
                                speakOut("\u0936\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0936\u094D\u0930";
                            }

                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 8 && j == 1) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    if(flag[8][1] == 1){
                                        speakOut("श्रम");
                                    }
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            Log.d("key", "2");
                            myVib.vibrate(50);
                        }
                    } else if (x > 2 * width/6 && x < 3 * width/6) {
                        if (flag[8][2] == 0) {
                            speakOut("स्वर वर्ण");
                            viewHandle.keyCodelabel = "\u093E";
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 8 && j == 2) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "3");
                            myVib.vibrate(50);
                        }
                    } else if (x > 3 * width/6 && x < 4 * width/6) {
                        if (flag[8][3] == 0) {

                            speakOut("Ru");
                            viewHandle.keyCodelabel = "\u0943";  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.

                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 8 && j == 3) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "4");
                            myVib.vibrate(50);
                        }
                    } else if (x > 4 * width/6 && x < 5 * width/6) {
                        if (flag[8][4] == 0) {
                            speakOut("अ");
                            viewHandle.keyCodelabel = "\u0905";
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                public void onTick(long millisUntilFinished) {
                                    PROXIMITY_CHECK =true;
                                    Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //Done timer time out.
                                    speakOut("आम");
                                    Log.i("Countdown Timer:","TimeOut");
                                }
                            }.start();
                            myVib.vibrate(500);
                            //Intialising the flag value of this column to 1 and rest of the keys to 0
                            for (int i = 0; i < 11; i++) {
                                for (int j = 0; j < 7; j++) {
                                    if (i == 8 && j == 4) {
                                        flag[i][j] = 1;
                                    } else {
                                        flag[i][j] = 0;
                                    }
                                }
                            }
                            Log.d("key", "5");

                        }
                    }else if (x > 5 * width/6 && x < 6 * width/6) {
                        speakOut("Rafaar");
                        viewHandle.keyCodelabel ="";
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                            public void onTick(long millisUntilFinished) {
                                PROXIMITY_CHECK=true;
                            }

                            public void onFinish() {
                                //Done timer time out.
                                Log.i("Countdown Timer:","TimeOut");
                            }
                        }.start();

                        //Intialising the flag value of this column to 1 and rest of the keys to 0
                        for (int i = 0; i < 11; i++) {
                            for (int j = 0; j < 7; j++) {
                                if (i == 0 && j == 5) {
                                    flag[i][j] = 1;
                                } else {
                                    flag[i][j] = 0;
                                }
                            }
                        }
                        myVib.vibrate(50);
                    }
                } else if (y > 8 * height/9 && y < 9 * height/9) {
                    if(x < width/6){
                        viewHandle.keyCodelabel = "";
                        key_touch(9,3,"Left",50);
                    }else if(x > width/6 && x< 2*width/6){
                        viewHandle.keyCodelabel = "";
                        key_touch(9,4,"Right",50);
                    }
                    if (x >2* width/6 && x < 5 * width/6) {
                        key_touch(9,0,"Space",50);
                    }
                    else if (x > 5 * width/6 && x < width){
                        key_touch(9,1,"Enter",50);
                    }
                }


                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 7; j++) {
                        if (flag[i][j] == 1) {
                            l = i;
                            k = j;
                        }
                    }
                }
                if(event.getPointerCount()==3){
                    viewHandle.desetArc();
                    tri_touch = true;
                    multi_touch=false;
                    navigator_flag = false;
                    viewHandle.setTouchDownPoint(event.getX(pointerIndex), event.getY(pointerIndex));
                    viewHandle.keyCodelabel="";
                }else if(event.getPointerCount()==2 && l<9 && l>0){
                    multi_touch = true;
                    navigator_flag = false;
                    tri_touch=false;
                    PointF f = new PointF();
                    f.x = event.getX(pointerIndex);
                    f.y = event.getY(pointerIndex);
                    Log.d("pointer_down_co", "(" + f.x + "," + f.y + ")");
                    viewHandle.setTouchDownPoint(f.x, f.y);
                    float touchMovementX = x - viewHandle.touchDownX;
                    float touchMovementY = y - viewHandle.touchDownY;

                    float theta = (float) Math.toDegrees(Math.atan2(touchMovementY,
                            touchMovementX));
                    Log.d("theta value", Integer.toString((int) theta));
                    viewHandle.arc = viewHandle.findArc(theta);
                    myVib.vibrate(50);
                }else if(event.getPointerCount() == 2 && l==9 && k==3){
                    Log.d("navigate", "left");
                    //split tap for left arrow
                    View focusCurrent = viewHandle.mHostActivity.getWindow().getCurrentFocus();
                    EditText edittext = (EditText) focusCurrent;
                    String s1 = edittext.getText().toString();
                    Editable editable = edittext.getText();
                    int start = edittext.getSelectionStart();
                    int selectionEnd = edittext.getSelectionEnd();
                    if (selectionEnd >= 0) {
                        // gives you the substring from start to the current cursor
                        // position
                        s1 = s1.substring(0, selectionEnd);
//                            speakOut_pitch(text);
                    }
                    String delimiter = " ";
                    int x = s1.lastIndexOf(delimiter);
                    Log.d("x",x+"");
                    if(x!=-1){
                        edittext.setSelection(x);
                        speakOut(getLastword(), (float) 0.8);
                    }else{
                        edittext.setSelection(0);
                        speakOut("Starting of the Text", (float) 0.8);
                    }


                }else if(event.getPointerCount() == 2 && l==9 && k==4){
                    //split tap for right arrow
                    View focusCurrent = viewHandle.mHostActivity.getWindow().getCurrentFocus();
                    EditText edittext = (EditText) focusCurrent;
                    String s1 = edittext.getText().toString();
                    Editable editable = edittext.getText();
                    int start = edittext.getSelectionStart();
                    int selectionEnd = edittext.getSelectionEnd();
                    if (selectionEnd >= 0) {
                        // gives you the substring from the current cursor to end
                        // position
                        s1 = s1.substring(selectionEnd);
//                            speakOut_pitch(text);
                    }
                    String delimiter = " ";
                    int x = s1.indexOf(" ");
                    Log.d("x",x+"");
                    if(x!=-1){
                        edittext.setSelection(x+1);
                        speakOut(getLastword(), (float) 0.8);
                    }else{
                        edittext.setSelection(0);
                        speakOut("Ending of the Text", (float) 0.8);
                    }


                    Log.d("navigate","right");

                }
                Log.d("check_view_added", "View added");
//                    Log.d("touch", "ACTION_POINTER_DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
                viewHandle.action_up =false;
                boolean leftflag=false,rightflag=false;
                if(tri_touch == true){
                    for (int i = 0; i < event.getPointerCount(); ++i) {
                        pointerIndex = i;
                        pointerId = event.getPointerId(pointerIndex);
                        Log.d("pointer id", Integer.toString(pointerId));

                        if (pointerId == 2) {
                            PointF f1 = new PointF();
                            f1.x = event.getX(pointerIndex);
                            f1.y = event.getY(pointerIndex);
                            if (viewHandle.touchDownX - f1.x > 170) {
//                                        Toast.makeText(mHostActivity,"left",Toast.LENGTH_SHORT).show();

                                for (i = 0; i < 11; i++) {
                                    for (int j = 0; j < 7; j++) {
                                        if (i == 9 && j == 5) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d("Direction","3-finger left swipe");
                            }
                            // left to right swipe
                            else if(f1.x - viewHandle.touchDownX >100) {
//                                        Toast.makeText(mHostActivity,"right",Toast.LENGTH_SHORT).show();

                                for (i = 0; i < 11; i++) {
                                    for (int j = 0; j < 7; j++) {
                                        if (i == 9 && j == 0) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d("Direction","right");
                            }else if(f1.x-viewHandle.touchDownX<100 || viewHandle.touchDownX-f1.x<100 ){
                                for (i = 0; i < 11; i++) {
                                    for (int j = 0; j < 7; j++) {
                                        if (i == 1 && j == 6) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d("Direction","touch");
                            }
                            Log.d("x,y", f1.x + "," + f1.y);
                        }

                    }


                }else if (multi_touch == true) {
                    Log.d("multitouch", "helo");
                    for (int i = 0; i < event.getPointerCount(); ++i) {
                        pointerIndex = i;
                        pointerId = event.getPointerId(pointerIndex);
                        Log.d("pointer id", Integer.toString(pointerId));

                        if (pointerId == 1) {
                            PointF f1 = new PointF();
                            f1.x = event.getX(pointerIndex);
                            f1.y = event.getY(pointerIndex);

                            viewHandle.handleMove((int) f1.x, (int) f1.y);

//                                MultiTouch mt1 = new MultiTouch(mHostActivity, null, null,f1.x,f1.y,"\u0916",mKeyboardView);
//                                mHostActivity.addContentView(mt1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                            Log.d("x,y", f1.x + "," + f1.y);
                        }

                    }
                } else {
                    // a pointer was moved
                    Log.d("touch", "movingfinger");

                    x = (int) event.getX();
                    y = (int) event.getY();
                    Display display1 = viewHandle.mHostActivity.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display1.getSize(size);
                    width = viewHandle.mKeyboardView.getWidth();
                    height = viewHandle.mKeyboardView.getHeight();
                    Log.d("MovementX,Y", x + "," + y);
//                    Log.d("width,height",width+","+height);

//                    Log.d("keycodeclicked", Integer.toString(curkey));

                    myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);
                    Log.d("height",String.valueOf(y));

                    //leftswipe for reading the word
                    if(y<0){
                        if(x < width/3){
                            if(flag[10][0]==0){
                                viewHandle.tv1 = (TextView) viewHandle.mHostActivity.findViewById(R.id.textView1);
                                String text = viewHandle.tv1.getText().toString();
                                speakOut(text);
                                Log.d("swipe", text);

                                for (int i = 0; i < 11; i++) {
                                    for (int j = 0; j < 7; j++) {
                                        if (i == 10 && j == 0) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }}
                        }else if(x > 2*width/3 && x < width){
                            viewHandle.et1 = (EditText) viewHandle.mHostActivity.findViewById(R.id.editText1);
                            speakOut(viewHandle.et1.getText().toString());
                            Log.d("swipe","b");
                            if(flag[10][1]==0){
                                for (int i = 0; i < 11; i++) {
                                    for (int j = 0; j < 7; j++) {
                                        if (i == 10 && j == 1) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }}
                        }

                    }


                    //delete
                    if(y < 2*height/9 && y > 0 ){
                        if(x > 5 *width/6 && x <width){

                            int r, s;
                            r = 0;
                            s = 6;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
//                                        Log.d("proximitydistance", String.valueOf(distancef));
                            } else {
                                if (flag[0][6] == 0) {
                                    speakOut("delete");
                                    viewHandle.keyCodelabel = "";
                                    CountDownTimer timer = new CountDownTimer(4000 /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
//                                                    long_backspace();
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                            if(viewHandle.action_up){
                                                long_backspace();}
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 6) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    }
                    if (y < height/9 && y > 0) {
                        if (x < width/6) {
                            int r, s;
                            r = 1;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {

                                if (flag[1][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0915\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0915";
                                        speakOut("\u0930\u094D\u0915");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0915\u093C";
                                        speakOut("\u0915" + "nukta");


                                    } else {
                                        speakOut("\u0915");
                                        viewHandle.keyCodelabel = "\u0915";


                                    }
                                    touchdX1 = x;
                                    touchdY1 = y;
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {

                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[1][0] == 1) {
                                                speakOut("काम");
                                            }
                                            PROXIMITY_CHECK = true;
                                            Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 1 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(500);

                                    Log.d("keycodelabel", viewHandle.keyCodelabel);
                                    Log.d("key", "1");
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 1;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {

                                if (flag[1][1] == 0) {

                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0916\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0916\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0916";
                                        speakOut("\u0930\u094D\u0916");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0916\u093C";
                                        speakOut("\u0916" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0916";
                                        Log.d("keycodelabel", viewHandle.keyCodelabel);
                                        speakOut("\u0916");


                                    }
                                    touchdX1 = x;
                                    touchdY1 = y;
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
//                                                viewHandle.PROXIMITY_CHECK=true;
                                            Log.i("Countdown Timer: ", String.valueOf(PROXIMITY_CHECK));
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[1][1] == 1) {
                                                speakOut("खुशी");
                                                PROXIMITY_CHECK = true;
                                                Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            }
                                            PROXIMITY_CHECK = true;
                                            Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    Log.d("timer", "aftertimer");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 1 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                    Log.d("key", "2");
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 1;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[1][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0917\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0917\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0917";
                                        speakOut("\u0930\u094D\u0917");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0917\u093C";
                                        speakOut("\u0917" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0917";
                                        speakOut("\u0917");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[1][2] == 1) {
                                                speakOut("गंगा");
                                                PROXIMITY_CHECK = true;
                                                Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    Log.d("timer", "aftertimer");

                                    Log.d("key", "3");
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 1 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 1;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[1][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0918\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0918\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0918";
                                        speakOut("र्\u0918");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "घ";
                                        speakOut("घ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0918";
                                        speakOut("\u0918");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[1][3] == 1) {
                                                speakOut("घंटा");
                                                PROXIMITY_CHECK = true;
                                                Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    Log.d("timer", "aftertimer");

//     keyCodelabel = "\u0918";
//                                speakOut("\u0918");
                                    Log.d("key", "4");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 1 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 1;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[1][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0919\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0919\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0919";
                                        speakOut("र्\u0919");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut("ङ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0919";
                                        speakOut("\u0919");

                                    }
                                    Log.d("timer", "aftertimer");
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[1][4] == 1) {
                                                PROXIMITY_CHECK = true;
                                                Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u0919";
//                                speakOut("\u0919");
                                    myVib.vibrate(500);

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 1 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");

                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){

                        }
                    } else if (y > height/9 && y < 2 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 2;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[2][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091A\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091A\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091A";
                                        speakOut("र्\u091A");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "च";
                                        speakOut("च");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091A";
                                        speakOut("\u091A");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[2][0] == 1) {
                                                speakOut("चम्मच");
                                                PROXIMITY_CHECK = true;
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();


//                                keyCodelabel = "\u091A";
//                                speakOut(keyCodelabel);


                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 2 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 2;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[2][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091B\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091B\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091B";
                                        speakOut("र्\u091B");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "छ";
                                        speakOut("छ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091B";
                                        speakOut("छ");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[2][1] == 1) {
                                                speakOut("छतरी");
                                                PROXIMITY_CHECK = true;
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();


//                                keyCodelabel = "\u091B";
//                                speakOut("\u091B");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 2 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 2;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[2][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091C\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091C\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091C";
                                        speakOut("र्\u091C");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u091C़";
                                        speakOut("\u091C" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091C";
                                        speakOut("\u091C");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[2][2] == 1) {
                                                speakOut("ज़मीन ");
                                                PROXIMITY_CHECK = true;
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u091C";
//                                speakOut("\u091C");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 2 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 2;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[2][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091D\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091D\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091D";
                                        speakOut("र्\u091D");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "झ";
                                        speakOut("झ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091D";
                                        speakOut("\u091D");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[2][3] == 1) {
                                                speakOut("झन्डा");
                                                PROXIMITY_CHECK = true;
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
// /                                keyCodelabel = "\u091D";
//                                speakOut("\u091D");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 2 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 2;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[2][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091E\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091E";
                                        speakOut("र्\u091E");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ञ";
                                        speakOut("ञ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091E";
                                        speakOut("\u091E");

                                    }
//                                keyCodelabel = "\u091E";
//                                speakOut("\u091E");


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[2][4] == 1) {
//                                                    speakOut("झन्डा");
                                                PROXIMITY_CHECK = true;
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 2 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x < 5*width/6 && x > width  ){

                        }
                    } else if (y > 2 * height/9 && y < 3 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 3;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[3][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091F\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091F\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091F";
                                        speakOut("र्\u091F");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ट";
                                        speakOut("ट");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091F";
                                        speakOut("\u091F");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[3][0] == 1) {
                                                speakOut("टमाटर");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u091F";
//                                speakOut("\u091F");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 3 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 3;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[3][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0920\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0920\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0920";
                                        speakOut("र्\u0920");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ठ";
                                        speakOut("ठ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0920";
                                        speakOut("\u0920");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[3][1] == 1) {
                                                speakOut("ठप्पा");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u0920";
//                                speakOut("\u0920");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 3 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 3;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[3][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0921\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0921\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0921";
                                        speakOut("र्\u0921");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0921़";
                                        speakOut("\u0921" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0921";
                                        speakOut("\u0921");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[3][2] == 1) {
                                                speakOut("डमरू");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u0921";
//                                speakOut("\u0921");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 3 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 3;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[3][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0922\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0922\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0922";
                                        speakOut("र्\u0922");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0922़";
                                        speakOut("\u0922" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0922";
                                        speakOut("\u0922");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[3][3] == 1) {
                                                speakOut("ढोलक");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u0922";
//                                speakOut("\u0922");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 3 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 3;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[3][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0923\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0923\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0923";
                                        speakOut("र्\u0923");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ण";
                                        speakOut("ण");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0923";
                                        speakOut("\u0923");
                                    }
//                                keyCodelabel = "\u0923";
//                                speakOut("\u0923");

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.

                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 3 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 6;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][0] == 0) {
                                    //speakOut("ं");
                                    speakOut("अनुस्वार");
                                    viewHandle.keyCodelabel = "\u0902";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 3 * height/9 && y < 4 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 4;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[4][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0924\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0924";
                                        speakOut("र्\u0924");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "त";
                                        speakOut("त");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0924";
                                        speakOut("\u0924");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[4][0] == 1) {
                                                speakOut("तरबूज़");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
//                                keyCodelabel = "\u0924";
//                                speakOut("\u0924");

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 4 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 4;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[4][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0925\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0925\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0925";
                                        speakOut("र्\u0925");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "थ";
                                        speakOut("थ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0925";
                                        speakOut("\u0925");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[4][1] == 1) {
                                                speakOut("थरमस");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 4 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 4;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[4][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0926\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0926\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0926";
                                        speakOut("र्\u0926");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "द";
                                        speakOut("द");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0926";
                                        speakOut("\u0926");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[4][2] == 1) {
                                                speakOut("दवात");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

//                                keyCodelabel = "\u0926";

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 4 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
//                                speakOut("\u0926");
                                    myVib.vibrate(500);
                                    Log.d("key", "3");
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 4;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[4][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0927\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0927\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0927";
                                        speakOut("र्\u0927");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ध";
                                        speakOut("ध");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0927";
                                        speakOut("\u0927");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[4][3] == 1) {
                                                speakOut("धनुष");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 4 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 4;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[4][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0928\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0928\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0928";
                                        speakOut("र्\u0928");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0928़";
                                        speakOut("\u0928" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0928";
                                        speakOut("\u0928");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[4][4] == 1) {
                                                speakOut("नल");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 4 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][1] == 0) {
                                    //speakOut("ः");
                                    speakOut("विसर्ग");
                                    viewHandle.keyCodelabel="\u0903";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 4 * height/9 && y < 5 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 5;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[5][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092A\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092A\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092A";
                                        speakOut("र्\u092A");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "प";
                                        speakOut("प");

                                    } else {
                                        viewHandle.keyCodelabel = "\u092A";
                                        speakOut("\u092A");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[5][0] == 1) {
                                                speakOut("पतंग");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 5 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 5;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[5][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092B\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092B\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092B";
                                        speakOut("र्\u092B");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u092B़";
                                        speakOut("\u092B" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092B";
                                        speakOut("\u092B");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[5][1] == 1) {
                                                speakOut("फल");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 5 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 5;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[5][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092C\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092C\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092C";
                                        speakOut("र्\u092C");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ब";
                                        speakOut("ब");

                                    } else {
                                        viewHandle.keyCodelabel = "\u092C";
                                        speakOut("\u092C");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[5][2] == 1) {
                                                speakOut("बतख");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 5 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 5;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[5][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092D\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092D\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092D";
                                        speakOut("र्\u092D");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "भ";
                                        speakOut("भ");
                                    } else {
                                        viewHandle.keyCodelabel = "\u092D";
                                        speakOut("\u092D");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[5][3] == 1) {
                                                speakOut("भालू");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 5 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 5;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[5][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092E\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092E";
                                        speakOut("र्\u092E");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "म";
                                        speakOut("म");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092E";
                                        speakOut("\u092E");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[5][4] == 1) {
                                                speakOut("मछली");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 5 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][2] == 0) {
                                    //speakOut("ँ");
                                    speakOut("चंद्रबिंदु");
                                    viewHandle.keyCodelabel = "\u0901";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 5 * height/9 && y < 6 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 6;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[6][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u092F\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092F\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u092F";
                                        speakOut("र्\u092F");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u092F़";
                                        speakOut("\u092F" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092F";
                                        speakOut("\u092F");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[6][0] == 1) {
                                                speakOut("यज्ञ");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 6 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 6;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[6][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0930";
                                        speakOut("र्\u0930");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0930़";
                                        speakOut("\u0930" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0930";
                                        speakOut("\u0930");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[6][1] == 1) {
                                                speakOut("रस्सी");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 6 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 6;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[6][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0932\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0932\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0932";
                                        speakOut("र्\u0932");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "\u0932़";
                                        speakOut("\u0932" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0932";
                                        speakOut("\u0932");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[6][2] == 1) {
                                                speakOut("लडका");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 6 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 6;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[6][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0935\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0935\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0935";
                                        speakOut("र्\u0935");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "व";
                                        speakOut("व");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0935";
                                        speakOut("\u0935");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[6][3] == 1) {
                                                speakOut("वन");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 6 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 6;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[6][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0936\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0936";
                                        speakOut("र्\u0936");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "श";
                                        speakOut("श");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0936";
                                        speakOut("\u0936");


                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK = true;
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[6][4] == 1) {
                                                speakOut("शहर");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 6 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][3] == 0) {
                                    speakOut("Nukta");
                                    viewHandle.keyCodelabel="";

                                    vowelBoolean = true;
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 6 * height/9 && y < 7 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 7;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[7][0] == 0) {


                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0937\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0937\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0937";
                                        speakOut("र्\u0937");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ष";
                                        speakOut("ष");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0937";
                                        speakOut("\u0937");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[7][0] == 1) {
                                                speakOut("षद्कोण");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 7 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 7;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[7][1] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0938\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0938\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0938";
                                        speakOut("र्\u0938");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "स";
                                        speakOut("स");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0938";
                                        speakOut("\u0938");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[7][1] == 1) {
                                                speakOut("सेब");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 7 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }

                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 7;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[7][2] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0939\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0939\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0939";
                                        speakOut("र्\u0939");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ह";
                                        speakOut("ह");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0939";
                                        speakOut("\u0939");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[7][2] == 1) {
                                                speakOut("हरिन");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 7 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 7;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[7][3] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0924\u094D\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0924\u094D\u0930";
                                        speakOut("र्\u0924\u094D\u0930");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut(".");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930";
                                        speakOut("\u0924\u094D\u0930");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[7][3] == 1) {
                                                speakOut("त्रिशुल");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 7 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 7;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[7][4] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0915\u094D\u0937\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0937\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0915\u094D\u0937";
                                        speakOut("र्\u0915\u094D\u0937");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "क्ष";
                                        speakOut("क्ष");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0937";
                                        speakOut("\u0915\u094D\u0937");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[7][4] == 1) {
                                                speakOut("क्षत्रिय");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 7 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");
                                    myVib.vibrate(50);
                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][4] == 0) {
                                    speakOut("trakaar");
                                    viewHandle.keyCodelabel ="";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 7 * height/9 && y < 8 * height/9) {
                        if (x < width/6) {
                            int r, s;
                            r = 8;
                            s = 0;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[8][0] == 0) {
                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u091C\u094D\u091E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091C\u094D\u091E\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u091C\u094D\u091E";
                                        speakOut("र्\u091C\u094D\u091E");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "ज्ञ";
                                        speakOut("ज्ञ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091C\u094D\u091E";
                                        speakOut("\u091C\u094D\u091E");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[8][0] == 1) {
                                                speakOut("ज्ञान");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 8 && j == 0) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "1");
                                    myVib.vibrate(500);
                                }
                            }
                        } else if (x > width/6 && x < 2 * width/6) {
                            int r, s;
                            r = 8;
                            s = 1;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[8][1] == 0) {

                                    if (viewHandle.flag2[1]) {
                                        speakOut("\u0936\u094D\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930\u094D\u0930";


                                    } else if (viewHandle.flag2[2]) {
                                        viewHandle.keyCodelabel = "र्\u0930\u094D\u0936";
                                        speakOut("र्\u0930\u094D\u0936");


                                    } else if (viewHandle.flag2[0]) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut(".");

                                    } else {
                                        speakOut("\u0936\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930";
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[8][1] == 1) {
                                                speakOut("श्रम");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 8 && j == 1) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "2");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 2 * width/6 && x < 3 * width/6) {
                            int r, s;
                            r = 8;
                            s = 2;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[8][2] == 0) {
                                    speakOut("स्वर वर्ण");
                                    viewHandle.keyCodelabel = "\u093E";

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 8 && j == 2) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "3");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 3 * width/6 && x < 4 * width/6) {
                            int r, s;
                            r = 8;
                            s = 3;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[8][3] == 0) {

                                    speakOut("Ru");
                                    viewHandle.keyCodelabel = "\u0943";
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 8 && j == 3) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "4");
                                    myVib.vibrate(50);
                                }
                            }
                        } else if (x > 4 * width/6 && x < 5 * width/6) {
                            int r, s;
                            r = 8;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[8][4] == 0) {
                                    speakOut("अ");
                                    viewHandle.keyCodelabel = "\u0905";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            Log.i("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
                                            PROXIMITY_CHECK = true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            if (flag[8][4] == 1) {
                                                speakOut("आम");
                                            }
                                            Log.i("Countdown Timer:", "TimeOut");
                                        }
                                    }.start();
                                    myVib.vibrate(500);
                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 8 && j == 4) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    Log.d("key", "5");

                                }
                            }
                        }else if( x > 5*width/6 && x < width  ){
                            int r, s;
                            r = 0;
                            s = 4;
                            if (PROXIMITY_CHECK) {
                                if (P_CHECK_FLAGS[r][s] == 0) {

                                    //check any other p_check_flags are intialised to 1
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (P_CHECK_FLAGS[i][j] == 1) {
                                                PREV_COORDINATE = true;
                                            }
                                        }
                                    }

                                    //if there is no other flags intilaise then set a new starting point
                                    if (PREV_COORDINATE == false) {
                                        for (int i = 0; i < 11; i++) {
                                            for (int j = 0; j < 7; j++) {
                                                if (i == r && j == s) {
                                                    P_CHECK_FLAGS[i][j] = 1;
                                                }
                                            }
                                        }
                                        touchdX = x;
                                        touchdY = y;
                                    }


                                }

                                //differnce b/w two points
                                touchdX1 = x - touchdX;
                                touchdY1 = y - touchdY;
                                distance = (float) Math.sqrt((touchdX1 * touchdX1) + (touchdY1 * touchdY1));

                                if (distance > THRESHOLD_DIST) {
                                    PROXIMITY_CHECK = false;
                                    //setting all the flags to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            P_CHECK_FLAGS[i][j] = 0;
                                        }
                                    }
                                    //setting prev cordinate to false
                                    PREV_COORDINATE = false;
                                }
                                Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
                                Log.d("proximitydistance", String.valueOf(distance));
                            } else {
                                if (flag[0][5] == 0) {
                                    speakOut("Rafaar");
                                    viewHandle.keyCodelabel ="";
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME /*For how long should timer run*/, 500 /*time interval after which `onTick()` should be called*/) {

                                        public void onTick(long millisUntilFinished) {
                                            PROXIMITY_CHECK=true;
                                        }

                                        public void onFinish() {
                                            //Done timer time out.
                                            Log.i("Countdown Timer:","TimeOut");
                                        }
                                    }.start();

                                    //Intialising the flag value of this column to 1 and rest of the keys to 0
                                    for (int i = 0; i < 11; i++) {
                                        for (int j = 0; j < 7; j++) {
                                            if (i == 0 && j == 5) {
                                                flag[i][j] = 1;
                                            } else {
                                                flag[i][j] = 0;
                                            }
                                        }
                                    }
                                    myVib.vibrate(50);
                                }
                            }
                        }
                    } else if (y > 8* height/9 && y < 9 * height/9) {
                        if(x < width/6){
                            int r, s,vibrate_time;
                            String label;
                            r = 9;
                            s = 3;
                            vibrate_time = 50;
                            label ="Left";
                            viewHandle.keyCodelabel ="";
                            if (PROXIMITY_CHECK) {
                                proximity_check(r,s);
                            } else {
                                key_touch(r,s,label,vibrate_time);
                            }
                        }else if(x > width/6 && x < 2*width/6){
                            int r, s,vibrate_time;
                            String label;
                            r = 9;
                            s = 4;
                            vibrate_time = 50;
                            label ="Right";
                            viewHandle.keyCodelabel ="";
                            if (PROXIMITY_CHECK) {
                                proximity_check(r,s);
                            } else {
                                key_touch(r,s,label,vibrate_time);
                            }
                        }
                        else if (x > 2 * width/6 && x < 5*width/6 ) {
                            int r, s,vibrate_time;
                            String label;
                            r = 9;
                            s = 0;
                            vibrate_time = 50;
                            label ="Space";

                            if (PROXIMITY_CHECK) {
                                proximity_check(r,s);
                            } else {
                                key_touch(r,s,label,vibrate_time);
                            }
                        }
                        else if (x > 5 * width/6 && x <  width) {
                            int r, s,vibrate_time;
                            String label;
                            r = 9;
                            s = 1;
                            vibrate_time = 50;
                            label ="Enter";

                            if (PROXIMITY_CHECK) {
                                proximity_check(r,s);
                            } else {
                                key_touch(r,s,label,vibrate_time);
                            }
                        }
                    }

                }



                break;

            case MotionEvent.ACTION_UP:
                Log.d("move","action_up");
                viewHandle.action_up =true;

                //detect flag
                l = 0;
                k = 0;
                for (int i = 0; i < 11; i++) {
                    for (int j = 0; j < 7; j++) {
                        if (flag[i][j] == 1) {
                            l = i;
                            k = j;
                        }
                    }
                }
                float a=0,b=0;
                a= event.getX();
                b= event.getY();
                if(l==10 && k == 0){

                }
                if(l==10 && k == 1){

                }
                if (l == 0 && k == 3) {
                    viewHandle.count_nukta += 1;//count_nukta == 2
                    if (viewHandle.count_nukta > 2) {
                        viewHandle.flag2[2] = false;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[0] = false;
                        viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_trakar = 1;
                        viewHandle.count_rafar = 1;
                        viewHandle.flag2[2] = false;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[0] = true;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.nukta));
                    }

                }
                if (l == 0 && k == 4) {

                    viewHandle.count_trakar += 1;
                    if (viewHandle.count_trakar > 2) {
                        viewHandle.flag2[2] = false;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[0] = false;
                        viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_nukta = 1;
                        viewHandle.count_rafar = 1;
                        viewHandle.flag2[2] = false;
                        viewHandle.flag2[1] = true;
                        viewHandle.flag2[0] = false;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.trakar));
                    }
                }

                if (l == 0 && k == 5) {
                    viewHandle.count_rafar += 1;

                    if (viewHandle.count_rafar > 2) {
                        viewHandle.flag2[2] = false;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[0] = false;
                        viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_trakar = 1;
                        viewHandle.count_nukta = 1;
                        viewHandle.flag2[2] = true;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[0] = false;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.rafar));

                    }
                }


//                        else if(l==2 && k==6){
//                            Log.d("navigation","right");
//                        }else if(l==3 && k==6){
//                            Log.d("navigation","touch and leave");
//                        }
//                        if (l == 0 && k > 2) {
//                            keyCodelabel = "";
//                        }

                View focusCurrent = viewHandle.mHostActivity.getWindow().getCurrentFocus();
                EditText edittext = (EditText) focusCurrent;
                String s = edittext.getText().toString();
                Editable editable = edittext.getText();
                int start = edittext.getSelectionStart();
                MediaPlayer mp = MediaPlayer.create(viewHandle.mHostActivity, R.raw.harpoon);
                //three finger left swipe
                if(l==9 && k==5){
                    String new_string,delimiter = " ";
//                            EditText et1 = (EditText) mHostActivity.findViewById(R.id.editText1);
                    log_chars('丐');
                    int lastindex ;
                    if(edittext.getSelectionEnd()!=0){
                        Log.d("selectionEnd",Integer.toString(edittext.getSelectionEnd()));
                        String part_1 = s.substring(0,edittext.getSelectionEnd());
                        String part_2 = s.substring(edittext.getSelectionEnd());
                        Log.d("parts", part_1 + "," + part_2);
                        int len = part_1.length();
                        Log.d("length", Integer.toString(len));
                        String c = part_1.charAt(len-1)+"";
                        Log.d("lastchar",c);
                        if(c.equals(" ")){
                            part_1 = s.substring(0,edittext.getSelectionEnd()-1);
                            Log.d("part1",part_1);
                        }

                        lastindex = part_1.lastIndexOf(delimiter);
                        if(lastindex!=-1) {
                            new_string = part_1.substring(0, lastindex);
                            Log.d("new_string",new_string);
                            editable.clear();
                            editable.append(new_string + " " + part_2);
                            edittext.setSelection(new_string.length() + 1);
                            Log.d("parts", part_1 + "," + part_2);
                            Log.d("lastindex", String.valueOf(lastindex));
                            mp.start();
                        }else{
                            editable.clear();
                            editable.append(part_2);
                            edittext.setSelection(0);
                            Log.d("parts", part_2);
                            Log.d("lastindex", String.valueOf(lastindex));
                            mp.start();
                        }

                    }
//                            if(s.length() != 0){
//                            if(lastindex!=-1){
//                            new_string = s.substring(0,lastindex);
//                          editable.clear();
//                                mp.start();
//                                editable.append(new_string);
//                            }else{
//                                editable.clear();
//                                mp.start();
//                            }
//
//                        }
                }
                if(l==1 && k==6){
                    speakOut(s);
                    log_chars('丑');
                }
                if (l == 9 && k == 0) {                     //space
                    viewHandle.keyCodelabel = "";
                    int selectionEnd = edittext.getSelectionEnd();
                    String text = edittext.getText().toString();
                    if (selectionEnd >= 0) {
                        // gives you the substring from start to the current cursor
                        // position
                        text = text.substring(0, selectionEnd);
//                            speakOut_pitch(text);
                    }
                    String delimiter = " ";
                    int lastDelimiterPosition = text.lastIndexOf(delimiter);
                    String lastWord = lastDelimiterPosition == -1 ? text :
                            text.substring(lastDelimiterPosition + delimiter.length());
                    speakOut(lastWord, (float) 0.8);
                    text = edittext.getText().toString();
                    StringBuffer buffer = new StringBuffer(text);
                    int cursor_position= edittext.getSelectionStart();
                    buffer.insert(cursor_position, " ");
                    edittext.setText(buffer.toString());
                    edittext.setSelection(cursor_position+1);


                } else if (l == 9 && k == 3) {
                    /**code for clear option
                     * keyCodelabel = "";
                     *edittext.setText("");
                     //                             */
                    int selectionEnd = edittext.getSelectionEnd();
                    if (selectionEnd != 0) edittext.setSelection(selectionEnd - 1);
                    speakOut(getLastword());

                } else if (l == 9 && k == 4) {
                    int selectionEnd = edittext.getSelectionEnd();
                    if(selectionEnd != s.length() )edittext.setSelection(selectionEnd+1);
                    speakOut(getLastword());
                }
                if (l == 0 && k == 6) {
                    Log.d("length of edit text", Integer.toString(s.length()));
                    String str = edittext.getText().toString();
                    String str1,str2;
                    int pos=0;
                    if (str.length() != 0) {

//                            mp.setVolume(2,3);
                        Log.d("selection", String.valueOf(edittext.getSelectionStart())+","+String.valueOf(str.length()));

                        if(edittext.getSelectionStart() == str.length() &&  edittext.getSelectionStart()!=0){
                            char lastchar = str.charAt(edittext.getSelectionStart()-1);
                            speakOut(String.valueOf(lastchar) + " deleted", 0.8f);

                            str = str.substring(0,str.length()-1);
                            pos = edittext.getSelectionStart()-1;
                            edittext.setText(str);
                            edittext.setSelection(pos);

                        }else if(edittext.getSelectionStart() < str.length() &&  edittext.getSelectionStart()!=0){
                            char lastchar = str.charAt(edittext.getSelectionStart()-1);
                            speakOut(String.valueOf(lastchar)+" deleted", 0.8f);
                            mp.start();
                            str1 = str.substring(0,(edittext.getSelectionStart()-1));
                            str2 = str.substring((edittext.getSelectionStart()));
                            str = str1+str2;

                            pos = edittext.getSelectionStart()-1;
                            Log.d("selection1", String.valueOf(edittext.getSelectionStart()));
                            if(edittext.getSelectionStart()!=0){
                                edittext.setText(str);
                                edittext.setSelection(pos);}
                        }


                    }

                }
                if(l==10 && k==6){

                }
                if (tri_touch==false && multi_touch == false && action_down_flag && y>0) {     //condition for lift to type model

                    viewHandle.speakOut_pitch(viewHandle.keyCodelabel);
                    action_down_flag = false;
                    editable.insert(start, viewHandle.keyCodelabel);
//                            keyCodelabel = "";
//                            int selectionEnd = edittext.getSelectionEnd();
//                            String text = edittext.getText().toString();
//                            if (selectionEnd >= 0) {
//                                // gives you the substring from start to the current cursor
//                                // position
//                                text = text.substring(0, selectionEnd);
////                            speakOut_pitch(text);
//                            }
//                            String delimiter = " ";
//                            int lastDelimiterPosition = text.lastIndexOf(delimiter);
//                            String lastWord = lastDelimiterPosition == -1 ? text :
//                                    text.substring(lastDelimiterPosition + delimiter.length());
//                            speakOut_pitch(lastWord);

                    if (l > 0 && l < 10 && viewHandle.flag2[0]) {
                        viewHandle.flag2[0] = false;
                        viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    } else if (l > 0 && l < 10 && viewHandle.flag2[1]) {
                        viewHandle.flag2[1] = false;
                        viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    } else if (l > 0 && l < 10 && viewHandle.flag2[2]) {
                        viewHandle.flag2[2] = false;
                        viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    }
                    action_down_flag = false;

                } else if (multi_touch == true && viewHandle.keyCodelabel != null && viewHandle.radius > viewHandle.mInnerRadius) {

                    int new_start = edittext.getSelectionStart();
                    if (viewHandle.keyCodelabel.equals("\u0905")) {
                        editable.insert(new_start, viewHandle.vowels_aah[viewHandle.arc]);
                        speakOut_pitch(viewHandle.vowels_aah[viewHandle.arc]);
//                                keyCodelabel = "";
//                                int selectionEnd = edittext.getSelectionEnd();
//                                String text = edittext.getText().toString();
//                                if (selectionEnd >= 0) {
//                                    // gives you the substring from start to the current cursor
//                                    // position
//                                    text = text.substring(0, selectionEnd);
////                            speakOut_pitch(text);
//                                }
//                                String delimiter = " ";
//                                int lastDelimiterPosition = text.lastIndexOf(delimiter);
//                                String lastWord = lastDelimiterPosition == -1 ? text :
//                                        text.substring(lastDelimiterPosition + delimiter.length());
//                                speakOut_pitch(lastWord);

                    } else if (viewHandle.keyCodelabel.equals("ृ")) {
                        editable.insert(new_start, viewHandle.vowes_uuh[viewHandle.arc]);
                        speakOut_pitch(viewHandle.vowes_uuh[viewHandle.arc]);
//                                keyCodelabel = "";
//                                int selectionEnd = edittext.getSelectionEnd();
//                                String text = edittext.getText().toString();
//                                if (selectionEnd >= 0) {
//                                    // gives you the substring from start to the current cursor
//                                    // position
//                                    text = text.substring(0, selectionEnd);
////                            speakOut_pitch(text);
//                                }
//                                String delimiter = " ";
//                                int lastDelimiterPosition = text.lastIndexOf(delimiter);
//                                String lastWord = lastDelimiterPosition == -1 ? text :
//                                        text.substring(lastDelimiterPosition + delimiter.length());
//                                speakOut_pitch(lastWord);

                    } else if (viewHandle.keyCodelabel.equals("ा")) {
                        editable.insert(new_start, viewHandle.vowels[viewHandle.arc]);
                        speakOut_pitch(viewHandle.vowels[viewHandle.arc]);
//                                keyCodelabel = "";
//                                int selectionEnd = edittext.getSelectionEnd();
//                                String text = edittext.getText().toString();
//                                if (selectionEnd >= 0) {
//                                    // gives you the substring from start to the current cursor
//                                    // position
//                                    text = text.substring(0, selectionEnd);
////                            speakOut_pitch(text);
//                                }
//                                String delimiter = " ";
//                                int lastDelimiterPosition = text.lastIndexOf(delimiter);
//                                String lastWord = lastDelimiterPosition == -1 ? text :
//                                        text.substring(lastDelimiterPosition + delimiter.length());
//                                speakOut_pitch(lastWord);

                    } else {

                        editable.insert(new_start, viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);
                        speakOut_pitch(viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);
//                                keyCodelabel = "";
//                                int selectionEnd = edittext.getSelectionEnd();
//                                String text = edittext.getText().toString();
//                                if (selectionEnd >= 0) {
//                                    // gives you the substring from start to the current cursor
//                                    // position
//                                    text = text.substring(0, selectionEnd);
////                            speakOut_pitch(text);
//                                }
//                                String delimiter = " ";
//                                int lastDelimiterPosition = text.lastIndexOf(delimiter);
//                                String lastWord = lastDelimiterPosition == -1 ? text :
//                                        text.substring(lastDelimiterPosition + delimiter.length());
//                                speakOut_pitch(lastWord);

                    }
                    if (l > 0 && l < 10 && viewHandle.flag2[0]) {
                        viewHandle.flag2[0] = false;
                        viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    } else if (l > 0 && l < 10 && viewHandle.flag2[1]) {
                        viewHandle.flag2[1] = false;
                        viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    } else if (l > 0 && l < 10 && viewHandle.flag2[2]) {
                        viewHandle.flag2[2] = false;
                        viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                    }

                }



                //setting touch flags to default values

                for (int i = 0; i < 11; i++) {
                    for (int j = 0; j < 7; j++) {

                        flag[i][j] = 0;

                    }
                }
                viewHandle.keyCodelabel = "";
                multi_touch = false;
                tri_touch =false;
                navigator_flag = false;
                action_down_flag= false;
                PROXIMITY_CHECK = false;
                PREV_COORDINATE =false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                View focusCurrent1 = viewHandle.mHostActivity.getWindow().getCurrentFocus();
                EditText edittext1 = (EditText) focusCurrent1;
                String s1 = edittext1.getText().toString();
                Editable editable1 = edittext1.getText();
                int start1 = edittext1.getSelectionStart();

                if (event.getPointerId(pointerIndex) != 0 && viewHandle.radius > viewHandle.mInnerRadius && tri_touch == false && multi_touch && navigator_flag==false) {
                    editable1.insert(start1, viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);
                    viewHandle.speakOut_pitch(viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);
                    if (viewHandle.flag2[0] || viewHandle.flag2[1] || viewHandle.flag2[2]) {
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));
                        viewHandle.flag2[0] = false;
                        viewHandle.flag2[1] = false;
                        viewHandle.flag2[2] = false;

                    }
                }
                if (event.getPointerId(pointerIndex) != 0 && tri_touch == false && multi_touch == false && navigator_flag) {
                }
                break;

        }
        if (event.getPointerCount() < 2 && pointerId == 0){
            multi_touch = false;
            navigator_flag=false;
        }
        viewHandle.invalidate();
        return true;
    }
    private void long_backspace() {
        if(viewHandle.action_up == false){
            View focusCurrent = viewHandle.mHostActivity.getWindow().getCurrentFocus();
            EditText edittext = (EditText) focusCurrent;
            edittext.setText("");
            MediaPlayer mp = MediaPlayer.create(viewHandle.mHostActivity, R.raw.harpoon);
            mp.start();}
    }


}


