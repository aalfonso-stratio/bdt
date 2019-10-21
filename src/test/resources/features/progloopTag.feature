Feature: Feature used in testing progamatic loop tag aspect

  @progloop(PROGLOOP,VAR_NAME)
  Scenario: Execute scenario ${PROGLOOP} times
    Given I run 'echo <VAR_NAME>' locally

  @runOnEnv(PROGLOOP)
  @progloop(PROGLOOP,VAR_NAME)
  Scenario: This scenario should be executed.
    Given I run 'echo <VAR_NAME> >> testProgLoopOutput.txt' locally
    When I run 'wc -l testProgLoopOutput.txt' locally
    Then the command output contains '<VAR_NAME>'

  Scenario: Clean
    Given I run 'rm testProgLoopOutput.txt' locally

  @skipOnEnv(PROGLOOP)
  @progloop(VAR,VAR_NAME)
  Scenario: This scenario should not be executed.
    Given I run 'echo 1' locally with exit status '200'

  @progloop(PROGLOOP,VAR_NAME)
  Scenario: With scenarios outlines and datatables
    Given I create file 'testSOATtag<VAR_NAME>.json' based on 'schemas/simple<VAR_NAME>.json' as 'json' with:
      | $.a | REPLACE | @{JSON.schemas/empty.json}     | object   |
    Given I save '@{JSON.testSOATtag<VAR_NAME>.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{"a":{}}" ]' locally