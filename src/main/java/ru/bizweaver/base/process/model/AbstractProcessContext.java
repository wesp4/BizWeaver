package ru.bizweaver.base.process.model;

import java.io.Serial;
import java.io.Serializable;

public abstract class AbstractProcessContext implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    @Override
    public AbstractProcessContext clone() {
        try {
            return (AbstractProcessContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
