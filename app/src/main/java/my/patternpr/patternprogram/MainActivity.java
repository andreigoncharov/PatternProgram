package my.patternpr.patternprogram;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private String vidAddress = "http://video.pr0gram1.xyz/video/patuern/patuern.m3u8";

    private CountryCodePicker ccp;
    private WebView webView;

    private SimpleExoPlayer player;
    private PlayerView playerView;

    private String site = "rtgefvgepat";
    private String landing = "60704a4b86a81e0026aa14a9";

    private RelativeLayout first_screen, second_screen, third_screen;
    private EditText input1, input2, input3, input4;
    private TextView finalText1, finalText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inits
        webView = findViewById(R.id.webView);

        playerView = findViewById(R.id.my_video);
        play();

        first_screen = findViewById(R.id.first_screen);
        second_screen = findViewById(R.id.second_screen);
        third_screen = findViewById(R.id.third_screen);

        input1 = findViewById(R.id.input1);
        input2 = findViewById(R.id.input2);
        input3 = findViewById(R.id.input3);
        input4 = findViewById(R.id.input4);
        ccp = findViewById(R.id.ccp);

        finalText1 = findViewById(R.id.finalText1);
        finalText2 = findViewById(R.id.finalText2);

        FirebaseMessaging.getInstance().subscribeToTopic("patternprogramNoReg");

        //voids
        getCountryCode();
    }


    public void getCountryCode(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://ip-api.com/json/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            ccp.setCountryForNameCode(jsonObject.get("countryCode").toString());
                        } catch (JSONException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    private void play() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.my_video);
        playerView.setPlayer(player);
        Uri uri = Uri.parse(vidAddress);
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "app-name"));
// Create a HLS media source pointing to a playlist uri.
        HlsMediaSource hlsMediaSource =
                new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);


        player.prepare(hlsMediaSource);
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);
    }


    public String sendUserDataToCRM(String first_name, String last_name, String email, String phone, String country) throws IOException, JSONException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String params = String.format("first_name=%s&last_name=%s&email=%s&phone=%s&country=%s&site=%s&landing=%s",
                first_name, last_name, email, phone, country, site, landing);
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://getyourapi.site/api/leads/google")
                .method("POST", body)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        JSONObject Jobject = new JSONObject(jsonData);
        JSONObject Jobject1 = new JSONObject(Jobject.getString("data"));
        try{
            return Jobject1.getString("redirectUrl");
        }
        catch (Exception e){
            return "null";
        }

    }

    public void newScreenFunc(View view) {
        first_screen.setVisibility(View.INVISIBLE);
        second_screen.setVisibility(View.VISIBLE);
        player.pause();
    }

    public void newScreenFunc2(View view) throws IOException, JSONException {
        if (check()){
            second_screen.setVisibility(View.INVISIBLE);
            third_screen.setVisibility(View.VISIBLE);
            String name=input1.getText().toString();
            String second_name = input2.getText().toString();
            finalText1.setText(name);
            finalText2.setText(second_name);


            Thread thread2 = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        String responce = sendUserDataToCRM(input1.getText().toString().trim(),input2.getText().toString().trim(),
                                input3.getText().toString().trim(),
                                ccp.getSelectedCountryCode().toString()+input4.getText().toString().trim(), ccp.getSelectedCountryNameCode());

                        if(!responce.equals("null")){
                            goToVebView(responce);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread2.start();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("patternprogramNoReg");
        }
    }

    public boolean check() throws IOException, JSONException {
        final boolean[] ch = {true};
        EditText input1 = findViewById(R.id.input1);
        EditText input2 = findViewById(R.id.input2);
        EditText input3 = findViewById(R.id.input3);
        final EditText input4 = findViewById(R.id.input4);

        if(input1.getText().toString().isEmpty()){
            input1.setError("Wpisz imię");
            ch[0] =  false;
        }
        if(input2.getText().toString().isEmpty()){
            input2.setError("Wpisz nazwisko");
            ch[0] =  false;
        }
        if(input3.getText().toString().isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(input3.getText().toString()).matches()){
            input3.setError("Wpisz swój e-mail");
            ch[0] =  false;
        }
        if(input4.getText().toString().isEmpty()){
            input4.setError("Wpisz swój numer telefonu");
            ch[0] =  false;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    CountryCodePicker ccp = (CountryCodePicker)  findViewById(R.id.ccp);
                    if(!validatePhone(ccp.getSelectedCountryCodeWithPlus().toString()+input4.getText().toString().trim())){
                        input4.setError("Numer telefonu nie został zweryfikowany");
                        ch[0] = false;
                        Log.i("Request", "ch: "+ch[0]);
                    }
                } catch (Exception e) {
                    Log.i("Request", "Error: "+e.getMessage());
                }
            }
        });

        thread.start();
        Log.i("Request", "ck1 "+ch[0]);

        return ch[0];
    }

    public boolean validatePhone(String phone) throws IOException, JSONException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String params = String.format("phone=%s", phone);
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),  params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://getyourapi.site/api/phone/validate")
                .method("POST", body)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        JSONObject Jobject = new JSONObject(jsonData);
        JSONObject Jobject1 = new JSONObject(Jobject.getString("data"));
        Log.i("Request", "Responce: "+Jobject + params);
        return Jobject1.getString("valid").equals("true");
    }

    public void goToVebView(String url){
        webView.setWebViewClient(new MyWebViewClient());
        webView.setVisibility(View.VISIBLE);
        // включаем поддержку JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        // указываем страницу загрузки
        webView.loadUrl(url);
    }

    private static class MyWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        // Для старых устройств
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
