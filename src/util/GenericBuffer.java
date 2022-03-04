package util;

import java.io.Serializable;
import java.util.LinkedList;

/*
 * bafer implementiran kao red(kruzni bafer), omogucava sinhronizovano dodavanje i dohvatanje elemenata
 * ako je broj elemenata u baferu jednak velicini, naredno ubacivanje elemenata
 * ce prouzrokovati izbacivanje elementa koristenjem funkcije pollLast();
 */
public class GenericBuffer<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private LinkedList<T> bafer = new LinkedList<>();
	protected final int VELICINA;
	
	public GenericBuffer(int size) {
		VELICINA = size;
	}
	
	public synchronized void add(T element){
		if(bafer.size() == VELICINA) {
			bafer.pollLast();
		}
		bafer.offerFirst(element);
	}
	
	public int getVelicina() {
		return VELICINA;
	}
	
	public synchronized T getElementAt(int pos) {
		return bafer.get(pos);
	}
	
	public synchronized LinkedList<T> getElements(){
		return bafer;
	}
	
	public synchronized int getBrojElemenata() {
		return bafer.size();
	}
}
