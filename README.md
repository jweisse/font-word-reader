# Courier New Font Reader

An application that can read and translate image files of word documents containing Courier New size 36 font.

The program first divides the image into individual character segments, and then uses a trained neural net to 
predict the value of each segment based on its pixel values.

Instructions:
1. Create a new word document with Courier New size 36 font, and export the document to a pdf file
2. Upload this file, or any of the files located in the Sample_Files/ folder from the Upload Image button
3. Press Translate to Text in order to convert the image file to a typed out transcription
