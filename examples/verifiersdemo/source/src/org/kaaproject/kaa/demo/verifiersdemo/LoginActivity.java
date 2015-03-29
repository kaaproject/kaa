/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.verifiersdemo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends FragmentActivity {
  
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "01Y9gbsMeGPetye1w9kkNvNMi";
    private static final String TWITTER_SECRET = "g4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc";

    private static final String TAG = "Example-LoginsActivity";
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final String USER_INFO = "userInfo";
    private static final String EVENT_MESSAGES = "eventMessages";

    // These values are used to describe current connection status on UI.
    private static CharSequence curUserName;
    private static CharSequence curUserId;
    private static CharSequence curUserInfo;
    private static CharSequence eventMessagesText;

    // Text view fields where a connection status is shown.
    private TextView greetingTextView;
    private TextView idTextView;
    private TextView infoTextView;

    // A text edit field for the event message input.
    private EditText msgEdit;

    // A text edit field for all event messages.
    private EditText eventMessagesEdit;

    // Either an event is sent or received
    public enum EventStatus {RCVD, SENT};

    // Buttons used to connect to corresponding social networks.
    private SignInButton googleButton;
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;
    private Button sendEventButton;
    private boolean buttonEnabled;

    // Classes which handle specific actions of each button.
    private GplusSigninListener gplusSigninListener;
    private FacebookSigninListener facebookSigninListener;
    private TwitterSigninListener twitterSigninListener;

    // A Google API client which is used to establish a connection with Google
    // and access its API.
    private static GoogleApiClient mGoogleApiClient;

    // A Facebook UI helper class which is used for managing the login UI.
    private static UiLifecycleHelper uiHelper;

    // Is used to connect the Kaa client to the Kaa server, manage events and configurations.
    private static KaaClient kaaClient;

    private static VerifiersDemoEventClassFamily vdecf;

    // An event listener.
    private static KaaEventListener listener;

    public enum AccountType {GOOGLE, FACEBOOK, TWITTER};

    // Configuration which consists of the three default Kaa verifiers tokens:
    // for Google, Facebook and Twitter (Kaa Configuration is used).
    private static KaaVerifiersTokens verifiersTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_logins);
        greetingTextView = (TextView) findViewById(R.id.greeting);
        idTextView = (TextView) findViewById(R.id.idText);
        infoTextView = (TextView) findViewById(R.id.infoText);
        sendEventButton = (Button) findViewById(R.id.sendEventButton);
        sendEventButton.setEnabled(buttonEnabled);
        msgEdit = (EditText) findViewById(R.id.msgBox);
        eventMessagesEdit = (EditText) findViewById(R.id.eventMessages);

        // Resume the saved state (for example, after the screen rotation), if any.
        if (savedInstanceState != null) {
            curUserName = savedInstanceState.getCharSequence(USER_NAME);
            curUserId = savedInstanceState.getCharSequence(USER_ID);
            curUserInfo = savedInstanceState.getCharSequence(USER_INFO);
            eventMessagesText = savedInstanceState.getCharSequence(EVENT_MESSAGES);
            updateViews();
        }

        // Create a Twitter authConfig for Twitter credentials verification.
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        // Register the application with the help of Fabric plug-in for managing Twitter 
        // apps (and library dependencies).
        Fabric.with(this, new Twitter(authConfig));

        // Create the Twitter button.
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_sign_in_button);

        // Enable the Twitter button, even if the user is signed in.
        twitterButton.setEnabled(true);
        twitterSigninListener = new TwitterSigninListener(this);

        // Attach listeners needed to keep track of the connection.
        twitterButton.setCallback(twitterSigninListener);
        twitterButton.setOnClickListener(twitterSigninListener);

        // Create a listeners class for Google+.
        gplusSigninListener = new GplusSigninListener(this);

        googleButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        googleButton.setOnClickListener(gplusSigninListener);

        // Create the Google API client which is capable of making requests for tokens, user info etc.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(gplusSigninListener)
                .addOnConnectionFailedListener(gplusSigninListener)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        gplusSigninListener.setClient(mGoogleApiClient);

        // Create a listeners class for Facebook.
        facebookSigninListener = new FacebookSigninListener(this);
        facebookButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        facebookButton.setUserInfoChangedCallback(facebookSigninListener);
        
        // Create the UI helper for managing the Facebook login UI.
        uiHelper = new UiLifecycleHelper(this, facebookSigninListener);
        uiHelper.onCreate(savedInstanceState);

        sendEventButton.setOnClickListener(new SendEventButtonClickListener());

        KaaClientPlatformContext platformContext = new AndroidKaaPlatformContext(this);
        kaaClient = Kaa.newClient(platformContext);
        kaaClient.start();

        verifiersTokens = kaaClient.getConfiguration();
        Log.i(TAG, "Verifiers tokens: " + verifiersTokens.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       
        // Call corresponding onActivityResult methods for Facebook, Twitter and Google.
        uiHelper.onActivityResult(requestCode, resultCode, data);
        twitterButton.onActivityResult(requestCode, resultCode, data);
        gplusSigninListener.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save the current state of the UI (for Facebook).
        uiHelper.onSaveInstanceState(outState);
        outState.putCharSequence(USER_NAME, greetingTextView.getText());
        outState.putCharSequence(USER_ID, idTextView.getText());
        outState.putCharSequence(USER_INFO, infoTextView.getText());
        outState.putCharSequence(EVENT_MESSAGES, eventMessagesEdit.getText().toString());
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gplusSigninListener.onStop();
    }

    public void updateUI(String userName, String userId, String token, AccountType type) {
        String kaaVerifierToken = null;
        String userIdCopy = userId;
        switch (type) {
            case GOOGLE:
                kaaVerifierToken = verifiersTokens.getGoogleKaaVerifierToken();
                // Log out from Facebook (to make Log out button disappear)
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Google user name: " + userName;
                userId = "Google user id: " + userId;
                break;
            case FACEBOOK:
                kaaVerifierToken = verifiersTokens.getFacebookKaaVerifierToken();
                userName = "Facebook user name: " + userName;
                userId = "Facebook user id: " + userId;
                break;
            case TWITTER:
                kaaVerifierToken = verifiersTokens.getTwitterKaaVerifierToken();
                // Log out from Facebook (to make Log out button disappear)
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Twitter user name: " + userName;
                userId = "Twitter user id: " + userId;
                break;
            default:
                break;
        }

        // Update userName and userId shown on UI.
        curUserName = userName;
        curUserId = userId;
        curUserInfo = "Waiting for Kaa response...";
        updateViews();

        Log.i(TAG, "Attaching user...");
        kaaClient.attachUser(kaaVerifierToken, userIdCopy, token,
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse userAttachResponse) {
                        Log.i(TAG, "User was attached... " + userAttachResponse.toString());

                        if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS) {
                            Log.i(TAG, "Successful Kaa verification");
                            Log.i(TAG, userAttachResponse.toString());
                            curUserInfo = "Successful Kaa verification";
                            buttonEnabled = true;
                            updateViews();
                            EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                            vdecf = eventFamilyFactory.getVerifiersDemoEventClassFamily();

                            List<String> FQNs = new LinkedList<>();
                            FQNs.add("org.kaaproject.kaa.demo.verifiersdemo.MessageEvent");

                            kaaClient.findEventListeners(FQNs, new FindEventListenersCallback() {
                                @Override
                                public void onRequestFailed() {
                                    Log.i(TAG, "Find event listeners request has failed");
                                }
                                @Override
                                public void onEventListenersReceived(List<String> eventListeners) {
                                    Log.i(TAG, "Event listeners received: " + eventListeners);
                                }
                            });
                            if (listener != null) {
                                vdecf.removeListener(listener);
                            }
                            listener = new KaaEventListener();
                        } else {
                            String failureString = userAttachResponse.getErrorReason() == null ?
                                    userAttachResponse.getErrorCode().toString() :
                                    userAttachResponse.getErrorReason().toString();
                            Log.i(TAG, "Kaa verification failure: " + failureString);
                            curUserInfo = "Kaa verification failure: " + failureString;
                            buttonEnabled = false;
                            updateViews();
                        }
                    }
                });
    }

    // Update text view fields, text edit fields and the Send button state.
    private void updateViews() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                greetingTextView.setText(curUserName);
                idTextView.setText(curUserId);
                infoTextView.setText(curUserInfo);
                if (eventMessagesText != null) {
                    eventMessagesEdit.setText(eventMessagesText);
                }
                sendEventButton.setEnabled(buttonEnabled);
            }
        });
    }

    // When the Send button is clicked.
    private class SendEventButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String message = msgEdit.getText().toString();
            prependToChatBox(EventStatus.SENT, message);
            if (message != null && message.length() > 0) {
                Log.i(TAG, "Sending event: " + message);
                vdecf.sendEventToAll(new MessageEvent(message));
            }
        }
    }

    private void prependToChatBox(final EventStatus status, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ");
                if (message != null && message.length() > 0) {
                    eventMessagesEdit.setText(status + " " + sdf.format(cal.getTime()) +
                            ": " + message + "\n" + eventMessagesEdit.getText());
                    eventMessagesText = eventMessagesEdit.getText();
                    msgEdit.setText(null);
                }
            }
        });
    }

    // A class which is used to handle events.
    private class KaaEventListener implements VerifiersDemoEventClassFamily.Listener {
        @Override
        public void onEvent(MessageEvent messageEvent, String sourceEndpoint) {
            Log.i(TAG, "Event was received: " + messageEvent.getMessage());
            prependToChatBox(EventStatus.RCVD, messageEvent.getMessage());
        }
    }

    // Detach the endpoint from the user.
    private void logout() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(kaaClient.getEndpointKeyHash());
        kaaClient.detachEndpoint(endpointKey, new OnDetachEndpointOperationCallback() {
            @Override
            public void onDetach(SyncResponseResultType arg0) {
                Log.i(TAG, "User was detached");
            }
        });
    }
}
