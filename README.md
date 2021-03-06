# Vision system to recognize and identify fuel prices (renewed)

The project was created as my master thesis in the field of Information and Communication Technology. Abstract is available in root of repository. If you are interested in full text feel free to ping me directly at jedrzej.lukasiuk@gmail.com.

Bascic goal of project is to provide vision system which is able recognize and identify fuel prices based on movies obtained from camera mounted inside vehicle. For this purpose I used bunch of technologies:

- Java SE 8 (with JavaFX and JFoniex http://www.jfoenix.com/) for building desktop application,
- OpenCV as library for image processing https://opencv.org/ ,
- FFmpeg as a tool for movie manipulation https://www.ffmpeg.org/ ,
- Tess4j as OCR engine https://mvnrepository.com/artifact/net.sourceforge.tess4j/tess4j/3.2.1 .

For movie recording I used Eken camera https://www.eken.com/ .

## Algorithm and requirements

Algorithm of identifying fuel prices in the first step is based on template mathching technique which try to match known temlpate to given images. Consequently you should possess at least 1 template and 1 movie where template exists. FFmpeg is used for extracting and dowsampling frames from movie. In the next steps of algorithm, a few morphorogical opeartions are implemented to find Regions of Interest. Ready regions we can pose to OCR engine (Tess4j). User have possibility to tune the parameters of algorithm.

Example materials are available in samples folder (template and movie).

## Building app
To build executable jar you have to have maven installed (https://maven.apache.org/download.cgi). Next step is to go into root directory and then compile and assebmbly the app. Please type on the root of the repository:
```
mvn clean compile assembly:single
```
After a while target folder should be created, and artifact should be placed iniside this folder. Please be aware that artifact is around 130 MB since it contains all dependencies neccessary to run the app (ffmpeg, tessdata) which are unpacked on the user computer during runtime.

## Starting app

To start the app please go to the target folder ```cd target``` and then run jar through the command line by typing:

```sh
java -jar visionsystem-1.0.0-jar-with-dependencies.jar
```

In this case you will have access to logs generated by the application. You can also start app by double-clicking on jar file but you will not see logs .

## Basic usage

How to use the app:
- click the hamburger icon placed in the right bottom to open menu
- in the top part of menu you can see 5 icons. Their functionality is as follows (from the left):
 -- guy: info about me :)
 -- camera: windows for camera calibartion
 -- picture: window for loading templates
 -- processor: tunning the algorithm
 -- folder: loading movie
- click the picture icon and load template located in sample folder (or your own)
- click folder icon to load movie (take it from samples or your own). Process of extracitng frames will start automatically
- in the middle of the menu you can see already loaded resources as well processing options. In auto mode algorithm will process frames one by one (hit play button on the bottom of the app to start). In manual mode you should scroll the frames and hit check button to process current frame. Checkbox 'use calibrated frames' should be checked before loading movie to automatically recitify frames based on camera intrinsic/extrinsic parameters. 'Filter output' checkbox should be checked to post-process read price to format it correclty (OCR often read some additional gibberish).
- on the bottom of the menu you can see place for the results. Results will show automatically once they are detected 

## Addtional features

There are some extra features in the app which can help optimize your results:
- camera calibartion - process of acquiring intrinsic / extrinsic parameters of camera. You should use set of images depicting a chessboard (click on camera icon in the top of menu)
- algorithm settings - you can customize your parameters in the special window. Parameters will save automatically to application settings after reprocesing it in settings window (click on processor image)