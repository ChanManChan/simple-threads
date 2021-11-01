package datasharing;

// Each of these methods can be called by any given number threads concurrently, so the class needs to be thread safe.
public class MinMaxMetrics {
    private volatile long minValue;
    private volatile long maxValue;

    public MinMaxMetrics() {
        this.minValue = Long.MIN_VALUE;
        this.maxValue = Long.MAX_VALUE;
    }

    public void addSample(long newSample) {
        synchronized (this) {
            this.minValue = Math.min(newSample, this.minValue);
            this.maxValue = Math.max(newSample, this.maxValue);
        }
    }

    public long getMinValue() {
        return this.minValue;
    }

    public long getMaxValue() {
        return this.maxValue;
    }
}
