package com.codemunger.fbstuff;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    CallbackManager callbackManager;
    SignInButton mSign;
    TextView mText;
    AccessToken mAccessToken;
    AccessTokenTracker accessTokenTracker;
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;

    private boolean signedInUser;

    private ConnectionResult mConnectionResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                Log.d("facebook","current access token");
                mAccessToken = currentAccessToken == null ? oldAccessToken :currentAccessToken;
                updateProfile();

            }
        };
        setContentView(R.layout.activity_main);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        mText = (TextView) findViewById(R.id.text);

        mSign = (SignInButton)findViewById(R.id.signin);
        mSign.setOnClickListener(this);

        // If using in a fragment
        //loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("facebook", "got result");


            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    private void updateProfile() {

        if(mAccessToken != null) {
            Log.d("facebook","am i here");
            queryFacebook();
        }
    }

    protected void queryFacebook  () {
        GraphRequest request = GraphRequest.newMeRequest(
                mAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                            Log.d("facebook", response.toString());

                            if(object != null){
                                updateData(object);
                            }                       // Application code
                    }


                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,birthday,gender,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 12) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
           Log.d("wtfs","wtfdsfsdfs");

            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    protected void updateData(JSONObject data) {


        try {
            Log.d("facebook",data.toString());

        }
        catch (Exception e) {
            Log.d("facebook","exception:"+e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        googlePlusLogin();
    }

    private void resolveSignInError() {

        if (mConnectionResult.hasResolution()) {

            try {

                mIntentInProgress = true;

                mConnectionResult.startResolutionForResult(this, 12);

            } catch (IntentSender.SendIntentException e) {

                mIntentInProgress = false;

                mGoogleApiClient.connect();

            }

        }

    }


    private void googlePlusLogin() {


            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, 12);


    }

    @Override
    protected void onDestroy() {

        Log.d("facebook","destroy");
        accessTokenTracker.stopTracking();
        super.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
        signedInUser = false;

        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {

            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();


            if (!mIntentInProgress) {

                mConnectionResult = connectionResult;

                if (signedInUser) {

                    resolveSignInError();

                }

            }

        }
    }
}
