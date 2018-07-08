/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.pluginapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.VideoCapturer;

import java.io.IOException;

import jp.shiguredo.sora.sdk.channel.SoraMediaChannel;
import jp.shiguredo.sora.sdk.channel.data.ChannelAttendeesCount;
import jp.shiguredo.sora.sdk.channel.option.SoraMediaOption;
import jp.shiguredo.sora.sdk.channel.option.SoraVideoOption;
import jp.shiguredo.sora.sdk.channel.signaling.message.PushMessage;
import jp.shiguredo.sora.sdk.error.SoraErrorReason;
import jp.shiguredo.sora.sdk.util.SoraLogger;

public class MainActivity extends PluginActivity {


    private SoraMediaChannel.Listener listener = new SoraMediaChannel.Listener() {

        @Override
        public void onRemoveRemoteStream(SoraMediaChannel soraMediaChannel, String s) {
            Log.d("MyPlugin", "onRemoveRemoteStream");
            /* do nothing */
        }

        @Override
        public void onPushMessage(SoraMediaChannel soraMediaChannel, PushMessage pushMessage) {
            Log.d("MyPlugin", "onPushMessage");
            /* do nothing */
        }

        @Override
        public void onAttendeesCountUpdated(SoraMediaChannel soraMediaChannel, ChannelAttendeesCount channelAttendeesCount) {
            /* do nothing */
        }

        @Override
        public void onAddRemoteStream(SoraMediaChannel soraMediaChannel, MediaStream mediaStream) {
            Log.d("MyPlugin", "onAddRemoteStream");
            /* do nothing */
        }

        @Override
        public void onAddLocalStream(SoraMediaChannel soraMediaChannel, MediaStream mediaStream) {
            Log.d("MyPlugin", "onAddLocalStream");
        }

        @Override
        public void onError(SoraMediaChannel soraMediaChannel, SoraErrorReason soraErrorReason) {
            //notificationLedBlink(LedTarget.LED3, LedColor.RED, 1000);
            Log.w("MyPlugin", "onError:" + soraErrorReason.toString());
            runOnUiThread(() -> {
                notificationError("");
            });
            closeSora();
        }

        @Override
        public void onClose(SoraMediaChannel soraMediaChannel) {
            Log.d("MyPlugin", "onClose");
            closeSora();
        }

        @Override
        public void onConnect(SoraMediaChannel soraMediaChannel) {
            Log.d("MyPlugin", "onConnect");
            /* do nothing */
            runOnUiThread(() -> {
                notificationLed3Show(LedColor.CYAN);
            });
            startCapturer();
        }

    };

    private SoraMediaChannel sora = null;
    private EglBase egl;
    private VideoCapturer capturer;

    // XXX: この３つは指定しても意味なかった
    private int videoWidth  = 2400;
    private int videoHeight = 1200;
    private int FPS = 15;

    private void closeSora() {
        if (sora != null) {
            sora.disconnect();
            sora = null;
            playStopSound();
        }
        stopCapturer();
        runOnUiThread(() -> {
            notificationLed3Show(LedColor.BLUE);
            notificationLedHide(LedTarget.LED7);
        });
    }

    private HandlerThread soraThread;
    private Handler soraHandler;


    private void switchSora() {
        Log.i("MyPlugin", "Switch Sora");
        if (sora == null) {

            Log.i("MyPlugin", "SORA ON");

            String endpoint  = getSignalingEndpoint();
            String channelId = getChannelId();
            String metadata  = getMetadata();

            SoraMediaOption option = new SoraMediaOption();

            option.enableMultistream();
            // XXX Audioがまだうまくいってない。雑音になってしまう。
            //option.enableAudioUpstream();
            option.enableVideoUpstream(capturer, egl.getEglBaseContext(), false);
            option.setVideoCodec(SoraVideoOption.Codec.VP9);

            sora = new SoraMediaChannel(
                    this,
                    endpoint,
                    channelId,
                    metadata,
                    option,
                    10,
                    listener);

            sora.connect();

            playStartSound();

            runOnUiThread(() -> {
                notificationLed3Show(LedColor.YELLOW);
                notificationLedShow(LedTarget.LED7);
            });

        } else {
            Log.i("MyPlugin", "SORA OFF");
            closeSora();
        }
    }

    private String getSignalingEndpoint() {
        /*
         * XXX
         * 本来ならConfigServerで設定されたデータを取得する
         */
        return "wss:/example.org/signaling";
    }

    private String getChannelId() {
        /*
         * XXX
         * 本来ならConfigServerで設定されたデータを取得する
         */
        return "my_channel";
    }

    private String getMetadata() {
        /*
         * XXX
         * 本来ならConfigServerで設定されたデータを取得する
         */
        return "my_metadata";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationLedShow(LedTarget.LED5);
        notificationLedShow(LedTarget.LED6);

        lockCamera();

        soraThread = new HandlerThread("sora");
        soraThread.start();
        soraHandler = new Handler(soraThread.getLooper());

        SoraLogger.Companion.setEnabled(true);

        capturer = new ThetaCameraCapturer(ThetaCameraCapturer.ShootingMode.Live640);
        egl      = EglBase.create();

        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    Log.d("MyPlugin", "CAMERA BUTTON PRESSED UP");
                    soraHandler.post(() -> {
                        switchSora();
                    });
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
            }
        });
        startConfigServer();
    }

    private ConfigServer configServer;

    private void startConfigServer() {
        Log.d("MyPlugin", "startConfigServer");
        if (configServer != null) {
            return;
        }

       configServer = new ConfigServer();
        try {
            configServer.start();
        } catch (IOException e) {
            Log.w("MyPlugin", "failed to start config server");
            e.printStackTrace();
        }
    }

    private void stopConfigServer() {
        if (configServer != null) {
            configServer.stop();
            configServer = null;
        }
    }

    private void startCapturer() {
        Log.d("MyPlugin", "startCapturer");
        // XXX ここで指定している数値は今回は意味がない。
        //     ThetaCameraCapturer側のShootingModeでサイズが決まる
        capturer.startCapture(videoWidth, videoHeight, FPS);
    }

    private void stopCapturer() {
        Log.d("MyPlugin", "stopCapturer");
        try {
            capturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        Log.d("MyPlugin", "onPause");
       super.onPause();
    }

    private void lockCamera() {
        runOnUiThread(() -> {
            Intent intent = new Intent("com.theta360.plugin.ACTION_MAIN_CAMERA_CLOSE");
            sendBroadcast(intent);
        });
    }

    private void unlockCamera() {
        runOnUiThread(() -> {
            Intent intent = new Intent("com.theta360.plugin.ACTION_MAIN_CAMERA_OPEN");
            sendBroadcast(intent);
        });
    }

    private void playStartSound() {
        runOnUiThread(() -> {
            Intent intent = new Intent("com.theta360.plugin.ACTION_AUDIO_MOVSTART");
            sendBroadcast(intent);
        });
    }

    private void playStopSound() {
        runOnUiThread(() -> {
            Intent intent = new Intent("com.theta360.plugin.ACTION_AUDIO_MOVSTOP");
            sendBroadcast(intent);
        });
    }

    @Override
    public void onDestroy() {
        Log.d("MyPlugin", "onDestroy");
        closeSora();
        capturer.dispose();
        unlockCamera();
        egl.release();
        soraThread.quit();
        stopConfigServer();
        super.onDestroy();
    }
}

