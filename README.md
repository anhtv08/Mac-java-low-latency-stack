# Low Latency Java Stack

A high-performance Java application combining LMAX Disruptor, Chronicle Map, and Object Pooling for ultra-low latency processing.

## Features

- **LMAX Disruptor**: Lock-free ring buffer for high-throughput event processing
- **Chronicle Map**: Memory-mapped file storage for persistent, low-latency data access
- **Object Pooling**: Reduces garbage collection pressure through object reuse
- **Thread-Safe**: Designed for concurrent, multi-threaded environments
- **Performance Optimized**: Sub-microsecond latencies with millions of events per second

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Event         │    │   Disruptor      │    │   Event         │
│   Publisher     │───▶│   Ring Buffer    │───▶│   Handler       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Object        │    │   Chronicle      │    │   Memory        │
│   Pool          │◀──▶│   Map Storage    │◀──▶│   Mapped Files  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
# Build the project
./build.sh

# Or manually:
mvn clean compile test package

# Run the application
java -jar target/low-latency-stack-1.0.0.jar

# Run with performance tuning
java -Xmx2g -XX:+UseG1GC -jar target/low-latency-stack-1.0.0.jar
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LowLatencyEngineTest

# Generate coverage report
mvn jacoco:report
```

## Usage Example

```java
try (LowLatencyEngine engine = new LowLatencyEngine()) {
    // Publish events to the ring buffer
    engine.publishEvent("AAPL", 150.0, 100, Event.EventType.TRADE);
    
    // Store data in Chronicle Map
    engine.storeData("position", "1000");
    
    // Use object pool for StringBuilder
    StringBuilder sb = engine.acquireStringBuilder();
    sb.append("Processing event...");
    engine.releaseStringBuilder(sb);
    
    // Get statistics
    System.out.println("Processed: " + engine.getProcessedEventCount());
}
```

## Performance Characteristics

- **Throughput**: 1M+ events/second
- **Latency**: Sub-microsecond processing
- **Memory**: Minimal GC pressure through object pooling
- **Storage**: Memory-mapped files for persistent data

## Configuration

### Ring Buffer Size
```java
private static final int RING_BUFFER_SIZE = 1024 * 64; // 64K entries
```

### Object Pool Settings
```java
private static final int OBJECT_POOL_SIZE = 1000;
```

### Chronicle Map Entries
```java
private static final long STORAGE_ENTRIES = 1_000_000;
```

## Testing

The project includes comprehensive unit tests and integration tests:

- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end functionality
- **Performance Tests**: Throughput and latency benchmarks
- **Concurrency Tests**: Multi-threaded safety verification

### Test Coverage

Run `mvn jacoco:report` to generate coverage reports in `target/site/jacoco/`.

## JVM Tuning

For optimal performance, use these JVM flags:

```bash
java -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=1 \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseTransparentHugePages \
     -jar target/low-latency-stack-1.0.0.jar
```

## Monitoring

The engine provides runtime statistics:

- `getProcessedEventCount()`: Total events processed
- `getStringBuilderPoolSize()`: Current pool size
- `getStorageSize()`: Chronicle Map entries

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License.