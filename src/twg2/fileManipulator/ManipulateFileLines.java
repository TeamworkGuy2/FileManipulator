package twg2.fileManipulator;

import java.io.File;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author TeamworkGuy2
 * @since 2014-10-16
 */
public final class ManipulateFileLines extends ManipulateLines<File, FileInfo, String> {

	public ManipulateFileLines(Function<String, Boolean> searchCondition, BiFunction<String, String, String> add,
			BiFunction<String, String, String> replace, Map<File, FileLineSearch> matches) {
		super(searchCondition, add, replace, matches);
	}

}