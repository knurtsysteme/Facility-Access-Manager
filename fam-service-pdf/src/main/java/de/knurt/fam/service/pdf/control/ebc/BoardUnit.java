package de.knurt.fam.service.pdf.control.ebc;

public interface BoardUnit<T, U> {
	public U process(T datum);
	
}