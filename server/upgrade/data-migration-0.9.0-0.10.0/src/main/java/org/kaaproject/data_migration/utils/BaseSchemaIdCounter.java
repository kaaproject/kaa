package org.kaaproject.data_migration.utils;


public class BaseSchemaIdCounter {
    private static BaseSchemaIdCounter instance;
    private Long value;
    private static boolean isInitMethodCalled;

    // can be called only once
    public static void setInitValue(Long value) {
        BaseSchemaIdCounter i = getInstance();
        if(isInitMethodCalled) {
            return;
        }
        isInitMethodCalled = true;
        i.value = value;
    }

    public Long getAndShift(Long shift) {
        Long oldValue = value;
        value += shift;
        return oldValue;
    }

    private BaseSchemaIdCounter() {

    }

    public static BaseSchemaIdCounter getInstance() {
        if(instance != null) {
            instance = new BaseSchemaIdCounter();
        }
        return instance;
    }



}
