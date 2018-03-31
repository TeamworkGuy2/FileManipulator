package twg2.fileManipulator.test;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import twg2.collections.builder.MapUtil;
import twg2.fileManipulator.FileManipulator;
import twg2.fileManipulator.FileManipulatorParameters;
import twg2.fileManipulator.ManipulateFileLines;
import twg2.fileManipulator.ManipulateLines.FileLineOp;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
public class FileManipulatorTest {
	private Charset cs = Charset.forName("UTF-8");

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



	private static Map<File, List<FileSearchTestData>> createFileManipulatorData() {
		// folders of files to:
		//    lines in file to:
		//       file line operations to:
		//          operation input lines to: 
		//             expected result lines
		Map<File, List<FileSearchTestData>> folders = new HashMap<>();

		folders.put(new File("folder-A"), List.of(
			new FileSearchTestData(
				new File("file-A-1"),
				list("heart", "head", "lung", "liver"),
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
			),
			new FileSearchTestData(
				new File("file-A-2"),
				list("a daisy", "a rose", "an archeria"),
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
		));

		return folders;
	}


	@Test
	public void fileManipulatorTest() {
		Map<File, List<FileSearchTestData>> filesData = createFileManipulatorData();

		filesData.forEach((folder, searchData) -> {
			// for each file and lines of text
			searchData.forEach((fileTests) -> {
				for(FileOpResult searchTest : fileTests.expectedResults) {
					// extract new search and manipulation data for each test
					Map<File, List<FileSearchTestData>> filesDat = createFileManipulatorData();
					Map<File, Map<File, List<String>>> fileLines = MapUtil.map(filesDat, (k, v) -> {
						return Tuples.of(k, MapUtil.map(v, (f) -> Tuples.of(f.file, f.fileLines)));
					});
					// run the search
					FileManipulator fileManipulator = new FileManipulator(fileLines);
					ManipulateFileLines manipOp = fileManipulator.search(searchTest.searchLine);
					manipOp.manipulateLinesLines(searchTest.op, searchTest.inputLines);
					// check the results
					AtomicInteger checked = new AtomicInteger();
					fileManipulator.forEachFile((fileRes, fileInfo) -> {
						if(fileRes.equals(fileTests.file)) {
							List<String> expectedLines = searchTest.resultLines;
							Assert.assertArrayEquals(searchTest.toString(), expectedLines.toArray(), fileInfo.getLines().toArray());
							checked.incrementAndGet();
						}
					});
					Assert.assertEquals(1, checked.get());
				}
			});
		});
	}


	@Test
	public void fileManipulatorParametersTest() {
		FileManipulatorParameters fmp = new FileManipulatorParameters(cs);
		fmp.setExcludePatterns(list("/bin/", ".dll$", "^/.vs", "^proj/", "/main$"));
		String[] res = filter(list(
				"/proj/src/main.cs",
				"/proj/src/helper.h",
				"/proj/lib/helper.dll",
				"/proj/lib/helper.dll.config",
				"/proj/bin/main.cs",
				"/proj/main.dll",
				"/.vs/src/settings.cs"
		), fmp::isFileNameMatch);
		Assert.assertArrayEquals(new String[] {
				"/proj/src/main.cs",
				"/proj/src/helper.h",
				"/proj/lib/helper.dll.config"
		}, res);

		FileManipulatorParameters fmp2 = new FileManipulatorParameters(cs);
		fmp2.setIncludePatterns(list(".cs$", ".java$", ".js$", ".ts$", "/helper"));
		fmp2.setExcludePatterns(list(".dll$"));
		String[] res2 = filter(list(
				"/proj/src/main.cs",
				"/proj/src/helper.h",
				"/proj/lib/helper.dll",
				"/proj/lib/helper.dll.config",
				"/proj/bin/main.cs",
				"/proj/main.dll",
				"/.vs/src/settings.cs"
		), fmp2::isFileNameMatch);
		Assert.assertArrayEquals(new String[] {
				"/proj/src/main.cs",
				"/proj/src/helper.h",
				"/proj/lib/helper.dll.config",
				"/proj/bin/main.cs",
				"/.vs/src/settings.cs"
		}, res2);
	}


	@SafeVarargs
	static final <T> ArrayList<T> list(T... ts) {
		ArrayList<T> list = new ArrayList<>();
		for(T t : ts) {
			list.add(t);
		}
		return list;
	}


	@SuppressWarnings("unchecked")
	static final <T> T[] filter(Collection<? extends T> coll, Predicate<T> filter) {
		AtomicReference<Class<?>> type = new AtomicReference<>();
		return coll.stream().filter((t) -> {
			type.set(t.getClass());
			return filter.test(t);
		}).toArray((i) -> (T[])Array.newInstance(type.get(), i));
	}



	/**
	 * @author TeamworkGuy2
	 * @since 2018-03-25
	 */
	public static class FileSearchTestData {
		File file;
		List<String> fileLines;
		List<FileOpResult> expectedResults;

		public FileSearchTestData(File file, List<String> fileLines, List<FileOpResult> expectedResults) {
			this.file = file;
			this.fileLines = fileLines;
			this.expectedResults = expectedResults;
		}
	}

}
