/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.view.grid;

import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;

/**
 * The Class KaaRowAction.
 */
public class KaaRowAction {

    /** The Constant SEND_NOTIFICATION. */
    public static final int SEND_NOTIFICATION = RowActionEvent.MAX_ACTION + 1;

    /** The Constant DOWNLOAD_LOG_SCHEMA_LIBRARY. */
    public static final int DOWNLOAD_LOG_SCHEMA_LIBRARY = RowActionEvent.MAX_ACTION + 2;

    /** The Constant DOWNLOAD_SCHEMA. */
    public static final int DOWNLOAD_SCHEMA = RowActionEvent.MAX_ACTION + 3;

    /** The Constant DOWNLOAD_BASE_SCHEMA. */
    public static final int DOWNLOAD_BASE_SCHEMA = RowActionEvent.MAX_ACTION + 4;

    /** The Constant DOWNLOAD_OVERRIDE_SCHEMA. */
    public static final int DOWNLOAD_OVERRIDE_SCHEMA = RowActionEvent.MAX_ACTION + 5;

    /** The Constant GENERATE_SDK. */
    public static final int GENERATE_SDK = RowActionEvent.MAX_ACTION + 6;
    
    /** The Constant CTL_EXPORT_SHALLOW. */
    public static final int CTL_EXPORT_SHALLOW = RowActionEvent.MAX_ACTION + 7;
    
    /** The Constant CTL_EXPORT_DEEP. */
    public static final int CTL_EXPORT_DEEP = RowActionEvent.MAX_ACTION + 8;
    
    /** The Constant CTL_EXPORT_FLAT. */
    public static final int CTL_EXPORT_FLAT = RowActionEvent.MAX_ACTION + 9;
    
    /** The Constant CTL_EXPORT_LIBRARY. */
    public static final int CTL_EXPORT_LIBRARY = RowActionEvent.MAX_ACTION + 10;

}
