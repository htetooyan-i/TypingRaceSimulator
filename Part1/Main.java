public class Main {
    public static void main(String[] args) {
        Typist htet = new Typist('①', "Htet", 0.9);
        Typist ty = new Typist('②', "Ty", 0.3);
        Typist alex = new Typist('③', "Alex", 0.7);
        TypingRace race = new TypingRace(20);
        race.addTypist(htet, 1);
        race.addTypist(ty, 2);
        race.addTypist(alex, 3);
        race.startRace();
    }
}
