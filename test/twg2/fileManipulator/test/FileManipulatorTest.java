package twg2.fileManipulator.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import twg2.collections.builder.MapBuilder;
import twg2.collections.builder.MapUtil;
import twg2.fileManipulator.FileManipulator;
import twg2.fileManipulator.FileManipulator.FileLineOp;
import twg2.fileManipulator.FileManipulator.FileManipulatorOp;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
public class FileManipulatorTest {

	private static class FileOpResult {
		private FileLineOp op;
		private String searchLine;
		private List<String> inputLines;
		private List<String> resultLines;


		public FileOpResult(FileLineOp op, String searchLine, List<String> inputLines, List<String> resultLines) {
			this.op = op;
			this.searchLine = searchLine;
			this.inputLines = inputLines;
			this.resultLines = resultLines;
		}


		@Override
		public String toString() {
			return op + ", search=\"" + searchLine + "\", input=" + inputLines + ", expect=" + resultLines;
		}

	}




	private static Map<File, Map<File, Entry<List<String>, List<FileOpResult>>>> createFileManipulatorData() {
		// folders of files to:
		//    lines in file to:
		//       file line operations to:
		//          operation input lines to: 
		//             expected result lines
		Map<File, Map<File, Entry<List<String>, List<FileOpResult>>>> folders = new HashMap<>();

		folders.put(new File("folder-A"), MapBuilder.mutable(
			Tuples.of(new File("file-A-1"),
				Tuples.of(list("heart", "head", "lung", "liver"),
					list(
						new FileOpResult(FileLineOp.ADD_LINE_AFTER, "ea",
							list("two"),
							list("heart", "two", "head", "two", "lung", "liver")
						),
						new FileOpResult(FileLineOp.ADD_LINE_BEFORE, "ea",
							list(""),
							list("", "heart", "", "head", "lung", "liver")
						),
						new FileOpResult(FileLineOp.APPEND_TO_LINE, "ea",
							list("-2", "postline"),
							list("heart-2", "postline", "head-2", "postline", "lung", "liver")
						),
						new FileOpResult(FileLineOp.PREPEND_TO_LINE, "ea",
							list("preline", "super-"),
							list("preline", "super-heart", "preline", "super-head", "lung", "liver")
						),
						new FileOpResult(FileLineOp.REMOVE_LINE, "ea",
							list("abc", "123", "alpha beta gamma"),
							list("lung", "liver")
						),
						new FileOpResult(FileLineOp.REPLACE_LINE, "ea",
							list("aa", "bb"),
							list("aa", "bb", "aa", "bb", "lung", "liver")
						),
						new FileOpResult(FileLineOp.REPLACE_MATCHING_PORTION, "ea",
							list("u", "nibble"),
							list("hurt", "nibble", "hud", "nibble", "lung", "liver")
						)
					)
				)
			),
			Tuples.of(new File("file-A-2"),
				Tuples.of(list("a daisy", "a rose", "an archeria"),
					list(
						new FileOpResult(FileLineOp.ADD_LINE_AFTER, "ea",
							list("two"),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.ADD_LINE_BEFORE, "ea",
							list(""),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.APPEND_TO_LINE, "ea",
							list("-2", "postline"),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.PREPEND_TO_LINE, "ea",
							list("preline", "super-"),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.REMOVE_LINE, "ea",
							list("abc", "123", "alpha beta gamma"),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.REPLACE_LINE, "ea",
							list("a1a", "b2b"),
							list("a daisy", "a rose", "an archeria")
						),
						new FileOpResult(FileLineOp.REPLACE_MATCHING_PORTION, "ea",
							list("u", "nibble"),
							list("a daisy", "a rose", "an archeria")
						)
					)
				)
			)
		));

		/*folders.put(new File("folder-B"), hashMapOf(listOf(
				Entries.of(new File("file-B-1"), listOf("line B one one", "line B one two")),
				Entries.of(new File("file-B-2"), listOf("javascript", "typescript", "coffeescript", "notschript")),
				Entries.of(new File("file-B-3"), listOf("line B three one", "line B three two", "line B three three"))
		)));*/

		return folders;
	}


	@Test
	public void fileManipulatorTest() {
		Map<File, Map<File, Entry<List<String>, List<FileOpResult>>>> filesData = createFileManipulatorData();

		filesData.forEach((folder, searchData) -> {
			// for each file and lines of text
			searchData.forEach((file, fileData) -> {
				List<FileOpResult> searchTests = fileData.getValue();
				// for each manipulate operation test
				for(FileOpResult searchTest : searchTests) {
					Map<File, Map<File, Entry<List<String>, List<FileOpResult>>>> filesDat = createFileManipulatorData();
					Map<File, Map<File, List<String>>> fileLines = MapUtil.map(filesDat, (k, v) -> {
						return Tuples.of(k, MapUtil.map(v, (k2, v2) -> Tuples.of(k2, v2.getKey())));
					});
					FileManipulator fileManipulator = new FileManipulator(fileLines);
					FileManipulatorOp manipOp = fileManipulator.searchFor(searchTest.searchLine);
					manipOp.manipulateLinesLines(searchTest.op, searchTest.inputLines);
					// for each matching set of file lines
					fileManipulator.forEachFile((fileRes, fileInfo) -> {
						if(fileRes.equals(file)) {
							List<String> expectedLines = searchTest.resultLines;
							Assert.assertArrayEquals(searchTest.toString(), expectedLines.toArray(), fileInfo.getLines().toArray());
						}
					});
				}
			});
		});
	}


	@SafeVarargs
	public static final <T> ArrayList<T> list(T... ts) {
		ArrayList<T> list = new ArrayList<>();
		for(T t : ts) {
			list.add(t);
		}
		return list;
	}

}
