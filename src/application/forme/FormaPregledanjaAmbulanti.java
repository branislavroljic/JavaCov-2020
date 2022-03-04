package application.forme;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import elementi.zdravstveniObjekti.Ambulanta;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import simulacija.Simulacija;

public class FormaPregledanjaAmbulanti implements Initializable{
	private Simulacija simulacija;
	private Scene thisScene;
	private Stage stage;
	
	@FXML
	private TableView<Ambulanta> table;
	
	@FXML
	private TableColumn<Ambulanta, Integer> id;
	
	@FXML
	private TableColumn<Ambulanta, Integer> kapacitet;
	
	@FXML
	private TableColumn<Ambulanta, Integer> popunjenost;
	
	@FXML
	private Label infoLabel;
	
	public ObservableList<Ambulanta> list;
	
	
	
	public FormaPregledanjaAmbulanti(Simulacija simulacija) {

			this.simulacija = simulacija;
			list = FXCollections.observableArrayList(simulacija.getMapa().ambulante);
			
			//Load the FXML file
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/fxmlFajlovi/PregledanjeAmbulanti.fxml"));
				
				//Set this class as controller
				loader.setController(this);
				
				stage = new Stage();
				stage.setResizable(false);
				//Save the scene
				thisScene = new Scene(loader.load());
				stage.setScene(thisScene);
				stage.setOnShowing( event -> Simulacija.setZaustavljena(true));
				stage.setOnCloseRequest(event -> Simulacija.glavnaForma.izmijenjenaMatrica("")); 
				stage.setOnHiding(event ->   Simulacija.setZaustavljena(false));
				stage.show();
				
			} catch (IOException ex) {
				Logger.getLogger(FormaPregledanjaAmbulanti.class.getName()).log(Level.SEVERE, null, ex);
			}
		}


	@Override
	    public void initialize(URL location, ResourceBundle resources) {
			updateTable();
		}
	
	/*
	 * update tabele koja priazuje stanje ambulanti
	 */
	private void updateTable() {

		id.setCellValueFactory(new Callback<CellDataFeatures<Ambulanta, Integer>, ObservableValue<Integer>>() {
		     @SuppressWarnings({ "unchecked", "rawtypes" })
			public ObservableValue<Integer> call(CellDataFeatures<Ambulanta, Integer >p) {
		         return new ReadOnlyObjectWrapper(p.getValue().getID());
		     }
		  }); 
		kapacitet.setCellValueFactory(new Callback<CellDataFeatures<Ambulanta, Integer>, ObservableValue<Integer>>() {
		     @SuppressWarnings({ "unchecked", "rawtypes" })
			public ObservableValue<Integer> call(CellDataFeatures<Ambulanta, Integer >p) {
		         return new ReadOnlyObjectWrapper(p.getValue().getKapacitet());
		     }
		  });
		popunjenost.setCellValueFactory(new Callback<CellDataFeatures<Ambulanta, Integer>, ObservableValue<Integer>>() {
		     @SuppressWarnings({ "rawtypes", "unchecked" })
			public ObservableValue<Integer> call(CellDataFeatures<Ambulanta, Integer >p) {
		         return new ReadOnlyObjectWrapper(p.getValue().getBrojZarazenih());
		     }
		  });
		table.setItems(list);
	}
	
	/*
	 * kreiranje novih ambulanti, stare ambulante ostaju, nove se dodaju po ivicama matrice
	 */
	@FXML
	private void novaAmbulantaPressed(ActionEvent event) {
		
		List<Ambulanta> prazneAmbulante = simulacija.getMapa().ambulante.parallelStream()
				.filter(a -> a.getKapacitet() == 0).collect(Collectors.toList());
		if (prazneAmbulante.size() == 0) {
			infoLabel.setText("Nema ambulanti sa kapacitetom 0");
			return;
		}
		int postavljenihAmbulanti = simulacija.getMapa().postaviAmbulante(prazneAmbulante.size(), 1, false);
													//jer svaka naknadno postavljena ambulanta ima 1 vozilo
		simulacija.setBrojAmbulantnihVozila(simulacija.getBrojAmbulantnihVozila() + postavljenihAmbulanti); 
		simulacija.setBrojAmbulanti(simulacija.getBrojAmbulanti() + postavljenihAmbulanti);;
		Simulacija.glavnaForma.izmijenjenaMatrica("");

		infoLabel.setText("Mapa je azurirana, izadjite da biste nastavili simulaciju!");
	}

}
