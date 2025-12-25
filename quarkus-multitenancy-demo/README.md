# 🧪 Quarkus Multi-Tenancy Demo

A complete demo showing:
- Multi-tenant PostgreSQL setup (tenant1 & tenant2)
- HTTP header-based tenant resolution
- ORM multi-tenancy via core adapter

---

## 🐳 Setup with Docker

Run the docker-compose.yml

```bash
docker compose down -v
docker compose up -d

Databases:
- tenant1-db → port 5433 → user1 / pass1
- tenant2-db → port 5434 → user2 / pass2

cd quarkus-multitenancy-demo
mvn quarkus:dev


🧪 Test Endpoints

Get Current Tenant
curl -H "X-Tenant: tenant1" http://localhost:8080/api/users/tenant
curl -H "X-Tenant: tenant2" http://localhost:8080/api/users/tenant

List Users 
curl -H "X-Tenant: tenant1" http://localhost:8080/api/users
curl -H "X-Tenant: tenant2" http://localhost:8080/api/users

Create User

curl -X POST -H "X-Tenant: tenant1" -H "Content-Type: application/json" \
  -d '{"name":"Manthos","email":"manthos@tenant1.com"}' \
  http://localhost:8080/api/users

🧰 Postman Collection

import the json file postman/demo.postman_collection.json into postman
and test tenants tenant1 and tenant2

