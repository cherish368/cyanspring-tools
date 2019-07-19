package org.cyanspring.tools.common;


import org.cyanspring.tools.utils.TimeUtil;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ClockSim extends Clock {
    private Date now;
    private Set<IClockListener> listeners = new HashSet<IClockListener>();
    private Thread manualClockThread;

    public ClockSim() {
        now = TimeUtil.getOnlyDate(new Date());
    }

    public boolean addClockListener(IClockListener listener) {
        return listeners.add(listener);
    }

    public boolean removeClockListener(IClockListener listener) {
        return listeners.remove(listener);
    }

    private void notifyListeners(Date time) {
        for (IClockListener listener : listeners) {
            listener.onTime(time);
        }
    }

    public void tick(long ms) {
        Date now = new Date(this.now.getTime() + ms);
        setNow(now);
    }

    public void startTicking(final long tick, final long duration) {
        manualClockThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = 0; i < duration; i += tick) {
                    ClockSim.this.now = new Date(ClockSim.this.now.getTime() + tick);
                    notifyListeners(ClockSim.this.now);
                }
            }
        });
    }

    public void stopTicking() {
        if (null != manualClockThread) {
            manualClockThread.interrupt();
            manualClockThread = null;
        }
    }

    @Override
    public Date now() {
        return this.now;
    }

    public void setNow(Date now) {
        this.now = now;
        notifyListeners(now);
    }

}
