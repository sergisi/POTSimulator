package POT.Entities;

public class SimulationResult {

    public long simulationTime;
    public long otTime;

    public SimulationResult(long simulationTime, long otTime) {
        this.simulationTime = simulationTime;
        this.otTime = otTime;
    }

    @Override
    public String toString() {
        return "SimulationResult{" +
                "simulationTime=" + simulationTime +
                ", otTime=" + otTime +
                '}';
    }
}
