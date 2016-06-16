package dev.tae.com.simplereactiveprogramming;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.tae.com.simplereactiveprogramming.contants.Constant;
import dev.tae.com.simplereactiveprogramming.contants.observers.Observer;
import dev.tae.com.simplereactiveprogramming.model.Model;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends AppCompatActivity {

    private Button goBtn;
    private ProgressBar progressBar;
    private static TextView tv_progress;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private RestAdapter.Builder restAdapter;
    private Model mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.pb_progress);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        goBtn = (Button) findViewById(R.id.btn_go);

        progressBar.setVisibility(View.INVISIBLE);
        tv_progress.setVisibility(View.INVISIBLE);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDialog();
            }
        });
    }

    private void downloadDialog() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();
        restAdapter = new RestAdapter.Builder();
        restAdapter.setEndpoint(Constant.baseURL())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();

        Observer api = restAdapter.build().create(Observer.class);
        compositeSubscription.add(api.fetchData()
                .observeOn(AndroidSchedulers.mainThread())
                        // .delay(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new rx.Observer<Model>() {
                               @Override
                               public void onNext(Model model) {
                                   mModel = model;

                               }

                               @Override
                               public void onError(Throwable e) {
                                   Log.wtf("onError", e.getMessage());
                                   progressBar.setVisibility(View.INVISIBLE);
                               }

                               @Override
                               public void onCompleted() {
                                   if ((mModel != null) && (mModel.getRelatedTopics().size() > 0)) {
                                       showProgressBar();
                                   }
                               }
                           }

                ));
    }

    private void showProgressBar() {

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(mModel.getRelatedTopics().size());
        tv_progress.setVisibility(View.VISIBLE);
        Thread r = new Thread() {

            @Override
            public void run() {
                super.run();
                int startSize = mModel.getRelatedTopics().size() / mModel.getRelatedTopics().size();
                int endPoint = mModel.getRelatedTopics().size();
                float progress;
                Bundle bundle = new Bundle();

                while (startSize <= endPoint) {

                    progress = (float) ((startSize *100)/endPoint);
                    try {
                        Thread.sleep(200);
                        Message msg = handle.obtainMessage();

                        bundle.putFloat("result", progress);
                        msg.setData(bundle);
                        msg.setTarget(handle);
                        handle.sendMessage(msg);
                    } catch (InterruptedException e) {
                    }

                    progressBar.setProgress(startSize);
                    System.out.println("startSize " + startSize);
                    System.out.println("EndPoint "+ endPoint);
                    System.out.println("currentState "+ progress);
                    startSize++;
                }
            }
        };
        r.start();
    }
   static Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if(bundle != null) {
                tv_progress.setText(bundle.getFloat("result")+ "%");
            }
        }
    };
}