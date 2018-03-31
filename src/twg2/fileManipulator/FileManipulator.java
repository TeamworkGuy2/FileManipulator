package twg2.fileManipulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.io.files.FileRecursion;
import twg2.text.stringUtils.StringReplace;

/** A set of methods for manipulating lines of text from multiple files.
 * The file lines can be modified and saved back to the file system.
 * @author TeamworkGuy2
 * @since 2014-10-16
 */
public class FileManipulator {
	private Map<File, Map<File, FileInfo>> filesByDir;
	private Map<File, FileInfo> allFiles;


	/** Create a file manipulator that manipulates all files in the specified folder
	 * @param folderFiles a map of folders and files and lines in each file
	 */
	public FileManipulator(Map<File, Map<File, List<String>>> folderFiles) {
		this.filesByDir = new HashMap<>();
		this.allFiles = new HashMap<>();

		folderFiles.forEach((folder, files) -> {
			Map<File, FileInfo> fileInfos = this.filesByDir.get(folder);
			if(fileInfos == null) {
				fileInfos = new HashMap<>();
				this.filesByDir.put(folder, fileInfos);
			}
			final Map<File, FileInfo> fileInfoMap = fileInfos;
			files.forEach((file, lines) -> {
				FileInfo fileInfo = new FileInfo(file, lines);
				this.allFiles.put(file, fileInfo);
				fileInfoMap.put(file, fileInfo);
			});
		});
	}


	/** Create a file manipulator that manipulates all files in the specified folder
	 * @param rootFolder the folder to load files from
	 * @param pathFilter a filter for the files found in the {@code rootFolder}, the path
	 * of each file is passed to this filter.  If the filter returns true for a given file path,
	 * that file is processed by this file manipulator, if false is returned, the file
	 * is not read or modified. Null allowed
	 * @param cs the charset of the files being loaded
	 * @param newline the type of newline to use when saving files
	 */
	public FileManipulator(Path rootFolder, Predicate<String> pathFilter, Charset cs, String newline) {
		this.filesByDir = new HashMap<>();
		this.allFiles = new HashMap<>();
		// load files from a directory recursively
		FileRecursion.forEachFileByFolderRecursively(rootFolder.toFile(), (folder, file) -> {
			String filePathStr = file.toString();
			// TODO poor workaround for backslashing acting as escape chars in regex, fix once when proper file wild pattern searching is implemented
			if(pathFilter != null && !pathFilter.test(filePathStr.replace('\\', '/'))) {
				return;
			}
			List<String> lines = null;
			try {
				lines = readLines(file.toPath(), cs);
			} catch (IOException e) {
				throw new RuntimeException(filePathStr, e);
			}
			FileInfo fileInfo = new FileInfo(file, lines);
			this.allFiles.put(file, fileInfo);
			Map<File, FileInfo> files = this.filesByDir.get(folder);
			if(files == null) {
				files = new HashMap<>();
				this.filesByDir.put(folder, files);
			}
			files.put(file, fileInfo);
		});
	}


	public int getFileCount() {
		int count = allFiles.size();
		return count;
	}


	/** Search for a string the set of files loaded into this file manipulator
	 * @param searchStr the string to search for. The string is compared to
	 * individual lines, a search string cannot match across multiple lines
	 * @return an object representing the results of this search operation
	 */
	public ManipulateFileLines search(String searchStr) {
		Map<File, FileLineSearch> allMatches = new HashMap<>();

		this.allFiles.forEach((f, fileInfo) -> {
			List<String> lines = fileInfo.getLines();
			FileLineSearch lineMatches = allMatches.get(f);

			for(int i = 0, size = lines.size(); i < size; i++) {
				String line = lines.get(i);
				int index = line.indexOf(searchStr);
				if(index > -1) {
					if(lineMatches == null) {
						lineMatches = new FileLineSearch(fileInfo, new IntArrayList(), new ArrayList<>());
						allMatches.put(f, lineMatches);
					}

					lineMatches.getMatchingLineNums().add(i);
					lineMatches.getMatchingLines().add(line);
				}
			}
		});

		return new ManipulateFileLines((s) -> {
			return s.indexOf(searchStr) > -1;
		}, (s1, s2) -> {
			return s1 + s2;
		}, (str, replace) -> {
			return StringReplace.replace(str, searchStr, replace);
		}, allMatches);
	}


	/** Call a consumer with each file and information about each file
	 * managed by this FileManipulator
	 * @param folderFileConsumer a consumer that receives a file and file info reference for
	 * each file managed by this file manipulator
	 */
	public void forEachFile(BiConsumer<File, FileInfo> folderFileConsumer) {
		filesByDir.entrySet().forEach((entry) -> {
			entry.getValue().forEach(folderFileConsumer);
		});
	}


	/** Save all of the modified files
	 * @param charset the charset to use when saving files
	 * @throws IOException if there is an error saving the files
	 */
	public void saveModifiedFiles(Charset charset) throws IOException {
		for(Map.Entry<File, Map<File, FileInfo>> dirFiles : filesByDir.entrySet()) {
			for(Map.Entry<File, FileInfo> fileInfo : dirFiles.getValue().entrySet()) {
				if(!fileInfo.getValue().isLinesModified()) {
					continue;
				}
				Files.write(fileInfo.getKey().toPath(), fileInfo.getValue().getLines(), charset);
				fileInfo.getValue().setLinesModified(false);
			}
		}
	}


	public static final List<String> readLines(Path file, Charset cs) throws IOException {
		int bufSize = 4096;
		CharsetDecoder decoder = cs.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getFileSystem().provider().newInputStream(file), decoder), bufSize)) {
            List<String> result = new ArrayList<>();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        }
	}

}
