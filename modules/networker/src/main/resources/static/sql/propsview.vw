create or replace view velkom.propsview as
select `mem`.`properties`.`idrec` AS `idrec`,`mem`.`properties`.`property` AS `property`,`mem`.`properties`.`valueofproperty` AS `valueofproperty`,`mem`.`properties`.`timeSet` AS `timeSet`,`mem`.`properties`.`setter` AS `setter` from `mem`.`properties`
