@rest
Feature: Status and Health Status

  Scenario: Obtain
    Given I authenticate to DCOS cluster '192.168.10.132' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/key.pem'
    And I get service '/jumanji-pp' status in cluster 'jumanji.labs.stratio.com' and save it in variable 'status'
    And I get service '/jumanji-pp' health status in cluster 'jumanji.labs.stratio.com' and save it in variable 'healthStatus'
    Then I wait '5' seconds

  Scenario: Check
    Then '!{status}' is 'running'
    And '!{healthStatus}' is 'healthy'

  Scenario: Double check
    Given I authenticate to DCOS cluster '192.168.10.132' using email '${DCOS_USER:-admin}' with user '${REMOTE_USER:-operador}' and pem file 'src/test/resources/key.pem'
    Then service '/jumanji-pp' status in cluster 'jumanji.labs.stratio.com' is 'running'
    And service '/jumanji-pp' health status in cluster 'jumanji.labs.stratio.com' is 'healthy'
    And service '/jumanji-pp' status in cluster 'jumanji.labs.stratio.com' is 'deploying' in less than '6' seconds checking every '2' seconds

  Scenario: Destroy
    When I destroy service 'service' in cluster 'nightly.labs.stratio.com'
    Then the service response status must be '200'
    And I wait '5' seconds