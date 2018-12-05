Feature: Cassandra steps test

  Scenario: Connect to Cassandra
    Given I connect to 'Cassandra' cluster at '${CASSANDRA_HOST}'

  @ignore @manual
  Scenario: Connect to Cassandra with security
    Given I open a ssh connection to '${BOOTSTRAP_IP}' with user 'root' and password 'stratio'
    #Retrieve Cassandra nodes
    Given I run 'echo ${MASTERS_LIST} | sed -n 1'p' | tr ',' '\n'' locally and save the value in environment variable 'master'
    When I open a ssh connection to '!{master}' with user '${ROOT_USER:-root}' and password '${ROOT_PASSWORD:-stratio}'
    Given I run 'getent hosts leader.mesos | awk '{print $1}'' in the ssh connection and save the value in environment variable 'MESOS_MASTER'
     And I run 'curl -s !{MESOS_MASTER}:5050/frameworks | jq '.frameworks[] | select(.name == "cassandrastratio") |.tasks[] | select(.name == "node-0-server") | .framework_id ' |  tr -d "\""' in the ssh connection and save the value in environment variable 'framework_id'
     And I run 'curl -s !{MESOS_MASTER}:5050/frameworks | jq '.frameworks[] | select(.name == "cassandrastratio") |.tasks[] | select(.name == "node-0-server") | .executor_id ' |  tr -d "\""' in the ssh connection and save the value in environment variable 'executor_id'
     And I run 'curl -s !{MESOS_MASTER}:5050/frameworks | jq '.frameworks[] | select(.name == "cassandrastratio") |.tasks[] | select(.name == "node-0-server") | .slave_id ' |  tr -d "\""' in the ssh connection and save the value in environment variable 'slave_id'
    #Retrieve Certificates
    Given I open a ssh connection to '${DCOS_CLI_HOST}' with user 'root' and password 'stratio'
    Then I run 'dcos task | grep node-0-server | awk '{print $2}'' in the ssh connection and save the value in environment variable 'ip-node'
    Then I open a ssh connection to '!{ip-node}' with user 'root' and password 'stratio'
    #Get Passwords:
    When I run 'cat /var/lib/mesos/slave/slaves/!{slave_id}/frameworks/!{framework_id}/executors/!{executor_id}/runs/latest/apache-cassandra-3.0.16/conf/cassandra.yaml | grep keystore_password | awk '{print $2}''' |sed -n 1p' in the ssh connection and save the value in environment variable 'keystore_password'
    Then I run 'cat /var/lib/mesos/slave/slaves/!{slave_id}/frameworks/!{framework_id}/executors/!{executor_id}/runs/latest/apache-cassandra-3.0.16/conf/cassandra.yaml | grep truststore_password | awk '{print $2}''' |sed -n 1p' in the ssh connection and save the value in environment variable 'truststore_password'
    #Copy Certificates to ssl conexion
    And I run 'sshpass -p "stratio" scp root@!{ip-node}:/var/lib/mesos/slave/slaves/!{slave_id}/frameworks/!{framework_id}/executors/!{executor_id}/runs/latest/apache-cassandra-3.0.16/certificates/cassandrastratio.jks target/test-classes/cassandrastratio.jks' locally
    And I run 'sshpass -p "stratio" scp root@!{ip-node}:/var/lib/mesos/slave/slaves/!{slave_id}/frameworks/!{framework_id}/executors/!{executor_id}/runs/latest/apache-cassandra-3.0.16/certificates/node-0-server.cassandrastratio.jks target/test-classes/node-0-server.cassandrastratio.jks' locally
    #Connect to Cassandra with certificates
    And I securely connect to 'Cassandra' cluster at '${CASSANDRA_HOST:-node-0-server.cassandrastratio.mesos}'

  Scenario: Create a keyspace in Cassandra
    Given I create a Cassandra keyspace named 'opera'
    Then a Cassandra keyspace 'opera' exists

  Scenario: Check keyspace does not exists
    Then a Cassandra keyspace 'invalidKeyspace' does not exist


  Scenario: Create a table in Cassandra
    And I create a Cassandra table named 'analyzertable' using keyspace 'opera' with:
      |name  | comment |lucene |
      | TEXT |TEXT     |TEXT   |
      |  PK  |         |       |

    And I insert in keyspace 'opera' and table 'analyzertable' with:
      |name 	    |comment				        |
      |'Kurt'      	|'Hello to a man'   			|
      |'Michael'    |'Hello to a woman'			    |
      |'Louis'     	|'Bye to a man' 				|
      |'John'     	|'Bye to a woman'  			    |
      |'James'     	|'Hello to a man and a woman'  	|

    Then a Cassandra keyspace 'opera' contains a table 'analyzertable'
    And a Cassandra keyspace 'opera' contains a table 'analyzertable' with '5' rows


  Scenario: Check table does not exist
    Then a Cassandra keyspace 'opera' does not contain a table 'invalidTable'

  Scenario: Querying table in Cassandra
    When a Cassandra keyspace 'opera' contains a table 'analyzertable' with values:
      |  comment-varchar |

  Scenario: I remove all data
    Given I drop a Cassandra keyspace 'opera'

  Scenario: Exception in query
    When I create a Cassandra keyspace named 'opera'
    And I create a Cassandra table named 'location' using keyspace 'opera' with:
      | place  | latitude | longitude |lucene |
      | TEXT   | DECIMAL  |  DECIMAL  |TEXT   |
      |  PK    | PK       |           |       |
    And I insert in keyspace 'opera' and table 'location' with:
      |latitude|longitude|place       |
      |2.5     |2.6      |'Madrid'    |
      |12.5    |12.6     |'Barcelona' |

    Given I execute a query over fields '*' with schema 'schemas/geoDistance.conf' of type 'string' with magic_column 'lucene' from table: 'location' using keyspace: 'opera' with:
      | col          | UPDATE  | geo_point  |
      | __lat        | UPDATE  | 0          |
      | __lon        | UPDATE  | 0          |
      | __maxDist    | UPDATE  | 720km      |
      | __minDist    | UPDATE  | -100km     |
    Then an exception 'IS' thrown with class 'Exception' and message like 'InvalidQueryException'

  Scenario: Truncate table in Cassandra
    Given I truncate a Cassandra table named 'analyzertable' using keyspace 'opera'

  Scenario: Drop table in Cassandra
    Given I drop a Cassandra table named 'analyzertable' using keyspace 'opera'

  Scenario: Drop keyspace in Cassandra
    Given I drop a Cassandra keyspace 'opera'


#Intbootstrap: mvn clean verify -Dit.test=com.stratio.qa.ATests.CassandraStepsIT -DBOOTSTRAP_IP=10.200.1.52 -DCASSANDRA_HOST=node-0-server.cassandrastratio.mesos -DVAULT_HOST=10.200.1.50 -DVAULT_PORT=8200 -DlogLevel=DEBUG -DMASTERS_LIST=10.200.0.242 -DDCOS_CLI_HOST=dcos-aceptacion -Djavax.net.ssl.trustStore=target/test-classes/cassandrastratio.jks -Djavax.net.ssl.trustStorePassword=xxx -Djavax.net.ssl.keyStore=target/test-classes/certificadoscassandra/node-0-server.cassandrastratio.jks -Djavax.net.ssl.keyStorePassword=xxx

