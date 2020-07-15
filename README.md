# CFGEncoder
Basic CFG Encoder

The encoding file follows the four principles of a CFG file therefore, it is split up into four sections these being: Variables, Terminals, Rules and Start Variable. Below is the extract of the encoding file of G0: 
 
G0 Encoding Text File: 
[VARIABLES] L,T,C [TERMINALS] a,b,c,; [RULES] L -> CT | e T -> ;CT |  e C -> a | b | c [START VARIABLE] L 
 
The encoding file is designed so that it can be easily seen that each section of a CFG is stored under the appropriate title. Thus, variable values would be stored under the ‘[VARIABLES]’ header. Variable and Terminal values are to be separated by a comma value and stored under their appropriate header. Rules are written in the format of ‘key name’ followed by a ‘->’ with each rule that relates to a variable being separated with a pipe symbol. This formatting has been chosen to closely mimic how CFG files were displayed in lectures. Lastly, start variable is stored as a single character that refers to a variable present under the ‘[VARIABLES]’ header. 
Assumptions made for this text file are that the character ‘e’ is reserved for an empty string and variables are to be of length 1. 
 
  
To run the program, it is to be called via the command line as such:  java CFGEncoder <encoding file>. The encoding file would need to be present in the same folder as CFGEncoder.java. 
 
After the program is called with the encoding file G0Encode.txt it displays the following output: With this output displaying the original encoding in Chomsky Normal Form instead. 
