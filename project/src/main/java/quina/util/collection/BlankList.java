package quina.util.collection;

import java.util.AbstractList;

public class BlankList<T> extends AbstractList<T>
	implements QuinaList<T> {
	@Override
	public T get(int index) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}
}
