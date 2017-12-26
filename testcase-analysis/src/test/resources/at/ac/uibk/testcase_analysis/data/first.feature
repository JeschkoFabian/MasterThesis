
# The first feature taken from the githubs 

Feature: Addition
WRRYY
As a Wrong field
In order to avoid silly mistakes
As a math idiot 
some more sample text 
I want to be told the sum of two numbers
this is some sample text

Scenario Outline: Add two numbers
	@sample_tag   
	@Tag2
	Given i have entered <input_1> into the calculator
	| table |
	| row   | 
	And i have entered <input_2> into the calculator
	When i press <button> 
	Then the result should be <output> on the screen "This is some String"
	'''
		This is a random code block
	'''
	
	Examples: 
	| input_1 | input_2 | button | output |
	| 20      | 30      | add    | 50     |
	| 2       | 5       | add    | 7      |
	| 0       | 40      | add    | 40     |
	