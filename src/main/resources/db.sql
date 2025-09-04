-- Create the PERSONS table
CREATE TABLE airdocs.PERSONS (
    USER_ID BIGINT PRIMARY KEY,
    FULL_NAME VARCHAR(255) NOT NULL,
    DATE_OF_BIRTH DATE,
    STREET VARCHAR(255),
    CITY VARCHAR(255),
    POSTAL_CODE VARCHAR(20),	
    DATA JSONB
);

-- Create the PRODUCTS table
CREATE TABLE airdocs.PRODUCTS (
    ID BIGINT PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    PRICE DECIMAL(10, 2),
    CATEGORY VARCHAR(100),
    JSON_DATA JSONB
);

-- Optional: Insert sample data into the tables

-- Insert data into PERSONS table
INSERT INTO airdocs.PERSONS (USER_ID, FULL_NAME, DATE_OF_BIRTH, STREET, CITY, POSTAL_CODE) VALUES
(1, 'John Doe', '1990-05-15', '123 Main St', 'Anytown', '12345'),
(2, 'Jane Smith', '1985-11-20', '456 Oak Ave', 'Somewhere', '67890');

-- Insert data into PRODUCTS table
INSERT INTO airdocs.PRODUCTS (ID, NAME, PRICE, CATEGORY) VALUES
(101, 'Laptop', 1200.00, 'Electronics'),
(102, 'T-Shirt', 25.50, 'Apparel'),
(103, 'Coffee Maker', 89.99, 'Home Goods');


-- Grant USAGE permission on the schema so the user can access its contents
GRANT USAGE ON SCHEMA airdocs TO app_admin_meta;

-- Grant SELECT permission on all existing tables in the schema
GRANT SELECT ON ALL TABLES IN SCHEMA airdocs TO app_admin_meta;

-- Grant UPDATE permission on all existing tables in the schema
GRANT UPDATE ON ALL TABLES IN SCHEMA airdocs TO app_admin_meta;

-- Optional: For future tables, you can set default permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA airdocs GRANT SELECT, UPDATE ON TABLES TO app_admin_meta;


-- Create the new table for employees
CREATE TABLE airdocs.employees (
    employee_id BIGINT PRIMARY KEY,
    employee_name VARCHAR(255),
    employee_age INT,
    employee_phone VARCHAR(255),
    street VARCHAR(255),
    city VARCHAR(255),
    zip_code VARCHAR(20),
    data JSONB -- Existing JSONB column
);

-- Insert record 1 (with phone number)
INSERT INTO airdocs.employees (
    employee_id,
    employee_name,
    employee_age,
    employee_phone,
    street,
    city,
    zip_code
) VALUES (
    1,
    'John Doe',
    20,
    '123456,567899',
    '123 Main St',
    'Anytown',
    '12345'
);

-- Insert record 2 (without phone number)
INSERT INTO airdocs.employees (
    employee_id,
    employee_name,
    employee_age,
    employee_phone,
    street,
    city,
    zip_code
) VALUES (
    2,
    'Jane Smith',
    25,
    NULL,
    '456 Oak Ave',
    'Somewhere',
    '67890'
);

SELECT * FROM airdocs.employees
ORDER BY employee_id ASC;

-- Add the new column to the existing table
ALTER TABLE airdocs.employees ADD COLUMN active_status INT;

-- Update the first record to have a value of 1 (will include phone)
UPDATE airdocs.employees SET active_status = 1 WHERE employee_id = 1;

-- Update the second record to have a value of 0 (will include address)
UPDATE airdocs.employees SET active_status = 0 WHERE employee_id = 2;


-- Table: airdocs.employees

-- DROP TABLE IF EXISTS airdocs.employees;

CREATE TABLE IF NOT EXISTS airdocs.employees
(
    employee_id bigint NOT NULL,
    employee_name character varying(255) COLLATE pg_catalog."default",
    employee_age integer,
    employee_phone character varying(255) COLLATE pg_catalog."default",
    street character varying(255) COLLATE pg_catalog."default",
    city character varying(255) COLLATE pg_catalog."default",
    zip_code character varying(20) COLLATE pg_catalog."default",
    data jsonb,
    active_status integer,
    CONSTRAINT employees_pkey PRIMARY KEY (employee_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS airdocs.employees
    OWNER to postgres;

REVOKE ALL ON TABLE airdocs.employees FROM app_admin_meta;

GRANT UPDATE, SELECT ON TABLE airdocs.employees TO app_admin_meta;

GRANT ALL ON TABLE airdocs.employees TO postgres;
