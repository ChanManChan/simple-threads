package interthreadcommunication;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicMetrics {
    private static class InternalMetric {
        public long count;
        public long sum;
    }

    private final AtomicReference<InternalMetric> internalMetric = new AtomicReference<>(new InternalMetric());

    public void addSample(long sample) {
        InternalMetric currentState;
        InternalMetric newState;
        do {
            currentState = internalMetric.get();
            newState = new InternalMetric();
            newState.count = currentState.count + 1;
            newState.sum = currentState.sum + sample;
        } while (!internalMetric.compareAndSet(currentState, newState));
    }

    public double getAverage() {
        InternalMetric currentState;
        InternalMetric newResetState = new InternalMetric();
        double average;
        do {
            currentState = internalMetric.get();
            average = (double) currentState.sum / currentState.count;
        } while (!internalMetric.compareAndSet(currentState, newResetState));
        return average;
    }
}
