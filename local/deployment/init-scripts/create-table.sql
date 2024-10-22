-- Enable the UUID-OSSP extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    role VARCHAR,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    email VARCHAR UNIQUE NOT NULL,
    image_url VARCHAR,
    email_verified BOOLEAN DEFAULT FALSE,
    provider VARCHAR NOT NULL,
    is_expired BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_credentials_expired BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- Indexes for the users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_email_role ON users(email, role);

-- Create the banks table
CREATE TABLE banks (
    bank_id SERIAL PRIMARY KEY,
    bank_name VARCHAR NOT NULL
);

-- Function to create partitions for new banks automatically
CREATE OR REPLACE FUNCTION create_bank_partition(new_bank_id INTEGER)
RETURNS void AS $$
DECLARE
    partition_table_name text := 'points_bank_' || new_bank_id;
    create_table_command text;
BEGIN
    -- Construct the CREATE TABLE command for the new partition
    create_table_command := format('CREATE TABLE IF NOT EXISTS %I PARTITION OF points FOR VALUES IN (%L);', 
                                   partition_table_name, new_bank_id);
    -- Execute the dynamic command
    EXECUTE create_table_command;
EXCEPTION
    WHEN duplicate_table THEN
        -- If the partition already exists, do nothing
        NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger function to call the partition creation function after a bank is inserted
CREATE OR REPLACE FUNCTION after_bank_insert()
RETURNS trigger AS $$
BEGIN
    -- Call the function to create a new partition for the bank_id
    PERFORM create_bank_partition(NEW.bank_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to execute after each insert into banks table
CREATE TRIGGER trigger_after_bank_insert
AFTER INSERT ON banks
FOR EACH ROW EXECUTE FUNCTION after_bank_insert();

-- Create the points table partitioned by list on bank_id
CREATE TABLE points (
    points_id UUID DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    points_balance INTEGER NOT NULL,
    bank_id INTEGER NOT NULL REFERENCES banks(bank_id),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (points_id, bank_id)
) PARTITION BY LIST (bank_id);

-- Trigger and function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Policy table and JSON policy example
CREATE TABLE IF NOT EXISTS policies (
    id SERIAL PRIMARY KEY,
    policy JSONB NOT NULL
);

-- Step 2: Insert the provided policy document
INSERT INTO policies (policy) VALUES (
'{
  "Owner": {
    "UserStorage": {
      "actions": ["read", "create", "update", "delete"]
    },
    "PointsLedger": {
      "actions": ["read", "update"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Manager": {
    "UserStorage": {
      "actions": ["read", "create", "update"]
    },
    "PointsLedger": {
      "actions": ["read", "update"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Engineer": {
    "UserStorage": {
      "actions": ["read"]
    },
    "PointsLedger": {
      "actions": ["read"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Product Manager": {
    "UserStorage": {
      "actions": ["read"],
      "constraints": ["exclude-admins"]
    },
    "PointsLedger": {
      "actions": ["read"]
    }
  }
}
'::JSONB);



-- Remember, when a new bank is added, the trigger will automatically create a new partition for that bank in the points table.
-- Manual creation of partitions for each bank at the schema setup is not required anymore.


-- Ensure there is a bank with bank_id = 1
INSERT INTO banks (bank_name) VALUES ('DBS Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('POSB Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('UOB Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('CIMB Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('OCBC Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('SMU Bank') ON CONFLICT (bank_id) DO NOTHING;
INSERT INTO banks (bank_name) VALUES ('Neil Bank') ON CONFLICT (bank_id) DO NOTHING;


-- Insert the user Neil Scallywag
INSERT INTO users (name, first_name, last_name, email, provider,role, created_at, updated_at)
VALUES ('Neil Scallywag', 'Neil', 'Scallywag', 'neilscallywag@gmail.com', 'google','Owner', NOW(), NOW());

INSERT INTO users (name, first_name, last_name, email, provider,role, created_at, updated_at)
VALUES ('Test User', 'Test User', 'ScallTeywag', 'nellywag@gmail.com', 'google','', NOW(), NOW());

INSERT INTO users (name, first_name, last_name, email, provider,role, created_at, updated_at)
VALUES ('Test User2', 'Test User', 'ScallTeywag', 'nellywaqwewqeg@gmail.com', 'google','Owner', NOW(), NOW());

INSERT INTO users (name, first_name, last_name, email, provider,role, created_at, updated_at)
VALUES ('wai wai', 'wai', 'wai', 'petyouyou12@gmail.com', 'google','Owner', NOW(), NOW());


-- Get the UUID of Neil Scallywag just inserted to use it for the points account
-- Note: This assumes 'email' is unique and Neil Scallywag is the only user with this email
INSERT INTO points (user_id, points_balance, bank_id, created_at, updated_at)
SELECT id, 100, 1, NOW(), NOW()
FROM users
WHERE email = 'neilscallywag@gmail.com';

-- SQL Script to create the policies table and insert the JSON policy document

-- Step 1: Create the table
CREATE TABLE IF NOT EXISTS policies (
    id SERIAL PRIMARY KEY,
    policy JSONB NOT NULL
);

-- Step 2: Insert the provided policy document
INSERT INTO policies (policy) VALUES (
'{
  "Owner": {
    "UserStorage": {
      "actions": ["read", "create", "update", "delete"]
    },
    "PointsLedger": {
      "actions": ["read", "update"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Manager": {
    "UserStorage": {
      "actions": ["read", "create", "update"]
    },
    "PointsLedger": {
      "actions": ["read", "update"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Engineer": {
    "UserStorage": {
      "actions": ["read"]
    },
    "PointsLedger": {
      "actions": ["read"]
    },
    "Logs": {
      "actions": ["read"]
    }
  },
  "Product Manager": {
    "UserStorage": {
      "actions": ["read"],
      "constraints": ["exclude-admins"]
    },
    "PointsLedger": {
      "actions": ["read"]
    }
  }
}
'::JSONB);
