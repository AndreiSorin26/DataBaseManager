# DataBaseManager

## 1. Non‑technical overview

DataBaseManager is a small console application built in Java for a third‑year university project. It lets you create users, add questions, assemble quizzes and record solutions entirely from the command line. All information is saved in plain CSV files which are loaded into memory when the program starts.

Keeping the data in CSV rather than a full database was intentional: it means the program runs with only a few megabytes of memory and no external services. For the sample data set of a few hundred entries each command completes in well under a second on standard hardware.

Typical usage looks like this:

```bash
# create a user
java Tema1 --create-user --u 'john' --p 'doe'

# add a question owned by that user
java Tema1 --create-question --u 'john' --p 'doe' --text '2+2?' --type 'single' --answer-1 '4' --answer-1-is-correct '1'
```

Once quizzes are created, participants can submit answers and the program will calculate a score.

## 2. Technical details

### Data model and storage

The project defines four models (`User`, `Question`, `Quiz`, `Submission`) that all implement a small `IModel` interface. They are managed by a generic class called `DataBase<T>` which loads and writes CSV files. Each specific database (for example `UserDB`) inherits from it and only implements conversion logic. This keeps the code compact and reduces duplication.

```java
public abstract class DataBase<T extends IModel> {
    public void Load(String fileName) { /* parse CSV */ }
    public void WriteBack(File file) { /* export CSV */ }
}
```

The CSV data is read once at startup so all queries are performed in memory. Because the project targets small files, a simple linear search through lists is more than fast enough.

### Command parsing and output

A lightweight argument parser splits the command line into a command and key‑value arguments. Responses are produced through a minimal JSON builder so the output can easily be consumed by other scripts. Below is an abbreviated outline of the parser:

```java
Args args = ArgParser.Parse(arguments);
```

### Scoring algorithm

When a quiz is submitted, each question distributes a fraction of one point over its answers. Correct answers add to the score while wrong answers subtract, and the final percentage is clamped to zero to avoid negative results. For small quizzes (ten questions or fewer) this computation is instantaneous.

### Running the project

Compile everything with `javac` and run `Tema1` with the desired command as shown above. The provided CSV files inside `src/database` hold the current state of users, questions, quizzes and submissions.

---

This repository served as practice in object‑oriented design and in handling simple persistence without requiring extra dependencies.
