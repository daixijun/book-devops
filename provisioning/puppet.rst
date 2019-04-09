Puppet
======

.. todo:: sprawdzić czy działają tematy związane z tworzeniem faktów
.. todo:: sprawdzić jak zachowa się to z Facterem
.. todo:: sprawdzić deklarowanie i używanie zmiennych
.. todo:: podzielić puppeta na osobne pliki per temat (zadanie do rozwiązania)
.. todo:: co z tematem odpalania jako user a nie root?
.. todo:: uspójnić wszędzie nazwy userów i grup (vagrant, ubuntu, www-data, myuser) wybrać jeden
.. todo:: błąd ze sprawdzaniem czy user i grupa www-data istnieją, kiedy wykorzystujemy moduł apache

.. contents::

Architektura
------------

* zasada działania
* infrastructure as a code
* ad hoc changes
* scaling
* wprowadzanie zmian w zależności od faktów
* developer env changes
* instalacja ręczna pakietów

Jak działa
^^^^^^^^^^
manifest.pp

.. code-block:: ruby

    file { '/var/www':
        ensure => 'directory',
        owner => 'www-data',
        group => 'www-data',
        mode  => '0755',
    }

.. code-block:: ruby

    exec { 'package definition update':
        command => '/usr/bin/apt update',
    }

    package { ['nmap', 'htop', 'git']:
        ensure => 'latest',
        require => Exec['package definition update'],
    }

Model
^^^^^
* klient server
* standalone - puppet apply
* fakty i kolejność wykonywania manifestów

Components
^^^^^^^^^^
* manifests (pliki z rozszerzeniem ``.pp``)
* zmienne
* classes
* resources
* facts

Puppet language
^^^^^^^^^^^^^^^
* DSL
* ruby
* ERB templates

Templates
---------
* ERB templates

Comment
^^^^^^^
.. code-block:: erb

    <%# This is a comment. %>

Variables
^^^^^^^^^
There are two ways to access variables in an ERB template:

.. code-block:: erb

    @variable

.. code-block:: erb

    scope['variable']

Example:

    .. code-block:: erb

        scope['ntp::tinker']

Printing variables
^^^^^^^^^^^^^^^^^^
.. code-block:: erb

    ServerName <%= @fqdn %>
    ServerAlias <%= @hostname %>

If
^^
.. code-block:: text

    if <CONDITION>
      ... code ...
    elsif <CONDITION>
      ... other code ...
    end

.. code-block:: erb

    <% if @broadcast != "NONE" %>
        broadcast <%= @broadcast %>
    <% end %>

For
^^^
.. code-block:: erb

    <% @values.each do |val| -%>
        Some stuff with <%= val %>
    <% end -%>

If $values was set to ['one', 'two'], this example would produce:

.. code-block:: text

    Some stuff with one
    Some stuff with two


Instalacja i konfiguracja
-------------------------
.. code-block:: console

    sudo apt-get update
    sudo apt-get install puppet

Zaglądnij do katalogu ``/etc/puppet``.
Co się tam znajduje?

Przejdź do katalogu ``/etc/puppet/manifests``.

.. warning:: Uwaga, puppet od wersji 4 ma inną składnię. W Ubuntu 16.04 (LTS) instaluje się Puppet 3.8.5. Wersja ta może być niekompatybilna z modułami pobieranymi przez Puppet (np. Apache, Tomcat, Java). Rozwiązaniem jest ściąganie modułów w niższych wersjach (pasujących do wersji 3.8.5) lub instalacja Puppet w wersji wyższej niż ta w LTS.

    .. code-block:: console

        # Instalacja puppet w ostatniej wersji

        # Yum-based systems (np. Enterprise Linux 7)
        sudo rpm -Uvh https://yum.puppet.com/puppet5/puppet5-release-el-7.noarch.rpm
        sudo yum -y install puppet-agent
        export PATH=/opt/puppetlabs/bin:$PATH

        # Apt-based systems (np. Ubuntu 16.04 Xenial Xerus)
        wget https://apt.puppetlabs.com/puppet5-release-xenial.deb
        sudo dpkg -i puppet5-release-xenial.deb
        sudo apt update
        sudo apt -y install puppet-agent
        export PATH=/opt/puppetlabs/bin:$PATH


HTTPS problem
-------------
Gdyby wystąpił problem z certyfikatem ``ssl`` przy instalacji modułów należy:

- postaw maszynę w Amazonie (Ubuntu LTS)
- zainstaluj squid

.. code-block:: console

    sudo apt update
    sudo apt install squid

- na maszynie gościa (tam gdzie chcesz instalować moduł puppeta ustaw:


.. code-block:: console

    export http_proxy=http://<IP>:3128
    export https_proxy=http://<IP>:3128

Lub:

.. code-block:: ini

    [user]
    http_proxy = http://<IP>:3128
    https_proxy = http://<IP>:3128

.. code-block:: console

    sudo service puppet restart
    sudo su -
    puppet module install


Ćwiczenia Praktyczne
--------------------

Facter
^^^^^^
Przyjrzyj się wynikom poleceń:

.. code-block:: console

    facter
    facter ipaddress
    facter lsbdistdescription

Co zauważyłeś? Jak można wykorzystać te informacje?

Kod przedstawia wynik polecenia ``facter`` na świerzej maszynie `Ubuntu` postawionej w `Amazon AWS`

.. literal-include:: src/facter.txt
    :language: console

Korzystanie z faktów w manifestach:

:Sposób klasyczny, jako zmienne na głównym poziomie:

.. code-block:: ruby

    # Definicja
    operatingsystem = 'Ubuntu'

    # Wykorzystanie
    case $::operatingsystem {
      'CentOS': { include centos }
      'MacOS':  { include mac }
    }

:Jako zmienne w tablicy faktów:

.. code-block:: ruby

    # Definicja
    $facts['fact_name'] = 'Ubuntu'

    # Wykorzystanie
    case $facts['fact_name'] {
      'CentOS': { include centos }
      'MacOS':  { include mac }
    }

Tworzenie nowych faktów:

.. code-block:: ruby

    require 'facter'

    Facter.add(:system_role) do
      setcode "cat /etc/system_role"
    end

.. code-block:: ruby

    require 'facter'

    Facter.add(:system_role) do
      setcode do
        Facter::Util::Resolution.exec("cat /etc/system_role")
      end
    end

Druga metoda tworzenia faktów:

.. code-block:: console

    export FACTER_system_role=$(cat /etc/system_role); facter

Przykłady
---------
.. code-block:: ruby

    Exec    { path => "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin" }
    group   { "vagrant": ensure => present }
    user    { "vagrant": ensure => present, gid => "vagrant" }
    exec    { "apt-get update": command => "/usr/bin/apt-get update" }

    package { [
        "git",
        "vim",
        "nmap",
        "htop",
        "wget",
        "curl",
        "nginx",
        "python3",
        "python3-dev",
        "python3-pip",
        "p7zip-full",
        "uwsgi",
        "uwsgi-plugin-python3",
        "postgresql-client",
        "postgresql",
        "postgresql-server-dev-all",
        "libmemcached-dev"
      ] :
        ensure => latest,
        require => Exec["apt-get update"],
    }

    file { [
        "/var/www",
        "/var/www/log",
        "/var/www/public",
        "/var/www/public/media",
        "/var/www/public/static",
        "/var/www/tmp",
        "/var/www/src"
      ]:
        ensure => directory,
        owner => "vagrant",
        group => "vagrant",
        mode => 0755,
    }

Unless
^^^^^^
.. code-block:: ruby

    exec { "set hostname":
        command => '/bin/hostname -F /etc/hostname',
        unless  => "/usr/bin/test `hostname` = `/bin/cat /etc/hostname`",
    }


Moduły
------
.. code-block:: console

    puppet module search apache
    puppet module install puppetlabs-apache

Java
^^^^
.. code-block:: ruby

    class { 'java' :
      package => 'java-1.8.0-openjdk-devel',
    }

.. code-block:: ruby

    java::oracle { 'jdk8' :
      ensure  => 'present',
      version => '8',
      java_se => 'jdk',
    }

.. code-block:: ruby

    java::oracle { 'jdk8' :
      ensure  => 'present',
      version_major => '8u101',
      version_minor => 'b13',
      java_se => 'jdk',
    }

JBoss
^^^^^
* https://github.com/coi-gov-pl/puppet-jboss

To install JBoss Application Server you can use just, it will install Wildfly 8.2.0.Final by default:

.. code-block:: ruby

    include jboss

To install JBoss EAP or older JBoss AS use:

.. code-block:: ruby

    class { 'jboss':
      product => 'jboss-eap',
      version => '6.4.0.GA',
    }

or use hiera:

.. code-block:: ruby

    jboss::params::product: 'jboss-as'
    jboss::params::version: '7.1.1.Final'

.. code-block:: ruby

    $user = 'jb-user'
    $passwd = 'SeC3eT!1'

    node 'controller' {
      include jboss::domain::controller
      include jboss
      jboss::user { $user:
        ensure   => 'present',
        password => $passwd,
      }
    }


Przydatny linki
---------------
* https://docs.puppet.com/puppet/4.9/lang_facts_and_builtin_vars.html#language:-facts-and-built-in-variables


Zadania do rozwiązania
----------------------

Instalacja pakietów za pomocą `Puppet`
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
- Manifest do tego zadania zapisz w pliku ``/etc/puppet/code/packages.pp``
- Zainstaluj następujące pakiety za pomocą `Puppet`:

    - ``nmap``
    - ``htop``
    - ``git``

- Upewnij się by `Puppet` wykonał polecenie ``apt-get update`` na początku

Zmiana hostname
^^^^^^^^^^^^^^^
- Manifest do tego zadania zapisz w pliku ``/etc/puppet/code/hostname.pp``
- Za pomocą manifestu zmień hostname maszyny na ``ecosystem.local``
- Upewnij się, że po wpisaniu polecenia ``hostname`` będzie ustawiona na odpowiednią wartość
- Upewnij się, że hostname nie przywróci się do domyślnej wartości po ponownym uruchomieniu

Zarządzanie użytkownikami, grupami i katalogami
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
- Manifest do tego zadania zapisz w pliku ``/etc/puppet/code/users.pp``
- Upewnij się, że użytkownik ``vagrant`` istnieje, ma ``uid=1337`` i należy do grupy ``vagrant``
- Upewnij się, że grupa ``vagrant`` istnieje i ma ``gid=1337``
- Upewnij się, że:

    - Katalog ``/var/www`` istnieje
    - Właścicielem jego jest user ``vagrant``
    - Właścicielem jego jest grupa ``vagrant``
    - Ma uprawnienia ``rwxr-xr-x``

Konfiguracja Apache2
^^^^^^^^^^^^^^^^^^^^
- Za pomocą Puppet upewnij się by był użytkownik ``www-data`` i miał ``uid=33``
- Za pomocą Puppet upewnij się by była grupa ``www-data`` i miała ``gid=33``
- Upewnij się że katalog ``/var/www`` istnieje i właścicielem jego są user ``www-data`` i grupa ``www-data`` i że ma uprawnienia ``rwxr-xr-x``
- Zainstaluj i skonfiguruj Apache2 wykorzystując moduł Puppet
- Z terminala wygeneruj certyfikaty self signed OpenSSL (``.cert`` i ``.key``) (za pomocą i umieść je w ``/etc/ssl/``)
- Za pomocą Puppet Stwórz dwa vhosty:

    - ``insecure.example.com`` na porcie 80 i z katalogiem domowym ``/var/www/insecure-example-com``
    - ``ssl.example.com`` na porcie 443 i z katalogiem domowym ``/var/www/ssl-example-com`` + używanie certyfikatów SSL wcześniej wygenerowanych

- Stwórz pliki z treścią:

    - ``/var/www/insecure-example-com/index.html`` z treścią ``Ehlo World! - Insecure``
    - ``/var/www/ssl-example-com/index.html`` z treścią ``Ehlo World! - SSL!``

- W przeglądarce na komputerze lokalnym wejdź na stronę:

    - http://127.0.0.1:8080
    - https://127.0.0.1:8443

.. warning:: Uwaga, puppet od wersji 4 ma inną składnię. W Ubuntu 16.04 (LTS) instaluje się Puppet 3.8.5. Puppet module instaluje zawsze najnowszą (w tym wypadku niekompatybilną z naszym puppetem)! Aby zainstalować apache należy wymusić odpowiednią wersję (ostatnia supportująca Puppeta 3.8 to 1.10.

    .. code-block:: console

        $ puppet module install puppetlabs-apache --version 1.10.0

Instalacja i konfiguracja MySQL
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
- Manifest do tego zadania zapisz w pliku ``/etc/puppet/code/mysql.pp``
- Zainstaluj bazę danych `MySQL` wykorzystując moduł `Puppet`
- Ustaw hasło dla użytkownika ``root`` na ``mypassword``
- Ustaw nasłuchiwanie serwera ``mysqld`` na wszystkich interfejsach (``0.0.0.0``)
- Stwórz bazę danych ``mydb`` z ``utf-8``
- Stwórz usera ``myusername`` z hasłem ``mypassword``
- Nadaj wszystkie uprawnienia dla usera ``myusername`` dla bazy ``mydb``
- Ustaw backupowanie bazy danych do ``/tmp/mysql-backup``

Instalacja i konfiguracja Tomcat
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
- Manifest do tego zadania zapisz w pliku ``/etc/puppet/code/tomcat.pp``
- Zainstaluj język `Java` za pomocą modułu `Puppet`
- Zainstaluj `Tomcat 8` za pomocą `Puppet` w katalogu ``/opt/tomcat8``
- Skonfiguruj dwie instancje `Tomcat` działające jednocześnie:

    - Jedna uruchamiana na domyślnych portach
    - Druga uruchamiana na ``8006`` a connector z portu ``8081`` przekierowywał na ``8443``
    - Na pierwszej uruchom ``war`` z lokacji ``/opt/tomcat8/webapps/docs/appdev/sample/sample.war``
