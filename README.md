# Email Analysis for Detecting Spam Messages

## About the Project

**Email Analysis for Detecting Spam Messages** is a Java-based console application that detects spam and suspicious emails using different Data Structures and Algorithms (DSA).

The system analyzes the content of emails and looks for suspicious words or phrases such as free prizes, urgent requests, account verification, password requests, lottery messages, and bank details. Email data and spam keywords are stored separately in text files, making it easy to add new emails or keywords without changing the main program.

The main goal of this project is to show how different DSA concepts can be combined to solve a practical real-world problem like email spam detection.

---

## Objectives

- Detect spam and suspicious email messages.
- Preprocess email content before analysis.
- Identify suspicious words and phrases using string-matching algorithms.
- Calculate a feature score using Dynamic Programming.
- Demonstrate network flow and approximation algorithms.
- Apply randomized hashing and parallel processing.
- Classify emails as SPAM or NOT SPAM.
- Generate a final summary of the analyzed emails.

---

## Features

- Reads emails from a text file.
- Reads spam keywords from a separate file.
- Preprocesses email text.
- Uses multiple string-matching algorithms.
- Detects suspicious words and phrases.
- Calculates a spam-related score.
- Uses Edmonds-Karp for network-flow analysis.
- Uses Greedy Set Cover for rule selection.
- Demonstrates randomized hashing.
- Supports parallel email processing.
- Classifies emails as SPAM or NOT SPAM.
- Generates a final email analysis report.
- Allows new emails and keywords to be added easily.

---

## Algorithms Used

### String Matching

The project uses the following string-matching algorithms:

- KMP (Knuth-Morris-Pratt)
- Rabin-Karp
- Z-Algorithm
- Aho-Corasick

These algorithms are used to search for spam-related words and phrases in email content.

### Dynamic Programming

**Bitmask Dynamic Programming** is used to calculate the best possible feature score from the detected spam-related features.

### Network Flow

**Edmonds-Karp Algorithm** is used to demonstrate the maximum-flow concept in the analysis of detected spam features.

### Approximation Algorithm

**Greedy Set Cover** is used to select rules that cover the detected suspicious features.

### Randomized Algorithm

**Randomized Hashing** is included to demonstrate the use of randomization in text processing.

### Parallel Algorithm

**Parallel Email Processing** uses multiple threads to process emails concurrently.

---

## Project Structure

```text
EmailSpamAnalysis/
│
├── src/
│   ├── Main.java
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
├── data/
    ├── emails.txt
    └── spam_keywords.txt
