# Email Content Analysis for Dectecting Spam Messages

A Java-based console application that detects and analyzes spam emails using advanced Data Structures and Algorithms.

## Overview

Email Spam Analysis processes email messages, identifies suspicious patterns and keywords, calculates a spam score, classifies messages, and generates a detailed analysis report.

The project combines multiple algorithmic techniques including string matching, dynamic programming, network flow, approximation algorithms, randomized algorithms, and parallel processing.

## Features

- Email data loading and preprocessing
- Spam keyword and pattern detection
- Multiple string matching algorithms
- Spam score calculation
- Spam category detection
- Confidence score and risk-level analysis
- URL detection
- Suspicious URL detection
- Network-flow based analysis
- Greedy rule selection
- Randomized hashing
- Parallel email processing
- Automatic report generation

## Algorithms Used

### String Matching
- **KMP (Knuth-Morris-Pratt)**
- **Rabin-Karp with Rolling Hash**
- **Z-Algorithm**
- **Aho-Corasick**

### Dynamic Programming
- **Bitmask Dynamic Programming**

### Network Flow
- **Edmonds-Karp Algorithm**

### Approximation
- **Greedy Set Cover Approximation**

### Randomized & Parallel Algorithms
- **Randomized Hashing**
- **Parallel Email Processing**

## How It Works

1. Email data and spam keywords are loaded from text files.
2. Email content is preprocessed for efficient analysis.
3. Multiple string matching algorithms search for suspicious patterns.
4. Detected patterns are assigned feature scores.
5. Bitmask Dynamic Programming evaluates the detected features.
6. Edmonds-Karp analyzes the feature network.
7. Greedy Set Cover selects relevant spam rules.
8. Additional features such as URLs, categories, confidence, and risk level are analyzed.
9. A final spam score is calculated.
10. The email is classified as **SPAM** or **NOT SPAM**.
11. The complete analysis is exported to a report file.

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
└── data/
    ├── emails.txt
    ├── spam_keywords.txt
    └── spam_analysis_report.txt
