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

package org.kaaproject.kaa.server.admin.client.mvp.view.dialog;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class KaaDialog extends DialogBox {

    private BottomPanel bottomPanel;
    private HorizontalPanel buttonsPanel;

    private static class BottomPanel extends HorizontalPanel {

        public BottomPanel() {
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            setWidth("100%");
        }

        public void doAttach() {
            super.onAttach();
        }

        public void doDetach() {
            super.onDetach();
        }
    }

    public KaaDialog(boolean autoHide, boolean modal)
    {
        super(autoHide, modal);
        setAutoHideOnHistoryEventsEnabled(true);
        setGlassEnabled(true);
        setAnimationEnabled(true);

        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);

        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        bottomPanel = new BottomPanel();
        bottomPanel.setStyleName("Bottom");
        bottomPanel.add(buttonsPanel);

        Element td = getCellElement(2, 1);

        DOM.insertChild(td,bottomPanel.getElement(),0);
        adopt(bottomPanel);
    }

    public void addButton(Button button) {
        buttonsPanel.add(button);
    }

    public void setTitle(String title) {
        setHTML(SafeHtmlUtils.fromTrustedString("<h2 title=\""+title+"\">"+title+"</h2>"));
    }

    @Override
    protected void doAttachChildren() {
        try {
            super.doAttachChildren();
        }
        finally {
            this.bottomPanel.doAttach();
        }
    }

    @Override
    protected void doDetachChildren() {

        try {
            super.doDetachChildren();
        }
        finally {
            this.bottomPanel.doDetach();
        }
    }

    /**
     *
     * @param resize
     * @param clientX
     * @return
     */
    private int getRelX(com.google.gwt.dom.client.Element resize, int clientX) {
        return clientX - resize.getAbsoluteLeft() +
          resize.getScrollLeft() +
          resize.getOwnerDocument().getScrollLeft();
    }

    /**
     *
     * @param resize
     * @param clientY
     * @return
     */
    private int getRelY(com.google.gwt.dom.client.Element resize, int clientY) {
        return clientY - resize.getAbsoluteTop() +
          resize.getScrollTop() +
          resize.getOwnerDocument().getScrollTop();
    }

    /**
     * Calculates the position of the mouse relative to the dialog box, and returns the corresponding "drag-mode"
     * integer, which describes which area of the box is being resized.
     *
     * @param clientX The x-coordinate of the mouse in screen pixels
     * @param clientY The y-coordinate of the mouse in screen pixels
     * @return A value in range [-1..8] describing the position of the mouse (see {@link #updateCursor(int)} for more
     *         information)
     */
    protected int calcDragMode(int clientX, int clientY) {
        com.google.gwt.dom.client.Element resize = this.getCellElement(2,2).getParentElement();
        int xr = this.getRelX(resize, clientX);
        int yr = this.getRelY(resize, clientY);

        int w = resize.getClientWidth();
        int h = resize.getClientHeight();

        if ((xr >= 0 && xr < w && yr >= -5 && yr < h)
                || (yr >= 0 && yr < h && xr >= -5 && xr < w))
            return 8;

        resize = this.getCellElement(2,0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if ((xr >= 0 && xr < w && yr >= -5 && yr < h)
                || (yr >= 0 && yr < h && xr >= 0 && xr < w+5))
            return 6;

        resize = this.getCellElement(0,2).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if ((xr >= 0 && xr < w && yr >= 0 && yr < h+5)
                || (yr >= 0 && yr < h && xr >= -5 && xr < w))
            return 2;

        resize = this.getCellElement(0,0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if ((xr >= 0 && xr < w && yr >= 0 && yr < h+5)
                || (yr >= 0 && yr < h && xr >= 0 && xr < w+5))
            return 0;

        resize = this.getCellElement(0,1).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if (yr >= 0 && yr < h)
            return 1;

        resize = this.getCellElement(1,0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if (xr >= 0 && xr < w)
            return 3;

        resize = this.getCellElement(2,1).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if (yr >= 0 && yr < h)
            return -1;//return 7;

        resize = this.getCellElement(1,2).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        if (xr >= 0 && xr < w)
            return 5;

        return -1;
    }

}
