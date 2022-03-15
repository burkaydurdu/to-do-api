## TODO API

#### Run
``````
lein deps
lein ring server-headless 3011
``````
#### Migrate
```
lein migratus migrate
```
[Migratus](https://github.com/yiogthos/migratus)

### Note
- You should create database in docker postgresql container.
```
psql -U bdurdu

CREATE DATABASE to_do;
```
- You should run migration in docker to_do_api container.
