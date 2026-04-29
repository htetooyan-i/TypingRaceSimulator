public class StatModifier {

    private double speed;
    private double accuracy;
    private double burnout;

    public StatModifier(double speed, double accuracy, double burnout) {
        this.speed = speed;
        this.accuracy = accuracy;
        this.burnout = burnout;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getBurnout() {
        return burnout;
    }
}