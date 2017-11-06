# Courier New Font Reader

An application that can read and translate image files of word documents containing Courier New size 36 font.

The program first divides the image into individual character segments, and then uses a trained neural net to 
predict the value of each segment based on its pixel values. This project was created to serve as an introduction 
to neural nets and image processing with OpenCV. Courier New was chosen because it is a monospaced or fixed-width font.
A monospaced font allows each image to be divided into individual characters based on pre-calculated character width constants.

Instructions:
1. Create a new word document with Courier New size 36 font, and export the document to a pdf file
2. In the application, press Upload Image to upload this file, or any of the files located in the Sample_Files folder 
3. Press Translate to Text in order to convert the image file to a typed out transcription

*Note: the runnable jar provided only works with Windows 64 bit systems*
