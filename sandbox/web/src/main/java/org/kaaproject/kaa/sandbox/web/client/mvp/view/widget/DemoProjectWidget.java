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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.widget;

import org.kaaproject.kaa.sandbox.demo.projects.Feature;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectAction;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEvent;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.ProjectActionEventHandler;
import org.kaaproject.kaa.sandbox.web.client.util.Utils;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DemoProjectWidget extends VerticalPanel implements
        HasProjectActionEventHandlers {

    private Image applicationImage;
    private HorizontalPanel featuresPanel;
    private Anchor projectTitle;
    private Anchor getSourceAnchor;
    private Anchor getBinaryAnchor;

    private Project project;

    private ProjectWidgetAnimation projectWidgetAnimation;

    public DemoProjectWidget() {
        super();

        addStyleName(Utils.sandboxStyle.demoProjectWidget());

        projectWidgetAnimation = new ProjectWidgetAnimation(this, 190, 10.0);

        VerticalPanel detailsPanel = new VerticalPanel();
        detailsPanel.addStyleName(Utils.sandboxStyle.details());
        detailsPanel.sinkEvents(Event.ONCLICK);

        detailsPanel.setWidth("100%");

        AbsolutePanel layoutPanel = new AbsolutePanel();

        VerticalPanel platformImagePanel = new VerticalPanel();
        platformImagePanel.addStyleName(Utils.sandboxStyle.detailsInnerTop());
        platformImagePanel.setWidth("100%");
        applicationImage = new Image();
        platformImagePanel
                .setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        platformImagePanel.add(applicationImage);

        layoutPanel.add(platformImagePanel);
        SimplePanel platformImageHoverPanel = new SimplePanel();
        platformImageHoverPanel.addStyleName(Utils.sandboxStyle
                .platformImageHover());
        layoutPanel.add(platformImageHoverPanel);
        platformImageHoverPanel.setSize("100%", "100%");
        layoutPanel.setSize("100%", "100%");

        detailsPanel.add(layoutPanel);
        SimplePanel titlePanel = new SimplePanel();
        titlePanel.addStyleName(Utils.sandboxStyle.detailsInnerCenter());
        projectTitle = new Anchor();
        projectTitle.addStyleName(Utils.sandboxStyle.title());
        titlePanel.add(projectTitle);

        detailsPanel.add(titlePanel);

        add(detailsPanel);

        featuresPanel = new HorizontalPanel();
        featuresPanel.addStyleName(Utils.sandboxStyle.detailsInnerCenter());
        add(featuresPanel);
        featuresPanel.getElement().getStyle().setPaddingTop(10, Unit.PX);

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setWidth("100%");
        buttonsPanel.addStyleName(Utils.sandboxStyle.detailsInnerBottom());
        getSourceAnchor = new Anchor(Utils.constants.getSourceCode());
        getSourceAnchor.addStyleName(Utils.sandboxStyle.action());
        getSourceAnchor.getElement().getStyle().setMarginRight(20, Unit.PX);
        getBinaryAnchor = new Anchor(Utils.constants.getBinary());
        getBinaryAnchor.addStyleName(Utils.sandboxStyle.action());
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(getSourceAnchor);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsPanel.add(getBinaryAnchor);
        add(buttonsPanel);

        detailsPanel.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project
                            .getId(), ProjectAction.OPEN_DETAILS);
                    fireEvent(action);
                }
            }
        }, ClickEvent.getType());

        getSourceAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project
                            .getId(), ProjectAction.GET_SOURCE_CODE);
                    fireEvent(action);
                }
            }
        });

        getBinaryAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (project != null) {
                    ProjectActionEvent action = new ProjectActionEvent(project
                            .getId(), ProjectAction.GET_BINARY);
                    fireEvent(action);
                }
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        if (project.getIconBase64() != null
                && project.getIconBase64().length() > 0) {
            applicationImage.setUrl("data:image/png;base64,"
                    + project.getIconBase64());
        } else {
            applicationImage.setResource(Utils.getPlatformIcon(project
                    .getPlatform()));
        }
        projectTitle.setText(project.getName());
        projectTitle.setTitle(project.getName());
        for (Feature feature : project.getFeatures()) {
            Image image = new Image(Utils.getFeatureIcon(feature, false));
            image.getElement().getStyle().setPaddingRight(8, Unit.PX);
            featuresPanel.add(image);
        }
    }

    public Project getProject() {
        return project;
    }

    @Override
    public HandlerRegistration addProjectActionHandler(
            ProjectActionEventHandler handler) {
        return this.addHandler(handler, ProjectActionEvent.getType());
    }

    public void show(boolean animate) {
        projectWidgetAnimation.show(animate);
    }

    public void hide(boolean animate) {
        projectWidgetAnimation.hide(animate);
    }

    static class ProjectWidgetAnimation extends Animation {
        
        private static final int ANIMATION_DURATION = 300;

        private Widget widget;

        private double opacityIncrement;
        private double targetOpacity;
        private double baseOpacity;

        private double marginIncrement;
        private double targetMargin;
        private double baseMargin;

        private int width;
        private double rightMargin;

        private boolean show = true;

        public ProjectWidgetAnimation(Widget widget, int width,
                double rightMargin) {
            this.widget = widget;
            this.width = width;
            this.rightMargin = rightMargin;
        }

        @Override
        protected void onUpdate(double progress) {
            widget.getElement().getStyle()
                    .setOpacity(baseOpacity + progress * opacityIncrement);
            widget.getElement()
                    .getStyle()
                    .setMarginRight(baseMargin + progress * marginIncrement,
                            Unit.PX);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            widget.getElement().getStyle().setOpacity(targetOpacity);
            widget.getElement().getStyle()
                    .setMarginRight(targetMargin, Unit.PX);
            if (!show) {
                widget.setVisible(false);
            }
        }

        public void show(boolean animate) {
            if (!show) {
                show = true;
                widget.setVisible(true);
                animate(0.0, 1.0, -width, rightMargin, animate ? ANIMATION_DURATION : 0);
            }
        }

        public void hide(boolean animate) {
            if (show) {
                show = false;
                animate(1.0, 0.0, rightMargin, -width, animate ? ANIMATION_DURATION : 0);
            }
        }

        private void animate(double baseOpacity, double targetOpacity, double baseMargin,
                double targetMargin, int duration) {
            this.baseOpacity = baseOpacity;
            this.targetOpacity = targetOpacity;
            this.baseMargin = baseMargin;
            this.targetMargin = targetMargin;
            widget.getElement().getStyle().setOpacity(this.baseOpacity);
            widget.getElement().getStyle()
                    .setMarginRight(this.baseMargin, Unit.PX);
            this.opacityIncrement = this.targetOpacity - this.baseOpacity;
            this.marginIncrement = this.targetMargin - this.baseMargin;
            if (duration > 0) {
                run(duration);
            } else {
                onComplete();
            }
        }

    }

}
