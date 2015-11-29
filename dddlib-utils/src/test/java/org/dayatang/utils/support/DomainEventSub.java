package org.dayatang.utils.support;

import java.util.Date;

/**
 * Created by yyang on 14-9-14.
 */
public class DomainEventSub extends DomainEvent {

    private String prop1;

    private String prop2;

    private transient String transientField;

    protected Date lastModified;

    public DomainEventSub() {
    }

    public DomainEventSub(Date occurredOn) {
        super(occurredOn);
    }

    public DomainEventSub(Date occurredOn, int version) {
        super(occurredOn, version);
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    public String getProp1() {
        return prop1;
    }

    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public String getTransientField() {
        return transientField;
    }

    public void setTransientField(String transientField) {
        this.transientField = transientField;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
