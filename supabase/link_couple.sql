-- =====================================================================
-- Sophames — link the two accounts into one couple (run ONCE, after schema.sql)
-- Author: direk james
--
-- 1. Create both users first: Authentication > Users > Add user.
-- 2. Replace the two emails below with the exact emails you used.
-- 3. Run in the SQL Editor.
-- =====================================================================

with new_couple as (
  insert into public.couples (start_date)
  values (date '2025-01-18')
  returning id
)
insert into public.profiles (user_id, couple_id, display_name)
select u.id, (select id from new_couple), v.name
from auth.users u
join (values
  ('james@example.com',  'James'),     -- <-- your email
  ('sophia@example.com', 'Sophia')     -- <-- her email
) as v(email, name) on v.email = u.email
on conflict (user_id) do update
  set couple_id = excluded.couple_id, display_name = excluded.display_name;

-- Check: should return exactly 2 rows sharing the same couple_id.
select p.display_name, u.email, p.couple_id
from public.profiles p
join auth.users u on u.id = p.user_id;
