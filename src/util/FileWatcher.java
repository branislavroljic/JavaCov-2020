package util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;


public abstract class FileWatcher implements Runnable{
	
	protected Path dir;
	private WatchService watcher;
	
	FileWatcher(Path dir) throws IOException{
		this.dir = dir;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dir.register(watcher, ENTRY_MODIFY);
	}

	/*
	 * obrada fajla u kome je registrovana modifikacija na odgovarajuci nacin
	 */
	public abstract void obrada(Path fileName);
	
	@Override 
	public void run() {
		
		while (true) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException ex) {
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Path fileName = (Path) event.context();
		        obrada(fileName);
			}

			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}
}