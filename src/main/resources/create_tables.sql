create table if not exists
    sequence(id integer primary key,
              trace text,
              class text,
              is_static boolean,
              samples integer,
              UNIQUE(trace, class)
);

create table if not exists
    method(id integer primary key,
            method_name text,
            args text,
            return_type text,
            class text,
            is_static boolean,
            UNIQUE(method_name, class, args)
);

create table if not exists
    repository(id integer primary key,
                repo_name text,
                namespace text,
                version text,
                author text,
                locator text unique,
                source text,
                date datetime
);