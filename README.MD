
# README

2nd place winner for the fall 2026 BYUI Buildathon. 

This is a simple photo editor that handles exposure adjustments, RGB level changes, and a simple crop.

AI was used to help handle null safety, JavaFX setup, and research with OpenCV and to clean up my code.
(ChatGPT & Gemini).

## Video:
[![Photo Editor Walkthrough](https://img.youtube.com/vi/VPX40LNpuQg/0.jpg)](https://youtu.be/VPX40LNpuQg)





## Build & Run

Prerequisites
<li>Maven</li>
<li>Java jdk 25 (Amazon Corretto was used for development)</li>
<li>An Internet Connection</li>


From the root of the project, run:

```bash
mvn install
mvn compile
mvn javafx:run
```
or (Prefered)
```bash
mvn install
mvn package
java -jar target/buildathon-1.0-SNAPSHOT.jar
```
All included Test Images are Copyright Joseph Robles 2026. All Rights reserved.
