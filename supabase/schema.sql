-- =====================================================================
-- Sophames — Supabase schema
-- Author: direk james
--
-- Run ONCE in: Supabase Dashboard > SQL Editor > New query > paste > Run.
-- Safe to re-run: everything is "if not exists" / "drop ... if exists".
-- =====================================================================

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------

create table if not exists public.couples (
  id          uuid primary key default gen_random_uuid(),
  invite_code text not null unique
              default upper(substr(replace(gen_random_uuid()::text, '-', ''), 1, 8)),
  start_date  date not null default date '2025-01-18',
  created_at  timestamptz not null default now()
);

create table if not exists public.profiles (
  user_id      uuid primary key references auth.users(id) on delete cascade,
  couple_id    uuid references public.couples(id) on delete set null,
  display_name text not null default '',
  created_at   timestamptz not null default now()
);

create table if not exists public.memories (
  id          uuid primary key,                       -- generated on the phone (offline-first)
  couple_id   uuid not null references public.couples(id) on delete cascade,
  author_id   uuid default auth.uid() references auth.users(id) on delete set null,
  title       text not null default '',
  note        text not null default '',
  memory_date date not null,
  photo_path  text,                                   -- "<couple_id>/<file>.jpg" in the 'memories' bucket
  deleted     boolean not null default false,         -- soft delete so deletions sync too
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);

create table if not exists public.events (
  id            uuid primary key,
  couple_id     uuid not null references public.couples(id) on delete cascade,
  author_id     uuid default auth.uid() references auth.users(id) on delete set null,
  title         text not null default '',
  event_date    date not null,
  repeat_yearly boolean not null default true,
  deleted       boolean not null default false,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);

create index if not exists memories_couple_updated on public.memories (couple_id, updated_at);
create index if not exists events_couple_updated   on public.events   (couple_id, updated_at);

-- The server stamps updated_at, so "give me everything changed since X" is reliable.
create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end $$;

drop trigger if exists memories_touch on public.memories;
create trigger memories_touch before insert or update on public.memories
  for each row execute function public.touch_updated_at();

drop trigger if exists events_touch on public.events;
create trigger events_touch before insert or update on public.events
  for each row execute function public.touch_updated_at();

-- ---------------------------------------------------------------------
-- Helper: which couple does the signed-in user belong to?
-- (security definer so policies can call it without recursing into RLS)
-- ---------------------------------------------------------------------

create or replace function public.my_couple_id()
returns uuid
language sql stable security definer
set search_path = public
as $$
  select couple_id from public.profiles where user_id = auth.uid()
$$;

-- ---------------------------------------------------------------------
-- Optional pairing functions (invite-code flow, for a future in-app screen).
-- For the surprise you will link the two accounts with link_couple.sql instead.
-- ---------------------------------------------------------------------

create or replace function public.create_couple(p_display_name text default '')
returns public.couples
language plpgsql security definer
set search_path = public
as $$
declare c public.couples;
begin
  if auth.uid() is null then raise exception 'not authenticated'; end if;
  if exists (select 1 from public.profiles where user_id = auth.uid() and couple_id is not null) then
    raise exception 'you are already in a couple';
  end if;
  insert into public.couples default values returning * into c;
  insert into public.profiles (user_id, couple_id, display_name)
    values (auth.uid(), c.id, p_display_name)
    on conflict (user_id) do update
      set couple_id = excluded.couple_id, display_name = excluded.display_name;
  return c;
end $$;

create or replace function public.join_couple(p_code text, p_display_name text default '')
returns public.couples
language plpgsql security definer
set search_path = public
as $$
declare c public.couples; members int;
begin
  if auth.uid() is null then raise exception 'not authenticated'; end if;
  if exists (select 1 from public.profiles where user_id = auth.uid() and couple_id is not null) then
    raise exception 'you are already in a couple';
  end if;
  select * into c from public.couples where invite_code = upper(trim(p_code));
  if not found then raise exception 'invalid invite code'; end if;
  select count(*) into members from public.profiles where couple_id = c.id;
  if members >= 2 then raise exception 'this couple is already full'; end if;
  insert into public.profiles (user_id, couple_id, display_name)
    values (auth.uid(), c.id, p_display_name)
    on conflict (user_id) do update
      set couple_id = excluded.couple_id, display_name = excluded.display_name;
  return c;
end $$;

revoke execute on function public.my_couple_id()               from PUBLIC, anon;
revoke execute on function public.create_couple(text)          from PUBLIC, anon;
revoke execute on function public.join_couple(text, text)      from PUBLIC, anon;
grant  execute on function public.my_couple_id()               to authenticated;
grant  execute on function public.create_couple(text)          to authenticated;
grant  execute on function public.join_couple(text, text)      to authenticated;

-- ---------------------------------------------------------------------
-- Row Level Security — ONLY the two members of a couple can see or change its rows.
-- This is what keeps your memories private even though the anon key ships inside the APK.
-- ---------------------------------------------------------------------

alter table public.couples  enable row level security;
alter table public.profiles enable row level security;
alter table public.memories enable row level security;
alter table public.events   enable row level security;

-- couples
drop policy if exists "members read their couple"   on public.couples;
drop policy if exists "members update their couple" on public.couples;
create policy "members read their couple" on public.couples
  for select to authenticated using (id = public.my_couple_id());
create policy "members update their couple" on public.couples
  for update to authenticated
  using (id = public.my_couple_id()) with check (id = public.my_couple_id());

-- profiles (joining a couple only happens through the functions above)
drop policy if exists "read own and partner profile" on public.profiles;
drop policy if exists "update own profile"           on public.profiles;
create policy "read own and partner profile" on public.profiles
  for select to authenticated
  using (user_id = auth.uid() or couple_id = public.my_couple_id());
create policy "update own profile" on public.profiles
  for update to authenticated
  using (user_id = auth.uid()) with check (user_id = auth.uid());

-- memories
drop policy if exists "couple reads memories"   on public.memories;
drop policy if exists "couple inserts memories" on public.memories;
drop policy if exists "couple updates memories" on public.memories;
create policy "couple reads memories" on public.memories
  for select to authenticated using (couple_id = public.my_couple_id());
create policy "couple inserts memories" on public.memories
  for insert to authenticated
  with check (couple_id = public.my_couple_id() and author_id = auth.uid());
create policy "couple updates memories" on public.memories
  for update to authenticated
  using (couple_id = public.my_couple_id()) with check (couple_id = public.my_couple_id());

-- events
drop policy if exists "couple reads events"   on public.events;
drop policy if exists "couple inserts events" on public.events;
drop policy if exists "couple updates events" on public.events;
create policy "couple reads events" on public.events
  for select to authenticated using (couple_id = public.my_couple_id());
create policy "couple inserts events" on public.events
  for insert to authenticated
  with check (couple_id = public.my_couple_id() and author_id = auth.uid());
create policy "couple updates events" on public.events
  for update to authenticated
  using (couple_id = public.my_couple_id()) with check (couple_id = public.my_couple_id());

-- Table privileges: signed-out visitors get nothing; signed-in users get only what the app needs.
revoke all on public.couples, public.profiles, public.memories, public.events from anon;

revoke insert, delete on public.couples from authenticated;
revoke update         on public.couples from authenticated;
grant  update (start_date) on public.couples to authenticated;

revoke insert, update, delete on public.profiles from authenticated;
grant  update (display_name)  on public.profiles to authenticated;

revoke delete on public.memories from authenticated;   -- the app soft-deletes (deleted = true)
revoke delete on public.events   from authenticated;

-- ---------------------------------------------------------------------
-- Realtime (live updates while the app is open)
-- ---------------------------------------------------------------------

do $$
begin
  begin alter publication supabase_realtime add table public.memories; exception when duplicate_object then null; end;
  begin alter publication supabase_realtime add table public.events;   exception when duplicate_object then null; end;
end $$;

-- ---------------------------------------------------------------------
-- Photo storage: a PRIVATE bucket; each couple can only touch files under its own folder.
-- Upload path convention: "<couple_id>/<file>.jpg"
-- ---------------------------------------------------------------------

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('memories', 'memories', false, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;

drop policy if exists "couple reads photos"   on storage.objects;
drop policy if exists "couple uploads photos" on storage.objects;
drop policy if exists "couple updates photos" on storage.objects;
drop policy if exists "couple deletes photos" on storage.objects;

create policy "couple reads photos" on storage.objects
  for select to authenticated
  using (bucket_id = 'memories' and (storage.foldername(name))[1] = public.my_couple_id()::text);
create policy "couple uploads photos" on storage.objects
  for insert to authenticated
  with check (bucket_id = 'memories' and (storage.foldername(name))[1] = public.my_couple_id()::text);
create policy "couple updates photos" on storage.objects
  for update to authenticated
  using (bucket_id = 'memories' and (storage.foldername(name))[1] = public.my_couple_id()::text);
create policy "couple deletes photos" on storage.objects
  for delete to authenticated
  using (bucket_id = 'memories' and (storage.foldername(name))[1] = public.my_couple_id()::text);
