FileManipulator
==============
version: 0.2.0

A Command Line Interface (CLI) tool for searching, removing, and replace text in files. 
The tool runs via the command line and can run in interactive mode where the user is asked for each required parameter or all parameters can be passed in and the tool will run immediately. 
See the FileManipulatorParameters class for exact parameter names and types. 

This is an example command to search for the text 'FileManipulator' in the FileManipulator project source folder:
```
java path/to/file_manipulator.jar -projectPath /home/TeamworkGuy2/java_projects/FileManipulator/src/fileManipulator -searchText FileManipulator -operation PRINT_MATCHING_LINES_PER_FILE -replaceText
```
