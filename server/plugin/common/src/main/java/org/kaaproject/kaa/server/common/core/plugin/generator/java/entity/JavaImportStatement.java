package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.ImportStatement;

public class JavaImportStatement implements ImportStatement {

    private static final String DEFAULT_TEMPLATE = "import %s";

    private final String body;
    private final String template;

    // TODO: Leave the template variable in case of a static import statement

    public JavaImportStatement(String body) {
        this(body, DEFAULT_TEMPLATE);
    }

    public JavaImportStatement(String body, String template) {
        this.body = body;
        this.template = template;
    }

    @Override
    public String getBody() {
        return String.format(this.template, this.body).trim();
    }

    @Override
    public String toString() {
        return this.getBody();
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
