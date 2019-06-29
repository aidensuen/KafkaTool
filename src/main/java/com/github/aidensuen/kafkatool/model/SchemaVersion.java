package com.github.aidensuen.kafkatool.model;

public class SchemaVersion {
    private String subject;
    private String version;
    private String id;
    private String schema;

    public SchemaVersion() {
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public static SchemaVersion.SchemaBuilder newBuilder() {
        return new SchemaVersion.SchemaBuilder();
    }

    public static final class SchemaBuilder {
        private SchemaVersion schemaVersion;

        private SchemaBuilder() {
            this.schemaVersion = new SchemaVersion();
        }

        public SchemaVersion.SchemaBuilder setSubject(String subject) {
            this.schemaVersion.setSubject(subject);
            return this;
        }

        public SchemaVersion.SchemaBuilder setVersion(String version) {
            this.schemaVersion.setVersion(version);
            return this;
        }

        public SchemaVersion.SchemaBuilder setId(String id) {
            this.schemaVersion.setVersion(id);
            return this;
        }

        public SchemaVersion.SchemaBuilder setRawSchema(String rawSchema) {
            this.schemaVersion.setSchema(rawSchema);
            return this;
        }

        public SchemaVersion build() {
            return this.schemaVersion;
        }
    }
}
