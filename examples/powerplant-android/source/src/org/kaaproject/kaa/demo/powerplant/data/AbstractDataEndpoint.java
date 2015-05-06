package org.kaaproject.kaa.demo.powerplant.data;

import org.kaaproject.kaa.demo.powerplant.fragment.DashboardFragment;

public abstract class AbstractDataEndpoint implements DataEndpoint {

    private static final float CONSUME_VOLTAGE_DIV = 5.0f;

    protected float getConsumption() {
        return DashboardFragment.MAX_VOLTAGE * DashboardFragment.NUM_PANELS + (float)Math.random() * CONSUME_VOLTAGE_DIV;
    }
}
