#  ThesisHSMZ-RLTicTacToe-Java

Dieses Repository enthält die Java Implementierung zu meiner Bachelorarbeit an der HS Mainz im Studiengang Wirtschaftsinformatik dual mit dem Titel: 

**Evaluation der Reinforcement Learning Algorithmen Sarsa und Q-Learning am Beispiel eines Strategiespiels**

Meine Bachelorarbeit kann eingesehen werden im [Repository ThesisHSMZ-RLTicTacToe](https://github.com/JonasBingel/ThesisHSMZ-RLTicTacToe)

## Allgemeine Informationen

Die Anwendung wurde mit Java 16 erstellt, die zum Zeitpunkt der Bearbeitung die aktuellste Version war.
Um die volle Kontrolle über die Implementierung der Reinforcement Learning Algorithmen und Agenten zu haben wurden keine zusätzlichen Bibliotheken eingebunden.
Die einzige Ausnahme ist die Bibliothek Apache CommonsCSV, die zur Erstellong von CSV-Dateien genutzt wird.

Alle Klassen und Methoden sind ausführlich mit JavaDoc auf Englisch kommentiert.
Eine Beschreibung der Architektur und Implementierung ist in Kapitel 4 meiner Bachelorarbeit, die [hier](https://github.com/JonasBingel/ThesisHSMZ-RLTicTacToe/blob/main/Bingel_Jonas_Bachelorarbeit.pdf) als PDF eingesehen werden kann.

## Überblick der Architektur

Das UML-Diagramm stellt keinen
Anspruch an Vollständigkeit, sondern dient der Vermittlung eines grundlegenden Verständnisses
für die wichtigsten Bestandteile der Anwendung sowie deren Interaktion. Der GameManager
ist die zentrale Klasse, die alle anderen Klassen verbindet, um Training und Evaluation der
Agenten durchzuführen. Auf Basis, der in ExperimentParameters definierten Werte erstellt
der GameManager zwei Agenten und weist diesen eine gemeinsame Experience Instanz zu.
Die beiden Agenten werden mittels Self-play trainiert und anschließend in Evaluationsspielen
getestet. Zur Bewertung der Agenten verwendet der GameManager den Minimax-Algorithmus.
Währenddessen erstellt der GameManager Log-Dateien auf deren Basis die Auswertung erfolgt.
Einerseits CSV-Dateien, die zum Plotten der Konvergenz und Berechnen der Spielstärke
verwendet werden. Andererseits Dateien, die für Training und Evaluation
verwendete Experimentparameter und Spielergebnisse enthalten.

![image](https://user-images.githubusercontent.com/30416267/157092882-2b01d106-dee9-40fb-8a94-08e72ebbfd53.png)
