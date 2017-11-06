package imageMat;

public class ImageAndText {
	public ImageMat image;
	public String text;
	
	public ImageAndText(ImageMat image, String text){
		this.image = image;
		this.text = text;
	}
	
	public String toFileName(){
		String fileName = "";
		for(int i = 0; i < text.length(); i++){
			if(!charIsInvalid(text.charAt(i))){
				fileName += text.charAt(i);
			}
		}
		if(fileName.length() == 0){
			return "punctuation";
		}
		return fileName;
	}

	private boolean charIsInvalid(char character) {
		char[] invalidChars = "\\/:*?<>|\"".toCharArray();
		for(char invalidChar : invalidChars){
			if(invalidChar == character){
				return true;
			}
		}
		return false;
	}
}
