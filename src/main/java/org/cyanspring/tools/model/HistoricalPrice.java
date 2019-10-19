package org.cyanspring.tools.model;

import com.alibaba.fastjson.annotation.JSONField;

import org.cyanspring.tools.utils.PriceUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class HistoricalPrice extends HistoricalBase {

    @JSONField(name = "sc")
    private String source;
    @JSONField(name = "sm")
    private String symbol;
    @JSONField(name = "kt", format = "yyyy-MM-dd HH:mm")
    private Date keyTime;
    @JSONField(name = "ts", format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date timeStamp;
    @JSONField(name = "v")
    private Double volume;
    @JSONField(name = "to")
    private Double turnover;

    public HistoricalPrice() {
    }

    public HistoricalPrice(Date keyTime, double prevClose) {
        this.keyTime = keyTime;
        if (!PriceUtil.isZero(prevClose)) {
            this.setOpen(prevClose);
            this.setHigh(prevClose);
            this.setLow(prevClose);
            this.setClose(prevClose);
        }
    }


    public void refreshData(Index index) {
        if (this.source == null) {
            this.setSource(index.getSource());
        }
        if (this.symbol == null) {
            this.setSymbol(index.getSymbol());
        }
        if (PriceUtil.isZero(this.open)) {
            this.setOpen(index.getValue());
        }
        if (PriceUtil.isZero(this.high)) {
            this.setHigh(index.getValue());
        } else {
            if (PriceUtil.GreaterThan(index.getValue(), this.high)) {
                this.setHigh(index.getValue());
            }
        }
        if (PriceUtil.isZero(this.low)) {
            this.setLow(index.getValue());
        } else {
            if (PriceUtil.LessThan(index.getValue(), this.low)) {
                this.setLow(index.getValue());
            }
        }
        this.setClose(index.getValue());
        this.volume += index.getQty();
        this.turnover += index.getQty() / index.getValue();
        this.setTimeStamp(index.getUpdatedTime());
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getKeyTime() {
        return keyTime;
    }

    public void setKeyTime(Date keyTime) {
        this.keyTime = keyTime;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getTurnover() {
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricalPrice that = (HistoricalPrice) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(keyTime, that.keyTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, symbol, keyTime);
    }

    @Override
    public String toString() {
        return "HistoricalPrice{" +
                "source='" + source + '\'' +
                ", symbol='" + symbol + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", keyTime=" + keyTime +
                ", timeStamp=" + timeStamp +
                ", volume=" + volume +
                ", turnover=" + turnover +
                '}';
    }
}
