/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.sandbox.web.client.mvp.view.dialog;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.atmosphere.gwt20.client.Atmosphere;
import org.atmosphere.gwt20.client.AtmosphereErrorHandler;
import org.atmosphere.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.gwt20.client.AtmosphereResponse;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.atmosphere.gwt20.client.managed.RPCSerializer;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConsoleDialog extends AvroUiDialog {

    private final static Logger logger = Logger.getLogger("ConsoleDialog");
    
    private final static int MAX_CONSOLE_BUFFER = 5000;
    
	private TextArea console;
	private Button okButton;
	
	private String consoleUuid;
	private ConsoleDialogListener listener;
	private static Atmosphere atmosphere;
	
	private String finishText = "";
	private boolean consoleSessionSucceded = false;
	private boolean rpcCallSucceded = false;
	
	public static ConsoleDialog startConsoleDialog(String initialMessage, ConsoleDialogListener listener) {
	    if (logger.isLoggable(Level.FINE)) {
	        logger.fine("startConsoleDialog");
	    }
		ConsoleDialog dialog = new ConsoleDialog(listener);
		dialog.center();
		dialog.startConsole(initialMessage);
		return dialog;
	}
	
	public ConsoleDialog(ConsoleDialogListener listener) {
		super(false, true);
		this.listener = listener;
		
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);
        
        console = new TextArea();
        console.setReadOnly(true);
        int width= Window.getClientWidth();
        int height= Window.getClientHeight();
        console.setSize(width*2/3 + "px", height*2/3   + "px");
        console.addStyleName(Utils.sandboxStyle.consoleArea());
        okButton = new Button("Ok");
        addButton(okButton);
        
        dialogContents.add(console);
        
        okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				if (ConsoleDialog.this.listener != null) {
					ConsoleDialog.this.listener.onOk(rpcCallSucceded && consoleSessionSucceded);
				}
			}
        });
        okButton.setEnabled(false);
	}
	
	private void startConsole(String initialMessage) {
	    
	    if (logger.isLoggable(Level.FINE)) {
	         logger.fine("startConsole");
	    }

		RPCSerializer rpc_serializer = GWT.create(RPCSerializer.class);
		if (atmosphere == null) {
		    atmosphere = Atmosphere.create();
		}
		atmosphere.unsubscribe();		
        AtmosphereRequestConfig rpcRequestConfig = AtmosphereRequestConfig.create(rpc_serializer);
        
        rpcRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/rpc");
        rpcRequestConfig.setTransport(AtmosphereRequestConfig.Transport.WEBSOCKET);
        rpcRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        
        rpcRequestConfig.setErrorHandler(new AtmosphereErrorHandler() {
            @Override
            public void onError(AtmosphereResponse response) {
                consoleSessionSucceded = false;
                ConsoleDialog.this.onError("Sorry, but there's some problem with your "
                        + "socket or the server is down!\n");
            }
        });
        
        rpcRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("onMessage, count: " + response.getMessages().size());
                    if (response.getMessages().size() > 0) {
                        Object message = response.getMessages().get(0);
                        logger.fine("onMessage, class: " + message.getClass().getName());
                    }
                }
            	List<RPCEvent> messages = response.getMessages();
            	
            	for (RPCEvent message : messages) {
            		GWT.log("onMessage: " + message.getMessage());
            		if (logger.isLoggable(Level.FINE)) {
            		    logger.fine("onMessage: " + message.getMessage());
            		}
            		if (consoleUuid == null || consoleUuid.length()==0) {
            			consoleUuid = message.getMessage();
            			listener.onStart(consoleUuid, ConsoleDialog.this, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
                                rpcCallSucceded = false;
								onError(Utils.getErrorMessage(caught)+"\nFailed!");
							}

							@Override
							public void onSuccess(Void result) {
                                rpcCallSucceded = true;
								appendToConsoleAtFinish("Finished!");
								ConsoleDialog.this.onSuccess();
							}
            			});
            		}
            		else if (message.getMessage().equals(consoleUuid + " finished")){
            			consoleSessionSucceded = true;
            			atmosphere.unsubscribe();
            			onSuccess();
            		}
            		else {
            			appendToConsole(message.getMessage());
            		}
            	}
            }
        });
        rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
        rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);
        rpcRequestConfig.clearFlags(AtmosphereRequestConfig.Flags.dropAtmosphereHeaders);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("atmosphere.subscribe ...");
        }

        appendToConsole(initialMessage + "\n");
        atmosphere.subscribe(rpcRequestConfig);
 
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("atmosphere.subscribed");
        }

	}
	
	private void onError(String error) {
	    appendToConsole(error);
	    atmosphere.unsubscribe();
	    okButton.setEnabled(true);
	}
	
	private void onSuccess() {
		if (rpcCallSucceded && consoleSessionSucceded) {
			appendToConsole(finishText);
	        okButton.setEnabled(true);
		}
	}
	
	public void appendToConsole(String text) {
	    String consoleText = console.getText() + text;
	    if (consoleText.length() > MAX_CONSOLE_BUFFER) {
	        int start = consoleText.length() - MAX_CONSOLE_BUFFER;
	        consoleText = consoleText.substring(start);
	    }
		console.setText(consoleText);
		if (console.getText().length()>0) {
			console.setCursorPos(console.getText().length()-1);
			console.getElement().setScrollTop(console.getElement().getScrollHeight());
		}
	}
	
	public void appendToConsoleAtFinish(String text) {
		this.finishText += text;
	}
	
	public interface ConsoleDialogListener {
		
		void onOk(boolean success);
		
		void onStart(String uuid, ConsoleDialog dialog, AsyncCallback<Void> callback);
		
	}
	
}
