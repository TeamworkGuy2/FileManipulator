package twg2.fileManipulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import twg2.cli.ParameterSet;
import twg2.fileManipulator.ManipulateLines.DebugOp;
import twg2.fileManipulator.ManipulateLines.FileLineOp;


/**
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
public class FileManipulatorMain {


	public static final void manipulateFileArgs(String[] args, Charset cs) throws IOException {
		String newline = "\n";

		System.out.println("args: " + Arrays.toString(args));

		FileManipulatorParameters params = new FileManipulatorParameters(cs);
		ParameterSet<String> paramParser = FileManipulatorParameters.createParameterParser(params);

		paramParser.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");

		Path searchDir = params.getSearchDirectory();
		FileManipulator folderManipulator = new FileManipulator(searchDir, params::isFileNameMatch, cs, newline);
		ManipulateFileLines searchSet = folderManipulator.search(params.getSearchString());
		PrintFileMatchOps printer = new PrintFileMatchOps();
		printer.writer = System.out;

		Enum<?> fileOp = params.getOperation();
		boolean isDebugOp = false;
		if(fileOp instanceof FileLineOp) {
			searchSet.manipulateLines((FileLineOp)fileOp, params.getReplaceString());
		}
		else if(fileOp instanceof DebugOp) {
			isDebugOp = true;
			searchSet.lineOperation((DebugOp)fileOp, printer::matchingFile, printer::matchingLine, printer::matchingLineCountPerFile, printer::matchingLineFromFile);
		}
		else {
			throw new IllegalStateException("unknown file operation '" + fileOp + "'");
		}

		if(!isDebugOp) {
			// save the files
			folderManipulator.saveModifiedFiles(cs);
			// print the list of modified files
			System.out.println("project base folder: " + searchDir);
			searchSet.forEachFile((f, fileInfo) -> {
				System.out.println("modified: " + searchDir.relativize(f.toPath()));
			});
		}

		System.out.println("project: " + searchDir);
		System.out.println(folderManipulator.getFileCount() + " total files, " +
				searchSet.getMatchingSourceCount() + " files " + (isDebugOp ? "matched" : "modified"));
	}


	public static final void manipulateFiles(Charset cs) throws IOException {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		System.out.print("enter folder path to load: ");
		String folderStr = in.nextLine();
		Path projFolder = Paths.get(folderStr);
		FileManipulator folderManipulator = new FileManipulator(projFolder, null, cs, "\n");
		PrintFileMatchOps printer = new PrintFileMatchOps();
		printer.writer = System.out;

		boolean isDebugOp = true;
		while(isDebugOp) {
			System.out.print("text to search for (or 'exit'): ");
			String searchStr = in.nextLine();

			if("exit".equals(searchStr)) {
				break;
			}

			ManipulateFileLines searchSet = folderManipulator.search(searchStr);

			System.out.print("operation (one of: " + Arrays.toString(FileLineOp.values()) +
					", or " + Arrays.toString(DebugOp.values()) + "): ");
			String fileOpStr = in.nextLine();

			try {
				FileLineOp fileOp = FileLineOp.valueOf(fileOpStr);

				System.out.print("operation input text: ");
				String line = in.nextLine();

				searchSet.manipulateLines(fileOp, line);
				isDebugOp = false;
			} catch(Exception e) {
				DebugOp debugOp = DebugOp.valueOf(fileOpStr);
				searchSet.lineOperation(debugOp, printer::matchingFile, printer::matchingLine, printer::matchingLineCountPerFile, printer::matchingLineFromFile);
			}

			if(!isDebugOp) {
				// save the files
				folderManipulator.saveModifiedFiles(cs);
				// print the list of modified files
				System.out.println("project base folder: " + projFolder);
				searchSet.forEachFile((f, fileInfo) -> {
					System.out.println("modified: " + projFolder.relativize(f.toPath()));
				});
			}

			System.out.println(folderManipulator.getFileCount() + " total files, " +
					searchSet.getMatchingSourceCount() + " files " + (isDebugOp ? "matched" : "modified"));

			System.out.println("current project: " + projFolder);
		}
	}




	public static class PrintFileMatchOps {
		public PrintStream writer;
		public Path root;


		// Consumer<File>
		public void matchingFile(File file) {
			writer.println(relativeFile(file));
		}


		// Consumer<Object>
		public void matchingLine(Object line) {
			writer.println(line);
		}


		// BiConsumer<Integer, File>
		public void matchingLineCountPerFile(Integer lineCount, File file) {
			writer.println(lineCount + " matching lines in: " + relativeFile(file) + '\n');
		}


		// BiConsumer<String, File>
		public void matchingLineFromFile(String line, File file) {
			writer.println(line + " matching line from: " + relativeFile(file) + '\n');
		}


		private Path relativeFile(File f) {
			return (root != null ? f.toPath().relativize(root) : f.toPath());
		}

	}




	public static void main(String[] args) throws IOException {
		Charset cs = null;
		try {
			cs = Charset.forName("UTF-8");
		} catch (Exception e) {
			cs = Charset.defaultCharset();
		}

		manipulateFileArgs(args, cs);
	}

}
