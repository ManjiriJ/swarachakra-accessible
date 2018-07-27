package game.Typing;

/**
 * Created by IDIN on 14-Jun-18.
 */

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import static game.Typing.R.id.textView;

public class KB extends FragmentActivity implements View.OnClickListener {

    Vibrator myVib;
    String[][] word_phrase;
    String value;
    TextView training,word_typed;
    TextToSpeech tts3;
    EditText typing;
    Button nexttext;
    int len;
    boolean leftflag = false,rightflag=false;
    long first_char_time,last_char_time,session_start_time,session_end_time;
    Bundle b;
    Database DB;
    SessionDetailsTable sDB;
    //int userId, sessionId,continueFrom;
    String kbname, imename;
    File dir, file;
    //String filePath;
    String log, oldText;
    //

    //DamerauLevenshteinAlgorithm editDistance = new DamerauLevenshteinAlgorithm(1,1,1,1);
    public static CustomKeyboard mCustomKeyboard;
    //Text to Speeches intialisation
    TextToSpeech.OnInitListener onInit = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {

            if (status == TextToSpeech.SUCCESS) {
                final Locale loc = new Locale("hin", "IND");
                int result = tts3.setLanguage(loc);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d("TTS", "This Language is not supported");
                } else {

//                    speakOut(keyCodelabel);
                }

            } else {
                Log.d("TTS", "Initilization Failed!");
            }
        }
    };
    public void speakOut_training(String keyCodelabel) {
//        if (!isAccessibilitySettingsOn(mHostActivity.getApplicationContext())) {
        tts3.setPitch(1);
        tts3.speak(keyCodelabel, TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (hasFocus) {
                this.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        tts3 = new TextToSpeech(this, onInit);
        mCustomKeyboard= new CustomKeyboard(this, null,R.id.keyboardview, R.xml.modified);
        mCustomKeyboard.registerEditText(R.id.editText1);
        this.addContentView(mCustomKeyboard, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        myVib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //FileOperations.write("##onCreate() in Training");

    }

    int f[]=new int[2];

    /**
     * Touch Event Used for detecting
     * 1.Top left corner touch
     * 2.Top right corner touch
     * */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", "true");
        int maskedAction = event.getActionMasked();
        float x1,y1;
        x1 = event.getX();
        y1 = event.getY();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        Log.d("dimensions",height+","+width);

        switch(maskedAction){
            case MotionEvent.ACTION_DOWN:
                if(y1<height/	6){
                    //top left corner
                    if(x1<width/3){
//						getActionBar().hide();
                        if(f[0]==0){

                            String text = training.getText().toString();
                            speakOut_training(text);
                            f[0]=1;
                            f[1]=0;
                            myVib.vibrate(50);
                        }

                    }
                    //top right corner
                    else if(x1 >2*width/3 && x1 <width){
                        if(f[1]==0){
                            String text = typing.getText().toString();
                            speakOut_training(text);
                            f[1]=1;
                            f[0]=0;
                            myVib.vibrate(50);
                        }

                    }

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(y1<height/6){
                    //top left corner
                    if(x1<width/3){
//						getActionBar().hide();
                        if(f[0]==0){
                            String text = training.getText().toString();
                            speakOut_training(text);
                            f[0]=1;
                            f[1]=0;
                            myVib.vibrate(50);
                        }

                    }
                    //top right corner
                    else if(x1 >2*width/3 && x1 <width){
                        if(f[1]==0){
                            String text = typing.getText().toString();
                            speakOut_training(text);
                            f[1]=1;
                            f[0]=0;
                            myVib.vibrate(50);
                        }

                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                f[0]=0;
                f[1]=0;
                break;
        }
        return false;
    }

    public void onBackPressed() {
        // TODO Auto-generated method stub
//		super.onBackPressed();
        Log.d("logger", "Do nothing");
        mCustomKeyboard.hideCustomKeyboard();
        nexttext.setEnabled(true);
        FileOperations.write("##onBackPressed()  in Training");
    }

    /*@Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        FileOperations.write("##onRestart() in Training");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        FileOperations.write("##onResume() in Training");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        FileOperations.write("##onPause() in Training");
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        FileOperations.write("##onStop() in Training");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        FileOperations.write("##onDestroy() in Training");
    }
*/
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        //FileOperations.write("##onStart() in Training");
        this.setTitle(R.string.write_this_word);
        /*log = "";
        oldText = "";
        session_start_time =0;
        session_end_time = 0;
        first_char_time = 0;
        last_char_time = 0;*/

        /*prgUserData = new ProgressDialog(this);
        prgUserData.setMessage(getString(R.string.please_wait_uploading));
        prgUserData.setCancelable(false);*/

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
		/*ActionBar actionBar = getActionBar();
		actionBar.hide();*/

        /*DB = new Database(this);
        sDB = new SessionDetailsTable(this);
        alt = new AlternativesParser(this);*/

        nexttext = (Button) findViewById(R.id.nextText);
        /*training = (TextView) findViewById(R.id.textView1);*/
        word_typed =(TextView) findViewById(textView);
        word_typed.setTextColor(Color.DKGRAY);
        typing = (EditText) findViewById(R.id.editText1);
        //typing.setLongClickable(false);
        typing.setText("");

        typing.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                //typing.setError(null);
                //.//String str;
                String str = typing.getText().toString();


               /* if (session_start_time == 0) {
                    session_start_time = System.currentTimeMillis();
                    session_end_time = System.currentTimeMillis();
                    //sDB.updateTrainingEntry(userId, sessionId, 0, session_start_time, session_end_time, SessionDetailsTable.SESSION_STATUS_STARTED);
                    //FileOperations.write("SessionTable updated: uid-" + userId + " , session id-" + sessionId + " , session rating" + 0 + " , session start time-" + session_start_time + " , session end time-" + session_end_time + " , session status -" + SessionDetailsTable.SESSION_STATUS_STARTED);
                }*/
                float time = Math.round((last_char_time - first_char_time)/60000.0);
                Log.d("logger","Time taken: "+time);
                if(time>0) {
                    int cpm = (int)Math.ceil((str.length() - 1) / (time));
                    word_typed.setText("CPM: "+cpm);
                    Log.d("logger","CPM: "+cpm);
                }


                if (first_char_time == 0) {

                    //if we are about to begin typing a new word and
                    //if default KB is not the one assigned to the user don't let the session start
                    //timings will not be affected

//					String currentDefaultKB = Settings.Secure.getString(
//							getContentResolver(),
//							Settings.Secure.DEFAULT_INPUT_METHOD
//							);

//					if(currentDefaultKB.equals(imename)==false)
//					{
//						//typing.setText("");
//						Toast.makeText(getApplicationContext(), "Default keyboard should be "+kbname, Toast.LENGTH_LONG).show();
//						getApplicationContext();
//						InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.showInputMethodPicker();
//
//						return;
//					}

                    first_char_time = System.currentTimeMillis();

                }
                last_char_time = System.currentTimeMillis();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
				/*if(oldText.compareToIgnoreCase(String.valueOf(s))!=0){
					Log.d("detailedLogger:before", String.valueOf(s) + "start:" + start + ", how many:" + count);
					log += System.currentTimeMillis() + " ; " + String.valueOf(s)+"\n";
					oldText = String.valueOf(s);
				}*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        typing.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //sendMessage();
                    Log.d("debug","txtbox says done");
                    nexttext.performClick();
                    handled = true;
                }
                return handled;
            }

        });


        nexttext.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String typedString = typing.getText().toString();
        ClipData clip = ClipData.newPlainText("copied text", typedString);
        clipboard.setPrimaryClip(clip);


        session_end_time = System.currentTimeMillis();
        //String typedText = typing.getText().toString().trim();
        boolean altMatch = false;
        //int index = continueFrom -1;
        /*String typedText = typing.getText().toString().trim();
        String presentedText = word_phrase[index][0].trim();*/




    }

}
