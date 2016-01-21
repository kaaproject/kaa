DROP KEYSPACE IF EXISTS kaa_test;

CREATE KEYSPACE IF NOT EXISTS kaa_test
WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };