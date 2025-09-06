## 📌 Project Overview

This repository contains my work for **Problem 1: Learning Distributed/Parallel Big Data Computing with Hadoop/Spark MapReduce**. I wrote two custom MapReduce programs:

- **Word Count (WC)** — counts the total frequency of each token across all files.  
- **Document Frequency (DF)** — counts in how many *documents* each token appears (presence/absence per file, not total occurrences).

I ran both programs on **100, 1000, and 5000 files**, and evaluated reducer counts (default vs. `2` vs. `4`) to observe performance trade-offs on a single-node Windows setup.

---

## 🛠️ Requirements

- **Hadoop 2.7.7** (single-node, Windows)  
- **Java JDK 11**  
- HDFS & YARN running (`start-dfs.cmd`, `start-yarn.cmd`)  
- `%HADOOP_HOME%` set (e.g., `D:\hadoop`)  
- Inputs in HDFS: `/input100`, `/input1000`, `/input5000`

> Tip: If you re-run jobs, first remove the old HDFS output folder:  
> `hdfs dfs -rm -r /outputXYZ`

---

## 📂 Repository Structure

```
├── src/                         # Java sources
│   ├── WordCount.java           # Map/Reduce for token counts
│   └── DocFrequency.java        # Map/Reduce for document frequency
├── KamyarJobs.jar               # Built jar with both jobs
└── README.md
```

---

## 🚀 Build & Run

### 1) Compile & Package (Windows, Command Prompt)

```bash
REM Move to your project root
cd D:\Projects\Fall2025BigData\A01\KamyarMapReduce

REM Compile Java sources into the current directory (classes land beside src)
javac -classpath "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d . src\*.java

REM Package classes into a single jar (no Main-Class needed; Hadoop selects by class name)
jar cf KamyarJobs.jar WordCount*.class DocFrequency*.class
```

> If you changed package names, update `jar` paths accordingly.

### 2) Run – Word Count (WC)

```bash
hadoop jar KamyarJobs.jar WordCount /input100  /output100_wc
hadoop jar KamyarJobs.jar WordCount /input1000 /output1000_wc
hadoop jar KamyarJobs.jar WordCount /input5000 /output5000_wc
```

### 3) Run – Document Frequency (DF)

```bash
hadoop jar KamyarJobs.jar DocFrequency /input100  /output100_df
hadoop jar KamyarJobs.jar DocFrequency /input1000 /output1000_df
hadoop jar KamyarJobs.jar DocFrequency /input5000 /output5000_df
```

### 4) Reducer Variations (example on /input1000)

```bash
hadoop jar KamyarJobs.jar WordCount /input1000 /output1000_wc_r2 -D mapreduce.job.reduces=2
hadoop jar KamyarJobs.jar WordCount /input1000 /output1000_wc_r4 -D mapreduce.job.reduces=4
```

---

## ⬇️ Download Results (from HDFS to local)

```bash
REM Word Count outputs
hdfs dfs -get /output100_wc/part-r-00000  output100_wc.txt
hdfs dfs -get /output1000_wc/part-r-00000 output1000_wc.txt
hdfs dfs -get /output5000_wc/part-r-00000 output5000_wc.txt

REM If you ran reducer variations:
hdfs dfs -get /output1000_wc_r2/part-r-00000 output1000_wc_r2.txt
hdfs dfs -get /output1000_wc_r4/part-r-00000 output1000_wc_r4.txt

REM Document Frequency outputs
hdfs dfs -get /output100_df/part-r-00000  output100_df.txt
hdfs dfs -get /output1000_df/part-r-00000 output1000_df.txt
hdfs dfs -get /output5000_df/part-r-00000 output5000_df.txt
```

> If you configured more than one reducer, you’ll have multiple parts (`part-r-00000`, `part-r-00001`, …). Concatenate them locally:
>
> ```bash
> type part-r-0000* > merged_output.txt   REM (Windows)
> ```

---

## 🧠 Interpreting WC vs. DF Outputs

- **WC output:**  
  ```
  token<TAB>global_count_across_all_files
  ```
- **DF output:**  
  ```
  token<TAB>number_of_distinct_documents_containing_token
  ```

### Get “Top-30” by DF (most documents containing the word)

**PowerShell (Windows):**
```powershell
Get-Content output100_df.txt |
  Sort-Object { [int]($_ -split "`t")[-1] } -Descending |
  Select-Object -First 30 > top30_df_100.txt
```

**Git Bash / WSL:**
```bash
sort -k2,2nr output100_df.txt | head -30 > top30_df_100.txt
```
