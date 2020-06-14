package com.intellij.task.rally.models;

public class RallyObject {
    public String _ref;
    public String _refObjectUUID;
    public String _refObjectName;
    public String Name;
    public long ObjectID;
    public String ObjectUUID;
    public String FormattedID;

    @Override
    public String toString() {
        return Name;
    }
}
