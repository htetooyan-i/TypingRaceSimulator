# TypingRaceSimulator

Object Oriented Programming Project — ECS414U

## Project Structure

```
TypingRaceSimulator/
├── Part1/    # Textual simulation (Java, command-line)
└── Part2/    # GUI simulation (Java Swing)
```

## Setup

1. Install a Java Development Kit (JDK) version 11 or higher.
2. Open the `TypingRaceSimulator` project folder in your terminal.
3. Use the commands in the Part 1 or Part 2 sections to compile and run the version you want.

## Git Workflow

This project uses Git for version control.

- The `main` branch contains the stable implementation of Part 1.
- A separate branch `gui-development` was used for developing Part 2 (GUI features).
- After completing Part 2, the branch was merged back into `main`, resolving any conflicts.

### Example workflow

```bash
git checkout -b gui-development
# develop GUI features
git add .
git commit -m "Add GUI race view"

git checkout main
git merge gui-development
```

## Dependencies

- Java Development Kit (JDK) 11 or higher
- Java standard library
- Java Swing for Part 2 GUI windows and controls
- No external libraries or package managers are required

## Part 1 — Textual Simulation

Part 1 is the command-line version of the typing race simulator. It keeps the core race mechanics from the coursework: typists advance through a passage turn by turn, mistypes can slide them backwards, and burnout can temporarily prevent typing.

### How to compile

```bash
cd Part1
javac Typist.java TypingRace.java
```

### How to run

The race is started by calling `startRace()` on a `TypingRace` object.
A simple way to test this is to add a `main` method to `TypingRace`, for example:

```java
public static void main(String[] args) {
    TypingRace race = new TypingRace(40);
    race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
    race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
    race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
    race.startRace();
}
```

Then run:

```bash
java TypingRace
```

### Usage Guidelines

- Create a `TypingRace` object with the desired passage length.
- Add one or more `Typist` objects before calling `startRace()`.
- Use this version to test the core simulation logic without the GUI.
- Run from inside the `Part1` folder so the source files compile correctly.

## Part 2 — GUI Simulation

Part 2 is the graphical version of the typing race simulator. It uses Java Swing to provide a full race setup screen, a live race view, statistics windows, and reward tracking.

### Features

- Passage selection with short, medium, long, and custom passages
- Adjustable seat count for 2 to 6 typists
- Difficulty modifiers including Autocorrect, Caffeine Mode, and Night Shift
- Customisable typists with typing style, keyboard type, symbol, colour, and accessories
- Live race rendering with character-by-character progress and mistype/burnout indicators
- Statistics panels for current race results, personal bests, history, and comparison views
- Leaderboard, points, titles, sponsor prizes, cumulative coins, and upgrade purchases

### How to compile

```bash
cd Part2
javac *.java
```

### How to run

Run the GUI from the main entry point:

```bash
java MainGUI
```

This opens the main configuration window and the leaderboard tab.

### Usage Guidelines

- Launch `MainGUI` to open the race configuration screen.
- Choose a passage, set the number of typists, and configure each typist before starting the race.
- Use the leaderboard tab to view points, coins, titles, sponsors, and upgrades.
- After each race, check the statistics windows for WPM, accuracy, burnouts, history, and comparisons.
- Run from inside the `Part2` folder so all GUI classes are available on the classpath.

## Notes

### Part 1

- Part 1 focuses on the core simulation mechanics and can be run from the terminal with standard Java tools.
- The starter code in Part1 was originally written by Ty Posaurus. It contains known issues — finding and fixing them is part of the coursework.

### Part 2

- Part 2 adds the full Java Swing interface, statistics, leaderboard, and reward systems.
- Both parts should compile and run using standard command-line tools without any IDE-specific configuration.
- This part builds on the core mechanics developed in Part 1 and provides a graphical interface for interaction.
