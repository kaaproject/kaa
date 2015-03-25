package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.ActionsLabel.ActionMenuItemListener;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class HeaderMenuItems extends HorizontalPanel {
    
    private ActionsLabel collapsedMenu;
    
    private List<ActionsLabel> items = new ArrayList<>();
    private boolean isCollapsed = false;
    private List<HandlerRegistration> registrations = new ArrayList<>();
    
    public HeaderMenuItems() {
        addStyleName(Utils.sandboxStyle.buttons());
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        setSize("100%", "100%");
        
        collapsedMenu = new ActionsLabel("", true);
        collapsedMenu.addStyleName(Utils.sandboxStyle.button());
        collapsedMenu.addStyleName(Utils.sandboxStyle.buttonLast());
        collapsedMenu.addStyleName(Utils.sandboxStyle.toggle());
        collapsedMenu.setVisible(false);
        add(collapsedMenu);
    }
    
    public void addMenuItem(String text,
            final ActionMenuItemListener listener) {
        
        ActionsLabel item = new ActionsLabel(text, false);
        item.addStyleName(Utils.sandboxStyle.button());
        items.add(item);
        
        registrations.add(item.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                listener.onMenuItemSelected();
            }
        }));
        
        item.setVisible(!isCollapsed);
        add(item);
        
        collapsedMenu.addMenuItem(text, listener);
        
        updateStyles();
    }
    
    private void updateStyles() {
        for (int i=0;i<items.size();i++) {
            if (i<items.size()-1) {
                items.get(i).removeStyleName(Utils.sandboxStyle.buttonLast());
            } else {
                items.get(i).addStyleName(Utils.sandboxStyle.buttonLast());
            }
        }
    }
    
    public void reset() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        collapsedMenu.clearItems();
        for (ActionsLabel item : items) {
            item.removeFromParent();
        }
        items.clear();
    }
    
    public void setCollapsed(boolean collapsed) {
        if (isCollapsed != collapsed) {
            isCollapsed = collapsed;
            collapsedMenu.setVisible(isCollapsed);
            for (ActionsLabel item : items) {
                item.setVisible(!isCollapsed);
            }
        }
    }
    
    public boolean isCollapsed() {
        return isCollapsed;
    }
    
}
