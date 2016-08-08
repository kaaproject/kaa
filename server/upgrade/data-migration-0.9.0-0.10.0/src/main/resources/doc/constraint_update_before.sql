SET @const_name = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA = 'kaa' AND TABLE_NAME = 'configuration_schems' and referenced_table_name='schems');
SET @sql = CONCAT('ALTER TABLE configuration_schems DROP FOREIGN KEY  ', @const_name);
PREPARE s from @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

SET @const_name =  (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA = 'kaa' AND TABLE_NAME = 'configuration' and referenced_table_name='configuration_schems');
SET @sql = CONCAT('ALTER TABLE configuration DROP FOREIGN KEY ', @const_name);
PREPARE s from @sql;
EXECUTE s;
DEALLOCATE PREPARE s;

ALTER TABLE configuration add constraint `FK_configuration_schems_id`
FOREIGN KEY (`configuration_schems_id`) REFERENCES `configuration_schems` (`id`)  ON DELETE CASCADE ON UPDATE CASCADE;