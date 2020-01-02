package com.yama.networkstate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

// Interface Declaration
interface MainActivityDataTaskNotification {
    void notifyMainActivity(String connStatus, String connInstruction);
}


public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    // Activity declaration of textviews
    TextView connectivityStatus;
    TextView connectivityInstruction;
    private String TAG = "NetworkState";
    private NetworkStateReceiver networkStateReceiver;      // Receiver that detects network state changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startNetworkBroadcastReceiver(this);

        // Assignment of the Textviews from the user interface. (See activity_main.xml)
        connectivityStatus =  findViewById(R.id.connectionStatus);
        connectivityInstruction =  findViewById(R.id.requestContainerInstruction);
    }


    @Override
    protected void onPause() {
        /***/
        unregisterNetworkBroadcastReceiver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        /***/
        registerNetworkBroadcastReceiver(this);
        super.onResume();
    }

    @Override
    public void networkAvailable() {
        Log.i(TAG, "networkAvailable()");
        //Proceed with online actions in activity (e.g. hide offline UI from user, start services, etc...)
        MakeRequestToUpdateView();
    }

    @Override
    public void networkUnavailable() {
        Log.i(TAG, "networkUnavailable()");
        //Proceed with offline actions in activity (e.g. sInform user they are offline, stop services, etc...)
        MakeRequestToUpdateView();
    }

    public void startNetworkBroadcastReceiver(Context currentContext) {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener((NetworkStateReceiver.NetworkStateReceiverListener) currentContext);
        registerNetworkBroadcastReceiver(currentContext);
    }

    /**
     * Register the NetworkStateReceiver with your activity
     * @param currentContext
     */
    public void registerNetworkBroadcastReceiver(Context currentContext) {
        currentContext.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     Unregister the NetworkStateReceiver with your activity
     * @param currentContext
     */
    public void unregisterNetworkBroadcastReceiver(Context currentContext) {
        currentContext.unregisterReceiver(networkStateReceiver);
    }
    /**
     * Callback method for the onClick of the @+id/requestData button in activity_main.xml
     *
     * @param view : the view from the activity, in this case the button.
     *
     */
    public void requestDataCallback(View view) {

        MakeRequestToUpdateView();
    }

    private void MakeRequestToUpdateView() {
        try {
            String connectionInstruction = "";
            String connectionStatus = "";
            if (isNetworkReachable()) {
                DataTask dataTask = new DataTask(mainActivityDataTaskNotification);

                connectionInstruction = dataTask.execute("https://httpbin.org/get?arg1=1&arg2=2").get();
                connectionStatus = "Connection Status: Online";
            } else {
                connectionInstruction = "Please check your internet connection and try your request again.";
                connectionStatus = "Connection Status: Offline";
            }

            mainActivityDataTaskNotification.notifyMainActivity(connectionStatus, connectionInstruction);
        } catch (Exception e) {
            String connectionInstruction = e.getMessage();
            String connectionStatus = "";
            mainActivityDataTaskNotification.notifyMainActivity(connectionStatus, connectionInstruction);
        }
    }

    /**
     * ConnectivityManager : Class that answers queries about the state of network connectivity.
     * It also notifies applications when network connectivity changes.
     * @url  https://developer.android.com/reference/android/net/ConnectivityManager
     */
    private boolean isNetworkReachable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * MainActivityDataTaskNotification (Closure) : Interface Method.
     */
    MainActivityDataTaskNotification mainActivityDataTaskNotification = new MainActivityDataTaskNotification() {
        @Override
        public void notifyMainActivity(String connStatus, String connInstruction) {
            connectivityInstruction.setText(connInstruction);
            connectivityStatus.setText(connStatus);
        }
    };

}