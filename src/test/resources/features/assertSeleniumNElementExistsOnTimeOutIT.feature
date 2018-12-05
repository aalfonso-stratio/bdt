@web
Feature: Checks if in less than N seconds, checking every N seconds, web elements are found, with a different location method

  Scenario: Use Google and id
    Given My app is running in 'www.google.es'
    When I browse to '/'
    Then in less than '5' seconds, checking each '1' seconds, '1' elements exists with 'id:cst'

  Scenario: Use Google and name
    Given My app is running in 'www.google.es'
    When I browse to '/'
    Then in less than '5' seconds, checking each '1' seconds, '1' elements exists with 'name:q'

  Scenario: Use Google and xpath
    Given My app is running in 'www.google.es'
    When I browse to '/'
    Then in less than '5' seconds, checking each '1' seconds, '1' elements exists with 'xpath://*[@id="cst"]'

  Scenario: Use Google and css
    Given My app is running in 'www.google.es'
    When I browse to '/'
    Then in less than '5' seconds, checking each '1' seconds, '1' elements exists with 'css:#cst'

  Scenario: Use Metabase and css
    Given My app is running in 'www.metabase.com'
    When I browse to '/'
    Then in less than '5' seconds, checking each '1' seconds, '1' elements exists with 'css:body > header > a:nth-child(1) > div'
