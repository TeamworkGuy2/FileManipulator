package twg2.fileManipulator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twg2.cli.ParameterBuilder;
import twg2.cli.ParameterData;
import twg2.cli.ParameterSet;
import twg2.collections.builder.ListUtil;
import twg2.collections.builder.MapBuilder;
import twg2.fileManipulator.ManipulateLines.DebugOp;
import twg2.fileManipulator.ManipulateLines.FileLineOp;

/** The parameters for running a {@link FileManipulator}
 * @author TeamworkGuy2
 * @since 2014-11-23
 */
public class FileManipulatorParameters {
	static String wildcardPathPrefix = "matching ";
	static String excludeFilePrefix = "exclude ";
	static String includeFilePrefix = "include ";
	int compareCount = 0;
	Charset cs;
	String rootPath;
	Path searchDir;
	String fileNamePattern;
	List<String> excludePatterns;
	List<String> excludeContains;
	List<String> excludeStartsWith;
	List<String> excludeEndsWith;
	List<String> includePatterns;
	List<String> includeContains;
	List<String> includeStartsWith;
	List<String> includeEndsWith;
	String searchString;
	Enum<?> operation;
	String replaceString;


	public FileManipulatorParameters(Charset cs) {
		this.cs = cs;
	}


	public FileManipulatorParameters(String rootPath, Charset cs) {
		this.rootPath = rootPath;
		this.cs = cs;
	}


	public Path getSearchDirectory() {
		return searchDir;
	}


	public void setSearchDirectory(String dir) {
		if(dir != null && dir.startsWith(wildcardPathPrefix)) {
			// remove the wildcard prefix
			dir = dir.substring(wildcardPathPrefix.length());
			System.out.println("original path: " + dir);

			int firstWildcardIndex = dir.indexOf('*');
			this.fileNamePattern = dir.replace('\\', '/').replace("*", ".*?");
			// get the portion of the path up to the last separator char, for example, gets "/users/public/tmp" from "matching /users/public/tmp/*.txt"
			String beforeWildcardStr = dir.substring(0, firstWildcardIndex);
			String staticPathPortion = dir.substring(0, Math.max(beforeWildcardStr.lastIndexOf('/'), beforeWildcardStr.lastIndexOf('\\')));
			if(rootPath != null) {
				this.searchDir = Paths.get(rootPath, staticPathPortion);
			}
			else {
				this.searchDir = Paths.get(staticPathPortion);
			}
		}
		else {
			try {
				if(rootPath != null) {
					this.searchDir = Paths.get(rootPath, dir);
				}
				else {
					this.searchDir = Paths.get(dir);
				}
			} catch(InvalidPathException e) {
				if(dir != null && dir.indexOf('*') > -1) {
					throw new RuntimeException("path contains a wildcard, to search by wildcards, begin the search path with '" + wildcardPathPrefix + "'", e);
				}
				throw e;
			}
		}
		System.out.println("project folder: " + this.searchDir);
		System.out.println("file pattern: " + this.fileNamePattern);
	}


	public List<String> getExcludePatterns() {
		return this.excludePatterns;
	}


	public void setExcludePatterns(Path file, boolean optional) {
		try {
			if(file.toFile().canRead() || !optional) {
				setExcludePatterns(ListUtil.map(FileManipulator.readLines(file, cs), (s) -> s.replace('\\', '/')));
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not find 'exclude' pattern file", e);
		}
	}


	public void setExcludePatterns(List<String> excludePatterns) {
		this.excludePatterns = excludePatterns;
		if(excludePatterns != null) {
			for(String pattern : excludePatterns) {
				int patternLen = pattern.length();
				if(patternLen > 1 && pattern.startsWith("^")) {
					if(this.excludeStartsWith == null) {
						this.excludeStartsWith = new ArrayList<String>();
					}
					this.excludeStartsWith.add(pattern.substring(1));
				}
				else if(patternLen > 1 && pattern.endsWith("$") && pattern.charAt(patternLen - 2) != '\\') {
					if(this.excludeEndsWith == null) {
						this.excludeEndsWith = new ArrayList<String>();
					}
					this.excludeEndsWith.add(pattern.substring(0, patternLen - 1));
				}
				else if(patternLen > 0) {
					if(this.excludeContains == null) {
						this.excludeContains = new ArrayList<String>();
					}
					String escapedPattern = pattern;
					if(escapedPattern.startsWith("\\^")) escapedPattern = escapedPattern.substring(1);
					if(escapedPattern.endsWith("\\$")) escapedPattern = escapedPattern.substring(0, escapedPattern.length() - 2) + "$";
					this.excludeContains.add(escapedPattern);
				}
			}
		}
	}


	public List<String> getIncludePatterns() {
		return this.excludePatterns;
	}


	public void setIncludePatterns(Path file, boolean optional) {
		try {
			if(file.toFile().canRead() || !optional) {
				setIncludePatterns(ListUtil.map(FileManipulator.readLines(file, cs), (s) -> s.replace('\\', '/')));
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not find 'include' pattern file", e);
		}
	}


	public void setIncludePatterns(List<String> includePatterns) {
		this.includePatterns = includePatterns;
		if(includePatterns != null) {
			for(String pattern : includePatterns) {
				int patternLen = pattern.length();
				if(patternLen > 1 && pattern.startsWith("^")) {
					if(this.includeStartsWith == null) {
						this.includeStartsWith = new ArrayList<String>();
					}
					this.includeStartsWith.add(pattern.substring(1));
				}
				else if(patternLen > 1 && pattern.endsWith("$") && pattern.charAt(patternLen - 2) != '\\') {
					if(this.includeEndsWith == null) {
						this.includeEndsWith = new ArrayList<String>();
					}
					this.includeEndsWith.add(pattern.substring(0, patternLen - 1));
				}
				else if(patternLen > 0) {
					if(this.includeContains == null) {
						this.includeContains = new ArrayList<String>();
					}
					String escapedPattern = pattern;
					if(escapedPattern.startsWith("\\^")) escapedPattern = escapedPattern.substring(1);
					if(escapedPattern.endsWith("\\$")) escapedPattern = escapedPattern.substring(0, escapedPattern.length() - 2) + "$";
					this.includeContains.add(escapedPattern);
				}
			}
		}
	}


	public boolean hasFileNamePattern() {
		return fileNamePattern != null;
	}


	public boolean isFileNameMatch(String fileName) {
		compareCount++;
		List<String> patterns;
		if(this.includePatterns != null) {
			boolean match = false;
			if((patterns = this.includeContains) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.indexOf(patterns.get(i)) > -1) {
						match = true;
						break;
					}
				}
			}
			if((patterns = this.includeStartsWith) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.startsWith(patterns.get(i))) {
						match = true;
						break;
					}
				}
			}
			if((patterns = this.includeEndsWith) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.endsWith(patterns.get(i))) {
						match = true;
						break;
					}
				}
			}
			if(!match) return false;
		}
		if(this.excludePatterns != null) {
			if((patterns = this.excludeContains) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.indexOf(patterns.get(i)) > -1) {
						return false;
					}
				}
			}
			if((patterns = this.excludeStartsWith) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.startsWith(patterns.get(i))) {
						return false;
					}
				}
			}
			if((patterns = this.excludeEndsWith) != null) {
				for(int i = 0, size = patterns.size(); i < size; i++) {
					if(fileName.endsWith(patterns.get(i))) {
						return false;
					}
				}
			}
		}
		if(this.fileNamePattern != null) {
			return fileName.matches(this.fileNamePattern);
		}
		// match all files by default
		return true;
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
				.setSetter(params::setSearchDirectory)
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

		ParameterData<String, Path> excludePatternsParam = ParameterBuilder.newPath()
				.setNameAndAliases("-exclude")
				.setSetter((p) -> params.setExcludePatterns(p, true))
				.setHelpMessage("optional text file path containing path name patterns to exclude from the search. The patterns are string literals with support for '^' (beginning) and '$' (end) of path")
				.setRequestParameterMessage("enter the exclude patterns file path: ")
				.setRequired(true)
				.build();

		ParameterData<String, Path> includePatternsParam = ParameterBuilder.newPath()
				.setNameAndAliases("-include")
				.setSetter((p) -> params.setIncludePatterns(p, true))
				.setHelpMessage("optional text file path containing path name patterns to include in the search. The patterns are string literals with support for '^' (beginning) and '$' (end) of path")
				.setRequestParameterMessage("enter the include patterns file path: ")
				.setRequired(true)
				.build();

		ParameterSet<String> paramParser = ParameterSet.newParameterSet(Arrays.asList(projFolderParam,
				searchTextParam, operationParam, replaceTextParam, excludePatternsParam, includePatternsParam), true, "-help", "help");

		return paramParser;
	}

}
