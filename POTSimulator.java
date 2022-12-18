import Crypto.RSA;
import Helpers.HelperString;
import Helpers.HelperTime;
import POT.Entities.Item;
import POT.Entities.SimulatorSettings;

import java.util.Date;

public class POTSimulator {

	private static String SEPARADOR = "===========================================================";

	public static void main(String[] args) {

		try {

			ExecuteSimulation();

		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			System.out.println("Exception: " + ex.getLocalizedMessage());
		}
	}

	private static void ExecuteSimulation() throws Exception {
		Date iniciSimulacio = new Date();

		SimulatorSettings settings = new SimulatorSettings();

		String mainTitle = "#################################### POT Simulator v.1.00 ####################################";
		System.out.println(mainTitle);
		System.out.println("");
		System.out.println("Settings:");
		System.out.println(SEPARADOR);
		System.out.println(settings.print());

		int aux = Runtime.getRuntime().availableProcessors();
		System.out.println("*** Parallelism: "+aux+" ***");
		
		
		System.out.println("");
		System.out.println("");
		System.out.println(SEPARADOR);
		System.out.println("--- PRICED OBLIVIOUS TRANSFER");
		System.out.println(SEPARADOR);

		POTSimulation(settings, 16, 10, false);
		POTSimulation(settings, 16, 10, true);
		POTSimulation(settings, 16, 50, false);
		POTSimulation(settings, 16, 50, true);
		POTSimulation(settings, 16, 100, false);
		POTSimulation(settings, 16, 100, true);

		System.out.println("");
		System.out.println("");
		System.out.println("Total simulator: " + HelperTime.GetFormattedInterval(iniciSimulacio));
		System.out.println(HelperString.Fill("", mainTitle.length(), '#'));
	}


	private static void POTSimulation(SimulatorSettings settings, int M, int items, boolean executeOT) throws Exception {

		RSA rsa = new RSA();
		
		settings.MAX_TICKETS = items;
		
		ManagerPOT manager = new ManagerPOT(settings, rsa);

		Item.M = M;
		Item.MAX_PRICE = (int) (Math.pow(2, M) - 1);

		System.out.println("");
		System.out.println(HelperString.Fill("", SEPARADOR.length(), '-'));
		String title = "--- SIMULATION [m=" + M + " - Items="+settings.MAX_TICKETS + " - Protocol OT=" + executeOT +  "]";
		System.out.println(title); 
		System.out.println(HelperString.Fill("", SEPARADOR.length(), '-'));
		System.out.println("");

		manager.initialize();

		Date ini = new Date();

		int simulations = settings.MAX_POT_SIMULATIONS/4;
		System.out.println("-> Simulating " + simulations  + " executions POT serial");

		ini = new Date();
		manager.simulateSerial(simulations, executeOT);

		System.out.println("");
		double tempsExecucioSerial = (double) ((double) HelperTime.GetMillisecondsSeconds(ini)
				/ (double)simulations);
		System.out.println("Avg execution: " + tempsExecucioSerial + " ms.");
		System.out.println("Avg/bit: " + (double)(tempsExecucioSerial/(double)M) + " ms.");


		System.out.println("");
		System.out.println("");

		simulations = settings.MAX_POT_SIMULATIONS;
		System.out.println("-> Simulating " + settings.MAX_POT_SIMULATIONS + " executions POT async");
		ini = new Date();
		manager.simulateAsync(simulations,  executeOT);

		
		double tempsExecucioAsync = (double) ((double) HelperTime.GetMillisecondsSeconds(ini)
				/ (double) simulations);
		System.out.println("Avg execution: " + tempsExecucioAsync + " ms.");
		System.out.println("Avg/bit: " + (double)(tempsExecucioAsync/(double)M) + " ms.");
		
	}
}
