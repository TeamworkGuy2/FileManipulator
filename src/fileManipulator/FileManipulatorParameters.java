package fileManipulator;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import fileManipulator.FileManipulator.DebugOp;
import fileManipulator.FileManipulator.FileLineOp;
import programParameter.ParameterBuilder;
import programParameter.ParameterData;
import programParameter.ParameterSet;
import twg2.collections.builder.MapBuilder;

/** The parameters for running a {@link FileManipulator}
 * @author TeamworkGuy2
 * @since 2014-11-23
 */
public class FileManipulatorParameters {
	static String wildcardPathPrefix = "matching ";
	int compareCount = 0;
	String rootPath;
	Path projectFolder;
	String fileNamePattern;
	String searchString;
	Enum<?> operation;
	String replaceString;


	public FileManipulatorParameters() {
	}


	public FileManipulatorParameters(String rootPath) {
		this.rootPath = rootPath;
	}


	public Path getProjectFolder() {
		return projectFolder;
	}


	public void setProjectFolder(String projFolder) {
		if(projFolder != null && projFolder.startsWith(wildcardPathPrefix)) {
			// remove the wildcard prefix
			projFolder = projFolder.substring(wildcardPathPrefix.length());
			System.out.println("original path: " + projFolder);

			int firstWildcardIndex = projFolder.indexOf('*');
			this.fileNamePattern = projFolder.replace('\\', '/').replace("*", ".*?");
			// get the portion of the path up to the last separator char, for example, gets "/users/public/tmp" from "matching /users/public/tmp/*.txt"
			String beforeWildcardStr = projFolder.substring(0, firstWildcardIndex);
			String staticPathPortion = projFolder.substring(0, Math.max(beforeWildcardStr.lastIndexOf('/'), beforeWildcardStr.lastIndexOf('\\')));
			if(rootPath != null) {
				this.projectFolder = Paths.get(rootPath, staticPathPortion);
			}
			else {
				this.projectFolder = Paths.get(staticPathPortion);
			}
		}
		else {
			try {
				if(rootPath != null) {
					this.projectFolder = Paths.get(rootPath, projFolder);
				}
				else {
					this.projectFolder = Paths.get(projFolder);
				}
			} catch(InvalidPathException e) {
				if(projFolder != null && projFolder.indexOf('*') > -1) {
					throw new RuntimeException("path contains a wildcard, to search by wildcards, begin the search path with '" + wildcardPathPrefix + "'", e);
				}
				throw e;
			}
		}
		System.out.println("project folder: " + this.projectFolder);
		System.out.println("file pattern: " + this.fileNamePattern);
	}


	public boolean hasFileNamePattern() {
		return fileNamePattern != null;
	}


	public boolean isFileNameMatch(String fileName) {
		compareCount++;
		return fileNamePattern == null || fileName.matches(fileNamePattern);
	}


	public String getSearchString() {
		return searchString;
	}


	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}


	public Enum<?> getOperation() {
		return operation;
	}


	public void setOperation(Enum<?> operation) {
		this.operation = operation;
	}


	public String getReplaceString() {
		return replaceString;
	}


	public void setReplaceString(String replaceString) {
		this.replaceString = replaceString;
	}


	public static final ParameterSet<String> createParameterParser(FileManipulatorParameters params) {
		ParameterData<String, String> projFolderParam = ParameterBuilder.newText()
				.setNameAndAliases("-projectPath")
				.setSetter(params::setProjectFolder)
				.setHelpMessage("set the folder to search (prefix with \"matching \" for wildcard searches, for example \"matching /users/public/tmp/*.txt\")")
				.setRequestParameterMessage("enter the project path to search (prefix with \"matching \" for wildcard searches): ")
				.setRequired(true)
				.build();

		ParameterData<String, String> searchTextParam = ParameterBuilder.newText()
				.setNameAndAliases("-searchText")
				.setSetter(params::setSearchString)
				.setHelpMessage("set the search string")
				.setRequestParameterMessage("enter the text to search for: ")
				.setRequired(true)
				.build();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ParameterData<String, Enum<?>> operationParam = ParameterBuilder.<Enum<?>>newEnumMap(
				MapBuilder.concat(MapBuilder.immutableEnumNames(FileLineOp.class), MapBuilder.immutableEnumNames(DebugOp.class)),
				(Class<Enum<?>>)(Class)Enum.class)
				.setNameAndAliases("-operation")
				.setSetter(params::setOperation)
				.setHelpMessage("the type of operation to perform")
				.setRequestParameterMessage("enter the type of text operation to perform: ")
				.setRequired(true)
				.build();

		ParameterData<String, String> replaceTextParam = ParameterBuilder.newText()
				.setNameAndAliases("-replaceText")
				.setSetter(params::setReplaceString)
				.setHelpMessage("set the replacement string for the search string")
				.setRequestParameterMessage("enter the replacement text for the operation: ")
				.setRequired(true)
				.build();

		ParameterSet<String> paramParser = ParameterSet.newParameterSet(Arrays.asList(projFolderParam,
				searchTextParam, operationParam, replaceTextParam), true, "-help", "help");

		return paramParser;
	}

}
