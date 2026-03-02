import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

export const options = {
  scenarios: {
    // Smoke test - basic functionality
    smoke: {
      executor: 'constant-vus',
      vus: 1,
      duration: '30s',
      tags: { test_type: 'smoke' },
    },
    // Load test - normal load
    load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10 },  // Ramp up
        { duration: '1m', target: 10 },    // Steady state
        { duration: '30s', target: 0 },    // Ramp down
      ],
      tags: { test_type: 'load' },
    },
    // Stress test - peak load
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      tags: { test_type: 'stress' },
    },
    // Spike test - sudden spike
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 5 },
        { duration: '30s', target: 100 }, // Spike
        { duration: '30s', target: 5 },
      ],
      tags: { test_type: 'spike' },
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    http_req_failed: ['rate<0.01'],    // Error rate < 1%
    errors: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test data
const entityTypePayload = JSON.stringify({
  name: `LoadTest_${Date.now()}`,
  description: 'Load test entity type',
});

const propertyPayload = JSON.stringify({
  name: 'test_property',
  description: 'Test property',
  dataType: 'STRING',
});

const instancePayload = JSON.stringify({
  attributes: {
    name: 'Test Entity',
    value: 100,
  },
});

export default function () {
  // Create Entity Type
  const createEntityType = http.post(
    `${BASE_URL}/api/v1/entity-types`,
    entityTypePayload,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(createEntityType, {
    'Entity Type Created': (r) => r.status === 201,
    'Has ID': (r) => r.json('id') !== undefined,
  }) || errorRate.add(1);

  const entityTypeId = createEntityType.json('id');
  sleep(0.5);

  // Create Property
  const createProperty = http.post(
    `${BASE_URL}/api/v1/entity-types/${entityTypeId}/properties`,
    propertyPayload,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(createProperty, {
    'Property Created': (r) => r.status === 201,
  }) || errorRate.add(1);

  const propertyId = createProperty.json('id');
  sleep(0.5);

  // Create Entity Instance
  const createInstance = http.post(
    `${BASE_URL}/api/v1/entities`,
    JSON.stringify({
      entityTypeId: entityTypeId,
      attributes: {
        name: `Test_${Date.now()}`,
        value: Math.floor(Math.random() * 1000),
      },
    }),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(createInstance, {
    'Instance Created': (r) => r.status === 201,
  }) || errorRate.add(1);

  const instanceId = createInstance.json('id');
  sleep(0.5);

  // Get Entity Type
  const getEntityType = http.get(
    `${BASE_URL}/api/v1/entity-types/${entityTypeId}`,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(getEntityType, {
    'Get Entity Type': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.5);

  // Get Properties
  const getProperties = http.get(
    `${BASE_URL}/api/v1/entity-types/${entityTypeId}/properties`
  );

  check(getProperties, {
    'Get Properties': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.5);

  // Get Entity Instance
  const getInstance = http.get(
    `${BASE_URL}/api/v1/entities/${instanceId}`
  );

  check(getInstance, {
    'Get Instance': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.5);

  // List Entity Types (paginated)
  const listEntityTypes = http.get(
    `${BASE_URL}/api/v1/entity-types?page=0&size=10`
  );

  check(listEntityTypes, {
    'List Entity Types': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.5);

  // List Entity Instances (paginated)
  const listInstances = http.get(
    `${BASE_URL}/api/v1/entities?page=0&size=10`
  );

  check(listInstances, {
    'List Instances': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(0.5);

  // Cleanup - Delete Instance
  http.del(`${BASE_URL}/api/v1/entities/${instanceId}`, null, {
    headers: { 'Content-Type': 'application/json' },
  });

  // Delete Property
  http.del(
    `${BASE_URL}/api/v1/entity-types/${entityTypeId}/properties/${propertyId}`,
    null,
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  // Delete Entity Type
  http.del(`${BASE_URL}/api/v1/entity-types/${entityTypeId}`, null, {
    headers: { 'Content-Type': 'application/json' },
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    'summary.json': JSON.stringify(data),
  };
}

function textSummary(data, opts) {
  const indent = opts.indent || '';
  let output = '\n' + indent + '=== Test Summary ===\n\n';

  output += indent + `Total Requests: ${data.metrics.http_reqs.values.count}\n`;
  output += indent + `Failed Requests: ${data.metrics.http_req_failed.values.passes}\n`;
  output += indent + `Duration: ${data.state.testRunDurationMs}ms\n\n`;

  output += indent + 'HTTP Response Times (ms):\n';
  output += indent + `  avg: ${data.metrics.http_req_duration.values.avg.toFixed(2)}\n`;
  output += indent + `  p95: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}\n`;
  output += indent + `  p99: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}\n`;
  output += indent + `  max: ${data.metrics.http_req_duration.values.max.toFixed(2)}\n\n`;

  return output;
}
