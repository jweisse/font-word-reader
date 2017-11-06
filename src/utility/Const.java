package utility;

import org.opencv.core.CvType;

public class Const {

	public static final int IMG_SIZE_WIDTH = 22;

	public static final int IMG_SIZE_HEIGHT = 32;

	public static final double NEURAL_NET_FUNC_ALPHA = 1.0;

	public static final double NEURAL_NET_FUNC_BETA = 1.0;

	public static final int NUM_HIDDEN_LAYERS = 1;

	public static final int NUM_FEATURES = IMG_SIZE_WIDTH * IMG_SIZE_HEIGHT;

	public static final double HISTOGRAM_BACKGROUND_PIXEL_THRESHOLD = 230;

	public static final int CONSECUTIVE_COL_SPACE_THRESHOLD = 10;

	public static final double PART_OF_CHAR_THRESHOLD = 250;

	public static final int CONSECUTIVE_CHAR_SPACE_THRESHOLD = 4;

	public static final double CONTOUR_BACKGROUND_PIXEL_THRESHOLD = 200;

	public static final int MAX_EXPECTED_ACTUAL_CHAR_SIZE_DIFFERENCE = 2;

	public static final int CHAR_CONTOUR_VERTICAL_PIXEL_DISTANCE_THRESHOLD = 3;

	public static final int IMAGE_MAT_TYPE = CvType.CV_8UC3;

	public static final int RIGHT_MARGIN_OFFSET = 72;

	public static final int CHARS_PER_ROW = 21;

	public static final double CHAR_WIDTH = 21.6;

	public static final int NUM_PAGES = 300;

	public static final String BASE_DIR = "Neural_Net_Files/";

	public enum ColType {
		Empty, PartOfChar
	}

	public static char[] getOCRChars(){
		return ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!.,?/();:$-'\"#".
				toCharArray());

	}
}
