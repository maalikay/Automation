#Author: kbaniya

@login @smoke
Feature: Login

  Background:
  	Given I am on the landing page
  	
  @Test @parallel
  Scenario: Login with valid credentials
    Given I click the login button
    And I provide valid login credential
    Then I should be logged into the application
  
  @Test2 @parallel
  Scenario: Login with invalid credentials
    Given I click the login button
    And I provide invalid login credential
    Then I should be logged into the application
    
    @Test3 @parallel
  Scenario: Cancel login scenario
    Given I click the login button
    And I click homepage icon
    Then I should be back on homepage