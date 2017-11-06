package application;

import utility.Const;

public class CharactersGenerator {

	public static void main(String[] args) {

		/**
		 * Prints to standard out a series of lines to be copy and pasted into
		 * a new Word document. From there, export the word doc to pdf, and then train the neural
		 * net using the pdf as a source
		 */

		char[] ocrChars = Const.getOCRChars();
		int numRows = 100 * ocrChars.length;

		for(int i = 0; i < numRows; i++){
			for(int k = 0; k < Const.CHARS_PER_ROW; k++){
				int randomIndex = (int) (Math.random() * ocrChars.length);
				System.out.print(ocrChars[randomIndex]);
			}
			System.out.println();
		}


	}

}
