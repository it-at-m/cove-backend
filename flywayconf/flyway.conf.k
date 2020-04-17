# Jdbc url to use to connect to the database
# Oracle*           : jdbc:oracle:thin:@//<host>:<port>/<service>
flyway.url=jdbc:<database>:thin:@//<host>:<port>/<service>

# Fully qualified classname of the jdbc driver (autodetected by default based on flyway.url)
# flyway.driver=

# User to use to connect to the database. Flyway will prompt you to enter it if not specified.
flyway.user=

# Password to use to connect to the database. Flyway will prompt you to enter it if not specified.

# Comma-separated list of locations to scan recursively for migrations. (default: filesystem:<<INSTALL-DIR>>/sql)
# The location type is determined by its prefix.
# Unprefixed locations or locations starting with classpath: point to a package on the classpath and may contain both sql and java-based migrations.
# Locations starting with filesystem: point to a directory on the filesystem and may only contain sql migrations.
flyway.locations=filesystem:src/main/resources/db/migration/oracle/dml,filesystem:src/main/resources/db/migration/oracle/ddl

# Whether to automatically call validate or not when running migrate. (default: true)
flyway.validateOnMigrate=true

# Whether to automatically call clean or not when a validation error occurs. (default: false)
# This is exclusively intended as a convenience for development. Even tough we
# strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
# way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
# the next migration will bring you back to the state checked into SCM.
# Warning ! Do not enable in production !
flyway.cleanOnValidationError=false

# Whether to disabled clean. (default: false)
# This is especially useful for production environments where running clean can be quite a career limiting move.
#flyway.cleanDisabled=true
flyway.table=schema-version
