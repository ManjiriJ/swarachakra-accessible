package game.Typing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;


public class CustomKeyboard extends View {
    private static final int SIZE = 800;
    static int flag_num = 0;
    static int flag_trakar = 0;
    static int flag_rafar = 0;
    private static boolean isChakraVisible;
    final int longpressTimeDownBegin = 1000;
    public String[] vowels = new String[]{"\u094D", "\u093E", "\u093F", "\u0940", "\u0941", "\u0942", "\u0947", "\u0948", "\u094B", "\u094C"};
    public String[] vowels_speak = new String[]{"विराम", "आ", "\u0907","\u0908","\u0909","\u090A","\u090F","\u0910","\u0913","\u0914"};
    //    , "\u094B", "\u094C"
    public String[] vowels_aah = new String[]{"\u0905","\u0906","\u0907","\u0908","\u0909","\u090A","\u090F","\u0910","\u0913","\u0914"};
    public String[] vowes_uuh = new String[]{"\u0960","\u0944","","\u093D","\u0946","\u094A","\u0945","\u0972","\u0949","\u0911"};
    public String keyCodelabel = "";
    public PopupWindow popUp;
    public int keyCodeClicked;
    public RelativeLayout relativelayout;
    //400:bkspc, 401:space, 402:enter, 404: shift, 405:left arrow, 406:right arrow, 407:settings, 408: en, mic:409
    //modal keys: 501/53:rafar, 502/52:trakar, 503/121:nukta, 504 / 51: eyelash
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

    private int arcVibrateDuration = 100;

    public int[][] kbLayoutRafar = {{109,108,48,120,49,BACKSPACE},{1,2,3,4,5,BACKSPACE},{6,7,8,9,10,EYELASHCODE},{11,12,13,14,15,RAKARCODE},{16,17,18,19,20,RAFARCODE},{21,22,23,24,25,NUKTACODE},{26,27,28,29,30,54},{31,32,33,34,35,60},{39,36,37,38,136,44},{SHIFT,LEFTARROW,RIGHTARROW,SETTINGS,LANGUAGE_EN,VOICETYPING},{SHIFT,SPACE,SPACE,SPACE,SPACE,ENTER}};
    //public int[][] kbLayoutRAFARCODE = {{109,108,48,120,49,BACKSPACE},{1,2,3,4,5,BACKSPACE},{6,7,8,9,10,EYELASHCODE},{11,12,13,14,15,RAKARCODE},{16,17,18,19,20,RAFARCODE},{21,22,23,24,25,NUKTACODE},{26,27,28,29,30,54},{31,32,33,34,35,60},{39,36,37,38,136,44},{SHIFT,VOICETYPING,LEFTARROW,RIGHTARROW,LANGUAGE_EN,SETTINGS},{SHIFT,SPACE,SPACE,SPACE,SPACE,ENTER}};

    public int[][] kbLayout = {{109,108,48,120,49,BACKSPACE},{1,2,3,4,5,BACKSPACE},{6,7,8,9,10,EYELASHCODE},{11,12,13,14,15,RAKARCODE},{16,17,18,19,20,RAFARCODE},{21,22,23,24,25,NUKTACODE},{26,27,28,29,30,SETTINGS},{31,32,33,34,35,LANGUAGE_EN},{39,36,37,38,137,VOICETYPING},{SHIFT,54,60,44,LEFTARROW,RIGHTARROW},{SHIFT,SPACE,SPACE,SPACE,SPACE,ENTER}};

    public int[][] kbLayoutNukta = {{109,108,48,120,49,BACKSPACE},{1,2,3,4,5,BACKSPACE},{6,7,8,9,10,EYELASHCODE},{11,12,13,14,15,RAKARCODE},{16,17,18,19,20,RAFARCODE},{21,22,23,24,25,NUKTACODE},{26,27,28,29,30,54},{31,32,33,34,35,60},{39,36,37,38,136,44},{SHIFT,LANGUAGE_EN,VOICETYPING,LEFTARROW,RIGHTARROW,43},{SHIFT,SETTINGS,SPACE,SPACE,SPACE,ENTER}};

    public int[][] kbLayoutTrakar = {{109,108,48,120,49,BACKSPACE},{1,2,3,4,5,BACKSPACE},{6,7,8,9,10,EYELASHCODE},{11,12,13,14,15,RAKARCODE},{16,17,18,19,20,RAFARCODE},{21,22,23,24,25,NUKTACODE},{26,27,28,29,30,54},{31,32,33,34,35,60},{39,36,37,38,136,44},{SHIFT,LANGUAGE_EN,VOICETYPING,LEFTARROW,RIGHTARROW,43},{SHIFT,SETTINGS,SPACE,SPACE,SPACE,ENTER}};
    //    final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
//        public void onLongPress(MotionEvent e) {
////            if(flag[1][0] == 1){
////                speakOut("kamal");
////            }else if(flag[1][1] == 1){
////                speakOut("खुशी");
////            }
//
//            Log.d("long","longclick");
//        }
//    });
//
//    public boolean onTouchEvent(MotionEvent event) {
//        return gestureDetector.onTouchEvent(event);
//
    public CustomTouchListener mTouchlistener;
    //Variables declaration and intialization
    TextView tv1;
    EditText et1;
    //int count_trakar=1,count_nukta=1,count_rafar=1;
    //float touchdX=0,touchdY=0,touchdX1=0,touchdY1=0,distance=0,distance1=0;

    //int COUNT_DOWN_TIME=1000;
    float touchMovementX, touchMovementY;
    //boolean touch_flag = false, multi_touch_flag = false, action_down_flag = false, multi_touch = false,tri_touch=false,navigator_flag=false;
    //boolean touch_flag = false, multi_touch_flag = false, action_down_flag = false, multi_touch = false,tri_touch=false,navigator_flag=false;
    float x, y, x1, y1, touchDownX, touchDownY;
    boolean single_touch = false;
    Timer longPressTimer= new Timer();
    Timer longPressArray[][] = new Timer[11][7];
    /*boolean PROXIMITY_CHECK=false;*/
    float mOuterRadius;
    int radius;
    int arc;
    float PITCH = 1f;
    //int THRESHOLD_DIST = 50,THRESHOLD_DIST_LAST_ROW = 60;
    Activity activity;
    boolean action_up = false;
    float mInnerRadius = (float)180;
    AccessibilityManager mAccessibilityManager;
    Paint paint_arc;
    float width, height;
    CustomKeyboard obj;
    //int l,k;
    /*boolean vowelBoolean = false;*/
    boolean flag_pointerdown = false;
    EditText et;
    Keyboard.Key k1;
    /*boolean flag2[] = new boolean[3];*/
    String TAG = "production";
    String CHAKRATAG = "chakra";
    /**
     * A link to the KeyboardView that is used to render this CustomKeyboard.
     */
    //Vibrator myVib;
    //AudioManager am;
    int count;
    KeyEvent ke;
    /**
     * A link to the activity that hosts the {@link #mKeyboardView}.
     */
    String keyUniCode;
    boolean long_click_flag=false;
    /*int flag[][] = new int[11][7];
    int flag1[] = new int[10];
    boolean action_downflag2[] = {false,false,false};
    int P_CHECK_FLAGS[][] =  new int[11][7];
    boolean PREV_COORDINATE=false;*/
    private SparseArray<PointF> mActivePointers;
    private Paint mPaint;
    private int[] colors = {Color.BLUE, Color.GREEN, Color.MAGENTA,
            Color.BLACK, Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY,
            Color.LTGRAY, Color.YELLOW};
    private Paint textPaint;
    private int keyCode;
    //private TextToSpeech tts, tts1;
    //Text to Speeches intialisation
    /*TextToSpeech.OnInitListener onInit = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                final Locale loc = new Locale("hin", "IND");
                int result = tts.setLanguage(loc);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(TAG, "1:This Language is not supported");
                    Toast.makeText(getContext(), "Your default text to speech engine does not support Hindi. Please download Hindi locale for current TTS or switch to a text to speech engine that supports Hindi locale.",Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getContext(), "Your default text to speech engine does not support Hindi. Please download Hindi locale for current TTS or switch to a text to speech engine that supports Hindi locale.",Toast.LENGTH_LONG).show();

                } else {

//                    speakOut(keyCodelabel);
                    Log.d(TAG, "2:This Language is supported");
                }

            } else {
                Log.d(TAG, "2:Initilization Failed!");
            }
        }
    };*/
    public KeyboardView mKeyboardView;
    public Activity mHostActivity;
    /**
     * The key (code) handler.
     */
    public OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodePrev = 55000;
        public final static int CodeAllLeft = 55001;
        public final static int CodeLeft = 55002;
        public final static int CodeRight = 55003;
        public final static int CodeAllRight = 55004;
        public final static int CodeNext = 55005;
        public final static int CodeClear = 55006;
        public int keyCodeClicked;



        @Override
        public void onKey(final int primaryCode, int[] keyCodes) {
//            Log.d("Count",Integer.toString(count));

            Log.d("On", "onkey");
            Log.d("Sequence","On key ");

            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;

            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            String text = editable.toString();
            int start = edittext.getSelectionStart();
            // Apply the key to the edittext
            if (primaryCode == CodeLeft) {

                Log.d("On", "codeleft");
                int selectionEnd = edittext.getSelectionEnd();
                if(selectionEnd != 0) edittext.setSelection(selectionEnd-1);
//                setContentDescription("leftarrow");

            } else if (primaryCode == CodeRight) {

                Log.d("On", "coderight");
                int selectionEnd = edittext.getSelectionEnd();
                if(selectionEnd != text.length() )edittext.setSelection(selectionEnd+1);

            } else if (primaryCode == CodeAllLeft) {

                Log.d("On", "codeallleft");
                edittext.setSelection(0);

            } else if (primaryCode == CodeAllRight) {

                Log.d("On", "codeallright");
                edittext.setSelection(edittext.length());

            } else if (primaryCode == CodeDelete) {

                String str = edittext.getText().toString();
                String str1,str2;
                int pos=0;
                if (str.length() != 0) {

//                            mp.setVolume(2,3);
                    Log.d("selection", String.valueOf(edittext.getSelectionStart())+","+String.valueOf(str.length()));

                    if(edittext.getSelectionStart() == str.length() &&  edittext.getSelectionStart()!=0){
                        char lastchar = str.charAt(edittext.getSelectionStart()-1);
                        speakOut(String.valueOf(lastchar), 0.8f);
                        MediaPlayer mp = MediaPlayer.create(mHostActivity, R.raw.harpoon);
                        mp.start();
                        str = str.substring(0,str.length()-1);
                        pos = edittext.getSelectionStart()-1;
                        edittext.setText(str);
                        edittext.setSelection(pos);

                    }else if(edittext.getSelectionStart() < str.length() &&  edittext.getSelectionStart()!=0){
                        char lastchar = str.charAt(edittext.getSelectionStart()-1);
                        speakOut(String.valueOf(lastchar), 0.8f);
                        MediaPlayer mp = MediaPlayer.create(mHostActivity, R.raw.closedoor);
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
            } else if (primaryCode == (23664)) {
                //for trakar
                if (flag_trakar == 0) {
                    new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.layout2);
                    flag_trakar = 1;
                } else {
                    new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.layout2);
                    flag_trakar = 0;
                }
            } else if (primaryCode == (23665)) {
                //for rafar
                Log.d(TAG,"Rafar");
                if (flag_rafar == 0) {
                    new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.layout2);
                    flag_rafar = 1;
                    flag_rafar = 1;
                } else {
                    new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.layout2);
                    flag_rafar = 0;
                }
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }

        }

        @Override
        public void onPress(int keyCode) {

            keyCodeClicked = keyCode;
            Log.d("production","onpress");
            Log.d("Sequence","On press ");
            //setiing the keycode currently clicked
//            mt.setKeyCode(keyCode);

        }


        @Override
        public void onRelease(int primaryCode) {
//            Log.d("On","onrelease");
        }

        @Override
        public void onText(CharSequence text) {
//            Log.d("On","onText");
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
            Log.d(TAG,"On swipeleft ");

            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;

            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            if (editable != null && start > 0) editable.delete(start - 1, start);

        }

        @Override
        public void swipeRight() {
            int arr[] = new int[32];
            onKey(32, arr);

        }


        @Override
        public void swipeUp() {
            Log.d("Sequence","On swipe up");
            if (flag_num == 0) {
//                new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.numpad);
                flag_num = 1;
            } else {
                new CustomKeyboard(mHostActivity, null, R.id.keyboardview, R.xml.layout2);
                flag_num = 0;
            }
        }
    };

    {
        mTouchlistener = new CustomTouchListener();
    }
    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host     The hosting activity.
     * @param viewid   The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public CustomKeyboard(Activity host, AttributeSet attrs, int viewid, int layoutid) {
        super(host, attrs);

        mHostActivity = host;
//        mainActivityObj = new MainActivity();

        mKeyboardView = (KeyboardView) mHostActivity.findViewById(viewid);
//        mKeyboardView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
        mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        mKeyboardView.setLongClickable(true);
        mKeyboardView.isProximityCorrectionEnabled();
        mKeyboardView.setFocusable(true);
//        mKeyboardView.setOnLongClickListener(longlistener);
        mKeyboardView.setOnTouchListener(mTouchlistener);
        mTouchlistener.setHandle(this);

        Log.d(TAG,"Init pending");
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        // Hide the standard keyboard initially
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initView();
        //setting vowel flags to  false
        /*for(int i = 0 ;i<3;i++){flag2[i] = false;}*/

        /*tts = new TextToSpeech(mHostActivity, onInit);
        tts1 = new TextToSpeech(mHostActivity, onInit1);*/
        mTouchlistener.initTTS();

    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
//        final String service = mHostActivity.getPackageName() + "/" + AccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v("TAG", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("TAG", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void speakOut_pitch(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        mTouchlistener.tts1.setPitch((float) 0.8);
        mTouchlistener.tts1.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    public void speakOut(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        mTouchlistener.tts.setPitch(1);
        mTouchlistener.tts.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    public void speakOut(String keyCodelabel,float pitch) {
//        if(!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())){
        if(pitch == 0.8){
            mTouchlistener.tts.setPitch((float) 0.8);
        }else{
            mTouchlistener.tts.setPitch(pitch);
        }
        mTouchlistener.tts.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);}

    public void setTouchDownPoint(float x1, float y1) {
        touchDownX = x1;
        touchDownY = y1;
    }

    private void initView() {
        Log.d(TAG,"initview");

        paint_arc = new Paint();
        paint_arc.setColor(Color.BLUE);
        paint_arc.setStrokeWidth(1);
        paint_arc.setStrokeWidth(1);
        paint_arc.setStyle(Paint.Style.STROKE);


        mActivePointers = new SparseArray<PointF>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(20);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(CHAKRATAG,"onDraw");

        if (mTouchlistener.multi_touch == false)
            return;
        DisplayMetrics metrics = new DisplayMetrics();
        mHostActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.heightPixels;
        height = metrics.widthPixels;

        //outer radium 35% of the min dimension
        // draw all pointers
        mOuterRadius = (float) (0.35 * Math.min(width, height));
        mInnerRadius = (float) (0.30 * mOuterRadius);

        final RectF bound = new RectF();
        final RectF boundOut;

        Log.d(CHAKRATAG, "pointer_down_co_ondraw: (" + touchDownX + "," + touchDownY + ")");

        bound.set(touchDownX - mOuterRadius, touchDownY, touchDownX + mOuterRadius, touchDownY + 2 * mOuterRadius);
//        boundOut = new RectF(mOuterRadius-3,mOuterRadius-3,(3*mOuterRadius)+3, (3*mOuterRadius)+3);
        final RectF bound1 = new RectF();

        //Chakra inner wheel
        Paint mInnerPaint = new Paint();
        mInnerPaint.setColor(getResources().getColor(R.color.bluegrey2));
        mInnerPaint.setAntiAlias(true);

        float centerX = bound.centerX();
        float centerY = bound.centerY();
        Paint mArcDividerPaint = new Paint();
        mArcDividerPaint.setColor(Color.rgb(200, 200, 200));
        mArcDividerPaint.setAntiAlias(true);

        Paint mArcDividerPaint1 = new Paint();
        mArcDividerPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);

        Paint mTransparentPaint = new Paint();//changes
        mTransparentPaint.setColor(Color.TRANSPARENT);//changes
        mTransparentPaint.setAntiAlias(true);//changes

        Paint mArcPaint = new Paint();
        //mArcPaint.setColor(Color.rgb(54, 89, 80));//changes
        mArcPaint.setColor(getResources().getColor(R.color.red));//changes
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //selected arc
        Paint mArcPrevPaint = new Paint();
        mArcPrevPaint.setColor(getResources().getColor(R.color.bluegrey2));//changes
//        mArcPrevPaint.setColor(Color.BLACK);//changes
        mArcPrevPaint.setAntiAlias(true);
        mArcPrevPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));

        Paint mArcPrevPaint1 = new Paint();
        mArcPrevPaint1.setColor(getResources().getColor(R.color.bluegrey));//changes
        // mArcPrevPaint1.setColor(Color.rgb(105, 105, 105));//changes
        mArcPrevPaint1.setAntiAlias(true);

        Paint transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //outer circle
        Log.d(CHAKRATAG, "Outer circle: I am Executing Pabba!");
        canvas.drawCircle(touchDownX, touchDownY + mOuterRadius, mOuterRadius, mArcDividerPaint);
        canvas.drawCircle(touchDownX, touchDownY + mOuterRadius, mOuterRadius, mTransparentPaint);

        canvas.drawCircle(touchDownX, touchDownY + mOuterRadius, mInnerRadius, mInnerPaint);//
//
        Paint arcPaint, arcPaint1;
        Float anglePerArc = (float) (360.0 / 10);
        View focusCurrent = mHostActivity.getWindow().getCurrentFocus();


        EditText edittext = (EditText) focusCurrent;
        Editable editable = edittext.getText();
        String text = editable.toString();
        int start = edittext.getSelectionStart();
//        if( touchMovementX - mOuterRadius > 87.5 || touchMovementY - mOuterRadius> 87.5){

        //Draw chakra letters
        for (int i = 0; i < 10; i++) {

            if (radius > mInnerRadius) {
                arcPaint = mArcPaint;
//            arcPaint1 = mArcPrevPaint1;
                if (i == arc) {

                    arcPaint = mArcPrevPaint;
                    String speak_text="";
                    if (arc == 10) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            Log.d(TAG,"Speak: "+speak_text);

                            mTouchlistener.myVib.vibrate(arcVibrateDuration);

                        }
                    } else if (arc == 1) {
                        if (mTouchlistener.flag1[arc] == 0) {


                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            Log.d(TAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);

                        }
                    } else if (arc == 2) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 3) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_speak[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }

                    } else if (arc == 4) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_speak[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 5) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text =  vowels_speak[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 6) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 7) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 8) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 9) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel + vowels[arc];
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text,PITCH);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    } else if (arc == 0) {
                        if (mTouchlistener.flag1[arc] == 0) {
                            if(keyCodelabel.equals("अ")){
                                speak_text = vowels_aah[arc];
                            }else if(keyCodelabel.equals("ृ")){
                                speak_text = vowes_uuh[arc];
                            }else if(keyCodelabel.equals("\u093E")){
                                speak_text = vowels_speak[arc];
                            }else{
                                speak_text = keyCodelabel+"विराम";
                            }
                            for (int i1 = 0; i1 < 10; i1++) {
                                if (i1 == arc) {
                                    mTouchlistener.flag1[i1] = 1;
                                } else {
                                    mTouchlistener.flag1[i1] = 0;
                                }
                            }
                            speakOut(speak_text);
                            Log.d(CHAKRATAG,"Speak: "+speak_text);
                            mTouchlistener.myVib.vibrate(arcVibrateDuration);
                        }
                    }

                    Log.d("Code", "Inner circle: I am Executing Pabba!");
                    canvas.drawArc(bound, getMidAngle(i) - anglePerArc / 2, anglePerArc - 1, true, arcPaint);
                }
            }else if(radius < mInnerRadius){
                for(int i1=0 ; i1< 10 ; i1 ++){
                    mTouchlistener.flag1[i1] = 0;
                }
            }//
//        }

        }
        drawLetters(canvas);
    }

    public String getText() {
        if (arc < 0) {
            return "k";
        }
        return getTextForArc(arc);
    }

    public String getTextForArc(int region) {
//        Log.d("Gettextforarc",keyCodelabel);
        String str;
        if(keyCodelabel.equals("\u0905")){
            str = vowels_aah[region];
        }else if(keyCodelabel.equals("ृ")){
            str = vowes_uuh[region];
        }else if(keyCodelabel.equals("")){
            str = vowels_aah[region];
        }else{
            str = keyCodelabel + vowels[region];
        }
        //Log.d("String", Integer.toString(keyCode));
        return str;
    }

    private void drawLetters(Canvas canvas) {
        float offsetY = 0;
        //int textSize = 50;
        float textSize = (float) (0.15*mOuterRadius);
        //textBounds = new Rect();
        Rect textBounds = new Rect();
        Paint mSelectedArcTextPaint = new Paint();
        //mInnerTextPaint.setColor(Color.BLACK);
        mSelectedArcTextPaint.setColor(getResources().getColor(R.color.white));
        mSelectedArcTextPaint.setAntiAlias(true);
        mSelectedArcTextPaint.setTextAlign(Paint.Align.CENTER);

        mSelectedArcTextPaint.getTextBounds(getText(), 0, getText().length(), textBounds);
        //offsetY = (textBounds.bottom - textBounds.top) / 2;

        mSelectedArcTextPaint.setTextSize(textSize);
//        canvas.drawText(getText(), touchDownX, touchDownY+ mOuterRadius, mInnerTextPaint);

        float offsetX = 0;

        Paint mArcTextPaint = new Paint();
        mArcTextPaint.setColor(Color.BLACK);
        mArcTextPaint.setAntiAlias(true);
        mArcTextPaint.setTextAlign(Paint.Align.CENTER);
        mArcTextPaint.setTextSize(textSize);
        float mArcTextRadius;

        mArcTextRadius = (float) (0.55 * mOuterRadius); //(0.9 * mOuterRadius)

        for (int i = 0; i < 10; i++) {
            //PointF textPos = getArcTextPoint(i);
            PointF textPos = new PointF();
            //Rect textBounds = new Rect();
            String text = getTextForArc(i);
            mArcTextPaint.getTextBounds(text, 0, text.length(), textBounds);

            offsetY = (textBounds.bottom - textBounds.top) / 2;
            float angleRad = (float) Math.toRadians(getMidAngle(i));
            textPos.x = touchDownX + (float) (mArcTextRadius * Math.cos(angleRad)) + offsetX;
            textPos.y = touchDownY + (float) (mArcTextRadius * Math.sin(angleRad)) + offsetY;


            if(i==arc)
                canvas.drawText(getTextForArc(i), textPos.x, textPos.y + mOuterRadius, mSelectedArcTextPaint);
            else
                canvas.drawText(getTextForArc(i), textPos.x, textPos.y + mOuterRadius, mArcTextPaint);
        }

    }

    public float getMidAngle(int region) {
        float anglePerArc = (float) (360.0 / 10);
        float offset = -90;
        float midAngle = region * anglePerArc + offset;
        return midAngle;
    }

    public void setKeyCode(int keyCodeClicked) {
        this.keyCode = keyCodeClicked;
    }

    //movement on chakra
    public void handleMove(int x, int y) {

        touchMovementX = (int) x - touchDownX;
        touchMovementY = (int) y - touchDownY;

        if (y == 0 && touchMovementX < mOuterRadius && touchMovementY < mOuterRadius) {

            float outerRadius = (float) (1.2 * mOuterRadius);
            touchMovementY = -(int) Math.sqrt(outerRadius * outerRadius
                    - touchMovementX * touchMovementX);
            Log.d(TAG,"No longer getting y coordinate.");
            Log.d(TAG,"Handle move: y=0, recalculating dy:"+touchMovementY);
        }else
            Log.d(TAG,"y coordinate:"+y);

        radius = (int) Math.sqrt((touchMovementX * touchMovementX)
                + (touchMovementY * touchMovementY));

        float theta = (float) Math.toDegrees(Math.atan2(touchMovementY,
                touchMovementX));

        if (radius > mInnerRadius) {

            arc = findArc(theta);
            setArc(arc);
        } else {
            desetArc();
        }

    }

    public void setArc(int region) {
        if (region != arc) {
            arc = region;
            invalidate();
        }
    }


//Ontouch ends

    public void desetArc() {
        arc = -1;
        invalidate();
    }

    public int findArc(float theta) {
        int nArcs = 10;
        float offset = (float) -(90.0 + 360.0 / (2 * nArcs));
        float relAngle = theta - offset;
        if (relAngle < 0) {
            relAngle = 360 + relAngle;
        }
        int region = (int) (relAngle * nArcs / 360);
        return region;
    }

    /*public void log_chars(char c){

//        //append & delete code for FT
             et1 = (EditText) mHostActivity.findViewById(R.id.editText1);
            et1.append(c+"");
            et1.getText().delete(et1.getText().length() - 1,
                    et1.getText().length());


    }*/



    /**
     * /**
     * Returns whether the CustomKeyboard is visible.
     */
    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /**
     * Make the CustomKeyboard visible, and hide the system keyboard for view v.
     */
    public void showCustomKeyboard(View v) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (v != null)
            ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * Make the CustomKeyboard invisible.
     */
    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        EditText edittext = (EditText) mHostActivity.findViewById(resid);

        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) showCustomKeyboard(v);
                else hideCustomKeyboard();
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        final Button next = (Button) mHostActivity.findViewById(R.id.nextText);
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);// Restore input type
                next.setEnabled(false);
                return true; // Consume touch event
            }
        });

        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    }

    /*public void key_touch(int r, int s, String label,int vibrate_time){
        Log.d(TAG,"On key touch");
        if (flag[r][s] == 0) {
            speakOut(label);
            mTouchlistener.myVib.vibrate(vibrate_time);
            CountDownTimer timer = new CountDownTimer(mTouchlistener.COUNT_DOWN_TIME *//*For how long should timer run*//*, 500 *//*time interval after which `onTick()` should be called*//*) {

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
    }*/
    /*public void assign_flag(int r,int s){
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

        View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
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
    }*/


}