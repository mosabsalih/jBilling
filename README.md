jBilling
========

**Requirements:**

* Java 6+
* Grails 1.3.4
* PostgreSQL 8+ *(required for testing, other runtime databases are supported)*

To run jBilling from source you will need to have Java 6+ and Grails 1.3.4 installed. To install grails, download version 1.3.4 from the [Grails Archive](http://www.grails.org/download/archive/Grails) and follow the installation instructions for your operating system. 

[Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [PostgreSQL](http://www.postgresql.org/) can be downloaded an installed by visiting the vendor's websites, or in linux environments by using the package manager (apt-get or yum).



Cloning the Source Code from GitHub
-----------------------------------

Install [Git](http://git-scm.com/download/). 

Clone the repository from `git@github.com:emilc/jBilling.git` 

<pre><code>clone git@github.com:emilc/jBilling.git jbilling-community</code></pre>



Configuring PostgreSQL
----------------------

To run jBilling with the out-of-box reference database, you must have PostgreSQL installed and configured with a **'jbilling'** user and an empty **'jbilling_test'** database. The setup scripts also expect that the user will allow local connections without a password.

Edit the PostgreSQL pg_hba.conf file and change the "local" and "IPv4" localhost connection types:

<pre><code># "local" is for Unix domain socket connections only
local   all         all                               trust
# IPv4 local connections:
host    all         all         127.0.0.1/32          trust
</code></pre>

Connect to PostgreSQL and create the test user and database.

<pre><code>CREATE ROLE jbilling WITH LOGIN SUPERUSER CREATEDB CREATEROLE PASSWORD 'jbilling';
CREATE DATABASE jbilling_test WITH OWNER jbilling;
</pre></code>



Setup
-----

Run the grails `compile` target to compile the jBilling source code, then run the `prepare-test` target to load the reference database and prepare all the required resources.

<pre><code>grails compile
grails prepare-test
</pre></code>

*The grails compile target may halt with a compiler error on some environments, running compile a second time usually resolves the issue.*



Running from Source
-------------------

**Windows**: ``run-app.bat``

**Linux/Mac**: ``./run-app.sh``






