**Banking API Postman Testing Guide**
Base URL Configuration
Set up your base URL in Postman environment:

Variable: base_url
Value: http://localhost:8080 (adjust port as needed)

**Authentication Endpoints**
1. Register User
Method: POST
URL: {{base_url}}/auth/register
Headers: Content-Type: application/json
Request Body:
<pre>
  json{
  "username": "testuser",
  "password": "testpassword"
}
Expected Response: 201 Created
json{
    "id": 1,
    "username": "testuser",
    // other user details
}
</pre>

3. Login User
Method: POST
URL: {{base_url}}/auth/login
Headers: Content-Type: application/json
Request Body:
<pre>
  json{
    "username": "testuser",
    "password": "testpassword"
}
Expected Response: 200 OK
json{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser",
    "userId": 1,
    "message": "Login successful"
}
</pre>

*Important: Save the token from the response for authenticated requests!*
Setting Up Authentication Token
Option 1: Manual Token Setup

Copy the token from the login response
For each authenticated request, add header:

Key: Authorization
Value: Bearer {{token}} (replace {{token}} with actual token)


**Banking Operations (Authenticated)**

3. Get User's Accounts
Method: GET
URL: {{base_url}}/accounts
Headers: Authorization: Bearer {{auth_token}}
Expected Response: 200 OK
<pre>
  json{
        "id": 1,
        "accountNumber": "ACC123456",
        "accountHolderName": "Test User",
        "balance": 1000.00,
        "userId": 1
  }
</pre>


4. Get All Accounts (Unauthenticated)
Method: GET
URL: {{base_url}}/accounts
Headers: None (no Authorization header)
Expected Response: 200 OK (returns all accounts in system)
5. Get Primary Account
Method: GET
URL: {{base_url}}/accounts/primary
Headers: Authorization: Bearer {{auth_token}}
Expected Response: 200 OK
<pre>
  json{
    "id": 1,
    "accountNumber": "ACC123456",
    "accountHolderName": "Test User",
    "balance": 1000.00,
    "userId": 1
}
</pre>


7. Get Current User Info
Method: GET
URL: {{base_url}}/me
Headers: Authorization: Bearer {{auth_token}}
Expected Response: 200 OK (same as Get User's Accounts)

8. Create New Account
Method: POST
URL: {{base_url}}/accounts
Headers:

Content-Type: application/json
Authorization: Bearer {{auth_token}}

Request Body:
<pre>
  json{
    "accountHolderName": "Test User Secondary"
}
Expected Response: 201 Created
json{
    "id": 2,
    "accountNumber": "ACC789012",
    "accountHolderName": "Test User Secondary",
    "balance": 0.00,
    "userId": 1
}
</pre>


8. Deposit Money
Method: POST
URL: {{base_url}}/deposit
Headers:

Content-Type: application/json
Authorization: Bearer {{auth_token}}

Request Body (to primary account):
<pre>
  json{
    "amount": 500.00
}
Request Body (to specific account):
json{
    "amount": 500.00,
    "accountId": 1
}
Expected Response: 200 OK
json"Deposit successful to account ACC123456"
</pre>


9. Withdraw Money
Method: POST
URL: {{base_url}}/withdraw
Headers:

Content-Type: application/json
Authorization: Bearer {{auth_token}}

<pre>
  Request Body (from primary account):
json{
    "amount": 200.00
}
Request Body (from specific account):
json{
    "amount": 200.00,
    "accountId": 1
}
Expected Response: 200 OK
json"Withdrawal successful from account ACC123456"
</pre>



10. Transfer Money
Method: POST
URL: {{base_url}}/transfer
Headers:

Content-Type: application/json
Authorization: Bearer {{auth_token}}

<pre>
  Request Body (from primary account):
json{
    "receiverAccountNumber": "ACC789012",
    "amount": 100.00
}
Request Body (from specific account):
json{
    "senderAccountNumber": "ACC123456",
    "receiverAccountNumber": "ACC789012",
    "amount": 100.00
}
Expected Response: 200 OK
json"Transfer successful from primary account to ACC789012"
</pre>

**Common Response Codes**

<pre>
200 OK: Request successful
201 Created: Resource created successfully
400 Bad Request: Invalid request data
401 Unauthorized: Authentication required
403 Forbidden: Access denied
404 Not Found: Resource not found
500 Internal Server Error: Server error
</pre>
