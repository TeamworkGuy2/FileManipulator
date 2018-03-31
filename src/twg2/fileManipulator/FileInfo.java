package twg2.fileManipulator;

import java.io.File;
import java.util.List;

/** A file data container
 * @author TeamworkGuy2
 * @since 2014-10-16
 */
public class FileInfo {
	private final File file;
	private boolean linesModified;
	private List<String> lines;


	/**
	 * @param file
	 * @param lines
	 */
	public FileInfo(File file, List<String> lines) {
		this.file = file;
		this.linesModified = false;
		this.lines = lines;
	}


	public File getFile() {
		return file;
	}


	/**
	 * @return a mutable set of lines representing the file's contents
	 */
	public List<String> getLines() {
		return lines;
	}


	public boolean isLinesModified() {
		return linesModified;
	}


	public void setLinesModified(boolean linesModified) {
		this.linesModified = linesModified;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "file=" + file + ", modified=" + linesModified + ", lines=" + lines.toString();
	}

}
