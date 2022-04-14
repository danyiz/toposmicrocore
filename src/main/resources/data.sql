INSERT INTO public.transaction_balances
(id, balance_components, create_date, modify_date, schema_code, transaction_group, custom_analitycal_attributes, transaction_code)
VALUES(3, '{"BOOK_BALANCE": 1, "VALUE_BALANCE": 1, "DUE_PENALTY_INTEREST": 1, "TOTAL_PENALTY_INTEREST": 1}'::jsonb, '2022-03-24 13:28:55.586', '2022-03-24 13:28:55.586', 'LOAN1', 'PENALTY_INTEREST', NULL, NULL);
INSERT INTO public.transaction_balances
(id, balance_components, create_date, modify_date, schema_code, transaction_group, custom_analitycal_attributes, transaction_code)
VALUES(2, '{"BOOK_BALANCE": 1, "DUE_INTEREST": 1, "VALUE_BALANCE": 1, "TOTAL_INTEREST": 1}'::jsonb, '2022-03-24 13:28:55.576', '2022-03-24 13:28:55.576', 'LOAN1', 'INTEREST', NULL, NULL);
INSERT INTO public.transaction_balances
(id, balance_components, create_date, modify_date, schema_code, transaction_group, custom_analitycal_attributes, transaction_code)
VALUES(1, '{"OVERPAYMENT": 1, "BOOK_BALANCE": 1, "VALUE_BALANCE": 1}'::jsonb, '2022-03-24 13:28:55.483', '2022-03-24 13:28:55.483', 'LOAN1', 'PAYMENT', NULL, NULL);
