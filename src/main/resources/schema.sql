create table if not exists accounts (
    id uuid primary key,
    balance numeric(19, 2) not null,
    currency varchar(3) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists transactions (
    id uuid primary key,
    debit_account_id uuid not null references accounts(id),
    credit_account_id uuid not null references accounts(id),
    amount numeric(19, 2) not null,
    currency varchar(3) not null,
    status varchar(20) not null,
    idempotency_key varchar(80) not null,
    created_at timestamptz not null
);

create index if not exists idx_transactions_idempotency_key on transactions(idempotency_key);

create table if not exists ledger_entries (
    id uuid primary key,
    transaction_id uuid not null references transactions(id),
    account_id uuid not null references accounts(id),
    entry_type varchar(10) not null,
    amount numeric(19, 2) not null,
    currency varchar(3) not null,
    created_at timestamptz not null
);

create index if not exists idx_ledger_entries_transaction on ledger_entries(transaction_id);

create table if not exists idempotency_keys (
    id uuid primary key,
    idempotency_key varchar(80) not null unique,
    request_hash varchar(128) not null,
    response_status int,
    response_body text,
    state varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_idempotency_state on idempotency_keys(state);
