package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.ImportStatement;

public class JavaImportStatement implements ImportStatement {

    private static final String DEFAULT = "import %s;";

    private final String template;
    private final String body;

    public JavaImportStatement(String body) {
        this(DEFAULT, body);
    }

    public JavaImportStatement(String template, String body) {
        this.template = template;
        this.body = body;
    }

    @Override
    public String getBody() {
        return String.format(template, this.body);
    }

    @Override
    public String toString() {
        return getBody();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JavaImportStatement other = (JavaImportStatement) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        return true;
    }
}
