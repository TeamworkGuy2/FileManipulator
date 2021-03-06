package twg2.fileManipulator;

import java.util.List;

import twg2.collections.primitiveCollections.IntArrayList;

/**
 * @author TeamworkGuy2
 * @since 2014-10-21
 */
final class FileLineSearch implements LineSearch<FileInfo, String> {
	private FileInfo fileInfo;
	private IntArrayList matchingLineNums;
	private List<String> matchingLineStrs;


	/**
	 * @param fileInfo
	 * @param matchingLineNums
	 * @param matchingLineStrs
	 */
	public FileLineSearch(FileInfo fileInfo, IntArrayList matchingLineNums, List<String> matchingLineStrs) {
		this.fileInfo = fileInfo;
		this.matchingLineNums = matchingLineNums;
		this.matchingLineStrs = matchingLineStrs;
	}


	@Override
	public FileInfo getSource() {
		return fileInfo;
	}


	@Override
	public void setModified() {
		fileInfo.setLinesModified(true);
	}


	@Override
	public List<String> getSourceLines() {
		return fileInfo.getLines();
	}


	@Override
	public IntArrayList getMatchingLineNums() {
		return matchingLineNums;
	}


	@Override
	public List<String> getMatchingLines() {
		return matchingLineStrs;
	}


	@Override
	public String toString() {
		return "file=" + fileInfo + ", matchLineNums=" + matchingLineNums + ", matchLines=" + matchingLineStrs;
	}

}