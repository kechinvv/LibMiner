create table if not exists
    sequence(id integer primary key,
              json_data string,
              class string,
              samples integer);

create table if not exists
    method(id integer primary key,
            name string,
            class string,
            UNIQUE(method_name,class));

create table if not exists
    repository(id integer primary key,
                name string,
                source string);