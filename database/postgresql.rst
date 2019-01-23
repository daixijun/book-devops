PostgreSQL
==========

.. code-block:: sh

    apt-get install --yes postgresql
    cat /etc/postgresql/9.3/main/pg_hba.conf

.. code-block:: text

    # TYPE DATABASE USER        ADDRESS         METHOD
    local   all     postgres                    peer
    local   all     all                         peer
    host    all     all         127.0.0.1/32    md5
    host    all     all         0.0.0.0/0       md5
    host    all     all         ::1/128         md5

.. code-block:: sh

    cat /etc/postgresql/9.3/main/postgresql.conf

.. code-block:: cfg

    data_directory = '/var/lib/postgresql/9.3/main'
    hba_file = '/etc/postgresql/9.3/main/pg_hba.conf'
    ident_file = '/etc/postgresql/9.3/main/pg_ident.conf'
    external_pid_file = '/var/run/postgresql/9.3-main.pid'
    listen_addresses = '*'
    port = 5432
    max_connections = 100
    unix_socket_directories = '/var/run/postgresql'
    ssl = true
    ssl_cert_file = '/etc/ssl/certs/ssl-cert-snakeoil.pem'
    ssl_key_file = '/etc/ssl/private/ssl-cert-snakeoil.key'
    shared_buffers = 128MB
    log_line_prefix = '%t '
    log_timezone = 'UTC'
    datestyle = 'iso, mdy'
    timezone = 'UTC'
    lc_messages = 'en_US.UTF-8'
    lc_monetary = 'en_US.UTF-8'
    lc_numeric = 'en_US.UTF-8'
    lc_time = 'en_US.UTF-8'
    default_text_search_config = 'pg_catalog.english'

.. code-block:: sh

    service postgresql restart
