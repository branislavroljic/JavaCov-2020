package util;

import java.io.IOException;
import java.nio.file.Path;
import simulacija.Mapa;
import simulacija.Simulacija;

public class ZarazeniFileWatcher extends FileWatcher{
	
	public final String watchFile;
	private Mapa mapa;

	public ZarazeniFileWatcher(Path dir, String watchFile, Mapa mapa) throws IOException{
		super(dir);
		this.watchFile = watchFile;
		this.mapa = mapa;
	}
	
	public void setMapa(Mapa mapa) {
		this.mapa = mapa;
	}
	
	//promjena vrijednosti labele u glavnoj formi aplikacije
	@Override
	public void obrada(Path fileName) {
		if(fileName.toString().trim().endsWith(watchFile)) {
			int ukupanBrojZarazenihUAmbulantama = mapa.ambulante.parallelStream().map(a -> a.getBrojZarazenih()).reduce(0, (a,b) -> a+b);
			Simulacija.glavnaForma.izmjenaBrojaca(ukupanBrojZarazenihUAmbulantama);
		}
	}
}
