# Email Content Analysis for Detecting Spam Messages

A Java-based application that detects and analyzes spam emails using advanced Data Structures and Algorithms, complete with a web interface and detailed reporting.

## Overview

Email Spam Analysis processes email messages, identifies suspicious patterns and keywords, calculates a spam score, classifies messages, and generates detailed analysis reports.

The project combines multiple algorithmic techniques including string matching, dynamic programming, network flow, approximation algorithms, randomized algorithms, and parallel processing.

## Features

- Email data loading and preprocessing
- Spam keyword and pattern detection
- Multiple string matching algorithms with benchmark comparisons
- Spam score calculation
- Spam category detection
- Confidence score and risk-level analysis
- URL and suspicious link detection
- Network-flow based analysis (Edmonds-Karp)
- Greedy rule selection (Set Cover)
- Randomized hashing verification
- Multi-threaded parallel email processing
- Interactive Web Dashboard
- Automatic report generation

## Algorithms Used

### String Matching
- **KMP (Knuth-Morris-Pratt)**: Pattern matching with preprocessing prefix table
- **Rabin-Karp**: Rolling hash based substring matching
- **Z-Algorithm**: Linear-time exact substring search using Z-array
- **Aho-Corasick**: Multi-pattern matching automaton for high-throughput scanning

### Dynamic Programming
- **Bitmask Dynamic Programming**: Optimal spam feature combination evaluation

### Network Flow
- **Edmonds-Karp Algorithm**: Max-flow calculation on feature dependency networks

### Approximation
- **Greedy Set Cover Approximation**: Minimal set of spam rules covering detected patterns

### Randomized & Parallel Algorithms
- **Randomized Hashing**: Fast polynomial hashing with random salt
- **Parallel Email Processing**: Multi-threaded batch email processing using thread pools

## Project Structure

```text
EmailContentSpamAnalysis/
│
├── src/
│   ├── Main.java
│   ├── AlgorithmComparison.java
│   ├── Email.java
│   ├── EmailReader.java
│   ├── TextPreprocessor.java
│   ├── KMP.java
│   ├── RabinKarp.java
│   ├── ZAlgorithm.java
│   ├── AhoCorasick.java
│   ├── BitmaskDP.java
│   ├── EdmondsKarp.java
│   ├── SetCoverApproximation.java
│   ├── RandomizedHash.java
│   ├── ParallelEmailProcessor.java
│   ├── SpamAnalyzer.java
│   ├── SpamCategory.java
│   └── ReportGenerator.java
│
├── web/
│   ├── WebServer.java
│   ├── index.html
│   ├── app.js
│   └── style.css
│
├── data/
│   ├── emails.txt
│   ├── spam_keywords.txt
│   ├── spam_analysis_report.txt
│   └── algorithm_comparison_report.txt
│
└── bin/
    └── (compiled .class files)
```

## How to Run

### Command Line
Compile all Java source files:
```powershell
javac -d bin src/*.java web/*.java
```

Run the console application:
```powershell
java -cp bin Main
```

Run the algorithm comparison benchmark:
```powershell
java -cp bin AlgorithmComparison
```

### Web Interface
Start the local web server:
```powershell
java -cp bin WebServer
```
Then open your browser at `http://localhost:8080`.
