package com.mp.unityandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;

public class MainActivity extends UnityPlayerActivity {

    // 유니티에서 스크립트가 붙을 오브젝트 이름
    private String UnityObjName = "pluginUnity";
    // 유니티에서 에러메세지 받을 함수 이름
    private String UnityMsg = "msgUnity";
    //유니티에서 음성인식 결과받을 함수 이름
    private String UnitySTTresult = "sttUnity";
    // 핸들러 관련
    private final int STTSTART = 1;
    private final int STTREADY = 2;
    private final int STTEND = 3;
    private msgHandle mHandler = null;
    // 음성인식 관련
    private SpeechRecognizer mRecognizer;
    private static Context context;
    private String recogLang = "en-US";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT).show();
//        getApplicationContext().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("call:010-4398-0100")));

        //핸들러 붙이고
        mHandler = new msgHandle();

        //음성인식 리스너등록합니다.
        context = getApplicationContext();
        if(mRecognizer == null){
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mRecognizer.setRecognitionListener(listener);
        }
        Log.e("TAGGER", "OnCreate");
    }

    class msgHandle extends Handler{
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case STTSTART:
                    //실제 음성인식을 작동시킬 것입니다.
                    StartSpeechRecoService();
                    Log.e("Gary", "Android : STTStart");
                    break;
                case STTREADY:
                    // 유니티에 음성인식이 실제로 시작했다는 메세지를 보냅니다.
                    UnityPlayer.UnitySendMessage(UnityObjName, UnityMsg, "START");
                    Log.e("Gary", "Android : STTReady");
                    break;
                case STTEND:
                    // 유니티에 음성인식을 종료했다는 메세지를 보냅니다.
                    UnityPlayer.UnitySendMessage(UnityObjName, UnityMsg, "END");
                    Log.e("Gary", "Android : STTSend");
                    break;
            }
        }
    }


    // 유니티에서 호출할 함수입니다.
    public void StartSpeechReco(String Lang) {
    // "en-US" : 미국영어 / "ko" : 한국어 /  "zh-CN" : 중국어 /  "ja" : 일본어
        recogLang = Lang;
        EndSpeechReco();
        //핸들러에 시작을 알리고 있습니다.
        try {
            Message msg1 = new Message();
            msg1.what = STTSTART;
            mHandler.sendMessage(msg1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 실제 음성인식을 호출합니다. recogLang은 음성인식할 언어로 주시면됩니다.
    public void StartSpeechRecoService(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // 음성인식
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, recogLang);
        mRecognizer.startListening(i);
    }

    //메세지 초기화
    public void EndSpeechReco() {
        this.mHandler.removeMessages(STTREADY);
        this.mHandler.removeMessages(STTEND);
        this.mHandler.removeMessages(STTSTART);
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            // ★ 음성인식이 실제로 작동하면 여기가 호출됩니다.
            mHandler.sendEmptyMessage(STTREADY);
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            // ★ 음성인식이 끝났다는 거구요
            mHandler.sendEmptyMessage(STTEND);
        }

        //★ 음성인식이 실패하면 나오는 에러입니다.
            // 스피치 타임아웃은 실제 음성 입력이 대기시간내에 없을 경우입니다.
            // 노매치는 음성인식 결과가 없을 경우입니다.
            // 레코그나이저비지는 음성인식 서버가 바쁘다고 합니다.
        @Override
        public void onError(int error) {
            String errMsg = "";
            switch (error) {
                case 1: errMsg = "ERROR_NETWORK_TIMEOUT"; break;
                case 2: errMsg = "ERROR_NETWORK"; break;
                case 3: errMsg = "ERROR_AUDIO"; break;
                case 4: errMsg = "ERROR_SERVER"; break;
                case 5: errMsg = "ERROR_CLIENT"; break;
                case 6: errMsg = "ERROR_SPEECH_TIMEOUT"; break;
                case 7: errMsg = "ERROR_NO_MATCH"; break;
                case 8: errMsg = "ERROR_RECOGNIZER_BUSY"; break;
            }
            try{
                UnityPlayer.UnitySendMessage(UnityObjName, UnityMsg, errMsg);
            }
            catch (Exception e) {
            }
        //★ 핸들러 메세지 초기화
            EndSpeechReco();
        }

        //★ 음성인식 결과입니다. matches는 음성인식결과가 배열로 여러개 들어오는데 그중에 첫번째 것만 사용합니다.
        @Override
        public void onResults(Bundle bundle) {
            mHandler.removeMessages(0);
            ArrayList matches = bundle.getStringArrayList("results_recognition");
            if(matches != null){
                try{
                    UnityPlayer.UnitySendMessage(UnityObjName, UnitySTTresult,((String)matches.get(0)));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }; // ★ 음성인식 리스너 여기까지입니다.
}
