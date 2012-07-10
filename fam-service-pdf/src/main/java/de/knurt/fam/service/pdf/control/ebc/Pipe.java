package de.knurt.fam.service.pdf.control.ebc;

public class Pipe<T, G, U> implements BoardUnit<T, U> {
	private BoardUnit<T, G> first;
	private BoardUnit<G, U> second;

	public Pipe(BoardUnit<T, G> first, BoardUnit<G, U> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public U process(T datum) {
		return second.process(first.process(datum));
	}

}