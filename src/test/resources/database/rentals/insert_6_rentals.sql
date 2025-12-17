INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (1,
        CURDATE(),
        DATE_ADD(CURDATE(), INTERVAL 9 DAY),
        null,
        1,
        1,
        'PENDING'
       );

INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (2,
        CURDATE(),
        DATE_ADD(CURDATE(), INTERVAL 10 DAY),
        null,
        1,
        2,
        'CONFIRMED'
       );

INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (3,
        CURDATE(),
        DATE_ADD(CURDATE(), INTERVAL 10 DAY),
        DATE_ADD(CURDATE(), INTERVAL 12 DAY),
        1,
        3,
        'COMPLETED'
       );

INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (4,
        DATE_ADD(CURDATE(), INTERVAL -10 DAY),
        DATE_ADD(CURDATE(), INTERVAL -2 DAY),
        null,
        1,
        4,
        'CONFIRMED'
       );

INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (5,
        DATE_ADD(CURDATE(), INTERVAL -10 DAY),
        DATE_ADD(CURDATE(), INTERVAL -5 DAY),
        null,
        1,
        1,
        'PENDING_FINE'
       );

INSERT INTO rentals(id, rental_date, return_date, actual_return_date, user_id, car_id, rental_status)
VALUES (6,
        CURDATE(),
        DATE_ADD(CURDATE(), INTERVAL 10 DAY),
        null,
        1,
        3,
        'CANCELED'
       );
