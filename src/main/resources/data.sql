insert into accounts (id, balance, currency, created_at, updated_at)
select 
    (lpad(seq::text, 8, '0') || '-1111-1111-1111-111111111111')::uuid, -- Conversão explícita adicionada aqui
    100000.00,
    'BRL', 
    now(), 
    now()
from generate_series(1, 50000) as seq
on conflict (id) do nothing;
