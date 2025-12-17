ALTER TABLE payments MODIFY COLUMN session_url VARCHAR(500);
INSERT INTO payments(id, payment_status, payment_type, rental_id, session_url, session_id, amount_to_pay)
VALUES (1,
        'PENDING',
        'PAYMENT',
        1,
        'https://checkout.stripe.com/pay/session_12345',
        'cs_test_session12345',
        350
       );

INSERT INTO payments(id, payment_status, payment_type, rental_id, session_url, session_id, amount_to_pay)
VALUES (2,
        'PAID',
        'PAYMENT',
        2,
        'https://checkout.stripe.com/pay/session_67890',
        'cs_test_session67890',
        450
       );

INSERT INTO payments(id, payment_status, payment_type, rental_id, session_url, session_id, amount_to_pay)
VALUES (3,
        'PENDING',
        'FINE',
        3,
        'https://checkout.stripe.com/pay/session_fine67890',
        'cs_test_session_fine67890',
        108
       );

INSERT INTO payments(id, payment_status, payment_type, rental_id, session_url, session_id, amount_to_pay)
VALUES (4,
        'PENDING',
        'PAYMENT',
        6,
        'https://checkout.stripe.com/pay/cancel_session_12345',
        'cs_test_cancel_session12345',
        350
       );

