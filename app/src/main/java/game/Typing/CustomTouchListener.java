package game.Typing;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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

/*import ai.liv.s2tlibrary.Speech2TextIntent;
import ai.liv.s2tlibrary.model.S2TError;
import ai.liv.s2tlibrary.model.Transcription;*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/*import ai.liv.s2tlibrary.Speech2TextIntent;
import ai.liv.s2tlibrary.model.S2TError;
import ai.liv.s2tlibrary.model.Transcription;*/

public class CustomTouchListener implements OnTouchListener {

    private CustomKeyboard viewHandle;
    private int x,y;
    private float width, height;
    public Vibrator myVib;
    private AudioManager am;
    public TextToSpeech tts, tts1;
    int COUNT_DOWN_TIME=1000;
    int COUNT_DOWN_INTERVAL = 500;
    int l,k;
    float touchdX=0,touchdY=0,touchdX1=0,touchdY1=0,distance=0,distance1=0;
    boolean touch_flag = false, multi_touch_flag = false, action_down_flag = false, multi_touch = false,tri_touch=false,navigator_flag=false;
    private int rowCount = 11, columnCount = 6;
    //Remember active key
    int flag[][] = new int[rowCount][columnCount];

    //Keeps track of active arc
    int flag1[] = new int[10];
    boolean action_downflag2[] = {false,false,false};

    int P_CHECK_FLAGS[][] =  new int[rowCount][columnCount];

    //keep track of modal flags
    //boolean flag2[] = new boolean[3];
    boolean PREV_COORDINATE=false;
    int THRESHOLD_DIST = 50,THRESHOLD_DIST_LAST_ROW = 60;
    boolean PROXIMITY_CHECK=false;
    boolean vowelBoolean = false;
    private int vibrateDuration = 50;
    //private int rowCount = 11, columnCount = 6;
    private int rowNo, columnNo;
    private Language languageObjects;
    private int keyCode = -1;
    private String keyLabel;
    private String sampleWord;

    private float higherVolume = (float)1;
    private float lowerVolume = (float)1;

    private boolean nuktaFlag, rakarFlag, rafarFlag, eyelashFlag;


    private String TAG = "production";
    private String POSITION = "position";
    private String GESTURE = "gesture";
    private String SEQUENCE = "sequence";


    final int BACKSPACE = 400;
    final int SPACE = 401;
    final int ENTER = 403;
    final int SHIFT = 404;
    final int LEFTARROW = 405;
    final int RIGHTARROW = 406;
    final int SETTINGS = 407;
    final int LANGUAGE_EN = 408;
    final int VOICETYPING = 409;

    final int RAFARCODE = 53;
    final int RAKARCODE = 52;
    final int NUKTACODE = 121;
    final int EYELASHCODE = 51;

    final String RAFAR = "\u0930\u094D";
    final String RAKAR = "\u094D\u0930";
    final String EYELASH = "\u0931\u200D\u094D";


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
        /*for(int i = 0 ;i<3;i++){
            flag2[i] = false;
        }*/
        languageObjects = new MainLanguage();
        int codePoint = (int)languageObjects.myKey.get(0).label.charAt(0);
        Log.d(TAG,codePoint+"");
        String packageN = viewHandle.mHostActivity.getPackageName();
        String word = "";

        myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
        am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);

        for(int i=0;i<languageObjects.nKeys;i++){
            word = "";
            if(languageObjects.myKey.get(i).label.length()==1) {

                codePoint = (int) languageObjects.myKey.get(i).label.charAt(0);
                Log.d(TAG, codePoint + "");

                int stringID = viewHandle.getResources().getIdentifier("a"+codePoint,"string",packageN);

                try {
                    word = viewHandle.getResources().getString(stringID);
                    //viewHandle.getResources().getString(stringID);

                }catch(Exception ex){

                    word="";
                    //ex.printStackTrace();
                }
                Log.d(TAG,languageObjects.myKey.get(i).label+":"+word);
                //languageObjects.myKey.get(i).exampleWord = word;
                /*Log.d(POSITION,languageObjects.myKey.get(i).exampleWord);*/
            }
            languageObjects.myKey.get(i).exampleWord = word;
            Log.d(POSITION,languageObjects.myKey.get(i).exampleWord);

           /* myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
            am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);*/


        }

        nuktaFlag = false;
        rakarFlag = false;
        rakarFlag = false;
        eyelashFlag = false;

    }

    public void speakOut_pitch(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        tts1.setPitch((float) 0.6);
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
        if(pitch == 0.6){
            tts.setPitch((float) 0.6);
        }else{
            tts.setPitch(pitch);
        }
        tts.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
    }

    //Called for keys like backspace, enter, space, etc
    public void key_touch(int r, int s, String label,int vibrate_time){
        Log.d(TAG,"On key touch");
        Log.d(SEQUENCE,"Handle vibration and audio feedback for function keys");

        if (flag[r][s] == 0) {
            speakOut(label);
            myVib.vibrate(vibrate_time);
            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , COUNT_DOWN_INTERVAL ) {

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

        /*for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == r && j == s) {
                    flag[i][j] = 1;
                } else {
                    flag[i][j] = 0;
                }
            }
        }*/

        resetFlagArray(0);
        flag[r][s]=1;
    }

    public void proximity_check(int r,int s){

        if(r>=rowCount || s>=columnCount) {
            Log.d(SEQUENCE, "Array index out of bounds");
            //return;
        }

        if (P_CHECK_FLAGS[r][s] == 0) {

            //check any other p_check_flags are intialised to 1
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j <columnCount; j++) {
                    if (P_CHECK_FLAGS[i][j] == 1) {
                        PREV_COORDINATE = true;
                    }
                }
            }

            //if there is no other flags intilaise then set a new starting point
            if (PREV_COORDINATE == false) {
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j <columnCount; j++) {
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
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j <columnCount; j++) {
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

                //Test
                /*Locale[] locales = Locale.getAvailableLocales();
                List<Locale> localeList = new ArrayList<Locale>();
                for (Locale locale : locales) {
                    int res = tts.isLanguageAvailable(locale);
                    if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        localeList.add(locale);
                        Log.d(TAG,locale.getDisplayLanguage()+":"+locale.getDisplayName()+":"+locale.getISO3Language()+":"+locale.getISO3Country());
                    }
                }*/

                final Locale loc = new Locale("hin", "IND");
                int result = tts.setLanguage(loc);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(TAG, "1:This Language is not supported");
                    Toast.makeText(viewHandle.getContext(), "Your default text to speech engine does not support Hindi. Please download Hindi locale for current TTS or switch to a text to speech engine that supports Hindi locale.",Toast.LENGTH_SHORT).show();
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


        /*width = viewHandle.mKeyboardView.getWidth();
        height = viewHandle.mKeyboardView.getHeight();
        x = (int) event.getX();
        y = (int) event.getY();

        keyCode = getKeyPosition();*/

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:

                Log.d(TAG,"Action down: key down");
                Log.d(SEQUENCE,"Action down: key down");

                //int curkey = viewHandle.keyCodeClicked;
                Display display = viewHandle.mHostActivity.getWindowManager().getDefaultDisplay();
                Point size1 = new Point();
                display.getSize(size1);

                /*x = (int) event.getX();
                y = (int) event.getY();*/

                width = viewHandle.mKeyboardView.getWidth();
                height = viewHandle.mKeyboardView.getHeight();
                x = (int) event.getX();
                y = (int) event.getY();

                Log.d(SEQUENCE,"Action down: get keycode and keylabels");
                keyCode = getKeyPosition();

                action_down_flag = true;
                viewHandle.action_up  = false;
                //Log.d("touch", "single touch");

                /*width = viewHandle.mKeyboardView.getWidth();
                height = viewHandle.mKeyboardView.getHeight();*/
//                    Log.d("width,height",width+","+height);

                //Log.d("keycodeclicked", curkey +"");

                /*myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
                am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);*/

                //keyCode = getKeyPosition();

                /*if(y <2*height/9){
                    if(x>5*width/6 && x < width){
                        key_touch(0,6,"delete",vibrateDuration);
                    }
                }*/
                //400:bkspc, 401:space, 402:enter, 404: shift, 405:left arrow, 406:right arrow, 407:settings, 408: en, mic:409
                //modal keys: 501/53:rafar, 502/52:trakar, 503/121:nukta, 504 / 51: eyelash
                //Log.d("height",String.valueOf(height));

               /* speakOut(keyLabel);*/
                Log.d(SEQUENCE,"Set keyCodelabel:"+keyLabel);
                viewHandle.keyCodelabel = keyLabel;
                //myVib.vibrate(COUNT_DOWN_INTERVAL);

                switch(keyCode){

                    //anchor keys
                    case 1:
                    case 5:
                    case 18:
                    case 39:
                    case 136:

                    myVib.vibrate(COUNT_DOWN_INTERVAL);
                    speakOut(keyLabel);
                    break;
                    case BACKSPACE:
                        key_touch(rowNo,columnNo,"delete",vibrateDuration);
                        break;
                    case LEFTARROW:
                        key_touch(rowNo,columnNo,"move one left",vibrateDuration);
                        break;
                    case RIGHTARROW:
                        key_touch(rowNo,columnNo,"move one right",vibrateDuration);
                        break;
                    case SPACE:
                        key_touch(rowNo,columnNo,"Space key",vibrateDuration);
                        break;
                    case ENTER:
                        key_touch(rowNo,columnNo,"Enter key",vibrateDuration);
                        break;
                    case VOICETYPING:
                        key_touch(rowNo,columnNo,"Start voice typing",vibrateDuration);
                        break;
                    case LANGUAGE_EN:
                        key_touch(rowNo,columnNo,"Switch to English keyboard",vibrateDuration);
                        break;
                    case SETTINGS:
                        key_touch(rowNo,columnNo,"Go to keyboard settings",vibrateDuration);
                        break;
                    case SHIFT:
                        key_touch(rowNo,columnNo,"Shift key",vibrateDuration);
                        break;

                    default:
                        myVib.vibrate(vibrateDuration);
                        speakOut(keyLabel);
                }

                if (y < height/9 ) {

                    Log.d(POSITION,"Row 1");

                   /* if (x < width/6) {
                        Log.d(POSITION,"Column 1");

                        if (flag[1][0] == 0) {

                            Log.d(POSITION,"Flag 0");

                            if (flag2[1]) {
                                Log.d(POSITION,"Flag2 true:1");
                                speakOut("\u0915\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0915\u094D\u0930";

                            } else if (flag2[2]) {
                                Log.d(POSITION,"Flag2 true:2");
                                viewHandle.keyCodelabel = "\u0930\u094D\u0915";
                                speakOut("\u0930\u094D\u0915");


                            } else if (flag2[0]) {
                                Log.d(POSITION,"Flag2 true:0");
                                viewHandle.keyCodelabel = "\u0915\u093C";
                                speakOut("\u0915"+"nukta");


                            } else {
                                Log.d(POSITION,"Flag2 false");
                                speakOut("\u0915");
                                viewHandle.keyCodelabel = "\u0915";
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME *//*, 500 *//*) {

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
                            myVib.vibrate(COUNT_DOWN_INTERVAL);

                            Log.d("keycodelabel", viewHandle.keyCodelabel);
                            Log.d("key", "1");
                        }
                    } else if (x > 5 * width/6 && x < 6 * width/6) {
                        Log.d(POSITION,"Column 6");
//                                speakOut("delete");
//                                CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                    }*/

                } else if (y >  height/9 && y < 2 * height/9) {
                    /*
                    Log.d(POSITION,"Row 2");

                    if (x < width/6) {
                        if (flag[2][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u091A\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091A\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091A";
                                speakOut("र्\u091A");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "च";
                                speakOut("च");

                            } else {
                                viewHandle.keyCodelabel = "\u091A";
                                speakOut("\u091A");

                            }
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u091B\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091B\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091B";
                                speakOut("र्\u091B");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "छ";
                                speakOut("छ");

                            } else {
                                viewHandle.keyCodelabel = "\u091B";
                                speakOut("\u091B");

                            }
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u091C\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091C\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091C";
                                speakOut("र्\u091C");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u091C़";
                                speakOut("\u091C"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u091C";
                                speakOut("\u091C");

                            }

                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u091D\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091D\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091D";
                                speakOut("र्\u091D");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "झ";
                                speakOut("झ");

                            } else {
                                viewHandle.keyCodelabel = "\u091D";
                                speakOut("\u091D");

                            }

                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u091E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091E\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091E";
                                speakOut("र्\u091E");


                            } else if (flag2[0]) {
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
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
//                                CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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

                    }*/
                } else if (y > 2 * height/9 && y < 3 * height/9) {
                    /*
                    if (x < width/6) {
                        if (flag[3][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u091F\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091F\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091F";
                                speakOut("र्\u091F");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ट";
                                speakOut("ट");


                            } else {
                                viewHandle.keyCodelabel = "\u091F";
                                speakOut("\u091F");
                            }
                                CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u0920\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0920\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0920";
                                speakOut("र्\u0920");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ठ";
                                speakOut("ठ");

                            } else {
                                viewHandle.keyCodelabel = "\u0920";
                                speakOut("\u0920");
                            }
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u0921\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0921\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0921";
                                speakOut("र्\u0921");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u0921़";
                                speakOut("\u0921"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0921";
                                speakOut("\u0921");

                            }
                             CountDownTimer timer = new CountDownTimer(5000 , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0922\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0922\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0922";
                                speakOut("र्\u0922");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u0922़";
                                speakOut("\u0922"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0922";
                                speakOut("\u0922");

                            }
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            if (flag2[1]) {
                                speakOut("\u0923\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0923\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0923";
                                speakOut("र्\u0923");


                            } else if (flag2[0]) {
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
                            }
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                            CountDownTimer timer = new CountDownTimer(5000 , 500 ) {
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
                    }*/
                }
                else if (y > 3 * height/9 && y < 4 * height/9) {
                    
                    /*if (x < width/6) {
                        if (flag[4][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u0924\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0924\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0924";
                                speakOut("र्\u0924");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "त";
                                speakOut("त");


                            } else {
                                viewHandle.keyCodelabel = "\u0924";
                                speakOut("\u0924");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0925\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0925\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0925";
                                speakOut("र्\u0925");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "थ";
                                speakOut("थ");


                            } else {
                                viewHandle.keyCodelabel = "\u0925";
                                speakOut("\u0925");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0926\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0926\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0926";
                                speakOut("र्\u0926");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "द";
                                speakOut("द");

                            } else {
                                viewHandle.keyCodelabel = "\u0926";
                                speakOut("\u0926");

                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0927\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0927\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0927";
                                speakOut("र्\u0927");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ध";
                                speakOut("ध");

                            } else {
                                viewHandle.keyCodelabel = "\u0927";
                                speakOut("\u0927");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0928\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0928\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0928";
                                speakOut("र्\u0928");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u0928़";
                                speakOut("\u0928"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0928";
                                speakOut("\u0928");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                    }*/
                } else if (y > 4 * height/9 && y < 5 * height/9) {
                   /* if (x < width/6) {
                        if (flag[5][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u092A\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092A\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092A";
                                speakOut("र्\u092A");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "प";
                                speakOut("प");

                            } else {
                                viewHandle.keyCodelabel = "\u092A";
                                speakOut("\u092A");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u092B\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092B\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092B";
                                speakOut("र्\u092B");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u092B़";
                                speakOut("\u092B"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u092B";
                                speakOut("\u092B");


                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u092C\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092C\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092C";
                                speakOut("र्\u092C");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ब";
                                speakOut("ब");

                            } else {
                                viewHandle.keyCodelabel = "\u092C";
                                speakOut("\u092C");


                            } CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u092D\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092D\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092D";
                                speakOut("र्\u092D");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "भ";
                                speakOut("भ");
                            } else {
                                viewHandle.keyCodelabel = "\u092D";
                                speakOut("\u092D");

                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u092E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092E\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092E";
                                speakOut("र्\u092E");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "म";
                                speakOut("म");


                            } else {
                                viewHandle.keyCodelabel = "\u092E";
                                speakOut("\u092E");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                    }*/

                } else if (y > 5 * height/9 && y < 6 * height/9) {
                    /*if (x < width/6) {
                        if (flag[6][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u092F\u094D\u0930");
                                viewHandle.keyCodelabel = "\u092F\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u092F";
                                speakOut("र्\u092F");


                            } else if (flag2[0]) {
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
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0930\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0930";
                                speakOut("र्\u0930");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u0930़";
                                speakOut("\u0930"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0930";
                                speakOut("\u0930");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0932\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0932\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0932";
                                speakOut("र्\u0932");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "\u0932़";
                                speakOut("\u0932"+"nukta");


                            } else {
                                viewHandle.keyCodelabel = "\u0932";
                                speakOut("\u0932");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0935\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0935\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0935";
                                speakOut("र्\u0935");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "व";
                                speakOut("व");

                            } else {
                                viewHandle.keyCodelabel = "\u0935";
                                speakOut("\u0935");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0936\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0936\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0936";
                                speakOut("र्\u0936");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "श";
                                speakOut("श");


                            } else {
                                viewHandle.keyCodelabel = "\u0936";
                                speakOut("\u0936");


                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                    }*/
                } else if (y > 6 * height/9 && y < 7 * height/9) {
                    /*if (x < width/6) {
                        if (flag[7][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u0937\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0937\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0937";
                                speakOut("र्\u0937");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ष";
                                speakOut("ष");


                            } else {
                                viewHandle.keyCodelabel = "\u0937";
                                speakOut("\u0937");
                            }
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0938\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0938\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0938";
                                speakOut("र्\u0938");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "स";
                                speakOut("स");


                            } else {
                                viewHandle.keyCodelabel = "\u0938";
                                speakOut("\u0938");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0939\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0939\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0939";
                                speakOut("र्\u0939");


                            } else if (flag2[0]) {
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
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0924\u094D\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0924\u094D\u0930\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0924\u094D\u0930";
                                speakOut("र्\u0924\u094D\u0930");


                            } else if (flag2[0]) {
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
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0915\u094D\u0937\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0915\u094D\u0937\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0915\u094D\u0937";
                                speakOut("र्\u0915\u094D\u0937");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "क्ष";
                                speakOut("क्ष");


                            } else {
                                viewHandle.keyCodelabel = "\u0915\u094D\u0937";
                                speakOut("\u0915\u094D\u0937");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                    }*/
                } else if (y > 7 * height/9 && y < 8 * height/9) {
                    /*if (x < width/6) {
                        if (flag[8][0] == 0) {
                            if (flag2[1]) {
                                speakOut("\u091C\u094D\u091E\u094D\u0930");
                                viewHandle.keyCodelabel = "\u091C\u094D\u091E\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u091C\u094D\u091E";
                                speakOut("र्\u091C\u094D\u091E");


                            } else if (flag2[0]) {
                                viewHandle.keyCodelabel = "ज्ञ";
                                speakOut("ज्ञ");


                            } else {
                                viewHandle.keyCodelabel = "\u091C\u094D\u091E";
                                speakOut("\u091C\u094D\u091E");
                            }

                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            if (flag2[1]) {
                                speakOut("\u0936\u094D\u0930\u094D\u0930");
                                viewHandle.keyCodelabel = "\u0936\u094D\u0930\u094D\u0930";


                            } else if (flag2[2]) {
                                viewHandle.keyCodelabel = "र्\u0930\u094D\u0936";
                                speakOut("र्\u0930\u094D\u0936");


                            } else if (flag2[0]) {
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
                            }  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            viewHandle.keyCodelabel = "\u0943";  CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                            CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                    }*/
                } else if (y > 8 * height/9 && y < 9 * height/9) {
                    /*if(x < width/6){
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
                    }*/
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

            Log.d(TAG,"Action pointer down: multiple fingers touching the screen");
                //Get the active key
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        if (flag[i][j] == 1) {
                            l = i;
                            k = j;
                        }
                    }
                }

                //Detect if three fingers down / second finger down / second finger down on arrow keys
                if(event.getPointerCount()==3){

                    viewHandle.desetArc();
                    tri_touch = true;
                    multi_touch=false;
                    navigator_flag = false;
                    viewHandle.setTouchDownPoint(event.getX(pointerIndex), event.getY(pointerIndex));
                    viewHandle.keyCodelabel="";
                    Log.d(TAG,"Three finger gesture");

                }else if(event.getPointerCount()==2 && l<rowCount && l>0){

                    Log.d(TAG,"Second finger down: split-tap gesture");
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
                    myVib.vibrate(vibrateDuration);

                }else if(event.getPointerCount() == 2 && keyCode == LEFTARROW){

                    //TODO:Move to the beginning of the current word:
                    Log.d("navigate", "Move to the beginning of the current word");
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
                        Log.d("navigate","Selected:"+s1);
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


                }else if(event.getPointerCount() == 2 && keyCode == RIGHTARROW){

                    //TODO:Move to the end of the current word
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
                //Log.d("check_view_added", "View added");
//                    Log.d("touch", "ACTION_POINTER_DOWN");
                break;

            case MotionEvent.ACTION_MOVE:

                Log.d(TAG, "ACTION_MOVE");
                viewHandle.action_up =false;
                boolean leftflag=false,rightflag=false;

                if(tri_touch == true){

                    Log.d(GESTURE, "Tri touch true");

                    for (int k = 0; k < event.getPointerCount(); ++k) {
                        pointerIndex = k;
                        pointerId = event.getPointerId(pointerIndex);
                        Log.d("pointer id", Integer.toString(pointerId));

                        //Why are we interested only in the second pointer?
                        if (pointerId == 2) {
                            PointF f1 = new PointF();
                            f1.x = event.getX(pointerIndex);
                            f1.y = event.getY(pointerIndex);

                            //if second pointer has moved left by atleast 170, detect it as 3-finger left swipe: backspace
                            if (viewHandle.touchDownX - f1.x > 170) {
//                                        Toast.makeText(mHostActivity,"left",Toast.LENGTH_SHORT).show();

                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {

                                        //TODO:Check this, wrong key being set as active
                                        if (i == 9 && j == 5) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d(GESTURE,"3-finger left swipe:"+f1.x);
                            }
                            // left to right swipe
                            //if second pointer has moved right by atleast 100, detect it as 3-finger right swipe: space ?delete
                            else if(f1.x - viewHandle.touchDownX >100) {
//                                        Toast.makeText(mHostActivity,"right",Toast.LENGTH_SHORT).show();

                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {
                                        if (i == 9 && j == 0) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d(GESTURE,"3-finger right swipe:"+f1.x);
                                Log.d(GESTURE,"key:"+flag);

                            }else if(f1.x-viewHandle.touchDownX<100 || viewHandle.touchDownX-f1.x<100 ){
                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {

                                        //TODO: What are we setting here?
                                        if (i == 1 && j == 6) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                                Log.d(GESTURE,"touch");
                            }
                            Log.d(GESTURE,"x,y:"+f1.x + "," + f1.y);
                        }

                    }
                }else if (multi_touch == true) {

                    Log.d(GESTURE, "Move on Chakra");

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
                            Log.d(TAG,"Current(x,y):"+f1.x + "," + f1.y);
                        }

                    }
                }else {
                    // a pointer was moved
                    Log.d(TAG, "Single moving finger");
                    Log.d("MovementX,Y", "Single moving finger");

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
                    //TODO: init only once?
                    /*myVib = (Vibrator) viewHandle.mHostActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    am = (AudioManager) viewHandle.mHostActivity.getSystemService(Context.AUDIO_SERVICE);*/

                    //Log.d("height",String.valueOf(y));
                    keyCode = getKeyPosition();
                    viewHandle.keyCodelabel = keyLabel;

                    Log.d("MovementX,Y","Keycode"+keyCode);

                    //TODO: remove from keyboard. Maybe needed in the tool
                    //leftswipe for reading the word
                    if (y < 0)
                    {
                        if (x < width / 3) {
                            if (flag[10][0] == 0) {
                                viewHandle.tv1 = (TextView) viewHandle.mHostActivity.findViewById(R.id.textView1);
                                String text = viewHandle.tv1.getText().toString();
                                speakOut(text);
                                Log.d("swipe", text);

                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j < columnCount; j++) {
                                        if (i == 10 && j == 0) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                            }
                        } else if (x > 2 * width / 3 && x < width) {
                            viewHandle.et1 = (EditText) viewHandle.mHostActivity.findViewById(R.id.editText1);
                            speakOut(viewHandle.et1.getText().toString());
                            Log.d("swipe", "b");
                            if (flag[10][1] == 0) {
                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j < columnCount; j++) {
                                        if (i == 10 && j == 1) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }
                            }
                        }

                    }

                    //delete
                    /*if(y < 2*height/9 && y > 0 ){
                        if(x > 5 *width/6 && x <width){*/
                    //TODO:See if this can be better resolved
                    if (keyCode == BACKSPACE) {
                        int r, s;
                        r = 0;
                        s = 5;

                        //? What do p_check flags do
                        //What is proximity check?
                        if (PROXIMITY_CHECK) {

                            if (P_CHECK_FLAGS[r][s] == 0) {

                                //check any other p_check_flags are intialised to 1
                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {
                                        if (P_CHECK_FLAGS[i][j] == 1) {
                                            PREV_COORDINATE = true;
                                        }
                                    }
                                }

                                //if there is no other flags intilaise then set a new starting point
                                if (PREV_COORDINATE == false) {
                                    for (int i = 0; i < rowCount; i++) {
                                        for (int j = 0; j <columnCount; j++) {
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
                                for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {
                                        P_CHECK_FLAGS[i][j] = 0;
                                    }
                                }
                                //setting prev cordinate to false
                                PREV_COORDINATE = false;
                            }
                            Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
//                                        Log.d("proximitydistance", String.valueOf(distancef));
                        } else {
                            if (flag[0][5] == 0) {
                                speakOut("delete");
                                viewHandle.keyCodelabel = "";
                                CountDownTimer timer = new CountDownTimer(4000, 500) {

                                    public void onTick(long millisUntilFinished) {
                                        PROXIMITY_CHECK = true;
//                                                    long_backspace();
                                    }

                                    public void onFinish() {
                                        //Done timer time out.
                                        Log.i("Countdown Timer:", "TimeOut");
                                        if (viewHandle.action_up) {
                                            long_backspace();
                                        }
                                    }
                                }.start();

                                //Intialising the flag value of this column to 1 and rest of the keys to 0
                                /*for (int i = 0; i < rowCount; i++) {
                                    for (int j = 0; j <columnCount; j++) {
                                        if (i == 0 && j == 5) {
                                            flag[i][j] = 1;
                                        } else {
                                            flag[i][j] = 0;
                                        }
                                    }
                                }*/
                                resetFlagArray(0);
                                flag[0][5]=1;
                                myVib.vibrate(vibrateDuration);
                            }
                        }
                  /*      }
                    }*/
                    }//backspace

                    handleExploreByTouch();

                    if (y < height / 9 && y > 0) {

                       /* if (x < width/6) {
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
                                    if (rakarFlag) {
                                        speakOut("\u0915\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0915";
                                        speakOut("\u0930\u094D\u0915");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0915\u093C";
                                        speakOut("\u0915" + "nukta");


                                    } else {
                                        speakOut("\u0915");
                                        viewHandle.keyCodelabel = "\u0915";


                                    }
                                    touchdX1 = x;
                                    touchdY1 = y;
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/ /*else if (x > width/6 && x < 2 * width/6) {
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

                                    if (rakarFlag) {
                                        speakOut("\u0916\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0916\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0916";
                                        speakOut("\u0930\u094D\u0916");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0916\u093C";
                                        speakOut("\u0916" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0916";
                                        Log.d("keycodelabel", viewHandle.keyCodelabel);
                                        speakOut("\u0916");


                                    }
                                    touchdX1 = x;
                                    touchdY1 = y;
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/ /*else if (x > 2 * width/6 && x < 3 * width/6) {
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
                                    if (rakarFlag) {
                                        speakOut("\u0917\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0917\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0917";
                                        speakOut("\u0930\u094D\u0917");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0917\u093C";
                                        speakOut("\u0917" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0917";
                                        speakOut("\u0917");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0918\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0918\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0918";
                                        speakOut("र्\u0918");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "घ";
                                        speakOut("घ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0918";
                                        speakOut("\u0918");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0919\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0919\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0919";
                                        speakOut("र्\u0919");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut("ङ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0919";
                                        speakOut("\u0919");

                                    }
                                    Log.d("timer", "aftertimer");
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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

                        }*/
                    } else if (y > height / 9 && y < 2 * height / 9) {

                        /*if (x < width/6) {
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
                                    if (rakarFlag) {
                                        speakOut("\u091A\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091A\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091A";
                                        speakOut("र्\u091A");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "च";
                                        speakOut("च");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091A";
                                        speakOut("\u091A");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u091B\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091B\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091B";
                                        speakOut("र्\u091B");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "छ";
                                        speakOut("छ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091B";
                                        speakOut("छ");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u091C\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091C\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091C";
                                        speakOut("र्\u091C");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u091C़";
                                        speakOut("\u091C" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091C";
                                        speakOut("\u091C");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u091D\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091D\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091D";
                                        speakOut("र्\u091D");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "झ";
                                        speakOut("झ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091D";
                                        speakOut("\u091D");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u091E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091E\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091E";
                                        speakOut("र्\u091E");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ञ";
                                        speakOut("ञ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u091E";
                                        speakOut("\u091E");

                                    }
//                                keyCodelabel = "\u091E";
//                                speakOut("\u091E");


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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

                        }*/
                    } else if (y > 2 * height / 9 && y < 3 * height / 9) {

                        /*if (x < width/6) {
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
                                    if (rakarFlag) {
                                        speakOut("\u091F\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091F\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091F";
                                        speakOut("र्\u091F");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ट";
                                        speakOut("ट");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091F";
                                        speakOut("\u091F");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0920\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0920\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0920";
                                        speakOut("र्\u0920");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ठ";
                                        speakOut("ठ");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0920";
                                        speakOut("\u0920");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0921\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0921\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0921";
                                        speakOut("र्\u0921");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0921़";
                                        speakOut("\u0921" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0921";
                                        speakOut("\u0921");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0922\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0922\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0922";
                                        speakOut("र्\u0922");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0922़";
                                        speakOut("\u0922" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0922";
                                        speakOut("\u0922");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0923\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0923\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0923";
                                        speakOut("र्\u0923");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ण";
                                        speakOut("ण");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0923";
                                        speakOut("\u0923");
                                    }
//                                keyCodelabel = "\u0923";
//                                speakOut("\u0923");

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 3 * height / 9 && y < 4 * height / 9) {
                        /*
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
                                    if (rakarFlag) {
                                        speakOut("\u0924\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0924";
                                        speakOut("र्\u0924");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "त";
                                        speakOut("त");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0924";
                                        speakOut("\u0924");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0925\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0925\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0925";
                                        speakOut("र्\u0925");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "थ";
                                        speakOut("थ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0925";
                                        speakOut("\u0925");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0926\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0926\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0926";
                                        speakOut("र्\u0926");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "द";
                                        speakOut("द");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0926";
                                        speakOut("\u0926");

                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0927\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0927\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0927";
                                        speakOut("र्\u0927");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ध";
                                        speakOut("ध");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0927";
                                        speakOut("\u0927");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0928\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0928\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0928";
                                        speakOut("र्\u0928");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0928़";
                                        speakOut("\u0928" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0928";
                                        speakOut("\u0928");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 4 * height / 9 && y < 5 * height / 9) {
                        /*
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
                                    if (rakarFlag) {
                                        speakOut("\u092A\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092A\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092A";
                                        speakOut("र्\u092A");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "प";
                                        speakOut("प");

                                    } else {
                                        viewHandle.keyCodelabel = "\u092A";
                                        speakOut("\u092A");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u092B\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092B\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092B";
                                        speakOut("र्\u092B");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u092B़";
                                        speakOut("\u092B" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092B";
                                        speakOut("\u092B");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u092C\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092C\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092C";
                                        speakOut("र्\u092C");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ब";
                                        speakOut("ब");

                                    } else {
                                        viewHandle.keyCodelabel = "\u092C";
                                        speakOut("\u092C");


                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u092D\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092D\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092D";
                                        speakOut("र्\u092D");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "भ";
                                        speakOut("भ");
                                    } else {
                                        viewHandle.keyCodelabel = "\u092D";
                                        speakOut("\u092D");

                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u092E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092E\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092E";
                                        speakOut("र्\u092E");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "म";
                                        speakOut("म");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092E";
                                        speakOut("\u092E");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 5 * height / 9 && y < 6 * height / 9) {
                        /*if (x < width/6) {
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
                                    if (rakarFlag) {
                                        speakOut("\u092F\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u092F\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u092F";
                                        speakOut("र्\u092F");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u092F़";
                                        speakOut("\u092F" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u092F";
                                        speakOut("\u092F");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0930\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0930";
                                        speakOut("र्\u0930");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0930़";
                                        speakOut("\u0930" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0930";
                                        speakOut("\u0930");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0932\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0932\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0932";
                                        speakOut("र्\u0932");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "\u0932़";
                                        speakOut("\u0932" + "nukta");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0932";
                                        speakOut("\u0932");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0935\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0935\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0935";
                                        speakOut("र्\u0935");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "व";
                                        speakOut("व");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0935";
                                        speakOut("\u0935");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0936\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0936";
                                        speakOut("र्\u0936");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "श";
                                        speakOut("श");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0936";
                                        speakOut("\u0936");


                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 6 * height / 9 && y < 7 * height / 9) {
                        /*if (x < width/6) {
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


                                    if (rakarFlag) {
                                        speakOut("\u0937\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0937\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0937";
                                        speakOut("र्\u0937");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ष";
                                        speakOut("ष");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0937";
                                        speakOut("\u0937");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0938\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0938\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0938";
                                        speakOut("र्\u0938");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "स";
                                        speakOut("स");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0938";
                                        speakOut("\u0938");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0939\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0939\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0939";
                                        speakOut("र्\u0939");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ह";
                                        speakOut("ह");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0939";
                                        speakOut("\u0939");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0924\u094D\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0924\u094D\u0930";
                                        speakOut("र्\u0924\u094D\u0930");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut(".");

                                    } else {
                                        viewHandle.keyCodelabel = "\u0924\u094D\u0930";
                                        speakOut("\u0924\u094D\u0930");
                                    }


                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    if (rakarFlag) {
                                        speakOut("\u0915\u094D\u0937\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0937\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0915\u094D\u0937";
                                        speakOut("र्\u0915\u094D\u0937");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "क्ष";
                                        speakOut("क्ष");


                                    } else {
                                        viewHandle.keyCodelabel = "\u0915\u094D\u0937";
                                        speakOut("\u0915\u094D\u0937");
                                    }

                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 7 * height / 9 && y < 8 * height / 9) {
                        /*
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
                                    if (rakarFlag) {
                                        speakOut("\u091C\u094D\u091E\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u091C\u094D\u091E\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u091C\u094D\u091E";
                                        speakOut("र्\u091C\u094D\u091E");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "ज्ञ";
                                        speakOut("ज्ञ");


                                    } else {
                                        viewHandle.keyCodelabel = "\u091C\u094D\u091E";
                                        speakOut("\u091C\u094D\u091E");
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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

                                    if (rakarFlag) {
                                        speakOut("\u0936\u094D\u0930\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930\u094D\u0930";


                                    } else if (rafarFlag) {
                                        viewHandle.keyCodelabel = "र्\u0930\u094D\u0936";
                                        speakOut("र्\u0930\u094D\u0936");


                                    } else if (nuktaFlag) {
                                        viewHandle.keyCodelabel = "";
                                        speakOut(".");

                                    } else {
                                        speakOut("\u0936\u094D\u0930");
                                        viewHandle.keyCodelabel = "\u0936\u094D\u0930";
                                    }
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                                    CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , 500 ) {

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
                        }*/
                    } else if (y > 8 * height / 9 && y < 9 * height / 9) {
                        /*if(x < width/6){
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
                    }*/

                    }
                }

                //else ends



                break;

            case MotionEvent.ACTION_UP:

                Log.d(TAG,"Action UP");
                viewHandle.action_up =true;

                //detect flag
                l = 0;
                k = 0;
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        if (flag[i][j] == 1) {
                            l = i;
                            k = j;
                        }
                    }
                }

                //get key details
                width = viewHandle.mKeyboardView.getWidth();
                height = viewHandle.mKeyboardView.getHeight();
                x = (int) event.getX();
                y = (int) event.getY();

                keyCode = getKeyPosition();


                /*float a=0,b=0;
                a= event.getX();
                b= event.getY();*/

                /*if(l==10 && k == 0){

                }
                if(l==10 && k == 1){

                }*/

                if(keyCode == NUKTACODE){

                    if(nuktaFlag){
                        Log.d(TAG,"Get rid of nukta");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = false;

                        /*flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));

                    }else{
                        Log.d(TAG,"Enable nukta");
                        nuktaFlag = true;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = false;

                       /* flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = true;*/

                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    }
                }else if(keyCode == RAFARCODE){

                    if(rafarFlag){

                        Log.d(TAG,"Get rid of rafar");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = false;

                        /*flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));

                    }else{
                        Log.d(TAG,"Enable rafar");
                        nuktaFlag = false;
                        rafarFlag = true;
                        eyelashFlag = false;
                        rakarFlag = false;

                        /*flag2[2] = false;
                        flag2[1] = true;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.rafar));
                    }
                }else if(keyCode == RAKARCODE){

                    if(rakarFlag){
                        Log.d(TAG,"Get rid of rakar");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = false;

                       /* flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));

                    }else{

                        Log.d(TAG,"Enable rakar");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = true;

                        /*flag2[2] = true;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.trakar));
                    }
                }else if(keyCode == EYELASHCODE){

                    if(eyelashFlag){

                        Log.d(TAG,"Get rid of eyelash");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;
                        rakarFlag = false;

                        /*flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));

                    }else{

                        Log.d(TAG,"Enable eyelash");
                        nuktaFlag = false;
                        rafarFlag = false;
                        eyelashFlag = true;
                        rakarFlag = false;

                       /* flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;*/
                        //viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    }
                }else if(keyCode == 138){
                    //startVoiceTyping();
                    nuktaFlag = false;
                    rafarFlag = false;
                    eyelashFlag = false;
                    rakarFlag = false;

                }

                /*if (l == 0 && k == 3) {

                    viewHandle.count_nukta += 1;//count_nukta == 2
                    if (viewHandle.count_nukta > 2) {
                        flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;
                        viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_trakar = 1;
                        viewHandle.count_rafar = 1;
                        flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = true;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.nukta));
                    }

                }
                if (l == 0 && k == 4) {

                    viewHandle.count_trakar += 1;
                    if (viewHandle.count_trakar > 2) {
                        flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;
                        viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_nukta = 1;
                        viewHandle.count_rafar = 1;
                        flag2[2] = false;
                        flag2[1] = true;
                        flag2[0] = false;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.trakar));
                    }
                }

                if (l == 0 && k == 5) {
                    viewHandle.count_rafar += 1;

                    if (viewHandle.count_rafar > 2) {
                        flag2[2] = false;
                        flag2[1] = false;
                        flag2[0] = false;
                        viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.modified));

                    } else {
                        viewHandle.count_trakar = 1;
                        viewHandle.count_nukta = 1;
                        flag2[2] = true;
                        flag2[1] = false;
                        flag2[0] = false;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.rafar));

                    }
                }*/


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

                MediaPlayer mpBackspace = MediaPlayer.create(viewHandle.mHostActivity, R.raw.woosh);
                mpBackspace.setVolume(higherVolume,lowerVolume);
                MediaPlayer mpSpace = MediaPlayer.create(viewHandle.mHostActivity, R.raw.tick);
                mpSpace.setVolume(lowerVolume,higherVolume);

                //three finger left swipe
                if(l==9 && k==5){
                    Log.d(SEQUENCE,"BKSpace");
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
                            mpBackspace.start();;
                        }else{
                            editable.clear();
                            editable.append(part_2);
                            edittext.setSelection(0);
                            Log.d("parts", part_2);
                            Log.d("lastindex", String.valueOf(lastindex));
                            mpBackspace.start();
                        }

                    }

                }
                if(l==1 && k==6){
                    speakOut(s);
                    log_chars('丑');
                }

                if ((l == 10 && (k >0 && k<=4)) || (l==9 && k==0)) {                     //space
                    Log.d(SEQUENCE,"Space");
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
                    mpSpace.start();


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
                if ((l == 0 || l==1) && k == 5) {
                    Log.d(SEQUENCE,"one bkSpace");

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
                            mpBackspace.start();
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

                    Log.d(TAG,"?condition for lift to type model");

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

                    if (l > 0 && l < 10 && nuktaFlag) {
                        nuktaFlag = false;
                        //viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    } else if (l > 0 && l < 10 && rakarFlag) {
                        rakarFlag = false;
                        //viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    } else if (l > 0 && l < 10 && rafarFlag) {
                        rafarFlag = false;
                        //viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
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
                    if (l > 0 && l < 10 && nuktaFlag) {
                        nuktaFlag = false;
                        //viewHandle.count_nukta = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    } else if (l > 0 && l < 10 && rakarFlag) {
                        rakarFlag = false;
                        //viewHandle.count_trakar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    } else if (l > 0 && l < 10 && rafarFlag) {
                        rafarFlag = false;
                        //viewHandle.count_rafar = 1;
                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                    }

                }



                //setting touch flags to default values

                /*for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {

                        flag[i][j] = 0;

                    }
                }*/
                resetFlagArray(0);

                viewHandle.keyCodelabel = "";
                multi_touch = false;
                tri_touch =false;
                navigator_flag = false;
                action_down_flag= false;
                PROXIMITY_CHECK = false;
                PREV_COORDINATE =false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG,"Action pointer UP");
                View focusCurrent1 = viewHandle.mHostActivity.getWindow().getCurrentFocus();
                EditText edittext1 = (EditText) focusCurrent1;
                String s1 = edittext1.getText().toString();
                Editable editable1 = edittext1.getText();
                int start1 = edittext1.getSelectionStart();

                if (event.getPointerId(pointerIndex) != 0 && viewHandle.radius > viewHandle.mInnerRadius && tri_touch == false && multi_touch && navigator_flag==false) {
                    editable1.insert(start1, viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);
                    viewHandle.speakOut_pitch(viewHandle.keyCodelabel + viewHandle.vowels[viewHandle.arc]);

                    if (nuktaFlag || rakarFlag || rafarFlag || eyelashFlag) {

                        viewHandle.mKeyboardView.setKeyboard(new Keyboard(viewHandle.mHostActivity, R.xml.layout2));
                        nuktaFlag = false;
                        rakarFlag = false;
                        rafarFlag = false;
                        eyelashFlag = false;

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
            //edittext.setText("");
            Log.d(SEQUENCE,"Supposed to clear the text box on long hold on backspace.");
            MediaPlayer mpBackspace = MediaPlayer.create(viewHandle.mHostActivity, R.raw.woosh);
            mpBackspace.setVolume(higherVolume,lowerVolume);
            mpBackspace.start();
        }
    }

    /************
     * Set the keycode, key label and sample word for the key
     * @return
     */
    private int getKeyPosition(){

        //Get the size of each key
        float rowHeight = height/rowCount;
        float columnWidth = width/columnCount;

        //Figure out the row and column the current (x,y) pair belong to
        rowNo = (int)Math.floor(y*1.0/rowHeight);
        columnNo = (int)Math.floor(x/columnWidth);
        int keyCode=0;


        Log.d(POSITION,"Row, col:"+rowNo+", "+columnNo);
        Log.d(SEQUENCE,"Active on key in - (row, col): ("+rowNo+", "+columnNo+")");

        //Log.d(POSITION,"Max row count: "+viewHandle.kbLayout.length+", Max column count:"+viewHandle.kbLayout[0].length);

        //If the x,y belong to the keyboard grid.
        Log.d(SEQUENCE, "Row details: "+rowNo+","+ viewHandle.kbLayout.length +","+ columnNo +","+ viewHandle.kbLayout[rowNo].length);
        if(rowNo < viewHandle.kbLayout.length && columnNo < viewHandle.kbLayout[rowNo].length) {

            Log.d(SEQUENCE,"Within keyboard bounds");

            //useful if rafar etc are modal keys
            if(rafarFlag)
                keyCode = viewHandle.kbLayout[rowNo][columnNo];//keyCode = viewHandle.kbLayoutRafar[rowNo][columnNo];
            else if(nuktaFlag)
                keyCode = viewHandle.kbLayout[rowNo][columnNo];//keyCode = viewHandle.kbLayoutNukta[rowNo][columnNo];
            else if(rakarFlag)
                keyCode = viewHandle.kbLayout[rowNo][columnNo];//keyCode = viewHandle.kbLayoutTrakar[rowNo][columnNo];
            else
                keyCode = viewHandle.kbLayout[rowNo][columnNo];

            Log.d(POSITION, "Needle:"+keyCode);
            Log.d(SEQUENCE,"Key code:"+keyCode);

            //look up label and sample words
            switch(keyCode) {

                case LEFTARROW:
                    //keyLabel = "left arrow key";
                    keyLabel = "";
                    sampleWord = "move left by one character";

                    break;
                case RIGHTARROW:
                    //keyLabel = "right arrow key";
                    keyLabel = "";
                    sampleWord = "move right by one character"; //moved right by one character

                    break;
                case BACKSPACE:
                    keyLabel = "";
                    sampleWord = "backspace key"; //moved right by one character
                    break;

                case SPACE:
                    keyLabel = "";
                    sampleWord = "space key"; //moved right by one character
                    break;

                case ENTER:
                    keyLabel = "";
                    sampleWord = "go to new line"; //moved right by one character
                    break;

                    default:
                 for (int i = 0; i < languageObjects.nKeys; i++) {

                    if (keyCode == languageObjects.myKey.get(i).code) {

                        keyLabel = languageObjects.myKey.get(i).label;
                        sampleWord = languageObjects.myKey.get(i).exampleWord;
                        Log.d(POSITION, "Label:" + keyLabel);
                        Log.d(POSITION, "Speak:" + keyLabel);
                        //viewHandle.keyCodelabel = keyLabel;
                        //speakOut(keyLabel);
                        //giveWordFeedback(sampleWord);

                    }
                }
                break;
            }
            Log.d(SEQUENCE,"Key label:"+keyLabel+", sample word:"+sampleWord);

            //Set flags
            //int zero = 0;
            //Arrays.fill(flag,zero);

            //Set the currently active key
            resetFlagArray(0);
            flag[rowNo][columnNo]= 1;

            /*for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    if (i == (rowNo+1) && j == columnNo) {
                        flag[i][j] = 1;
                    } else {
                        flag[i][j] = 0;
                    }
                }
            }*/

        }else{
            Log.d(SEQUENCE,"!! Not a key from the grid. !!");
        }

        //Handle modal flags
        /*if (rakarFlag) {
            Log.d(SEQUENCE,"Trakar active");
            keyLabel = keyLabel+ RAKAR;
            sampleWord="";
            //speakOut("\u0915\u094D\u0930");
            //viewHandle.keyCodelabel = "\u0915\u094D\u0930";

        } else if (rafarFlag) {
            Log.d(SEQUENCE,"Rafar active");
            keyLabel = RAFAR + keyLabel;
            sampleWord="";
            //viewHandle.keyCodelabel = "\u0930\u094D\u0915";
            //speakOut("\u0930\u094D\u0915");

        } else if (nuktaFlag) {
            Log.d(SEQUENCE,"Nukta active");
            //viewHandle.keyCodelabel = "\u0915\u093C";
            //speakOut("\u0915"+"nukta");
            sampleWord="";


        }else if (eyelashFlag) {
            Log.d(SEQUENCE,"eyelash active");
            keyLabel = EYELASH + keyLabel;
            sampleWord="";
            //viewHandle.keyCodelabel = "\u0915\u093C";
            //speakOut("\u0915"+"nukta");


        } else {
            Log.d(SEQUENCE,"None of the modal keys active");

        } //Handle modal flags*/

        Log.d(SEQUENCE,"Key code:"+keyCode);
        Log.d(SEQUENCE,"Modal key check: Key label:"+keyLabel+", sample word:"+sampleWord);

        //if(sampleWord!=null && sampleWord.length()>0)
        giveWordFeedback(sampleWord,keyLabel);

        return keyCode;

    }

    public String getKeyLabel(int code){
        String label = "";

        for(int i=0;i<languageObjects.nKeys;i++){

            if(code == languageObjects.myKey.get(i).code){
                Log.d(POSITION, "Label:"+languageObjects.myKey.get(i).label);
                Log.d(POSITION, "Speak:"+languageObjects.myKey.get(i).label);
                label = languageObjects.myKey.get(i).label;

            }else{
                //Log.d(POSITION,"Code:"+languageObjects.myKey.get(i).code);
            }
        }

        return label;

    }

    private void resetFlagArray(int fillValue){

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                    flag[i][j] = fillValue;
                }
            }
    }

    public void startVoiceTyping(String layoutFile){

           /* if(layoutFile.compareTo("voice")==0){

                //Input space to take care of timestamp
                //commitVoiceText(" ");

                //Check for permission
                //checkForPermissions();

                Speech2TextIntent s2TIntent = new Speech2TextIntent.Speech2TextIntentBuilder(mContext,this)
                        .setLanguage(mExceptionLangHandler.speechLanguage)
                        .setView(Speech2TextIntent.VIEW_KEYBOARD)
                        .setHeightInPixels(800) //Height in Pixels for the view wanted, default height is 260dp
                        .build();
                //s2TIntent.setLanguage(lang);
                Log.d("voice", "Start voice service");
                Log.d(TAG,"Start voice service");

                s2TIntent.startService();
                //voice log
                //mKeyLogger.saveSessionData(System.currentTimeMillis(),System.currentTimeMillis(),0,0,0);

            }else if(layoutFile.compareTo("stopvoice")==0){

                Log.d("voice", "Stop voice service");
                //Log.d(TAG,"Stop voice service");
                //s2TIntent.stopService();
            }*/


        /*Speech2TextIntent.Speech2TextIntentCallback callbackFromS2T = new Speech2TextIntent.Speech2TextIntentCallback() {
            @Override
            public void onTranscriptionReceived(ArrayList<Transcription> transcriptions, Canonical Data) {
                //Write Code to handle the list of transcriptions in streaming mode, received with their confidence scores
                if (transcriptions.size() > 0) {
                    for(Transcription transcription : transcriptions){
                        Log.d(TAG, "Transcription:"+transcription.getText()+", English Transcription:"+transcription.getEnglishText()", Confidence:"+transcription.getConfidence());
                    }
                }
            }

            @Override
            public void onPartialTranscriptionReceived(ArrayList<Transcription> transcriptions) {
                //Write Code to handle the list of transcriptions in non streaming mode, received with their confidence scores
                if (transcriptions.size() > 0) {
                    for(Transcription transcription : transcriptions){
                        Log.d(TAG, "Transcription:"+transcription.getText()+", English Transcription:"+transcription.getEnglishText()+", Confidence:"+transcription.getConfidence());
                    }
                }
            }

            @Override
            public void onError(S2TError error) {
                //Write code to handle each error from the error codes by comparing error.errorCode to constants in Error class
                if(error.errorCode == S2TError.ERROR_NO_USER_ID){
                    Log.d(TAG, "No user id,"+error.message);
                }
            }


            @Override
            public void onRecordingEnd() {
                //Called when recording is stopped and SDK is awaiting a result
                Log.d(TAG,"onRecordingEnd");
            }

            @Override
            public void onAmplitudeChanged(double amplitude) {
                //Callback Every 60ms with average amplitude for the duration, for animation drawing.
                Log.d(TAG,"onAmplitudeChanged"+amplitude);
            }

            @Override
            public void onTransactionEnd() {
                //Called when service is stopped
                ripple.setVisibility(View.GONE);
                isRecording = false;
            }
        };

        Speech2TextIntent s2TIntent = new Speech2TextIntent.Speech2TextIntentBuilder(viewHandle.getActivity(), callbackFromS2T)
                .setLanguage(Speech2TextIntent.LANGUAGE_MARATHI)
                .setView(Speech2TextIntent.VIEW_KEYBOARD)
                .setStreaming(true)
                .build();
*/


    }

    private void handleExploreByTouch(){

        int r, s;
        r = rowNo; //rowNo+1;
        s = columnNo;
        Log.d(SEQUENCE, "Handle explore by touch:"+keyCode+", Proximity check:"+PROXIMITY_CHECK);

        //if(keyCode == ENTER || keyCode == SPACE || keyCode == LEFTARROW || keyCode == RIGHTARROW ||keyCode == VOICETYPING || keyCode == SHIFT || keyCode == SETTINGS || keyCode == LANGUAGE_EN){

            //|| keyCode == RAFARCODE || keyCode == RAKARCODE || keyCode == EYELASHCODE

            //Log.d(SEQUENCE,"On one of the function keys.");

            if (PROXIMITY_CHECK) {
                proximity_check(r,s);

            } else {
                //key_touch(r,s,label,vibrateDuration);
                switch(keyCode){

                    //anchor keys
                    case 1:
                    case 5:
                    case 18:
                    case 39:
                    case 136:
                    myVib.vibrate(COUNT_DOWN_INTERVAL);
                    speakOut(keyLabel);
                    Log.d(SEQUENCE, "One of the corner keys");
                    break;
                   case BACKSPACE:
                        key_touch(rowNo,columnNo,"delete",vibrateDuration);
                        break;
                    case LEFTARROW:
                        key_touch(rowNo,columnNo,"left arrow",vibrateDuration);
                        break;
                    case RIGHTARROW:
                        key_touch(rowNo,columnNo,"right arrow",vibrateDuration);
                        break;
                    case SPACE:
                        key_touch(rowNo,columnNo,"Space key",vibrateDuration);
                        break;
                    case ENTER:
                        key_touch(rowNo,columnNo,"Enter key",vibrateDuration);
                        break;
                    case VOICETYPING:
                        key_touch(rowNo,columnNo,"Start voice typing",vibrateDuration);
                        break;
                    case LANGUAGE_EN:
                        key_touch(rowNo,columnNo,"Switch to English keyboard",vibrateDuration);
                        break;
                    case SETTINGS:
                        key_touch(rowNo,columnNo,"Go to keyboard settings",vibrateDuration);
                        break;
                    case SHIFT:
                        key_touch(rowNo,columnNo,"Shift key",vibrateDuration);
                        break;
                    default:
                        myVib.vibrate(vibrateDuration);
                        speakOut(keyLabel);
                        Log.d(SEQUENCE, "One of the regular keys");




                }
            }

            //return;
        //}

        if (PROXIMITY_CHECK) {

            Log.d(TAG, "Proximity check true");
            Log.d(SEQUENCE, "Not a function key, proximity flag true.");

            if (P_CHECK_FLAGS[r][s] == 0) {

                Log.d(SEQUENCE, "Check flag not set for the key");
                //check any other p_check_flags are intialised to 1
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        if (P_CHECK_FLAGS[i][j] == 1) {
                            PREV_COORDINATE = true;
                            Log.d(SEQUENCE, "Check flag set for another key:("+i+","+j+")");
                        }
                    }
                }

                //if there is no other flags intilaise then set a new starting point
                if (PREV_COORDINATE == false) {
                    Log.d(TAG, "Init the flag");

                    Log.d(SEQUENCE, "Set flag for the key");
                    for (int i = 0; i < rowCount; i++) {
                        for (int j = 0; j < columnCount; j++) {

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
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        P_CHECK_FLAGS[i][j] = 0;
                        Log.d(SEQUENCE, "Distance travelled more than threshold. Reset the flags for all keys");
                    }
                }
                //setting prev cordinate to false
                PREV_COORDINATE = false;
            }
            Log.d("proximity", "touchMovement X: " + String.valueOf(touchdX) + "touchMovementY:" + String.valueOf(touchdY));
            Log.d("proximitydistance", String.valueOf(distance));
        } else {

            //if (flag[1][0] == 0) {

                /*if (rakarFlag) {
                    speakOut(keyLabel);
                    viewHandle.keyCodelabel = keyLabel+ "\u094D\u0930";

                } else if (rafarFlag) {
                    viewHandle.keyCodelabel = "\u0930\u094D\u0915";
                    speakOut("\u0930\u094D\u0915");

                } else if (nuktaFlag) {
                    viewHandle.keyCodelabel = "\u0915\u093C";
                    speakOut("\u0915" + "nukta");

                } else {
                    speakOut("\u0915");
                    viewHandle.keyCodelabel = "\u0915";

                }*/
                speakOut(keyLabel);
                viewHandle.keyCodelabel = keyLabel;

                touchdX1 = x;
                touchdY1 = y;
                CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , COUNT_DOWN_INTERVAL ) {

                    public void onTick(long millisUntilFinished) {

                        PROXIMITY_CHECK = true;
                    }

                    public void onFinish() {
                        //Done timer time out.
                        //if (flag[1][0] == 1) {
                        if(sampleWord!=null)
                            speakOut(sampleWord);
                        //}
                        //PROXIMITY_CHECK = true;
                        //Log.i("Proximity: ", String.valueOf(PROXIMITY_CHECK));
                        //Log.i("Countdown Timer:", "TimeOut");
                    }
                }.start();

                //Intialising the flag value of this column to 1 and rest of the keys to 0
                //TODO: See if we can get rid of the flag array altogether
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        if (i == r && j == s) {
                            flag[i][j] = 1;
                        } else {
                            flag[i][j] = 0;
                        }
                    }
                }
                    resetFlagArray(0);
                    flag[r][s] = 1;

                switch(keyCode) {

                    //anchor keys
                    case 1:
                    case 5:
                    case 18:
                    case 39:
                    case 136:
                        myVib.vibrate(COUNT_DOWN_INTERVAL);
                        break;
                    default:
                        myVib.vibrate(vibrateDuration);
                }
                //myVib.vibrate(COUNT_DOWN_INTERVAL);

                Log.d("keycodelabel", viewHandle.keyCodelabel);
                //Log.d("key", "1");
            //}
        }
    }

    private void giveWordFeedback(String word, String label){

        //flag[1][0]==1
        final String exWord = word;
        final String keyLabel = label;
        final int keyPressed = keyCode;
        Log.d(POSITION,"Give word feedback:"+word);
        Log.d(SEQUENCE,"User stays longer give feedback:"+word);


        CountDownTimer timer = new CountDownTimer(COUNT_DOWN_TIME , COUNT_DOWN_INTERVAL ) {

            int activatorKeyCode = keyCode;

            public void onTick(long millisUntilFinished) {
                PROXIMITY_CHECK=true;
                //Log.d("proximity","Check is true for 1 0");
                //Log.d("Countdown Timer: ", "seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //Done timer time out.
                //if(flag[1][0]==1){
                if(activatorKeyCode == keyCode) {

                    Log.d(SEQUENCE,"User still on the same key give feedback:"+exWord);
                    switch (keyPressed) {

                        case 400:
                            long_backspace();
                            break;
                        default:
                            if (exWord != null)
                                speakOut(exWord);
                    }
                }else{
                    Log.d(SEQUENCE,"Key no longer active");
                }

                //}
                //Log.d("Countdown Timer:","TimeOut");
            }
        }.start();

    }


}


