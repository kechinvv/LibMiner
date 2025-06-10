create table if not exists
    sequence(id integer primary key,
              trace text,
              class text,
              samples integer,
              extract_method text,
              UNIQUE(trace, class, extract_method)
);


create table if not exists
    repository(id integer primary key,
                repo_name text,
                namespace text,
                version text,
                author text,
                url text unique,
                source text,
                path text,
                date datetime
);

