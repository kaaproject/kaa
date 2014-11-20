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

package org.kaaproject.kaa.demo.smarthousedemo.command;

import java.util.concurrent.TimeoutException;

import android.os.AsyncTask;
import android.util.Log;

public abstract class CommandAsyncTask<P,V> extends AsyncTask<P,String,V> {
    private V result = null;
    private Throwable throwable = null;
    private CommandCallback<V> callback;
    
    public CommandAsyncTask(CommandCallback<V> callback) {
        this.callback = callback;
    }
    
    @Override
    protected V doInBackground(P... params) {
        try {
            result = executeCommand(params);
        }
        catch (Throwable e) {
            throwable = e;
            Log.e(CommandAsyncTask.class.getName(), "Throwable catched in command async task", e);
        }
        return result;
    }
    
    @Override
    protected void onPostExecute(V result) {
        if (throwable != null) {
            if (throwable instanceof TimeoutException) {
                callback.onCommandTimeout();
            }
            else {
                callback.onCommandFailure(throwable);
            }
        }
        else {
            callback.onCommandSuccess(result);
        }
    }
    
    protected abstract V executeCommand(P... params) throws Throwable;
    
}