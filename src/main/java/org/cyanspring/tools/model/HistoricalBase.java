package org.cyanspring.tools.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.cyanspring.tools.utils.PriceUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class HistoricalBase implements Serializable {

    @JSONField(name = "o")
    protected Double open;
    @JSONField(name = "c")
    protected Double close;
    @JSONField(name = "h")
    protected Double high;
    @JSONField(name = "l")
    protected Double low;

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }
}
