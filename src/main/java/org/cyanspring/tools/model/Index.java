package org.cyanspring.tools.model;


import org.cyanspring.tools.common.Clock;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Index implements Cloneable, Serializable {




    private String symbol;
    private String source;
    private double value;
    private double qty;
    private Date updatedTime;

    public Index(String symbol, double value) {
        super();
        this.symbol = symbol;
        this.value = value;
        updatedTime = Clock.getInstance().now();
    }

    public Index(String symbol, double value, Date date) {
        this(symbol, value);
        updatedTime = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    private void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    @Override
    public Index clone() {
        try {
            return (Index) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }

    public Index clone(String symbol) {
        Index result = this.clone();
        result.setSymbol(symbol);
        return result;
    }

    public boolean isExpiredWithSeconds(int seconds) {
        Date now = Clock.getInstance().now();
        if ((now.getTime() - this.getUpdatedTime().getTime()) > seconds * 1000) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return Objects.equals(symbol, index.symbol) &&
                Objects.equals(source, index.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, source);
    }

    @Override
    public String toString() {
        return "[" + this.source + ", " + this.symbol + ", " + this.value + ", " + this.qty + ", " + this.updatedTime + "]";
    }
}
