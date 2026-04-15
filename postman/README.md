# Postman Requests

Files:
- `mts-api.postman_collection.json`
- `mts-local.postman_environment.json`

## Import

1. Import collection file.
2. Import environment file.
3. Select environment `mts-local`.

## Notes

- Auth is session-based (cookie `JSESSIONID`).
- Run senior and manager flows in different Postman tabs/workspaces if needed.
- Set real values for:
  - `managerId` (id of registered manager)
  - `orderId` (id of existing order)
- `POST /api/orders` returns `200 OK` with empty body.
