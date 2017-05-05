
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/* This is the Model part of the MVC architecture. The model stores data that is retrieved according to 
  * commands from the controller and displayed in the view.
  */
public class Model {
	
	public static final int MAX_NUM_COLORS = 8; // Maximum number of colors in palette


	/********* Instance Variables *********/
	
	private IView myView; // The View Associated with this Model 
	
	/* Relating to Colors */
	private ArrayList<Color> myColors; // Contains basic 6 colors. Allows the addition of two more custom colors.
	private Color currentColor = Color.BLACK; // Default
	
	/* Relating to Stroke Size */
	private ArrayList<Integer> strokeSizes; // Allowing you to add more sizes later
	public static final int THIN_STROKE = 1;
	public static final int MED_STROKE = 3;
	public static final int THICK_STROKE = 5;
	private int currentStrokeSize = MED_STROKE; // Default
	
	/* Relating to Drawing on Canvas */
	private int strokeCurrX = 0;
	private int strokeCurrY = 0;
	private int strokeStartX =0;
	private int strokeStartY =0;
	private boolean startedToDraw = false;
	private int counter = 1;
	
	/* To Implement Undo */
	private Stack<Image> undoStack;
	private Stack<Image> redoStack;
	
	/* Relating to Saving */
	private ArrayList<FileNameExtensionFilter> fileExtensions; // Allowed file extensions
	
	/* Relating to Slider */
	private int knobLocation = 0; // Default
	private boolean hasUndoed = false;
	public boolean inUndoOperation = false;
	
	
	/************* Constructor ************/
	public Model() {
		
		// Initialize Colors
		this.initializeColors();
		this.initializeStrokeSizes();
		undoStack = new Stack<Image>();
		redoStack = new Stack<Image>();
		this.initializeFileExtensions();
	}
		
	/************* MVC Methods ************/
	
	/* Pre: aView is not null
	 * Post: Associates aView with this Model
	 */
	public void setView(IView aView) {
		myView = aView;
		this.updateView();
	}
	
	/* Pre:  None
	 * Post: Updates myView. This method is called when changes are made to this model.
	 */
	public void updateView() {
		if(myView != null) {
			myView.updateView();
		}
	}
	
	/*********** Getters/Setters **********/
	
	public ArrayList<Color> Colors() 		{ return myColors; 	  }
	public ArrayList<Integer> StrokeSizes() { return strokeSizes; }
	public Stack<Image> undo()				{ return undoStack;	  }
	public Stack<Image> redo()				{ return redoStack;	  }

	public int getStrokeCurrX()   		{ return strokeCurrX;   }
	public int getStrokeCurrY()   		{ return strokeCurrY;   }
	public int getStrokeStartX() 		{ return strokeStartX;  }
	public int getStrokeStartY() 		{ return  strokeStartY; }	
	public void setStrokeCurrX(int x)   { strokeCurrX = x;  	}
	public void setStrokeCurrY(int y)   { strokeCurrY = y;  	}	
	
	public void setStrokeStartCoor(int x, int y) {
		strokeStartX = x;
		strokeStartY = y;
		this.updateView();
	}
	
	public int counter() {	return counter; }
	
	public boolean startedToDraw()	{ return startedToDraw; }
	
	public void setStartedToDraw(Boolean bool) { 
		startedToDraw = bool; 
		this.updateView();
	}
	
	public Color getCurrentColor() 		 { return currentColor; }
	public void setCurrentColor(Color c) { currentColor = c; 	}
	
	public void setCurrentStrokeSize(int size) { currentStrokeSize = size; }
	public int getCurrentStrokeSize()		   { return currentStrokeSize; }
	
	public ArrayList<FileNameExtensionFilter> fileExtensions() {  return fileExtensions; }
	
	public int getKnobLocation() 		 {	return this.knobLocation; }
	public void setKnobLocation(int loc) {	
		this.knobLocation = loc;  
		try {
			this.updateView();
		} catch(StackOverflowError e) {
			// do nothing
		}
	}
	
	public boolean hasUndoed() { return hasUndoed(); }
	public void setHasUndoed(boolean b) { hasUndoed = b; }
	
	/*********** Public Methods ***********/

	public void doUndo(int numToUndo) {
		this.hasUndoed = true;
		System.out.println("--In doUndo--");
		for(int i=0; i< numToUndo; i++) {
			Image m = (Image)this.undo().pop();
			this.redo().push(m);
			System.out.println("Current Size of Undo Stack: " + this.undo().size());
			System.out.println("Current Size of Redo Stack: " + this.redo().size());
		}
		this.updateView();
	}
	
	
	public void resetRedo() {
		System.out.println("in reset redo");
		for(int i=0; i<this.redo().size(); i++) {
			Image m = (Image)this.redo().pop();
			this.undo().push(m);
		}
	}
	
	
	/* Pre:  None
	 * Post: Adds img to undo stack
	 */
	public void addToStack(Image img) {
		this.undo().push(img);
		this.updateView();
	}
	
	/* Pre:  undo stack is initialized
	 * Post: resets this model - sets everything to default values
	 */
	public void reset() {
		this.undo().removeAllElements();
		this.redo().removeAllElements();
		this.setStartedToDraw(false);
		this.setCurrentColor(Color.BLACK); // Default
		this.setCurrentStrokeSize(MED_STROKE); // Default
		this.counter = 1; 
		this.knobLocation = 0;
		this.hasUndoed = false;
		this.inUndoOperation = false;
		this.updateView();
	}
	
	/* Pre:   saveDialog is not null
	 * Post:  Saves the image in one of the following formats: Binary, JPG, PNG, GIF, BMP
	 */
	public void save(JFileChooser saveDialog) {
    	File aFile = saveDialog.getSelectedFile();
		if ( saveDialog.getFileFilter() instanceof FileNameExtensionFilter) { // If user doesn't select all files
			FileNameExtensionFilter myFilter = (FileNameExtensionFilter) saveDialog.getFileFilter();		
	        if( myFilter.getDescription() == "binary") { // save in binary format
	        	if (this.getFileExtension(aFile) != "") { // If user chooses binary, then its binary. Remove extension
	        		int index = aFile.getPath().indexOf(".");
	        		String newName = aFile.getPath().substring(0, index);
	        		aFile = new File(newName);
	        	}
	        	this.saveInBinaryFormat(aFile);
	        } else if(myFilter.getDescription() == "txt") { // Save in text format
	        	aFile = new File(saveDialog.getSelectedFile() + "." + saveDialog.getFileFilter().getDescription());
	        	this.saveInTextFormat(aFile);
	        } else { // Save in JPG, GIF, BMP, PNG Format
	        	aFile = new File(saveDialog.getSelectedFile() + "." + saveDialog.getFileFilter().getDescription());
	           this.saveInImageFormat(aFile, saveDialog.getFileFilter().getDescription());
	        }
		} else { // If user selects all files
			assert(saveDialog.getFileFilter().getDescription().equals("All Files"));
			String ext = getFileExtension(aFile);
			if (ext == "") {
				this.saveInBinaryFormat(aFile);
			} else if( ext.equals("jpg") | ext.equals("png") | ext.equals("gif") | ext.equals("bmp") ) {
				this.saveInImageFormat(aFile, ext);
			} else if ( ext.equals("txt") ) {
				this.saveInTextFormat(aFile);
			}
		}
	}
	
	/* Pre:	 openDialog is not null
	 * Post: Opens the file selected by user
	 */
	public void openFile(JFileChooser openDialog) {
		File aFile = openDialog.getSelectedFile();
		String ext = getFileExtension(aFile);
		boolean isImage = (ext.equals("jpg") | ext.equals("png") | ext.equals("gif") | ext.equals("bmp") );
		if(isImage) { 
			this.openImageFile(aFile);
		} else if (ext == "") { // dont forget to reset
			this.openBinaryFile(aFile);
		}
	    this.updateView();
	}
	
	public void incrementCounter() {
		counter++;
	}
	
	/********** Private Methods ***********/
	
	/* Pre:  none
	 * Post: Creates an array with 6 basic colors and two colors to be chosen by user.
	 */
	private void initializeColors() {
		myColors = new ArrayList<Color>();
		myColors.add(Color.BLACK);
		myColors.add(Color.MAGENTA);
		myColors.add(Color.BLUE);
		myColors.add(Color.GREEN);
		myColors.add(Color.RED);
		myColors.add(Color.YELLOW);
		myColors.add(Color.ORANGE);
	}
	
	/* Pre: none
	 * Post: Creates an arraylist with three stroke sizes
	 */
	private void initializeStrokeSizes() {
		strokeSizes = new ArrayList<Integer>();
		strokeSizes.add(THIN_STROKE);
		strokeSizes.add(MED_STROKE);
		strokeSizes.add(THICK_STROKE);
	}
	
	/* Pre:  none
	 * Post: initializes array fileExtensions - An array of allowed file extensions
	 */
	private void initializeFileExtensions() {
		fileExtensions = new ArrayList<FileNameExtensionFilter>(); 
		fileExtensions.add(new FileNameExtensionFilter("binary", " "));
		fileExtensions.add(new FileNameExtensionFilter("txt", "txt"));
		fileExtensions.add(new FileNameExtensionFilter("jpg", "jpg"));
		fileExtensions.add(new FileNameExtensionFilter("png", "png"));
		fileExtensions.add(new FileNameExtensionFilter("gif", "gif"));
		fileExtensions.add(new FileNameExtensionFilter("bmp", "bmp"));
	}

	/* Pre:  aFile is not null
	 * Post: saves the canvas in binary format
	 */
	private void saveInBinaryFormat(File aFile) {
		try {
			FileOutputStream fos = new FileOutputStream(aFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			ImageIcon img = new ImageIcon((Image)this.undo().peek());
			oos.writeObject(img);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e1) { 
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e2) { 
			System.out.println(e2.getMessage());
			e2.printStackTrace();
		}
	}
	
	/* Pre:  aFile is not null
	 * Post: saves the canvas in binary format
	 */
	private void saveInTextFormat(File aFile) {
		// NOT IMPLEMENTED
	}
	
	/* Pre:  aFile is not null
	 * Post: saves the canvas in JPG, BMP, PNG or GIF Format
	 */
	private void saveInImageFormat(File aFile, String extension) {
		BufferedImage bi = null;
		if (this.undo().peek() instanceof BufferedImage) {
			bi = (BufferedImage)this.undo().peek();
		} else {
			bi = new BufferedImage(this.undo().peek().getWidth(null), this.undo().peek().getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D aG = (Graphics2D)bi.getGraphics();
			aG.drawImage(this.undo().peek(), 0, 0, null);
		}
		assert(bi!=null);
         try {
         	FileOutputStream fos = new FileOutputStream(aFile);
			ImageIO.write(bi, extension, fos);
			fos.close();
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	/* Pre:  aFile is not null
	 * Post: Returns the extension of aFile
	 */
	private String getFileExtension(File aFile) {
		String myFileExt = "";
		int index = aFile.getPath().indexOf(".");
		if(index > 0 ) {
			myFileExt = aFile.getPath().substring(index + 1);
		}
		return myFileExt;
	}
	
	/* Pre:  aFile is not null
	 * Post: loads the image saved in aFile onto the canvas
	 */
	private void openImageFile(File aFile) {
		this.reset(); // this sets startedToDraw to False
		this.startedToDraw = true; // Otherwise, it'll paint blank screen on opened image
		BufferedImage imgToOpen = null;
		try {
		    imgToOpen = ImageIO.read(aFile);
		    this.undo().push(imgToOpen);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} 
	}
	
	/* Pre:  aFile is not null. aFile is a binary File
	 * Post: Deserializes object stored in binary file 
	 */
	private void openBinaryFile(File aFile) {
		this.reset(); // this sets startedToDraw to False
		this.startedToDraw = true; // Otherwise, it'll paint blank screen on opened image
		FileInputStream fileIn;
		ImageIcon myImage = null;
		try {
			fileIn = new FileInputStream(aFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			myImage = (ImageIcon)in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		assert(myImage != null);
        this.undo().push(myImage.getImage());
	}
	
}
