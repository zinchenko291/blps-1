# WebSocket Contract (Manager Notifications)

## 1. Transport

- Protocol: `WebSocket + STOMP`
- Handshake endpoint: `/ws`
- Broker prefix: `/topic`
- Application prefix: `/app` (reserved for future client->server messages)

## 2. Subscriptions

### 2.1 Senior Manager channel

- Destination: `/topic/notifications/senior-managers`
- Receives:
  - new order created (`NEW_ORDER_CREATED`)
  - order status changed (`ORDER_STATUS_CHANGED`)

### 2.2 Manager personal channel

- Destination: `/topic/notifications/managers/{managerId}`
- Example: `/topic/notifications/managers/2f7de4ac-6547-4a8e-bd84-f6d4ba81c3f8`
- Receives:
  - order assigned to this manager (`ORDER_ASSIGNED`)

## 3. Message Payload

All notifications are JSON with this shape:

```json
{
  "type": "NEW_ORDER_CREATED | ORDER_ASSIGNED | ORDER_STATUS_CHANGED",
  "orderId": "uuid",
  "managerId": "uuid|null",
  "status": "NEW | REJECTED | PLACED",
  "timestamp": "2026-04-15T12:34:56.789+03:00"
}
```

## 4. Event Semantics

### 4.1 `NEW_ORDER_CREATED`

- Sent when customer creates a new order.
- Destination: `/topic/notifications/senior-managers`
- Fields:
  - `orderId`: new order id
  - `managerId`: `null`
  - `status`: `NEW`

Example:

```json
{
  "type": "NEW_ORDER_CREATED",
  "orderId": "8f2f1407-1b80-4e01-95ae-605fbe8e7fd7",
  "managerId": null,
  "status": "NEW",
  "timestamp": "2026-04-15T13:02:17.001+03:00"
}
```

### 4.2 `ORDER_ASSIGNED`

- Sent when senior manager assigns an order to manager.
- Destination: `/topic/notifications/managers/{managerId}`
- Fields:
  - `orderId`: assigned order id
  - `managerId`: assigned manager id
  - `status`: current order status (usually `NEW`)

Example:

```json
{
  "type": "ORDER_ASSIGNED",
  "orderId": "8f2f1407-1b80-4e01-95ae-605fbe8e7fd7",
  "managerId": "2f7de4ac-6547-4a8e-bd84-f6d4ba81c3f8",
  "status": "NEW",
  "timestamp": "2026-04-15T13:05:10.450+03:00"
}
```

### 4.3 `ORDER_STATUS_CHANGED`

- Sent when manager changes status (`NEW -> REJECTED | PLACED`).
- Destination: `/topic/notifications/senior-managers`
- Fields:
  - `orderId`: changed order id
  - `managerId`: manager who owns this order
  - `status`: new status (`REJECTED` or `PLACED`)

Example:

```json
{
  "type": "ORDER_STATUS_CHANGED",
  "orderId": "8f2f1407-1b80-4e01-95ae-605fbe8e7fd7",
  "managerId": "2f7de4ac-6547-4a8e-bd84-f6d4ba81c3f8",
  "status": "PLACED",
  "timestamp": "2026-04-15T13:07:42.913+03:00"
}
```

## 5. Ordering and Delivery Guarantees

- Delivery type: best-effort (in-memory simple broker).
- No persistence/replay.
- No strict ordering guarantee between different topics.

## 6. Client Integration Notes

- Subscribe after successful auth and obtaining current user role/id.
- Senior manager UI subscribes to:
  - `/topic/notifications/senior-managers`
- Manager UI subscribes to:
  - `/topic/notifications/managers/{currentManagerId}`
