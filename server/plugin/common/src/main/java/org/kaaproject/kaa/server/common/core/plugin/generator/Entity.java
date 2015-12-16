package org.kaaproject.kaa.server.common.core.plugin.generator;

import java.text.MessageFormat;

public class Entity {

    public static final String CONVERTER_NAME_TEMPLATE = "entity{0}Converter";

    private int id;
    private Class<?> entityClass;

    public Entity(int id, Class<?> entityClass) {
        this.id = id;
        this.entityClass = entityClass;
    }

    public String getConverterName() {
        return MessageFormat.format(CONVERTER_NAME_TEMPLATE, Integer.toString(id));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClazz(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (this.getClass() != o.getClass()) {
            return false;
        }

        Entity other = (Entity) o;

        if (this.entityClass == null) {
            if (other.entityClass != null) {
                return false;
            }
        } else if (!this.entityClass.equals(other.entityClass)) {
            return false;
        }

        return true;
    }
}
