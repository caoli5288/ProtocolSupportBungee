package protocolsupport.protocol.storage;

import java.util.Collection;
import java.util.LinkedList;

public class LinkedTokenBasedThrottler<T> {

    private LinkedList<T> buf = new LinkedList<>();
    private int threshold;
    private int token;
    private long period;
    private long latestUpdate;

    public LinkedTokenBasedThrottler(long period, int threshold) {
        this.period = period;
        this.threshold = threshold;
    }

    public LinkedTokenBasedThrottler(int threshold) {
        this(1000, threshold);
    }

    public void updateToken(long current) {
        if (token != threshold) {
            long delta = current - latestUpdate;
            if (delta >= period) {
                token = threshold;
            } else {
                token += Math.toIntExact((threshold * delta) / period);
                if (token > threshold) {
                    token = threshold;
                }
            }
        }
        latestUpdate = current;
    }

    public boolean isTokenOrObjEmpty() {
        return isTokenEmpty() || isEmpty();
    }

    public T useTokenAndObj() {
        if (isTokenOrObjEmpty()) {
            return null;
        }
        token--;
        return buf.poll();
    }

    public boolean useToken() {
        if (isTokenEmpty()) {
            return false;
        }
        token--;
        return true;
    }

    public boolean isTokenEmpty() {
        return token == 0;
    }

    public boolean isEmpty() {
        return buf.isEmpty();
    }

    public boolean remove(T id) {
        int index = buf.indexOf(id);
        if (index != -1) {
            buf.remove(index);
            return true;
        }
        return false;
    }

    public void add(T id) {
        buf.add(id);
    }

    public void addAll(Collection<T> in) {
        buf.addAll(in);
    }

    public T remove() {
        return buf.remove();
    }

    public void clear() {
        buf.clear();
    }

    public LinkedTokenBasedThrottler<T> threshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public LinkedTokenBasedThrottler<T> period(long period) {
        this.period = period;
        return this;
    }
}
