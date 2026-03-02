# Bedomain API Tests

This directory contains automated tests for the Bedomain API:

## Directory Structure

```
tests/
├── postman/                    # Functional tests
│   └── bedomain-api.postman_collection.json
└── k6/                         # Performance tests
    ├── load-test.js           # Load, stress, spike tests
    └── performance-test.js    # Baseline performance test
```

## Postman - Functional Testing

### Prerequisites
- [Postman](https://www.postman.com/downloads/) installed
- Bedomain application running

### Running Tests

1. **Import Collection**:
   - Open Postman
   - Click Import → Select `bedomain-api.postman_collection.json`

2. **Configure Environment**:
   - Create a new environment (e.g., "Local")
   - Set `baseUrl` to `http://localhost:8080`
   - Set `jwtToken` to your Keycloak token (if auth enabled)

3. **Run Collection**:
   - Select the "Bedomain API" collection
   - Click "Run" to execute all tests
   - View results in the "Test Results" tab

### Test Coverage
- ✅ Entity Types CRUD
- ✅ Properties CRUD
- ✅ Entity Instances CRUD
- ✅ Error handling (404, 400)
- ✅ Pagination

---

## k6 - Performance Testing

### Prerequisites
- [k6](https://k6.io/docs/getting-started/installation/) installed
- OR use Docker to run k6

### Running Tests

#### Local k6 Installation

```bash
# Run smoke test
k6 run tests/k6/load-test.js

# Run specific scenario
k6 run --env SCENARIO=smoke tests/k6/load-test.js

# Run with environment variable
BASE_URL=http://localhost:8080 k6 run tests/k6/load-test.js
```

#### Using Docker

```bash
# Run load test
docker run --rm -v $(pwd)/tests/k6:/scripts \
  -e BASE_URL=http://host.docker.internal:8080 \
  grafana/k6 run /scripts/load-test.js

# Run with custom scenarios
docker run --rm -v $(pwd)/tests/k6:/scripts \
  grafana/k6 run --env SCENARIO=smoke /scripts/load-test.js
```

### Test Types

| Test | Description | Duration | Target |
|------|-------------|----------|--------|
| **Smoke** | Basic sanity check | 30s | 1 VU |
| **Load** | Normal load simulation | 2min | 10 VUs |
| **Stress** | Peak load test | 2min | 50 VUs |
| **Spike** | Sudden spike test | 70s | 100 VUs |
| **Baseline** | Performance baseline | 1min | 5 VUs |

### Performance Thresholds

| Metric | Threshold |
|--------|-----------|
| HTTP Response Time (p95) | < 500ms |
| Error Rate | < 1% |
| Create Operation | < 200ms avg |
| Read Operation | < 100ms avg |
| List Operation | < 150ms avg |

### Output

- Console summary after each run
- JSON report (`summary.json`) generated in the tests directory

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: API Tests

on: [push, pull_request]

jobs:
  functional-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Postman Tests
        uses: postmanlabs/newman@v5
        with:
          collection: ./bedomain/tests/postman/bedomain-api.postman_collection.json
          environment: ./bedomain/tests/postman/local.postman_environment.json

  performance-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run k6 Test
        uses: grafana/k6-action@v0.2.0
        with:
          scripts: ./bedomain/tests/k6/load-test.js
          env: BASE_URL=http://localhost:8080
```

---

## Test Data Cleanup

Both test suites include automatic cleanup:
- Postman: "Delete Entity Type (Cleanup)" folder
- k6: `http.del()` calls after each iteration

Data is also cleaned up automatically by soft-delete in the application.
