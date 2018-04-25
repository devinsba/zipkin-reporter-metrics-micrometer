package zipkin.reporter.metrics.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import zipkin2.reporter.ReporterMetrics;

public class ReporterMetricsMicrometer implements ReporterMetrics {
  private static final String PREFIX = "zipkin.reporter.";

  private MeterRegistry meterRegistry;

  final Counter messages;
  final Counter messageBytes;
  final ConcurrentHashMap<Class<? extends Throwable>, Counter> messagesDroppedWithCause;
  final Counter spans;
  final Counter spanBytes;
  final Counter spansDropped;
  final AtomicInteger queuedSpans;
  final AtomicInteger queuedBytes;

  public ReporterMetricsMicrometer(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    messages = meterRegistry.counter(PREFIX + "messages");
    messageBytes = Counter.builder(PREFIX + "messages").baseUnit("bytes").register(meterRegistry);
    messagesDroppedWithCause = new ConcurrentHashMap<Class<? extends Throwable>, Counter>();
    spans = meterRegistry.counter(PREFIX + "spans");
    spanBytes = Counter.builder(PREFIX + "spans").baseUnit("bytes").register(meterRegistry);
    spansDropped = meterRegistry.counter(PREFIX + "spans.dropped");
    queuedSpans = meterRegistry.gauge(PREFIX + "queue.spans", new AtomicInteger(0));
    queuedBytes = meterRegistry.gauge(PREFIX + "queue.bytes", new AtomicInteger(0));
  }

  public void incrementMessages() {
    messages.increment();
  }

  public void incrementMessageBytes(int i) {
    messageBytes.increment(i);
  }

  public void incrementMessagesDropped(Throwable throwable) {
    Counter counter = null;
    if (!messagesDroppedWithCause.containsKey(throwable.getClass())) {
      counter = messagesDroppedWithCause.putIfAbsent(throwable.getClass(), meterRegistry.counter("messages.dropped","cause", throwable.getClass().getSimpleName()));
    }
    if (counter == null) {
      counter = messagesDroppedWithCause.get(throwable.getClass());
    }
    counter.increment();
  }

  public void incrementSpans(int i) {
    spans.increment(i);
  }

  public void incrementSpanBytes(int i) {
    spanBytes.increment(i);
  }

  public void incrementSpansDropped(int i) {
    spansDropped.increment(i);
  }

  public void updateQueuedSpans(int i) {
    queuedSpans.set(i);
  }

  public void updateQueuedBytes(int i) {
    queuedBytes.set(i);
  }
}
