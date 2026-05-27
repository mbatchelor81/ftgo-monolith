FROM mysql:8.0
COPY schema.sql /docker-entrypoint-initdb.d/schema.sql
CMD ["mysqld", "--default-authentication-plugin=mysql_native_password"]
