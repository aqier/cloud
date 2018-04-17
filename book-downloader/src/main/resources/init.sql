select * from novel where rownum = 1;

create table behavioral_statistics (
    id varchar(32) primary key,
    
    source varchar(16) not null, /*请求来源*/
    target varchar(256) not null, /*请求目标*/
    data varchar(4000), /*请求数据*/
    use_time number(8) not null, /*处理请求耗时(毫秒)*/
    
    deleted_flag varchar(4) not null default 'no',
    version_number bigint not null default 0,
    create_by varchar(32) not null,
    create_date datetime not null,
    last_update_by varchar(32) not null,
    last_update_date datetime not null
);

create table novel(
    id varchar(32) primary key,
    
    name varchar(4000) not null, /*名称*/
    author varchar(256), /*作者*/
    catalog_url varchar(4000) not null, /*章节目录*/
    chapter_number number(8,0) not null, /*章节总数*/
    
    deleted_flag varchar(4) not null default 'no',
    version_number bigint not null default 0,
    create_by varchar(32) not null,
    create_date datetime not null,
    last_update_by varchar(32) not null,
    last_update_date datetime not null
);

create table chapter(
    id varchar(32) primary key,
    
    novel_id varchar(32) not null,/*所属书籍编号*/
    serial_number number(8,0), /*章节序号*/
    title varchar(4000) not null, /*章节标题*/
    content_url varchar(4000), /*内容URL路径*/
    content clob, /*内容*/
	
    deleted_flag varchar(4) not null default 'no',
    version_number bigint not null default 0,
    create_by varchar(32) not null,
    create_date datetime not null,
    last_update_by varchar(32) not null,
    last_update_date datetime not null
);