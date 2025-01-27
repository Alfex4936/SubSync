# SubSync

on going

# Stack
- Java 23
- Spring boot 3
- Spring Security 6
- Postgres 17
- Redis 7.4

# Stripe API

Frontend: Initiate SetupIntent → Collect Payment → Webhook → Save Payment Method

```bash
stripe listen --forward-to localhost:8080/stripe/webhook

# Create customer and attach test payment method
stripe customers create
stripe payment_methods attach --customer=cu_xxx --payment-method=pm_card_visa

# Trigger webhook events
stripe trigger payment_intent.succeeded
stripe trigger setup_intent.succeeded
```

## example output on successful payment
```java
2025-01-27 20:54:38  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3Q71uJnNRTV]
2025-01-27 20:54:38   --> payment_intent.succeeded [evt_3Q71Qj8yeIy]
2025-01-27 20:54:38   --> payment_intent.created [evt_3Q71uEPYjDK]
2025-01-27 20:54:38  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3Q71uEPYjDK]
2025-01-27 20:54:38  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3Q71Qj8yeIy]
2025-01-27 20:54:39   --> charge.succeeded [evt_3QGX70ENnCbTa]
2025-01-27 20:54:39  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3QGX70ENnCbTa]
2025-01-27 20:54:39   --> payment_intent.succeeded [evt_3QGX70sgdT8Ns]
2025-01-27 20:54:39  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3QGX70sgdT8Ns]
2025-01-27 20:54:39   --> payment_intent.created [evt_3QX70GCqv87Z]
2025-01-27 20:54:39  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3QX70GCqv87Z]
2025-01-27 20:54:40   --> charge.updated [evt_3QP]
2025-01-27 20:54:40  <--  [200] POST http://localhost:8080/stripe/webhook [evt_3QP]
2025-01-27 20:54:42   --> charge.updated [evt_y6Io]
2025-01-27 20:54:42  <--  [200] POST http://localhost:8080/stripe/webhook [evt_y6Io]
```
# References

- [KakaoPay JPA Transactional에 대하여](https://tech.kakaopay.com/post/jpa-transactional-bri/#jpa-transactional%EC%97%90-%EB%8C%80%ED%95%98%EC%97%AC)