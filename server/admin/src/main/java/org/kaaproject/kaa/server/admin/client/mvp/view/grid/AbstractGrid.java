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

package org.kaaproject.kaa.server.admin.client.mvp.view.grid;

import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.HasRowActionEventHandlers;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowAction;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowActionEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.grid.RowActionEventHandler;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.ConfirmDialog;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.ConfirmDialog.ConfirmListener;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionsButtonCell;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.ActionsButtonCell.ActionMenuItemListener;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.cell.LinkCell;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;

public abstract class AbstractGrid<T, K> extends DockLayoutPanel implements HasRowActionEventHandlers<K> {

    private static KaaAdminGridResources gridResources = GWT.create(KaaAdminGridResources.class);
    private static KaaAdminGridResourcesSmall gridResourcesSmall = GWT.create(KaaAdminGridResourcesSmall.class);

    private static SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    private static KaaAdminPagerResourcesSmall pagerResourcesSmall = GWT.create(KaaAdminPagerResourcesSmall.class);

    protected DataGrid<T> table;
    private MultiSelectionModel<T> selectionModel;

    private float prefferredWidth = 0f;

    protected boolean enableActions;
    protected boolean embedded;
    protected boolean editable;

    protected Column<T,T> deleteColumn;

    public AbstractGrid(Style.Unit unit) {
        this(unit, true, false, false);
    }

    public AbstractGrid(Style.Unit unit, boolean enableActions) {
        this(unit, enableActions, false, false);
    }
    
    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded) {
        this(unit, enableActions, embedded, false);
    }

    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded, boolean editable) {
        super(unit);
        ProvidesKey<T> keyProvider = new ProvidesKey<T>() {
            @Override
            public Object getKey(T item) {
                return item != null ? getObjectId(item) : null;
            }
        };

        this.enableActions = enableActions;
        this.embedded = embedded;
        this.editable = editable;

        Resources localGridResources = embedded ? gridResourcesSmall : gridResources;
        table = new DataGrid<T>(20, localGridResources, keyProvider);
        table.setAutoHeaderRefreshDisabled(true);
        Label emptyTableLabel = new Label(Utils.constants.dataGridEmpty());
        if (embedded) {
            emptyTableLabel.getElement().getStyle().setFontSize(14, Unit.PX);
            emptyTableLabel.getElement().getStyle().setColor("#999999");
        }
        table.setEmptyTableWidget(emptyTableLabel);

        selectionModel = new MultiSelectionModel<T>(keyProvider);

        table.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<T> createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                table.redrawHeaders();
            }
        });

        prefferredWidth = initColumns(table);
        table.setMinimumTableWidth(prefferredWidth, Unit.PX);

        SimplePager.Resources localPagerResources = embedded ? pagerResourcesSmall : pagerResources;

        SimplePager pager = new SimplePager(TextLocation.CENTER, localPagerResources, false, 0, true) {
            @Override
            protected String createText() {
                HasRows display = getDisplay();
                Range range = display.getVisibleRange();
                int currentPage = range.getStart() / (range.getLength() != 0 ? range.getLength() : 1) + 1;
                int total = ((int)Math.ceil((float)display.getRowCount()/(float)range.getLength()));
                if (total == 0) {
                    total = 1;
                }
                return Utils.messages.pagerText(currentPage, total);
            }
        };
        pager.setDisplay(table);

        String pagerId = "pager_"+pager.hashCode();

        String html =     "<table " +
                        "style='width:100%'>" +
                        "  <tr>" +
                        "     <td" +
                        "       align='right'>" +
                        "        <div id='" + pagerId + "'/>" +
                        "     </td>" +
                        "  </tr>" +
                        "</table>";

        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(pager, pagerId);

        //

        //add(header);
        //add(dPanel);
        addNorth(htmlPanel, 40);
        add(table); // center

        if (embedded) {
            getElement().getStyle().setProperty("boxShadow", "0px 0px 8px rgba(0,0,0,0.5)");
            table.getElement().getStyle().setMargin(10, Unit.PX);
        }
        else {
            getElement().getStyle().setMargin(10, Unit.PX);
        }

        //setSize("100%", "100%");

        //dPanel.setWidth(prefferredWidth+"px");
    }

    public MultiSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public HasData<T> getDisplay() {
        return table;
    }

    protected void onRowClicked(K id) {
        RowActionEvent<K> rowClickEvent = new RowActionEvent<>(id, RowAction.CLICK);
        fireEvent(rowClickEvent);
    }

    protected void onRowDelete(K id) {
        RowActionEvent<K> rowClickEvent = new RowActionEvent<>(id, RowAction.DELETE);
        fireEvent(rowClickEvent);
    }

    @Override
    public HandlerRegistration addRowActionHandler(RowActionEventHandler<K> handler) {
        return this.addHandler(handler, RowActionEvent.getType());
    }

    private float initColumns (DataGrid<T> table) {
        float prefWidth = 0f;
        //Column<T, Boolean> selectionColumn = constructSelectionColumn();
        //Header<Boolean> selectAllHeader = constructSelectAllHeader();

        //table.addColumn(selectionColumn, selectAllHeader);
        //table.setColumnWidth(selectionColumn, 40, Unit.PX);

        //prefWidth += 40f;

        prefWidth += constructColumnsImpl(table);
        prefWidth += constructActions(table, prefWidth);
        return prefWidth;
    }

    protected float constructStringColumn(DataGrid<T> table, String title,
            final StringValueProvider<T> valueProvider, float prefWidth) {
        Header<SafeHtml> header = new SafeHtmlHeader(
                SafeHtmlUtils.fromSafeConstant(title));
        Column<T, String> column = new Column<T, String>(new LinkCell()) {
            @Override
            public String getValue(T item) {
                return valueProvider.getValue(item);
            }
        };
        column.setFieldUpdater(new FieldUpdater<T,String>() {
            @Override
            public void update(int index, T object, String value) {
                onRowClicked(getObjectId(object));
            }
        });
        table.addColumn(column, header);
        table.setColumnWidth(column, prefWidth, Unit.PX);
        return prefWidth;
    }

    protected float constructBooleanColumn(DataGrid<T> table, String title,
            final BooleanValueProvider<T> valueProvider, float prefWidth) {
        Header<SafeHtml> header = new SafeHtmlHeader(
                SafeHtmlUtils.fromSafeConstant(title));
        Column<T, Boolean> column = new Column<T, Boolean>(new CheckboxCell()) {
            @Override
            public Boolean getValue(T item) {
                return valueProvider.getValue(item);
            }
        };
        column.setFieldUpdater(new FieldUpdater<T,Boolean>() {
            @Override
            public void update(int index, T object, Boolean value) {
                onRowClicked(getObjectId(object));
            }
        });
        table.addColumn(column, header);
        table.setColumnWidth(column, prefWidth, Unit.PX);
        return prefWidth;
    }

    protected float constructActions(DataGrid<T> table, float prefWidth) {
        if (enableActions) {
            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, 40, Unit.PX);
                return 40;
            }
            else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    protected float removeActions(DataGrid<T> table) {
        int index = table.getColumnIndex(deleteColumn);
        if (index > -1) {
            table.removeColumn(deleteColumn);
            return 40;
        }
        return 0;
    }

    public void setEnableActions(boolean enableActions) {
        this.enableActions = enableActions;
        if (enableActions) {
            prefferredWidth += constructActions(table, prefferredWidth);
            table.setMinimumTableWidth(prefferredWidth, Unit.PX);
        }
        else {
            prefferredWidth -= removeActions(table);
            table.setMinimumTableWidth(prefferredWidth, Unit.PX);
        }
    }

    protected Column<T, T> constructActionsColumn() {
        ActionsButtonCell<T> cell = new ActionsButtonCell<T>(
                Utils.resources.drop_down(), Utils.constants.actions());

        cell.addMenuItem(Utils.resources.remove(), Utils.constants.delete(),
                new ActionMenuItemListener<T>() {
                    @Override
                    public void onMenuItemSelected(T value) {
                        deleteItem(value);
                    }
                });

        constructAdditionalActions(cell);

        Column<T, T> column = new Column<T, T>(cell) {
            @Override
            public T getValue(T item) {
                return item;
            }
        };
        return column;
    }

    protected void constructAdditionalActions(ActionsButtonCell<T> cell) {
    }

    protected Column<T, T> constructDeleteColumn(String text) {
        ActionButtonCell<T> cell = new ActionButtonCell<T>(Utils.resources.remove(),
                text, embedded,
                new ActionListener<T> () {
                    @Override
                    public void onItemAction(T value) {
                        deleteItem(value);
                    }
                },
                new ActionValidator<T> () {
                    @Override
                    public boolean canPerformAction(T value) {
                        return canDelete(value);
                    }
                }
        );
        Column<T, T> column = new Column<T, T>(cell) {
            @Override
            public T getValue(T item) {
                return item;
            }
        };
        //column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return column;
    }

    protected void deleteItem(final T value) {
        ConfirmListener listener = new ConfirmListener() {
            @Override
            public void onNo() {
            }

            @Override
            public void onYes() {
                onRowDelete(getObjectId(value));
            }
        };

        String question = deleteQuestion();
        String title = deleteTitle();

        ConfirmDialog dialog = new ConfirmDialog(listener, title, question);
        dialog.center();
        dialog.show();
    }

    protected String deleteQuestion() {
        return Utils.messages.deleteSelectedEntryQuestion();
    }

    protected String deleteTitle() {
        return Utils.messages.deleteSelectedEntryTitle();
    }

    protected boolean canDelete(T value) {
        return true;
    }


//    private Column<T, Boolean> constructSelectionColumn() {
//        Column<T, Boolean> checkColumn = new Column<T, Boolean>(
//                new CheckboxCell(true, false)) {
//            @Override
//            public Boolean getValue(T object) {
//                // Get the value from the selection model.
//                return selectionModel.isSelected(object);
//            }
//        };
//        return checkColumn;
//    }

    @SuppressWarnings("unchecked")
    protected K getObjectId(T value) {
        if (value instanceof HasId) {
            return (K)((HasId) value).getId();
        }
        else {
            return null;
        }
    }

    protected abstract float constructColumnsImpl(DataGrid<T> table);

    public interface StringValueProvider<T> {
        String getValue(T item);
    }

    public interface BooleanValueProvider<T> {
        Boolean getValue(T item);
    }

}
