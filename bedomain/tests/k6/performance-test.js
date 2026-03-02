import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const createDuration = new Trend('entity_create_duration');
const readDuration = new Trend('entity_read_duration');
const listDuration = new Trend('entity_list_duration');

export const options = {
  scenarios: {
    // Baseline performance test
    baseline: {
      executor: 'constant-vus',
      vus: 5,
      duration: '1m',
      tags: { test_type: 'baseline' },
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<300'], // 95% under 300ms
    http_req_failed: ['rate<0.01'],
    entity_create_duration: ['avg<200'],
    entity_read_duration: ['avg<100'],
    entity_list_duration: ['avg<150'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Create Entity Type and measure time
  const startCreate = new Date();
  const createRes = http.post(
    `${BASE_URL}/api/v1/entity-types`,
    JSON.stringify({
      name: `PerfTest_${Date.now()}_${__VU}`,
      description: 'Performance test entity',
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );
  const endCreate = new Date();
  createDuration.add(endCreate - startCreate);

  check(createRes, {
    'Created': (r) => r.status === 201,
  }) || errorRate.add(1);

  const entityId = createRes.json('id');
  sleep(0.1);

  // Read Entity and measure time
  const startRead = new Date();
  const readRes = http.get(`${BASE_URL}/api/v1/entity-types/${entityId}`);
  const endRead = new Date();
  readDuration.add(endRead - startRead);

  check(readRes, {
    'Read OK': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.1);

  // List Entities and measure time
  const startList = new Date();
  const listRes = http.get(`${BASE_URL}/api/v1/entity-types?page=0&size=20`);
  const endList = new Date();
  listDuration.add(endList - startList);

  check(listRes, {
    'List OK': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Cleanup
  http.del(`${BASE_URL}/api/v1/entity-types/${entityId}`);
  sleep(0.5);
}

export function handleSummary(data) {
  return {
    stdout: textSummary(data),
  };
}

function textSummary(data) {
  let out = '\n=== Performance Test Results ===\n\n';
  out += `Duration: ${data.state.testRunDurationMs}ms\n`;
  out += `Total Requests: ${data.metrics.http_reqs.values.count}\n`;
  out += `Failed: ${data.metrics.http_req_failed.values.passes}\n\n`;

  out += 'Response Times:\n';
  out += `  avg: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms\n`;
  out += `  p95: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms\n`;
  out += `  p99: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms\n`;
  out += `  max: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms\n\n`;

  out += 'Custom Timings:\n';
  out += `  Create avg: ${data.metrics.entity_create_duration.values.avg.toFixed(2)}ms\n`;
  out += `  Read avg: ${data.metrics.entity_read_duration.values.avg.toFixed(2)}ms\n`;
  out += `  List avg: ${data.metrics.entity_list_duration.values.avg.toFixed(2)}ms\n`;

  return out;
}
