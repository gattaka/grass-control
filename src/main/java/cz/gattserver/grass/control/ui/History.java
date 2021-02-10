package cz.gattserver.grass.control.ui;

public class History<T> {

	private static class Item<T> {
		T value;
		Item<T> next;
		Item<T> prev;

		public Item(T value, Item<T> prev) {
			this.value = value;
			this.prev = prev;
			if (prev != null)
				prev.next = this;
		}
	}

	private Item<T> current;
	private Item<T> first;
	private Item<T> last;

	public void push(T value) {
		Item<T> item = new Item<>(value, current);
		if (current == null)
			first = item;
		current = item;
		last = item;
	}

	public boolean isFirst() {
		return current == null || current == first;
	}

	public boolean isLast() {
		return current == null || current == last;
	}

	public boolean isEmpty() {
		return current == null;
	}

	public T back() {
		if (current == null)
			throw new IllegalStateException("History is empty");
		if (current.prev == null)
			throw new IllegalStateException("History has no previous");
		current = current.prev;
		return current.value;
	}

	public T forward() {
		if (current == null)
			throw new IllegalStateException("History is empty");
		if (current.next == null)
			throw new IllegalStateException("History has no next");
		current = current.next;
		return current.value;
	}

	public T getCurrent() {
		return current == null ? null : current.value;
	}

}
