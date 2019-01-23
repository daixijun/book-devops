Vagrant
=======

.. todo:: https://www.youtube.com/watch?v=kbarwyun-c4

.. contents::

.. warning:: Na windows installer może się pod koniec instalacji wywalać. Wtedy trzeba urtuchomić ``cmd`` jako Administrator i uruchomić installer z terminala.

Tworzenie i konfigurowanie maszyny
----------------------------------
- Poniższe polecenia wykonaj w pliku ``Vagrantfile``
- Stwórz maszynę z oficjalnego obrazu 64 bitowej wersji `Ubuntu LTS` (`Long Time Support`)
- Ustaw hostname na ``ubuntu.local``
- Jeżeli masz słabszą maszynę (2 CPU core, 4 GB RAM):

    - 1 CPU core
    - 1024 MB Ram

- Jeżeli masz lepszy komputer:

    - 2 CPU core
    - 4096 MB RAM

- Ustaw forwarding portu ``80`` na ``8080`` hosta oraz ``443`` na ``8443``
- Ustaw aby ten katalog był synchronizowany na maszynie gościa w ``/var/www/host``
- Zrób by maszyna była stawiana z manifestu `Puppet`
- Podnieś maszynę i rozpocznij pobieranie obrazu

Uruchamianie maszyny
^^^^^^^^^^^^^^^^^^^^
.. code-block:: sh

    vagrant init ubuntu/bionic64

.. code-block:: text

    Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/bionic64"
    end

.. code-block:: sh

    vagrant up

    # Alternatywnie
    vagrant up --provider virtualbox

Ustawianie hasła
^^^^^^^^^^^^^^^^
.. warning:: `Ubunutu` w nowych wersjach zmieniło hasło na użytkownika i nie da się tak łatwo na niego dostać. Użyj wtedy:
.. note:: Basically the ``ubuntu/xenial32`` and ``ubuntu/xenial64`` images are flawed as they don't come with the vagrant user out of the box. This is against the `Vagrant` specifications!

    .. tip:: Rozwiązanie: http://askubuntu.com/a/854849/427956

    .. code-block:: ruby

        Vagrant.configure("2") do |config|

            apt-get install -y expect
            echo '#!/usr/bin/expect
              set timeout 20
              spawn sudo passwd ubuntu
              expect "Enter new UNIX password:" {send "ubuntu\\r"}
              expect "Retype new UNIX password:" {send "ubuntu\\r"}
              interact' > change_ubuntu_password
            chmod +x change_ubuntu_password
          ./change_ubuntu_password

        end

.. warning:: This bug is now fixed in ubuntu/xenial64 v20180112.0.0.

    Update your vagrant boxes:

    .. code-block:: console

        $ vagrant box update
        $ vagrant up
        $ vagrant ssh

    (notice you're "vagrant").

Usuwanie maszyny
^^^^^^^^^^^^^^^^
.. code-block:: sh

    vagrant halt
    vagrant destroy

Konfiguracja forwardingu portów
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: ruby

    config.vm.network :forwarded_port, guest: 8080, host: 8080
    config.vm.network :forwarded_port, guest: 9000, host: 9000

Synchronizowanie katalogów
^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: ruby

    config.vm.synced_folder ".", "/vagrant"


Provisioning za pomocą shell
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: ruby

    Vagrant.configure("2") do |config|
      config.vm.provision "shell" do |s|
        s.inline = "echo $1"
        s.args   = ["hello, world!"]
      end
    end

.. code-block:: ruby

    Vagrant.configure("2") do |config|
      config.vm.provision "shell", path: "script.sh"
    end

.. code-block:: ruby

    Vagrant.configure("2") do |config|
      config.vm.provision "shell", path: "https://example.com/provisioner.sh"
    end

Provisioning za pomocą `Puppet`
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: ruby

    config.vm.provision :puppet do |puppet|
        puppet.module_path = "puppet/modules"
        puppet.manifests_path = "puppet/manifests"
        puppet.manifest_file = "default.pp"
    end


Finalna konfiguracja
^^^^^^^^^^^^^^^^^^^^
Twoja konfuguracja `Vagrant` powinna wyglądać tak:

.. code-block:: ruby

    CPU = 1
    RAM = 1024

    Vagrant.configure("2") do |config|
        config.vm.hostname = "ubuntu.local"

        config.vm.box = "ubuntu/xenial64"
        # config.vm.box = "ubuntu-lts"
        # config.vm.box_url = "http://cloud-images.ubuntu.com/xenial/current/xenial-server-cloudimg-amd64-vagrant.box"

        config.vm.network :forwarded_port, guest: 80, host: 8080
        config.vm.network :forwarded_port, guest: 443, host: 8443
        config.vm.synced_folder ".", "/var/www/host"

        config.vm.provider "virtualbox" do |v|
            v.name = "ubuntu.local"
            v.cpus = CPU
            v.memory = RAM
        end

        config.vm.provision "shell" do |s|
          s.inline = "echo $1"
          s.args   = ["hello, world!"]
        end

    end

.. code-block:: sh

    vagrant provision


Zadania do rozwiązania
----------------------

Automatyzacja tworzenia wirtualnej maszyny
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
- Użyj pliku ``Vagrantfile`` do przetrzymywania następującej konfiguracji
- Stwórz maszynę z oficjalnego obrazu 32 bitowej wersji `Ubuntu LTS` (Long Time Support)
- Ustaw hostname na ``ubuntu.local``
- Ustaw zasoby przydzielane towjej maszynie wirtialnej w zależności od mocy komputera:

    - 1 CPU core, 1024 MB RAM (jeżeli masz około 2 CPU core, 4 GB RAM)
    - 2 CPU core, 8196 MB RAM (jeżeli masz mocniejszą maszynę)

- Ustaw forwarding portów:

    - 80 -> 8888
    - 443 -> 8443
    - 7990 -> 7990
    - 7999 -> 7999
    - 8080 -> 8080
    - 8081 -> 8081
    - 8090 -> 8090
    - 9000 -> 9000
    - 5432 -> 5432
    - 3306 -> 3306

- Ustaw aby ten katalog był synchronizowany na maszynie gościa w ``/var/www/host``
- Podnieś maszynę z ``Vagrantfile`` i rozpocznij pobieranie obrazu `Ubuntu`

Vagrant + Puppet
^^^^^^^^^^^^^^^^
- Skopiuj dotychczasowe manifesty z poprzednich zadań (``/etc/puppet/manifests/*``) na swój komputer do katalogu ``puppet/manifests/``
- Skopiuj certyfikaty SSL, które wygenerowałeś na swój komputer do katalogu ``ssl/``
- Wyłącz maszynę ``vagrant halt``, a następnie ją usuń ``vagrant destroy``
- Edytuj plik ``Vagrantfile`` i dopisz, by maszyna była stawiana z manifestów `Puppet`
- W pliku ``Vagrantfile`` trzymaj jak najmniej logiki i wszystko rób za pomocą `Puppet`
- Zrób by certyfikaty były przenoszone z twojego komputera na maszynę gościa (nie generuj nowych, tylko wykorzystaj stare!) oczywiście za pomocą `Puppet`, umieść to w pliku ``puppet/manifests/certificates.pp``
- Każdy z manifestów powinien być w osobnych plikach a jeden ``puppet/main.pp`` powinien includować pozostałe z katalogu ``puppet/manifests/*``

.. warning:: Ubuntu 16.04 (LTS) nie zawiera w sobie puppeta, co jest sprzeczne z wymaganiem (standardem) vagrantowym. Trzeba go zainstalować za pomocą provisioningu shella, a później odpalać manifesty puppetowe.
