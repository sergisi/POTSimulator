import Crypto.DSA;
import Crypto.RSA;
import Helpers.HelperString;
import Helpers.HelperTime;
import POT.Entities.Item;
import POT.Entities.SimulatorSettings;

import java.util.Date;

public class POTSimulatorImproved {

    private static final String SEPARADOR = "===========================================================";

    public static void main(String[] args) {

        try {

            ExecuteSimulation();

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void ExecuteSimulation() throws Exception {
        Date initDate = new Date();

        SimulatorSettings settings = new SimulatorSettings();

        String mainTitle = "#################################### POT Simulator v.1.00 ####################################";
        System.out.println(mainTitle);
        System.out.println();
        System.out.println("Settings:");
        System.out.println(SEPARADOR);
        System.out.println(settings.print());

        int aux = Runtime.getRuntime().availableProcessors();
        System.out.println("*** Parallelism: " + aux + " ***");


        System.out.println();
        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println("--- PRICED OBLIVIOUS TRANSFER");
        System.out.println(SEPARADOR);

        POTSimulation(settings, 16, 10);
        POTSimulation(settings, 16, 50);
        POTSimulation(settings, 16, 100);
        POTSimulation(settings, 16, 1000);

        System.out.println();
        System.out.println();
        System.out.println("Total simulator: " + HelperTime.GetFormattedInterval(initDate));
        System.out.println(HelperString.Fill("", mainTitle.length(), '#'));
    }


    private static void POTSimulation(SimulatorSettings settings, int M, int items) throws Exception {

        ManagerPOTImproved manager = getManagerPOT(settings, M, items);

        printHeaders(settings, M);

        var ini = new Date();
        manager.initialize();
        var initCost = new Date().getTime() - ini.getTime();
        System.out.println("Init time: " + initCost);
        serialSimulation(settings, M, manager);
        // asyncSimulation(settings, M, manager);

    }

    private static void asyncSimulation(SimulatorSettings settings, double M, ManagerPOTImproved manager) {
        Date ini;
        int simulations;

        simulations = settings.MAX_POT_SIMULATIONS;
        System.out.println("-> Simulating " + settings.MAX_POT_SIMULATIONS + " executions POT async");
        ini = new Date();
        manager.simulateAsync(simulations);


        double timeAsync = (double) HelperTime.GetMillisecondsSeconds(ini)
                / (double) simulations;
        System.out.println("Avg execution: " + timeAsync + " ms.");
        System.out.println("Avg/bit: " + (timeAsync / M) + " ms.");
    }

    private static void serialSimulation(SimulatorSettings settings, double M, ManagerPOTImproved manager) throws Exception {
        int simulations = settings.MAX_POT_SIMULATIONS;
        System.out.println("-> Simulating " + simulations + " executions POT serial");

        var timeSerial = manager.simulateSerial(simulations);
        System.out.println("Avg execution: " + timeSerial + " ms.");
        System.out.println("Avg/bit: " + (timeSerial / M) + " ms.");

        System.out.println();
        System.out.println();
    }

    private static void printHeaders(SimulatorSettings settings, int M) {
        System.out.println();
        System.out.println(HelperString.Fill("", SEPARADOR.length(), '-'));
        String title = "--- SIMULATION [m=" + M + " - Items=" + settings.MAX_TICKETS;
        System.out.println(title);
        System.out.println(HelperString.Fill("", SEPARADOR.length(), '-'));
        System.out.println();
    }

    private static ManagerPOTImproved getManagerPOT(SimulatorSettings settings, int M, int items) {
        RSA rsa = new RSA();
        DSA dsa = new DSA();

        settings.MAX_TICKETS = items;

        ManagerPOTImproved manager = new ManagerPOTImproved(settings, rsa, dsa);

        Item.M = M;
        Item.MAX_PRICE = (int) (Math.pow(2, M) - 1);
        return manager;
    }
}
