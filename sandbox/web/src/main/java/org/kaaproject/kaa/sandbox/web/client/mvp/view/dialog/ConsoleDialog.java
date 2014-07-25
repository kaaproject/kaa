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
import org.atmosphere.gwt20.client.AtmosphereCloseHandler;
import org.atmosphere.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.gwt20.client.AtmosphereResponse;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.atmosphere.gwt20.client.managed.RPCSerializer;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConsoleDialog extends KaaDialog {

    private final static Logger logger = Logger.getLogger("ConsoleDialog");
    
	private TextArea console;
	private Button okButton;
	
	private String consoleUuid;
	private ConsoleDialogListener listener;
	private Atmosphere atmosphere;
	
	private String finishText = "";
	private boolean consoleSessionFinished = false;
	private boolean rpcCallFinished = false;
	private boolean success = false;
	
	public static ConsoleDialog startConsoleDialog(ConsoleDialogListener listener) {
	    if (logger.isLoggable(Level.FINE)) {
	        logger.fine("startConsoleDialog");
	    }
		ConsoleDialog dialog = new ConsoleDialog(listener);
		dialog.center();
		dialog.startConsole();
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
        Style consoleStyle = console.getElement().getStyle();
        consoleStyle.setPropertyPx("minHeight", 200);
        consoleStyle.setBackgroundColor("#000000");
        consoleStyle.setColor("#00FF00");
        consoleStyle.setFontSize(11, Unit.PX);
        consoleStyle.setProperty("fontFamily", "Georgia");
        okButton = new Button("Ok");
        addButton(okButton);
        
        dialogContents.add(console);
        
        okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				if (ConsoleDialog.this.listener != null) {
					ConsoleDialog.this.listener.onOk(success);
				}
			}
        });
        okButton.setEnabled(false);
	}
	
	private void startConsole() {
	    
	    if (logger.isLoggable(Level.FINE)) {
	         logger.fine("startConsole");
	    }

		RPCSerializer rpc_serializer = GWT.create(RPCSerializer.class);
		atmosphere = Atmosphere.create();
        AtmosphereRequestConfig rpcRequestConfig = AtmosphereRequestConfig.create(rpc_serializer);
        rpcRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/rpc");
        rpcRequestConfig.setTransport(AtmosphereRequestConfig.Transport.WEBSOCKET);
        rpcRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        rpcRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
			@Override
			public void onClose(AtmosphereResponse response) {
				
				okButton.setEnabled(true);
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
								appendToConsoleAtFinish(Utils.getErrorMessage(caught)+"\n");
								appendToConsoleAtFinish("Failed!");
								rpcCallFinished = true;
								success = false;
		            			finished();
							}

							@Override
							public void onSuccess(Void result) {
								appendToConsoleAtFinish("Finished!");
								rpcCallFinished = true;
								success = true;
		            			finished();
							}
            			});
            		}
            		else if (message.getMessage().equals(consoleUuid + " finished")){
            			consoleSessionFinished = true;
            			finished();
            		}
            		else {
            			appendToConsole(message.getMessage());
            		}
            	}
            }
        });
        rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
        rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("atmosphere.subscribe ...");
        }

        atmosphere.subscribe(rpcRequestConfig);
 
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("atmosphere.subscribed");
        }

	}
	
	private void finished() {
		if (rpcCallFinished && consoleSessionFinished) {
			appendToConsole(finishText);
			this.atmosphere.unsubscribe();
		}
	}
	
	public void appendToConsole(String text) {
		console.setText(console.getText() + text);
		if (console.getText().length()>0) {
			console.setSelectionRange(console.getText().length()-1, 0);
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
