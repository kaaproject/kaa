1. Get all ids from configuration_schema table
2. Remember all records related to particular ids from schems table and then delete these from the table 
3. Get the highest identifier from base_schems table and add this number to all ids in configuration schema table and created in previous step mapping 
4. Create CTL (for each id)
    4.1. Parse field schems : longtext -> get fqn and default record 
    4.2. Retrieve App by app_id and get tenant_id 
    4.3. Create ctl_metainfo base on fqn, tenant_id and app_id
    4.4. Create record in ctl based on data created on previous steps
    4.5. Create new mapping -- ctl_id -> configuration record 
5. Group record with the same fqn (schema), app_id and tenant_id    
6. Based on data created on fourth step and mapping in second step -- create records for base_schems that relate to records in configuration schema  table