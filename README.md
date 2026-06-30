# Session-Analyzer
A simple Java application which statistically interprets speedcubing session data exported from CSTimer.net

IMPORTANT:
The only way to change between session files is by using an IDE or other code editor.

To use your own CSTimer session file, you'll need to change the code to load your file.
The first line inside the Main.java class is the FILEPATH variable. Make sure your session file (.csv) is stored in the same folder as Main.java, then:

private static final String FILEPATH = "src/your_file_name.csv";

This will be addressed later
