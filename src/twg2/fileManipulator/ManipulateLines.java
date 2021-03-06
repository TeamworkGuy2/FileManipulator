package twg2.fileManipulator;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.functions.TriConsumer;

/** An action that manipulates {@link LinSearch} data
 * @param <K> the type of key the manipulator bases it's source map on
 * @param <V> the type of line search source data
 * @param <T> the type of data that the search lines are stored as
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
public abstract class ManipulateLines<K, V, T> {

	/** An enumeration of the operations that can be applied to a set of matched lines
	 * @author TeamworkGuy2
	 * @since 2014-10-20
	 */
	public static enum FileLineOp {
		/** Add text to the end of each matched line */
		APPEND_TO_LINE,
		/** Add text to the beginning of each matched line */
		PREPEND_TO_LINE,
		/** Add a new line of text after each matched line */
		ADD_LINE_AFTER,
		/** Add a new line of text before each matched line */
		ADD_LINE_BEFORE,
		/** Remove each matched line */
		REMOVE_LINE,
		/** Replace the entirety of each matched line */
		REPLACE_LINE,
		/** Replace the portion of each line that was matched */
		REPLACE_MATCHING_PORTION;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-4-13
	 */
	public static enum ReplacementLinesOp {
		APPEND,
		PREPEND,
		REPLACE_ALL,
		REPLACE_PORTION,
	}




	/** An enumeration of debug operations that can be applied to a set of matched lines
	 * @author TeamworkGuy2
	 * @since 2014-11-16
	 */
	public static enum DebugOp {
		/** Print out a list of all matching lines */
		PRINT_MATCH_FILES,
		/** Print out a list of files containing matching lines */
		PRINT_MATCH_LINES,
		/** Print out a list of matching line counts per file */
		PRINT_MATCHING_LINE_COUNT_PER_FILE,
		/** Print out a list of matching lines per file */
		PRINT_MATCHING_LINES_PER_FILE;
	}


	private final Map<K, ? extends LineSearch<? extends V, T>> matches;
	private final Function<T, Boolean> searchCondition;
	private final BiFunction<T, T, T> replaceFunc;
	private final BiFunction<T, T, T> add;


	public ManipulateLines(Function<T, Boolean> searchCondition, BiFunction<T, T, T> add,
			BiFunction<T, T, T> replaceFunc, Map<K, ? extends LineSearch<? extends V, T>> matches) {
		this.matches = matches;
		this.searchCondition = searchCondition;
		this.replaceFunc = replaceFunc;
		this.add = add;
	}


	public Map<K, LineSearch<V, T>> getMatches() {
		@SuppressWarnings("unchecked")
		Map<K, LineSearch<V, T>> m = (Map<K, LineSearch<V, T>>) this.matches;
		return m;
	}


	public ManipulateLines<K, V, T> manipulateLines(FileLineOp op, T line) {
		// indicates whether the line being manipulated matches this search condition
		boolean insertMatches = searchCondition.apply(line);
		switch(op) {
		case ADD_LINE_AFTER:
			int insertOffset = 1;
			//$FALL-THROUGH$
		case ADD_LINE_BEFORE:
			if(op == FileLineOp.ADD_LINE_BEFORE) {
				insertOffset = 0;
			}
			else {
				insertOffset = 1;
			}
			final int insertOff = insertOffset;
			matches.forEach((f, lineSearch) -> {
				IntArrayList lineNums = lineSearch.getMatchingLineNums();
				List<T> matchingLines = lineSearch.getMatchingLines();
				List<T> lines = lineSearch.getSourceLines();
				lineSearch.setModified();
				// for each matching line in each file, insert a line before or
				// after the current line and increment line indices occurring after the inserted index
				for(int i = 0; i < lineNums.size(); i++) {
					lines.add(lineNums.get(i) + insertOff, line);
					if(insertMatches) {
						lineNums.add(i + insertOff, lineNums.get(i) + insertOff);
						matchingLines.add(i + insertOff, line);
						i++;
					}

					for(int ii = (insertMatches ? i + insertOff : i + insertOff), size = lineNums.size(); ii < size; ii++) {
						lineNums.set(ii, lineNums.get(ii) + 1);
					}
				}
			});
			break;
		case APPEND_TO_LINE:
			boolean append = true;
			//$FALL-THROUGH$
		case PREPEND_TO_LINE:
			if(op == FileLineOp.PREPEND_TO_LINE) {
				append = false;
			}
			else {
				append = true;
			}
			final boolean prepend = !append;
			matches.forEach((f, lineSearch) -> {
				IntArrayList lineNums = lineSearch.getMatchingLineNums();
				List<T> matchingLines = lineSearch.getMatchingLines();
				List<T> lines = lineSearch.getSourceLines();
				lineSearch.setModified();
				for(int i = 0, size = lineNums.size(); i < size; i++) {
					int lineNum = lineNums.get(i);
					T res = null;
					if(prepend) {
						res = add.apply(line, lines.get(lineNum));							
					}
					else {
						res = add.apply(lines.get(lineNum), line);
					}
					// append to the file line
					lines.set(lineNum, res);
					matchingLines.set(i, res);
				}
			});
			break;
		case REMOVE_LINE:
			matches.forEach((f, lineSearch) -> {
				IntArrayList lineNums = lineSearch.getMatchingLineNums();
				List<T> lines = lineSearch.getSourceLines();
				lineSearch.setModified();
				// for each matching line in each file, insert a line before or
				// after the current line and increment line indices occurring after the inserted index
				for(int i = lineNums.size() - 1; i > -1; i--) {
					lines.remove(lineNums.get(i));
				}
				lineNums.clear();
				lineSearch.getMatchingLines().clear();
			});
			break;
		case REPLACE_LINE:
			matches.forEach((f, lineSearch) -> {
				IntArrayList lineNums = lineSearch.getMatchingLineNums();
				List<T> matchingLines = lineSearch.getMatchingLines();
				List<T> lines = lineSearch.getSourceLines();
				lineSearch.setModified();
				for(int i = 0, size = lineNums.size(); i < size; i++) {
					int lineNum = lineNums.get(i);
					// for each matching line of each file, replace the matching line with the new line
					lines.set(lineNum, line);
					matchingLines.set(i, line);
				}
			});
			break;
		case REPLACE_MATCHING_PORTION:
			matches.forEach((f, lineSearch) -> {
				IntArrayList lineNums = lineSearch.getMatchingLineNums();
				List<T> matchingLines = lineSearch.getMatchingLines();
				List<T> lines = lineSearch.getSourceLines();
				lineSearch.setModified();
				for(int i = 0, size = lineNums.size(); i < size; i++) {
					int lineNum = lineNums.get(i);
					T lineReplaced = replaceFunc.apply(lines.get(lineNum), line);
					// for each matching line of each file, replace the matching line with the new line
					lines.set(lineNum, lineReplaced);
					matchingLines.set(i, lineReplaced);
				}
			});
			break;
		default:
			throw new IllegalStateException("unsupported enum value: " + op);
		}
		return this;
	}


	public ManipulateLines<K, V, T> manipulateLinesLines(FileLineOp op, List<T> lines) {
		// indicates whether the line being manipulated matches this search condition
		int lineCount = lines.size();
		boolean[] insertMatches = new boolean[lineCount];
		for(int i = 0; i < lineCount; i++) {
			insertMatches[i] = searchCondition.apply(lines.get(i));
		}

		switch(op) {
		case ADD_LINE_AFTER:
			//$FALL-THROUGH$
		case ADD_LINE_BEFORE:
			addToMatchingLines(null, op == FileLineOp.ADD_LINE_AFTER, op == FileLineOp.ADD_LINE_AFTER ? 1 : 0, lines, insertMatches);
			break;
		case APPEND_TO_LINE:
			addToMatchingLines(ReplacementLinesOp.APPEND, true, 1, lines, insertMatches);
			break;
		case PREPEND_TO_LINE:
			addToMatchingLines(ReplacementLinesOp.PREPEND, false, 0, lines, insertMatches);
			break;
		case REMOVE_LINE:
			removeMatchingLines();
			break;
		case REPLACE_LINE:
			addToMatchingLines(ReplacementLinesOp.REPLACE_ALL, true, 1, lines, insertMatches);
			break;
		case REPLACE_MATCHING_PORTION:
			addToMatchingLines(ReplacementLinesOp.REPLACE_PORTION, true, 1, lines, insertMatches);
			break;
		default:
			throw new IllegalStateException("unsupported enum value: " + op);
		}
		return this;
	}


	// package-private
	void addToMatchingLines(ReplacementLinesOp op, boolean modifyFirst, int stepOffset, List<T> insertLines, boolean[] insertMatches) {
		matches.forEach((f, lineSearch) -> {
			IntArrayList matchLineNums = lineSearch.getMatchingLineNums();
			List<T> matchLines = lineSearch.getMatchingLines();
			List<T> lines = lineSearch.getSourceLines();
			lineSearch.setModified();
			// for each matching line in each file, insert the insertLines list before or after it and
			// increment line indices occurring after the inserted index
			for(int i = 0; i < matchLineNums.size(); i++) {
				int insertOff = stepOffset;
				// insert the list of new items
				for(int k = 0, count = insertLines.size(); k < count; k++) {
					// modify existing item
					if(op != null && ((modifyFirst && k == 0) || (!modifyFirst && k == count - 1))) {
						int lineNum = matchLineNums.get(i);
						T line = lines.get(lineNum);
						T insertLine = insertLines.get(k);

						if((op == ReplacementLinesOp.APPEND || op == ReplacementLinesOp.PREPEND)) {
							T first = op == ReplacementLinesOp.PREPEND ? insertLine : line;
							T second = op == ReplacementLinesOp.PREPEND ? line : insertLine;
							T res = add.apply(first, second);
							lines.set(lineNum, res);
							matchLines.set(i, res);
						}
						else if(op == ReplacementLinesOp.REPLACE_ALL) {
							// for each matching line of each file, replace the matching line with the new line
							lines.set(lineNum, insertLine);
							matchLines.set(i, insertLine);
						}
						else if(op == ReplacementLinesOp.REPLACE_PORTION) {
							T lineReplaced = replaceFunc.apply(lines.get(lineNum), insertLine);
							// for each matching line of each file, replace the matching line with a new line with the matching portion replaced
							lines.set(lineNum, lineReplaced);
							matchLines.set(i, lineReplaced);
						}
					}
					// insert new item
					else {
						lines.add(matchLineNums.get(i) + insertOff, insertLines.get(k));

						if(insertMatches[k]) {
							matchLineNums.add(i + insertOff, matchLineNums.get(i) + insertOff);
							matchLines.add(i + insertOff, insertLines.get(k));
							i++;
						}
						for(int ii = (insertMatches[k] ? i + insertOff : i + insertOff), size = matchLineNums.size(); ii < size; ii++) {
							matchLineNums.set(ii, matchLineNums.get(ii) + 1);
						}
						insertOff += stepOffset;
					}
				}
			}
		});
	}


	// package-private
	void removeMatchingLines() {
		matches.forEach((f, lineSearch) -> {
			IntArrayList lineNums = lineSearch.getMatchingLineNums();
			List<T> lines = lineSearch.getSourceLines();
			lineSearch.setModified();
			// for each matching line in each file, insert a line before or
			// after the current line and increment line indices occurring after the inserted index
			for(int i = lineNums.size() - 1; i > -1; i--) {
				lines.remove(lineNums.get(i));
			}
			lineNums.clear();
			lineSearch.getMatchingLines().clear();
		});
	}


	/** Pass all of the matching lines to a callback function
	 * @param op the {@link DebugOp} operation to use to determine which lines to pass to the consumer function
	 * @param matchingFile if {@link DebugOp} is {@link DebugOp#PRINT_MATCH_FILES}, each of the
	 * matching files in this manipulator are passed to this callback function
	 * @param matchingLine if {@link DebugOp} is {@link DebugOp#PRINT_MATCH_LINES}, each of the
	 * matching lines in this manipulator are passed to this callback function
	 * @param matchingLineCountPerFile if {@link DebugOp} is {@link DebugOp#PRINT_MATCHING_LINE_COUNT_PER_FILE},
	 * each of the matching line counts in this manipulator are passed to this callback function
	 * @param matchingLineFromFile if {@link DebugOp} is {@link DebugOp#PRINT_MATCHING_LINES_PER_FILE}, each of the
	 * matching lines of each file in this manipulator are passed to this callback function
	 * @return this instance
	 */
	public ManipulateLines<K, V, T> lineOperation(DebugOp op, Consumer<K> matchingFile, Consumer<T> matchingLine,
			BiConsumer<Integer, K> matchingLineCountPerFile, BiConsumer<T, K> matchingLineFromFile) {
		switch(op) {
		case PRINT_MATCH_FILES:
			matches.forEach((k, lineSearch) -> {
				matchingFile.accept(k);
			});
			break;
		case PRINT_MATCH_LINES:
			matches.forEach((k, lineSearch) -> {
				List<T> lineSet = lineSearch.getMatchingLines();
				for(int i = 0, size = lineSet.size(); i < size; i++) {
					matchingLine.accept(lineSet.get(i));
				}
			});
			break;
		case PRINT_MATCHING_LINE_COUNT_PER_FILE:
			matches.forEach((k, lineSearch) -> {
				matchingLineCountPerFile.accept(lineSearch.getMatchingLines().size(), k);
			});
			break;
		case PRINT_MATCHING_LINES_PER_FILE:
			matches.forEach((k, lineSearch) -> {
				List<T> lineSet = lineSearch.getMatchingLines();
				for(int i = 0, size = lineSet.size(); i < size; i++) {
					matchingLineFromFile.accept(lineSet.get(i), k);
				}
			});
			break;
		default:
			throw new IllegalStateException("unsupported enum value: " + op);
		}
		return this;
	}


	/** Loop through every line in this matching manipulator set
	 * @param task the function to call with each matching line
	 */
	public void forEach(TriConsumer<K, T, Integer> task) {
		matches.forEach((f, searchObj) -> {
			List<T> lines = searchObj.getMatchingLines();
			IntArrayList lineNums = searchObj.getMatchingLineNums();
			for(int i = 0, size = lines.size(); i < size; i++) {
				task.accept(f, lines.get(i), lineNums.get(i));
			}
		});
	}


	public void forEachFile(BiConsumer<K, V> task) {
		matches.forEach((f, searchObj) -> {
			task.accept(f, searchObj.getSource());
		});
	}


	public int getMatchingSourceCount() {
		return matches.size();
	}


	public int getMatchingLineCount() {
		int count = 0;
		for(Map.Entry<K, ? extends LineSearch<? extends V, T>> file : matches.entrySet()) {
			count += file.getValue().getMatchingLines().size();
		}
		return count;
	}

}