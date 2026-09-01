-- ============================================================
-- V2: Synthetic seed data for demo/dev profile
-- ~10 corridors, ~60 defects (20 per source), ~30 block requests,
-- 60 days of COA availability
-- ============================================================

-- ---- CORRIDORS ----
INSERT INTO corridor (corridor_name, zone, division, route_km) VALUES
('Mumbai CST - Pune',           'CR',  'Mumbai',     192.00),
('Pune - Solapur',              'CR',  'Pune',       260.00),
('Delhi - Mathura',             'NCR', 'Agra',       141.00),
('Delhi - Ambala',              'NR',  'Delhi',      198.00),
('Chennai - Vijayawada',        'SR',  'Chennai',    432.00),
('Kolkata - Howrah - Burdwan',  'ER',  'Howrah',     109.00),
('Ahmedabad - Surat',           'WR',  'Ahmedabad',  264.00),
('Bengaluru - Mysuru',          'SWR', 'Bengaluru',  139.00),
('Hyderabad - Secunderabad',    'SCR', 'Hyderabad',  15.00),
('Jaipur - Ajmer',              'NWR', 'Jaipur',     131.00);

-- ---- DEFECTS — TMS (Engineering / Track) ----
INSERT INTO defect (source_system, department, corridor_id, asset_type, km_marker, severity, date_raised, due_date, status, estimated_repair_hours) VALUES
('TMS', 'Engineering', 1, 'Rail Track', 45.2,  'Critical', NOW()-INTERVAL'15 days', NOW()+INTERVAL'2 days',  'Overdue', 8.0),
('TMS', 'Engineering', 1, 'Sleeper',    78.5,  'Major',    NOW()-INTERVAL'10 days', NOW()+INTERVAL'5 days',  'Open',    4.0),
('TMS', 'Engineering', 2, 'Rail Track', 120.0, 'Critical', NOW()-INTERVAL'20 days', NOW()-INTERVAL'2 days',  'Overdue', 10.0),
('TMS', 'Engineering', 2, 'Ballast',    95.3,  'Minor',    NOW()-INTERVAL'5 days',  NOW()+INTERVAL'15 days', 'Open',    2.0),
('TMS', 'Engineering', 3, 'Bridge',     55.0,  'Critical', NOW()-INTERVAL'8 days',  NOW()+INTERVAL'1 days',  'Open',    12.0),
('TMS', 'Engineering', 3, 'Rail Track', 88.0,  'Major',    NOW()-INTERVAL'12 days', NOW()+INTERVAL'3 days',  'Open',    6.0),
('TMS', 'Engineering', 4, 'Sleeper',    30.0,  'Minor',    NOW()-INTERVAL'3 days',  NOW()+INTERVAL'20 days', 'Open',    1.5),
('TMS', 'Engineering', 4, 'Rail Track', 150.0, 'Major',    NOW()-INTERVAL'18 days', NOW()+INTERVAL'4 days',  'Overdue', 5.0),
('TMS', 'Engineering', 5, 'Crossing',   200.0, 'Critical', NOW()-INTERVAL'25 days', NOW()-INTERVAL'5 days',  'Overdue', 9.0),
('TMS', 'Engineering', 5, 'Ballast',    310.0, 'Minor',    NOW()-INTERVAL'2 days',  NOW()+INTERVAL'25 days', 'Open',    2.0),
('TMS', 'Engineering', 6, 'Rail Track', 60.0,  'Major',    NOW()-INTERVAL'7 days',  NOW()+INTERVAL'8 days',  'Open',    4.5),
('TMS', 'Engineering', 7, 'Sleeper',    180.0, 'Minor',    NOW()-INTERVAL'1 days',  NOW()+INTERVAL'30 days', 'Open',    1.0),
('TMS', 'Engineering', 8, 'Bridge',     90.0,  'Critical', NOW()-INTERVAL'14 days', NOW()+INTERVAL'1 days',  'Open',    11.0),
('TMS', 'Engineering', 9, 'Rail Track', 8.0,   'Major',    NOW()-INTERVAL'9 days',  NOW()+INTERVAL'6 days',  'Open',    3.5),
('TMS', 'Engineering', 10,'Crossing',   70.0,  'Critical', NOW()-INTERVAL'30 days', NOW()-INTERVAL'10 days', 'Overdue', 8.0),
('TMS', 'Engineering', 1, 'Rail Track', 130.0, 'Major',    NOW()-INTERVAL'6 days',  NOW()+INTERVAL'7 days',  'Open',    5.0),
('TMS', 'Engineering', 2, 'Sleeper',    250.0, 'Minor',    NOW()-INTERVAL'4 days',  NOW()+INTERVAL'18 days', 'Open',    2.5),
('TMS', 'Engineering', 3, 'Ballast',    40.0,  'Critical', NOW()-INTERVAL'22 days', NOW()-INTERVAL'3 days',  'Overdue', 7.0),
('TMS', 'Engineering', 6, 'Rail Track', 75.0,  'Major',    NOW()-INTERVAL'11 days', NOW()+INTERVAL'4 days',  'Open',    4.0),
('TMS', 'Engineering', 7, 'Bridge',     200.0, 'Minor',    NOW()-INTERVAL'2 days',  NOW()+INTERVAL'22 days', 'Open',    3.0);

-- ---- DEFECTS — SMMS (S&T / Signalling) ----
INSERT INTO defect (source_system, department, corridor_id, asset_type, km_marker, severity, date_raised, due_date, status, estimated_repair_hours) VALUES
('SMMS', 'S&T', 1, 'Signal',         50.0,  'Critical', NOW()-INTERVAL'16 days', NOW()+INTERVAL'1 days',  'Overdue', 6.0),
('SMMS', 'S&T', 1, 'Track Circuit',  80.0,  'Major',    NOW()-INTERVAL'9 days',  NOW()+INTERVAL'5 days',  'Open',    3.0),
('SMMS', 'S&T', 2, 'Signal',         130.0, 'Major',    NOW()-INTERVAL'11 days', NOW()+INTERVAL'3 days',  'Open',    4.0),
('SMMS', 'S&T', 3, 'Interlocking',   60.0,  'Critical', NOW()-INTERVAL'19 days', NOW()-INTERVAL'1 days',  'Overdue', 8.0),
('SMMS', 'S&T', 4, 'Signal',         35.0,  'Minor',    NOW()-INTERVAL'4 days',  NOW()+INTERVAL'20 days', 'Open',    2.0),
('SMMS', 'S&T', 4, 'Track Circuit',  160.0, 'Major',    NOW()-INTERVAL'13 days', NOW()+INTERVAL'2 days',  'Open',    3.5),
('SMMS', 'S&T', 5, 'Signal',         220.0, 'Critical', NOW()-INTERVAL'21 days', NOW()-INTERVAL'2 days',  'Overdue', 7.0),
('SMMS', 'S&T', 5, 'Cable',          350.0, 'Minor',    NOW()-INTERVAL'3 days',  NOW()+INTERVAL'24 days', 'Open',    1.5),
('SMMS', 'S&T', 6, 'Interlocking',   70.0,  'Major',    NOW()-INTERVAL'8 days',  NOW()+INTERVAL'7 days',  'Open',    5.0),
('SMMS', 'S&T', 7, 'Signal',         190.0, 'Critical', NOW()-INTERVAL'26 days', NOW()-INTERVAL'6 days',  'Overdue', 9.0),
('SMMS', 'S&T', 8, 'Track Circuit',  100.0, 'Minor',    NOW()-INTERVAL'2 days',  NOW()+INTERVAL'28 days', 'Open',    2.0),
('SMMS', 'S&T', 9, 'Signal',         10.0,  'Major',    NOW()-INTERVAL'7 days',  NOW()+INTERVAL'9 days',  'Open',    4.0),
('SMMS', 'S&T', 10,'Cable',          80.0,  'Critical', NOW()-INTERVAL'28 days', NOW()-INTERVAL'8 days',  'Overdue', 6.5),
('SMMS', 'S&T', 1, 'Signal',         145.0, 'Minor',    NOW()-INTERVAL'5 days',  NOW()+INTERVAL'17 days', 'Open',    1.5),
('SMMS', 'S&T', 2, 'Interlocking',   180.0, 'Major',    NOW()-INTERVAL'10 days', NOW()+INTERVAL'6 days',  'Open',    5.5),
('SMMS', 'S&T', 3, 'Signal',         50.0,  'Critical', NOW()-INTERVAL'17 days', NOW()+INTERVAL'0 days',  'Overdue', 7.5),
('SMMS', 'S&T', 6, 'Track Circuit',  85.0,  'Minor',    NOW()-INTERVAL'1 days',  NOW()+INTERVAL'29 days', 'Open',    2.0),
('SMMS', 'S&T', 7, 'Signal',         210.0, 'Major',    NOW()-INTERVAL'12 days', NOW()+INTERVAL'3 days',  'Open',    3.5),
('SMMS', 'S&T', 8, 'Cable',          115.0, 'Minor',    NOW()-INTERVAL'3 days',  NOW()+INTERVAL'21 days', 'Open',    1.0),
('SMMS', 'S&T', 9, 'Interlocking',   12.0,  'Critical', NOW()-INTERVAL'23 days', NOW()-INTERVAL'3 days',  'Overdue', 8.0);

-- ---- DEFECTS — TDMS (Traction Distribution / Power) ----
INSERT INTO defect (source_system, department, corridor_id, asset_type, km_marker, severity, date_raised, due_date, status, estimated_repair_hours) VALUES
('TDMS', 'Traction Distribution', 1, 'OHE Wire',      55.0,  'Critical', NOW()-INTERVAL'14 days', NOW()+INTERVAL'2 days',  'Overdue', 5.0),
('TDMS', 'Traction Distribution', 1, 'Mast',           85.0,  'Major',    NOW()-INTERVAL'8 days',  NOW()+INTERVAL'6 days',  'Open',    3.0),
('TDMS', 'Traction Distribution', 2, 'OHE Wire',       115.0, 'Major',    NOW()-INTERVAL'12 days', NOW()+INTERVAL'4 days',  'Open',    4.0),
('TDMS', 'Traction Distribution', 3, 'Sub-station',    65.0,  'Critical', NOW()-INTERVAL'20 days', NOW()-INTERVAL'2 days',  'Overdue', 12.0),
('TDMS', 'Traction Distribution', 4, 'OHE Wire',       40.0,  'Minor',    NOW()-INTERVAL'5 days',  NOW()+INTERVAL'19 days', 'Open',    2.0),
('TDMS', 'Traction Distribution', 4, 'Mast',           170.0, 'Major',    NOW()-INTERVAL'15 days', NOW()+INTERVAL'1 days',  'Open',    3.5),
('TDMS', 'Traction Distribution', 5, 'OHE Wire',       240.0, 'Critical', NOW()-INTERVAL'22 days', NOW()-INTERVAL'4 days',  'Overdue', 6.0),
('TDMS', 'Traction Distribution', 5, 'Feeder',         380.0, 'Minor',    NOW()-INTERVAL'3 days',  NOW()+INTERVAL'23 days', 'Open',    1.5),
('TDMS', 'Traction Distribution', 6, 'Sub-station',    75.0,  'Major',    NOW()-INTERVAL'9 days',  NOW()+INTERVAL'8 days',  'Open',    8.0),
('TDMS', 'Traction Distribution', 7, 'OHE Wire',       195.0, 'Critical', NOW()-INTERVAL'27 days', NOW()-INTERVAL'7 days',  'Overdue', 5.5),
('TDMS', 'Traction Distribution', 8, 'Mast',           105.0, 'Minor',    NOW()-INTERVAL'2 days',  NOW()+INTERVAL'27 days', 'Open',    1.5),
('TDMS', 'Traction Distribution', 9, 'OHE Wire',       11.0,  'Major',    NOW()-INTERVAL'6 days',  NOW()+INTERVAL'10 days', 'Open',    3.0),
('TDMS', 'Traction Distribution', 10,'Feeder',          85.0,  'Critical', NOW()-INTERVAL'29 days', NOW()-INTERVAL'9 days',  'Overdue', 5.0),
('TDMS', 'Traction Distribution', 1, 'OHE Wire',       140.0, 'Minor',    NOW()-INTERVAL'4 days',  NOW()+INTERVAL'16 days', 'Open',    2.0),
('TDMS', 'Traction Distribution', 2, 'Sub-station',    195.0, 'Major',    NOW()-INTERVAL'11 days', NOW()+INTERVAL'5 days',  'Open',    7.0),
('TDMS', 'Traction Distribution', 3, 'OHE Wire',       55.0,  'Critical', NOW()-INTERVAL'18 days', NOW()+INTERVAL'1 days',  'Overdue', 5.5),
('TDMS', 'Traction Distribution', 6, 'Mast',           90.0,  'Minor',    NOW()-INTERVAL'1 days',  NOW()+INTERVAL'28 days', 'Open',    1.0),
('TDMS', 'Traction Distribution', 7, 'OHE Wire',       215.0, 'Major',    NOW()-INTERVAL'13 days', NOW()+INTERVAL'4 days',  'Open',    3.5),
('TDMS', 'Traction Distribution', 8, 'Feeder',         120.0, 'Minor',    NOW()-INTERVAL'2 days',  NOW()+INTERVAL'22 days', 'Open',    2.0),
('TDMS', 'Traction Distribution', 9, 'Sub-station',    13.0,  'Critical', NOW()-INTERVAL'24 days', NOW()-INTERVAL'4 days',  'Overdue', 10.0);

-- ---- BLOCK REQUESTS (BDMS) — sampling from defects above ----
-- defect IDs 1-20=TMS, 21-40=SMMS, 41-60=TDMS (sequential insert order)
INSERT INTO block_request (defect_id, requesting_department, corridor_id, requested_on, requested_window_date, requested_start_hour, requested_duration_hours, approval_status) VALUES
(1,  'Engineering',          1,  NOW()-INTERVAL'5 days',  NOW()+INTERVAL'3 days',  1,  6.0,  'Pending'),
(2,  'Engineering',          1,  NOW()-INTERVAL'4 days',  NOW()+INTERVAL'4 days',  2,  3.0,  'Pending'),
(3,  'Engineering',          2,  NOW()-INTERVAL'6 days',  NOW()+INTERVAL'2 days',  1,  8.0,  'Approved'),
(5,  'Engineering',          3,  NOW()-INTERVAL'3 days',  NOW()+INTERVAL'2 days',  0,  10.0, 'Pending'),
(8,  'Engineering',          4,  NOW()-INTERVAL'7 days',  NOW()+INTERVAL'3 days',  2,  4.0,  'Pending'),
(9,  'Engineering',          5,  NOW()-INTERVAL'8 days',  NOW()+INTERVAL'1 days',  1,  7.0,  'Approved'),
(13, 'Engineering',          8,  NOW()-INTERVAL'4 days',  NOW()+INTERVAL'2 days',  0,  9.0,  'Pending'),
(15, 'Engineering',          10, NOW()-INTERVAL'9 days',  NOW()+INTERVAL'1 days',  1,  6.0,  'Pending'),
(21, 'S&T',                  1,  NOW()-INTERVAL'5 days',  NOW()+INTERVAL'2 days',  1,  5.0,  'Pending'),
(22, 'S&T',                  1,  NOW()-INTERVAL'3 days',  NOW()+INTERVAL'4 days',  2,  2.5,  'Pending'),
(24, 'S&T',                  3,  NOW()-INTERVAL'7 days',  NOW()+INTERVAL'1 days',  0,  6.0,  'Approved'),
(27, 'S&T',                  5,  NOW()-INTERVAL'8 days',  NOW()+INTERVAL'1 days',  1,  5.5,  'Pending'),
(30, 'S&T',                  7,  NOW()-INTERVAL'9 days',  NOW()+INTERVAL'0 days',  2,  7.0,  'Approved'),
(33, 'S&T',                  10, NOW()-INTERVAL'10 days', NOW()+INTERVAL'1 days',  1,  5.0,  'Pending'),
(36, 'S&T',                  3,  NOW()-INTERVAL'6 days',  NOW()+INTERVAL'2 days',  0,  6.0,  'Pending'),
(41, 'Traction Distribution', 1, NOW()-INTERVAL'4 days',  NOW()+INTERVAL'3 days',  1,  4.0,  'Pending'),
(42, 'Traction Distribution', 1, NOW()-INTERVAL'3 days',  NOW()+INTERVAL'4 days',  2,  2.5,  'Pending'),
(44, 'Traction Distribution', 3, NOW()-INTERVAL'7 days',  NOW()+INTERVAL'1 days',  0,  10.0, 'Approved'),
(47, 'Traction Distribution', 5, NOW()-INTERVAL'8 days',  NOW()+INTERVAL'1 days',  1,  5.0,  'Pending'),
(50, 'Traction Distribution', 7, NOW()-INTERVAL'9 days',  NOW()+INTERVAL'0 days',  2,  4.5,  'Approved'),
(53, 'Traction Distribution', 10,NOW()-INTERVAL'10 days', NOW()+INTERVAL'1 days',  1,  4.0,  'Pending'),
(56, 'Traction Distribution', 3, NOW()-INTERVAL'6 days',  NOW()+INTERVAL'2 days',  0,  4.5,  'Pending'),
(60, 'Traction Distribution', 9, NOW()-INTERVAL'5 days',  NOW()+INTERVAL'2 days',  1,  8.0,  'Pending');

-- ---- COA AVAILABILITY — 45 windows across next 14 days ----
DO $$
DECLARE
    d DATE;
    c INT;
BEGIN
    FOR c IN 1..10 LOOP
        FOR d IN
            SELECT generate_series(NOW()::DATE, (NOW() + INTERVAL '13 days')::DATE, INTERVAL '1 day')::DATE
        LOOP
            -- Night window 1-4am (most corridors)
            IF c <= 7 THEN
                INSERT INTO coa_availability (corridor_id, available_date, window_start_hour, max_duration_hours, reason)
                VALUES (c, d, 1, 3.0, 'Night maintenance window — low traffic')
                ON CONFLICT (corridor_id, available_date, window_start_hour) DO NOTHING;
            END IF;
            -- Extended window on weekends (Sat/Sun)
            IF EXTRACT(DOW FROM d) IN (0, 6) THEN
                INSERT INTO coa_availability (corridor_id, available_date, window_start_hour, max_duration_hours, reason)
                VALUES (c, d, 0, 5.0, 'Weekend extended maintenance window')
                ON CONFLICT (corridor_id, available_date, window_start_hour) DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;
