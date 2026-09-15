# Email Content Analysis for Detecting Spam Messages


## About

This project applies multiple algorithmic techniques to identify suspicious email content and generate a detailed spam analysis report. It demonstrates the practical application of string matching, dynamic programming, network flow, approximation, randomized, and parallel algorithms.

## Features

- Spam and non-spam classification
- Multiple string matching algorithms
- Spam category detection
- Confidence and risk-level analysis
- URL and suspicious URL detection
- Automatic analysis report generation
- Parallel email processing

## Algorithms Used

- KMP (Knuth-Morris-Pratt)
- Rabin-Karp with Rolling Hash
- Z-Algorithm
- Aho-Corasick
- Bitmask Dynamic Programming
- Edmonds-Karp Network Flow
- Greedy Set Cover Approximation
- Randomized Hashing
- Parallel Processing

## Project Structure

```text
EmailSpamAnalysis/
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
└── data/
    ├── emails.txt
    └── spam_keywords.txt
