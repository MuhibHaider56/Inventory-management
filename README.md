# Inventory Backend (Spring Boot + MySQL)

This is a complete backend for a simple **Inventory + Orders + Profit/Loss** system.

## ✅ Features
- Products (cost price, selling price, unit)
- Inventory (stock in/out, current stock)
- Customers
- Orders (place order, auto stock deduction, profit calculation)
- Cancel order (restocks inventory)
- Expenses
- Reports (sales/profit/expenses/net profit for a date range)
- Invoice JSON endpoint (frontend can render/print)

## ✅ Prerequisites
- Java 17 (or newer LTS)
- Maven 3.8+
- MySQL running (XAMPP MySQL is fine)

## ✅ Database
Create a database in phpMyAdmin:

- **Database name**: `inventory_db`

(You already created the tables manually — this project can work with that.  
Hibernate is set to `update` to avoid startup failures in dev.)

## ✅ Run
From the project folder:

```bash
mvn clean spring-boot:run
```

Server starts on:

- http://localhost:8080

Health check:

- GET http://localhost:8080/api/health


## ✅ Swagger / OpenAPI (API documentation)
Once the app is running, you can explore and test all endpoints using Swagger UI:

- Swagger UI: http://localhost:8080/swagger-ui/index.html  
  (Some setups also redirect from: http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## ✅ Config (XAMPP default)
Edit: `src/main/resources/application.properties`

By default:

- user: `root`
- password: empty

## ✅ API Overview (main)
- Products:
  - POST /api/products
  - GET /api/products
  - GET /api/products/{id}
  - PUT /api/products/{id}
  - DELETE /api/products/{id}

- Inventory:
  - GET /api/inventory
  - GET /api/inventory/{productId}
  - POST /api/inventory/adjust

- Customers:
  - POST /api/customers
  - GET /api/customers
  - GET /api/customers/{id}
  - PUT /api/customers/{id}
  - DELETE /api/customers/{id}

- Orders:
  - POST /api/orders
  - GET /api/orders
  - GET /api/orders/{id}
  - POST /api/orders/{id}/cancel
  - GET /api/orders/{id}/invoice

- Expenses:
  - POST /api/expenses
  - GET /api/expenses
  - GET /api/expenses/{id}
  - PUT /api/expenses/{id}
  - DELETE /api/expenses/{id}

- Reports:
  - GET /api/reports/summary?start=2026-02-01&end=2026-02-29
  - GET /api/reports/low-stock?threshold=10

## Notes
- All money uses `BigDecimal`.
- Order placement uses DB locking to prevent stock race conditions.


## Sample Requests (copy/paste)

### 1) Create Product
POST `/api/products`
```json
{
  "name": "Wheat",
  "unit": "kg",
  "costPrice": 100,
  "sellingPrice": 120,
  "initialStock": 2000
}
```

### 2) Create Customer
POST `/api/customers`
```json
{
  "name": "Ali Traders",
  "phone": "0300-0000000",
  "address": "Lahore"
}
```

### 3) Place Order
POST `/api/orders`
```json
{
  "customerId": 1,
  "status": "COMPLETED",
  "items": [
    { "productId": 1, "quantity": 500 }
  ]
}
```

### 4) Get Invoice JSON
GET `/api/orders/1/invoice`

### 5) Add an Expense
POST `/api/expenses`
```json
{
  "title": "Transport",
  "amount": 5000,
  "expenseDate": "2026-02-01",
  "description": "Delivery to customer"
}
```

### 6) Profit/Loss Summary
GET `/api/reports/summary?start=2026-02-01&end=2026-02-29`


### Deployment guide
# 1. Upload the jar to server
scp target/inventory-backend-0.1.0-SNAPSHOT.jar muhib@68.178.164.161:/tmp/app.jar

# 2. SSH into server
ssh muhib@68.178.164.161

# 3. Stop the app
sudo systemctl stop inventory

# 4. Replace the jar
sudo cp /tmp/app.jar /opt/inventory/app.jar
sudo chown inventoryapp:inventoryapp /opt/inventory/app.jar

# 5. Start the app
sudo systemctl start inventory

# 6. Watch logs to confirm it started
sudo journalctl -u inventory -f