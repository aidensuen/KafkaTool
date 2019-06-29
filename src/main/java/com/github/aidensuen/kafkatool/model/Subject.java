package com.github.aidensuen.kafkatool.model;

import java.util.List;

public class Subject {
    private String subjectName;
    private List<SchemaVersion> schemaVersionList;

    public Subject() {
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<SchemaVersion> getSchemaVersionList() {
        return this.schemaVersionList;
    }

    public void setSchemaVersionList(List<SchemaVersion> schemaVersion) {
        this.schemaVersionList = schemaVersion;
    }

    public static Subject.SubjectBuilder newBuilder() {
        return new Subject.SubjectBuilder();
    }

    public static final class SubjectBuilder {
        private Subject subject;

        private SubjectBuilder() {
            this.subject = new Subject();
        }

        public Subject.SubjectBuilder setSubjectName(String subjectName) {
            this.subject.setSubjectName(subjectName);
            return this;
        }

        public Subject.SubjectBuilder setSchemaList(List<SchemaVersion> schemaVersion) {
            this.subject.setSchemaVersionList(schemaVersion);
            return this;
        }

        public Subject build() {
            return this.subject;
        }
    }
}