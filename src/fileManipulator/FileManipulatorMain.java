package fileManipulator;

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

import programParameter.ParameterSet;
import fileManipulator.FileManipulator.DebugOp;
import fileManipulator.FileManipulator.FileLineOp;
import fileManipulator.FileManipulator.FileManipulatorOp;


/**
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
public class FileManipulatorMain {


	public static final void manipulateFileArgs(String[] args) throws IOException {
		Charset cs = Charset.forName("UTF-8");
		String newline = "\n";

		System.out.println("args: " + Arrays.toString(args));

		FileManipulatorParameters params = new FileManipulatorParameters();
		ParameterSet<String> paramParser = FileManipulatorParameters.createParameterParser(params);

		paramParser.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");

		Path projFolder = params.getProjectFolder();
		FileManipulator folderManipulator = new FileManipulator(projFolder, params::isFileNameMatch, cs, newline);
		FileManipulatorOp searchSet = folderManipulator.searchFor(params.getSearchString());
		PrintFileMatchOps<File, String> printer = new PrintFileMatchOps<File, String>();
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
			System.out.println("project base folder: " + projFolder);
			searchSet.forEachFile((f, fileInfo) -> {
				System.out.println("modified: " + projFolder.relativize(f.toPath()));
			});
		}

		System.out.println(folderManipulator.getFileCount() + " total files, " +
				searchSet.getMatchingSourceCount() + " files " + (isDebugOp ? "matched" : "modified"));

		System.out.println("current project: " + projFolder);
	}


	public static final void manipulateFiles() throws IOException {
		Charset cs = Charset.forName("UTF-8");
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		System.out.print("enter folder path to load: ");
		String folderStr = in.nextLine();
		Path projFolder = Paths.get(folderStr);
		FileManipulator folderManipulator = new FileManipulator(projFolder, null, cs, "\n");
		PrintFileMatchOps<File, String> printer = new PrintFileMatchOps<File, String>();
		printer.writer = System.out;

		boolean isDebugOp = true;
		while(isDebugOp) {
			System.out.print("text to search for (or 'exit'): ");
			String searchStr = in.nextLine();

			if("exit".equals(searchStr)) {
				break;
			}

			FileManipulatorOp searchSet = folderManipulator.searchFor(searchStr);

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




	public static class PrintFileMatchOps<K extends File, L extends String> {
		public PrintStream writer;
		public Path root;


		// Consumer<K>
		public void matchingFile(K file) {
			writer.println(relativeFile(file));
		}


		// Consumer<L>
		public void matchingLine(L line) {
			writer.println(line);
		}


		// BiConsumer<Integer, K>
		public void matchingLineCountPerFile(Integer lineCount, K file) {
			writer.println(lineCount + " matching lines in: " + relativeFile(file) + '\n');
		}


		// BiConsumer<String, K>
		public void matchingLineFromFile(String line, K file) {
			writer.println(line + " matching line from: " + relativeFile(file) + '\n');
		}


		private Path relativeFile(K f) {
			return (root != null ? f.toPath().relativize(root) : f.toPath());
		}

	}




	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			manipulateFileArgs(args);
		}
		else {
			//fileManipulatorTest();
			manipulateFiles();
		}
	}

}
