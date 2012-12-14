create table ONT_PROCESS_STATUS (
    process_id   int identity(1,1) PRIMARY KEY, 
    process_type_cd varchar(50),
    start_date   datetime, 
    end_date     datetime,
    process_step_cd   varchar(50),
    process_status_cd   varchar(50),
    crc_upload_id  varchar(5),
    message      varchar(2000),
    status_cd    varchar(50),
    entry_date   datetime,
    change_date  datetime,
    changedby_char char(50)
);