package twg2.fileManipulator;

import java.util.List;

import twg2.collections.primitiveCollections.IntArrayList;

/**
 * @param <S> the source object/information
 * @param <T> the type of lines of data from the source
 * @author TeamworkGuy2
 * @since 2014-10-22
 */
interface LineSearch<S, T> {

	public S getSource();

	public void setModified();

	public List<T> getSourceLines();

	public IntArrayList getMatchingLineNums();

	public List<T> getMatchingLines();

}