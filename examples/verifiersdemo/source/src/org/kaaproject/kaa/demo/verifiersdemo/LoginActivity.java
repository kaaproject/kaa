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
import org.kaaproject.kaa.client.event.FetchEventListeners;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily.DefaultEventFamilyListener;

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

    // these values are used to describe current connection status on UI
    private static CharSequence curUserName;
    private static CharSequence curUserId;
    private static CharSequence curUserInfo;
    private static CharSequence eventMessagesText;

    // text views, where connection status is shown
    private TextView greetingTextView;
    private TextView idTextView;
    private TextView infoTextView;

    // text edit for event message input
    private EditText msgEdit;

    // text edit for all events messages
    private EditText eventMessagesEdit;

    // either event is sent or it is received
    public enum EventStatus {RCVD, SENT};

    // buttons used to connect to corresponding social networks
    private SignInButton googleButton;
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;
    private Button sendEventButton;
    private boolean buttonEnabled;

    // classes, handling each button's specific actions
    private GplusSigninListener gplusSigninListener;
    private FacebookSigninListener facebookSigninListener;
    private TwitterSigninListener twitterSigninListener;

    // Google API client, which is used to establish connection with Google
    // and access its API
    private static GoogleApiClient mGoogleApiClient;

    // Facebook UI helper class, used for managing login UI
    private static UiLifecycleHelper uiHelper;

    // Kaa endpoint registration manager, responsible for attaching users to
    // endpoints
    private static EndpointRegistrationManager endpointRegistrationManager;

    // Is used to attach to Kaa server, manage events or configurations
    private static KaaClient kaaClient;

    private static VerifiersDemoEventClassFamily vdecf;

    // Events listener
    private static KaaEventListener listener;

    public enum AccountType {GOOGLE, FACEBOOK, TWITTER};

    // Configuration, which consists of three default Kaa verifiers tokens:
    // for Google, Facebook and Twitter (Kaa Configuration is used)
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

        // Resume saved state (i.e. after screen rotation), if any
        if (savedInstanceState != null) {
            curUserName = savedInstanceState.getCharSequence(USER_NAME);
            curUserId = savedInstanceState.getCharSequence(USER_ID);
            curUserInfo = savedInstanceState.getCharSequence(USER_INFO);
            eventMessagesText = savedInstanceState.getCharSequence(EVENT_MESSAGES);
            updateViews();
        }

        // Twitter authConfig for Twitter credentials verification
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        // Register application with the help of Fabric plug-in for managing Twitter 
        // apps (and library dependencies) register application
        Fabric.with(this, new Twitter(authConfig));

        // Create Twitter button
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_sign_in_button);

        // Enable button, even if a user is signed-in
        twitterButton.setEnabled(true);
        twitterSigninListener = new TwitterSigninListener(this);

        // Attach listeners needed to keep track of connection
        twitterButton.setCallback(twitterSigninListener);
        twitterButton.setOnClickListener(twitterSigninListener);

        // create listeners class for Google+
        gplusSigninListener = new GplusSigninListener(this);

        googleButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        googleButton.setOnClickListener(gplusSigninListener);

        // Google API client, which is capable of making requests for tokens, user info etc.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(gplusSigninListener)
                .addOnConnectionFailedListener(gplusSigninListener)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        gplusSigninListener.setClient(mGoogleApiClient);

        // create listeners class for Facebook
        facebookSigninListener = new FacebookSigninListener(this);
        facebookButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        facebookButton.setUserInfoChangedCallback(facebookSigninListener);
        // UI helper is used for managing Facebook's log in UI
        uiHelper = new UiLifecycleHelper(this, facebookSigninListener);
        uiHelper.onCreate(savedInstanceState);

        sendEventButton.setOnClickListener(new SendEventButtonClickListener());

        KaaClientPlatformContext platformContext = new AndroidKaaPlatformContext(this);
        kaaClient = Kaa.newClient(platformContext);
        kaaClient.start();

        verifiersTokens = kaaClient.getConfiguration();
        Log.i(TAG, "Verifiers tokens: " + verifiersTokens.toString());

        endpointRegistrationManager = kaaClient.getEndpointRegistrationManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // call corresponding onActivityResult methods for Facebook, Twitter and Google
        // respectively
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
        // Save current state of the UI (for Facebook)
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

        // Update  userName and userId shown on UI
        curUserName = userName;
        curUserId = userId;
        curUserInfo = "Waiting for Kaa response...";
        updateViews();

        Log.i(TAG, "Attaching user...");
        endpointRegistrationManager.attachUser(kaaVerifierToken, userIdCopy, token,
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse userAttachResponse) {
                        Log.i(TAG, "User was attached... " + userAttachResponse.toString());

                        if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS) {
                            Log.i(TAG, "Successful Kaa verification");
                            // Detach endpoint from user
                            logout();
                            Log.i(TAG, userAttachResponse.toString());
                            curUserInfo = "Successful Kaa verification";
                            buttonEnabled = true;
                            updateViews();
                            EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                            vdecf = eventFamilyFactory.getVerifiersDemoEventClassFamily();

                            List<String> FQNs = new LinkedList<>();
                            FQNs.add("org.kaaproject.kaa.demo.verifiersdemo.MessageEvent");

                            EventListenersResolver eventListenersResolver = kaaClient.getEventListenerResolver();

                            eventListenersResolver.findEventListeners(FQNs, new FetchEventListeners() {
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
                            vdecf.addListener(listener);
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

    // update text views, edits and send button state
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

    // when Send button is clicked
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

    // class, which is used to handle events
    private class KaaEventListener implements DefaultEventFamilyListener {
        @Override
        public void onEvent(MessageEvent messageEvent, String sourceEndpoint) {
            Log.i(TAG, "Event was received: " + messageEvent.getMessage());
            prependToChatBox(EventStatus.RCVD, messageEvent.getMessage());
        }
    }

    // detach connected to Kaa user
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
