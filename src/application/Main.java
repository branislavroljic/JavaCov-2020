package application;
	
import application.forme.PocetnaForma;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			PocetnaForma scc = new PocetnaForma();
			System.out.println("Pocelo je....");
			scc.showStage();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
