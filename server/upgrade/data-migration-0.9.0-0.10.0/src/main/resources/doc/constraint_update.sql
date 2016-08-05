ALTER TABLE configuration_schems DROP FOREIGN KEY FK_6c1w1hvw3794uenuce20voli9;

ALTER TABLE configuration DROP FOREIGN KEY FK_smcmof238to6x3ta7enujgd8m;
ALTER TABLE configuration add constraint `FK_configuration_schems_id`
FOREIGN KEY (`configuration_schems_id`) REFERENCES `configuration_schems` (`id`)  ON DELETE CASCADE ON UPDATE CASCADE;