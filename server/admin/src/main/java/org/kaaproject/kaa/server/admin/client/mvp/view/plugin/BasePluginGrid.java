package org.kaaproject.kaa.server.admin.client.mvp.view.plugin;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.grid.AbstractKaaGrid;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid;

public class BasePluginGrid<T extends PluginDto> extends AbstractKaaGrid<T, String> {

    public BasePluginGrid(boolean embedded) {
        super(Unit.PX, true, embedded);
    }

    @Override
    protected float constructColumnsImpl(DataGrid<T> table) {
        float prefWidth = 0;

        prefWidth += constructStringColumn(table, Utils.constants.name(),
                new StringValueProvider<T>() {
            @Override
            public String getValue(T item) {
                return item.getName();
            }
        }, 80);
        
        prefWidth += constructStringColumn(table, Utils.constants.type(),
                new StringValueProvider<T>() {
            @Override
            public String getValue(T item) {
                return item.getPluginTypeName();
            }
        }, 80);
        return prefWidth;
    }

}
