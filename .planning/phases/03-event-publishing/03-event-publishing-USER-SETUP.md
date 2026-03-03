# Phase 3: Event Publishing - User Setup

**Status:** Incomplete - Requires Kafka broker configuration

---

## Kafka Broker

### Environment Variables

Add the following to your environment or `.env` file:

| Variable | Value | Description |
|----------|-------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` (default) | Kafka broker address |

### Setup Options

**Option 1: Local Kafka**
1. Install Kafka from https://kafka.apache.org/downloads
2. Start ZooKeeper: `bin/windows/zookeeper-server-start.bat config/zookeeper.properties`
3. Start Kafka: `bin/windows/kafka-server-start.bat config/server.properties`

**Option 2: Docker**
```bash
docker run -p 9092:9092 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 confluentinc/cp-kafka
```

**Option 3: Managed Service**
- Confluent Cloud
- Amazon MSK
- Aiven Kafka

### Verification

```bash
# Test connection (requires Kafka tools)
kafka-broker-api-versions --bootstrap-server localhost:9092

# Or use the application health endpoint after starting
curl http://localhost:8080/actuator/health
```

---

*Setup required before running 03-02 (Event Publisher Service)*
