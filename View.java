
/* Implemented: Left ToolBar
 * 				Canvas
 * 				Drawing color and drawing size
 * 				Customizable Color Buttons on double click
 * 				File Menu: New, Open, Save, Exit. Can open/save binary, image formats. Text not supported
 * 			    Playback control: View set. Slider ticks updating correctly.
 *  */

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;


/* Interface IView */
interface IView {
	public void updateView();	
}

/* This is the View part of the MVC structure. The view handles drawing to the screen. This class
 * also contains the controller, implemented as anonymous inner classes.
 */
public class View extends JFrame implements IView{
		
	/************** Constants **************/
	
	public static final int INIT_WH = 700; // Initial frame width and height
	public static final int MIN_WH = 350; // Minimum frame width and height
	public static final int LEFT_TOOLBAR_WIDTH = 100;
	
	/********* Instance Variables *********/

	// Model associated with this view
	Model myModel;	
	
	// Window Sizing
	int myWidth;
	int myHeight;
	
	// Relating to Drawing Canvas
	private DrawCanvas myCanvas;
	
	// Relating to Toolbars
	private JToolBar leftToolBar; // To contain color palette and stroke sizing options
	ArrayList<JButton> myColorButtons; // Colors are chosen and created in the model. This specific view chooses to implement them as buttons
	ArrayList<JButton> myStrokeButtons; 

	// Relating to Playback Control
	PlayBackControl playCont;
	
	/************* Constructor ************/
	
	/* Pre:  m is not null.
	 * Post: Associates Model m with this view. Sets up the color toolbar, stroke toolbar,
	 * 		 playback toolbar and menu.
	 */
	public View(Model m) {
		
		myModel = m;
		this.createMenu(); // Need to do this before making frame visible and packing
		playCont = new PlayBackControl();
		this.add(BorderLayout.SOUTH, playCont);

		myCanvas = new DrawCanvas();
		this.add(BorderLayout.CENTER, myCanvas);
		
		this.createLeftToolBar(); // adds it to BorderLayout.WEST
		this.setDefaults();
		
	}
	
	/************* MVC Methods ************/
	
	/* Pre: 
	 * Post: Updates the screen according to the current state of the model
	 */
	public void updateView() {
		myCanvas.repaint();
		playCont.update();
	}
	
	/*********** Getters/Setters **********/
	
	
	/*********** Public Methods ***********/
	
	/********** Private Methods ***********/
	
	/* Pre:   None
	 * Post:  Creates a Menu Bar with two items: File and View
	 */
	private void createMenu() {	
		// Create the Bar
		JMenuBar myMenu = new JMenuBar();
		this.setJMenuBar(myMenu);
		// Create and Add File Menu
		JMenu fileMenu = this.createFileMenu();
		myMenu.add(fileMenu);

	}
	
	/* Pre:  None
	 * Post: Creates a dropdown file menu with four options: New, Open, Save, Exit
	 */
	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(this.createNewButton());
		fileMenu.add(this.createOpenButton());
		fileMenu.add(this.createSaveButton());
		fileMenu.add(this.createExitButton());
		return fileMenu;
	}
	
	
	/* Pre:  None
	 * Post: Returns a JMenuItem, New. Attaches an ActionListener to new. When clicked, user is prompted to save. If user doesn't hit cancel,
	 * 		 the canvas is cleared.
	 */
	private JMenuItem createNewButton() {
		JMenuItem newDoodle = new JMenuItem("New");
		newDoodle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = promptUserToSave();
				switch(returnVal) {
				case JOptionPane.CANCEL_OPTION:
					break; // Do nothing. Don't really need this
				case JOptionPane.NO_OPTION:
					//myCanvas.counter = 1;
					myModel.reset();
					break;
				case JOptionPane.YES_OPTION:
					createSaveDialog();
					//myCanvas.counter = 1;
					myModel.reset();
					
				}	
			}
		});
		return newDoodle;
	}
	
	
	/* Pre:
	 * Post: 
	 */
	private JMenuItem createOpenButton() {
		JMenuItem openDoodle = new JMenuItem("Open");
		openDoodle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	        	int returnVal = promptUserToSave();
	        	switch(returnVal) {
				case JOptionPane.CANCEL_OPTION:
					break; // Do nothing. Don't really need this
				case JOptionPane.NO_OPTION:
					openFile();
					break;
				case JOptionPane.YES_OPTION:
					createSaveDialog();
					openFile();
				}	
			}
		});
		return openDoodle;
	}
	
	
	
	/* Pre:  myModel.fileExtensions() is not null
	 * Post: Asks model to open file selected by user
	 */
	private void openFile() {
		JFileChooser openDialog = new JFileChooser();
		for(FileNameExtensionFilter f : myModel.fileExtensions()) {
			openDialog.setFileFilter(f);
		}
		int returnVal = openDialog.showOpenDialog(myCanvas);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	myModel.openFile(openDialog);
        } 
	}
	
	/* Pre:  myModel is not null. myModel.fileExtensions() is not null and is initialized.
	 * Post: Returns a JMenuItem, Save. Attaches an ActionListener to Save - when save is clicked, view asks model to save image.
	 */
	private JMenuItem createSaveButton() {
		JMenuItem saveDoodle = new JMenuItem("Save");
		saveDoodle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createSaveDialog();
			}
		});
		return saveDoodle;
	}
	
	/* Pre:  myModel is not null. 
	 * Post: Asks the user if he/she would like to save current canvas. Acts accordingly. Then exits the application.
	 */
	private JMenuItem createExitButton() {
		JMenuItem exitDoodle = new JMenuItem("Exit");
		exitDoodle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = promptUserToSave();
				switch(returnVal) {
				case JOptionPane.CANCEL_OPTION:
					break; // Do nothing. Don't really need this
				case JOptionPane.NO_OPTION:
					System.exit(0);
				case JOptionPane.YES_OPTION:
					createSaveDialog();
					System.exit(0);
				}	
			}
		});
		return exitDoodle;
	}
	
		
	/* Pre:   None
	 * Post:  Prompts the user to save the image
	 */
	private int promptUserToSave() {
		Object[] options = {"Yes", "No", "Cancel"};
		return JOptionPane.showOptionDialog(myCanvas, "Do you Want to Save?", "Save Dialog", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);	
	}
	
	/* Pre:	 myModel is not null. myModel.fileExtensions() is not null and contains a list of acceptable file extensions. 
	 * Post: Creates a save dialog allowing the user to save the image in one of the formats allowed by the model.
	 */
	private void createSaveDialog() {
		JFileChooser saveDialog = new JFileChooser();
		for(FileNameExtensionFilter f : myModel.fileExtensions()) {
			saveDialog.setFileFilter(f);
		}
		int returnVal = saveDialog.showSaveDialog(myCanvas);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	myModel.save(saveDialog);
        	Object[] options = { "OK"};
        	JOptionPane.showOptionDialog(null, "Your file was saved!", "Message", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        } 
	}

	
	/* Pre:  None;
	 * Post: Initializes the size of this view and sets all defaults. 
	 */
	private void setDefaults() {
		this.myWidth = INIT_WH;
		this.myHeight = INIT_WH;
		this.setPreferredSize(new Dimension(myWidth,myHeight));
		this.setMinimumSize(new Dimension(MIN_WH,MIN_WH));
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setName("Aseel's Paint Program - Version 11");	
		System.out.println("Version 11");
	}
	
	/* Pre:  none
	 * Post: Creates a dockable toolbar that allows user to select color and stroke size. Initializes
	 * 		 toolbarListener(controller) so that it modifies the model according to user input. 
	 */
	private void createLeftToolBar() {
		
		leftToolBar = new JToolBar("Drawing Options", SwingConstants.VERTICAL);
		leftToolBar.setPreferredSize(new Dimension(LEFT_TOOLBAR_WIDTH,200)); // The Width is being respected
		leftToolBar.setFloatable(true);
		
		this.createColorPalette(); 
		
		this.createCustomColorButton();
		
		this.createStrokeListener();
		this.createStrokeSizeSelectors();
		
		this.add(BorderLayout.WEST, leftToolBar);
		
	}
	
	
	/* Pre:  leftToolBar is initialized. 
	 * Post: Creates a color palette with colors according to model. For each color specified by model,
	 * 		 it creates a corresponding JButton, and stores the Jbutton in the arraylist myColorButtons.
	 * 	     Controller: Associates a MouseListener with the toolbar. When the user clicks a color, the
	 * 		 model updates its current color to that color. If the user double clicks a color, a
	 * 		 JColorChooser appears allowing user to select a custom color.
	 */
	private void createColorPalette() {
		
		leftToolBar.add(new JLabel("Color", SwingConstants.CENTER));
		myColorButtons = new ArrayList<JButton>();

		MouseAdapter colorListener = new MouseAdapter() { // Controller. 
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() instanceof JButton) { // Only do this if button was clicked. Otherwise class cast exception
					JButton b = (JButton)e.getSource();	
					if(e.getClickCount() == 1) { // One click: user simply choosing color			
						Color chosenColor = b.getBackground();
						myModel.setCurrentColor(chosenColor);
						b.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
						for( JButton aButton : myColorButtons) {
							if (!aButton.equals(b)) {
								aButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1)); // make sure other buttons don't have a border
							}
						}
					}
					if(e.getClickCount() == 2) { // user wants to customize color button
						Color cusColor = JColorChooser.showDialog(null, "Choose Your Color", myModel.getCurrentColor()); // If user just clicks ok without picking color, we don't want color to change
						myModel.setCurrentColor(cusColor);
						b.setBackground(cusColor);
					}
				}
			}
		};
		leftToolBar.addMouseListener(colorListener);
		for (Color c : myModel.Colors()) {
			JButton b = new JButton();
			b.setBackground(c);
			b.setMaximumSize(new Dimension((int) leftToolBar.getPreferredSize().getWidth(),30)); // What box layout respects
			
			b.addMouseListener(colorListener);
			
			if( c == myModel.getCurrentColor()) {
				b.setBorder(BorderFactory.createLineBorder(Color.GRAY,3));
			} else {
				b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			}
			leftToolBar.add(b);		
			myColorButtons.add(b);
		}
	}
	
	/* Pre:  leftToolBar is not null. strokeListener is initialized and not null
	 * Post: Creates buttons that allow user to pick stroke size
	 */
	private void createStrokeSizeSelectors() {
		ActionListener strokeListener = this.createStrokeListener();
		assert(strokeListener != null);
		leftToolBar.add(new JLabel("Stroke", SwingConstants.CENTER));
		myStrokeButtons = new ArrayList<JButton>();
		for (Integer size : myModel.StrokeSizes()) {
			JButton b = new JButton(String.valueOf(size));  // Keep track of stroke size
			b.setBackground(Color.WHITE);
			BufferedImage img = new BufferedImage(50, 30, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = img.createGraphics();
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(size.intValue())); // from model
			g2.drawLine(0,15,50,15);
			b.setIcon(new ImageIcon(img));
			b.setMaximumSize(new Dimension((int) leftToolBar.getPreferredSize().getWidth(),30)); // What box layout respects
			if ( Integer.parseInt(b.getText()) == myModel.getCurrentStrokeSize() ) {
				b.setBorder(BorderFactory.createLineBorder(Color.GRAY,3));
			} 
			b.addActionListener(strokeListener);
			myStrokeButtons.add(b);
			leftToolBar.add(b); 
		} 
	}
	
	
	/* Pre:  leftToolBar is initialized. myModel is initialized.
	 * Post: Creates a button that when clicked, allows the user to select a custom color
	 */
	private void createCustomColorButton() {
		JButton chooseColor = new JButton("Choose Color");
		chooseColor.setMaximumSize(new Dimension((int)leftToolBar.getMaximumSize().getWidth(),30));
		chooseColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color cusColor = JColorChooser.showDialog(null, "Choose Your Color", Color.WHITE);
				myModel.setCurrentColor(cusColor);
			}			
		});
		leftToolBar.add(chooseColor);
		
	}
	
	
	/* Pre:  myModel is initialized
	 * Post: Initializes strokeLIstener. When user selects a stroke size, model saves stroke size. 
	 * 		 Selected stroke size gets a border around it
	 */
	private ActionListener createStrokeListener() {
		ActionListener strokeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton b = (JButton)e.getSource();
				myModel.setCurrentStrokeSize(Integer.parseInt(b.getText()));
				b.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
				for(JButton aButton : myStrokeButtons) {
					if (!aButton.equals(b)) {
						aButton.setBorder(BorderFactory.createEmptyBorder()); // make sure other buttons don't have a border
					}
				}
			}
		};
		return strokeListener;
	}
	
	
	
	
	/* INNER CLASS: This class represents the Canvas on which the user draws. The user is essentially drawing on an image, and the image
	 * 				is drawn on the screen.
	 */
	class DrawCanvas extends JPanel implements Serializable {
		
		/********** Instance Variables *********/
		
		private Graphics2D g2; // For Drawing on Image
		private boolean dragged = false;
			
		/************* Constructor ************/
		
		/* Pre:  myModel has been initialized and is not null
		 * Post: Creates a Drawing Canvas on the screen and attaches mouse listeners to it 
		 */
		public DrawCanvas() {
			
			this.addMouseListener( new MouseAdapter() { // Controller
				// Mouse Pressed: Get old image from undo stack. Copy it into a new image. Get the new image's
				// graphic g2 and save it. Use it to draw on the image. 
				public void mousePressed(MouseEvent e) { 
					System.out.println("Mouse Pressed");
					if(myModel.inUndoOperation) {
						myModel.inUndoOperation = false;
						myModel.redo().removeAllElements();
					}
					if(!myModel.startedToDraw()) {
						myModel.setStartedToDraw(true);
					}
					Image oldImage = myModel.undo().peek(); 
					Image newImage = createImage(getWidth(), getHeight()); 
					g2 = (Graphics2D)newImage.getGraphics(); // g2 now draws on new image
					g2.drawImage(oldImage, 0, 0, null); // Copy old image onto new image
					myModel.addToStack(newImage);
					System.out.println("New Stack Size: " + myModel.undo().size());
					myModel.setStrokeStartCoor(e.getX(), e.getY());
				}
				public void mouseReleased(MouseEvent e) {
					if(dragged != true) {
						myModel.undo().pop(); // Ignore clicks on canvas
						System.out.println("Stack Size: " + myModel.undo().size());
					}
					dragged = false;
					//updateView();
				}
			});
			
			// Mouse Dragged: Draw stroke on screen based on model properties
			this.addMouseMotionListener(new MouseMotionAdapter() { // Controller
				public void mouseDragged(MouseEvent e) {
					dragged = true;
					myModel.setStrokeCurrX(e.getX());
					myModel.setStrokeCurrY(e.getY());
					if(g2 != null) { 
						g2.setColor(myModel.getCurrentColor());
						g2.setStroke(new BasicStroke(myModel.getCurrentStrokeSize()));
						g2.drawLine(myModel.getStrokeStartX(), myModel.getStrokeStartY(), myModel.getStrokeCurrX(), myModel.getStrokeCurrY()); 
						repaint();
						myModel.setStrokeStartCoor(myModel.getStrokeCurrX(), myModel.getStrokeCurrY());
					}
				}
			});
		}
		
		/* Pre:  None
		 * Post: Draw image on screen
		 */
		public void paintComponent(Graphics g) {
			if (!myModel.startedToDraw() && myModel.counter() == 1) { // Screen is blank
				myModel.incrementCounter(); // WE DON'T WANT IT TO KEEP ON ADDING BLANK IMAGES TO STACK
				Image newImage = this.createImage(this.getWidth(), this.getHeight());
				g2 = (Graphics2D)newImage.getGraphics();
				myModel.undo().push(newImage);				
				this.clear();
			}
			g.drawImage(myModel.undo().peek(), 0, 0, null); // Draw image on top of stack on screen	
		}
		
		
		/* Pre:  g2 is not null.
		 * Post: Fill image with white background (i.e, clear canvas)
		 */
		public void clear() {
			assert (g2 != null);
			g2.setPaint(Color.WHITE);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2.setPaint(Color.BLACK);
			//repaint();
		}
		
		
	}

	/* INNER CLASS: This class consists of the playback control
	 */
	class PlayBackControl extends JPanel{
		
		/********** Instance Variables *********/
		JButton play;
		JButton start;
		JButton end;
		JSlider slider;
		boolean progSetValue = false;
		
		/************* Constructor ************/
		
		public PlayBackControl() {
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.setPreferredSize(new Dimension(myWidth,60));
			this.addPlayBackControls();
			this.attachActionListenersToButtons();
			this.attachChangeListenerToSlider();
			this.update();
			
		}
		
		
		/************* Getters/Setters ***********/
		
		/************* Public Methods ************/
		
		public void update() {
			if(!myModel.inUndoOperation) {
				this.resetSlider();
				if( myModel.undo().size() <= 1) {
					this.disableButtons();
				} else {
					this.enableButtons();
					int numStrokes = myModel.undo().size()-1; 
					slider.setMinimum(0);
					slider.setMaximum(numStrokes);
					slider.setPaintTicks(true);
					slider.setMajorTickSpacing(1);
					progSetValue=true;
					slider.setValue(myModel.getKnobLocation());
					
				} 
			}
		}
		
		/************* Private Methods ***********/
		
		/* Pre:  slider is not null
		 * Post: resets the slider
		 */
		private void resetSlider() {
			progSetValue = true;
			slider.setMinimum(0);
			progSetValue = true;
			slider.setMaximum(myModel.undo().size() - 1);
			progSetValue = true;
			slider.setValue(myModel.getKnobLocation());
			
		}
		
		/* Disables Buttons */
		private void disableButtons() {
			play.setEnabled(false);
			start.setEnabled(false);
			end.setEnabled(false);
			slider.setEnabled(false);
		}
		
		/*Enables Buttons*/
		private void enableButtons() {
			play.setEnabled(true);
			start.setEnabled(true);
			end.setEnabled(true);
			slider.setEnabled(true);
		}
		
		/* Pre: None
		 * Post: Adds Play Button, Slider, Start, End
		 */
		private void addPlayBackControls() {
			play = new JButton("Play");
			start = new JButton("Start");
			end = new JButton("End");
			progSetValue = true;
			slider = new JSlider(0, myModel.undo().size(), 0);
			this.add(Box.createHorizontalStrut(20));
			this.add(play);
			this.add(Box.createHorizontalStrut(10));
			this.add(slider);
			this.add(Box.createHorizontalStrut(10));
			this.add(start);
			this.add(Box.createHorizontalStrut(5));
			this.add(end);
			this.add(Box.createHorizontalStrut(20));
		}
		
		
		/* Pre:  play, start and end have all been initialized
		 * Post: Attaches an actionListener to each of the three buttons
		 */
		private void attachActionListenersToButtons() {
			ActionListener myActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(e.getSource().equals(start)) {
						slider.setValue(0);
						myModel.setKnobLocation(0);
					} 
					if(e.getSource().equals(end)) {
						slider.setValue(slider.getMaximum());
						myModel.setKnobLocation(slider.getMaximum());
					}
					if(e.getSource().equals(play)) {
						//play();
					}
				}
			};
			start.addActionListener(myActionListener);
			end.addActionListener(myActionListener);
			play.addActionListener(myActionListener);
		}
		
		/* Attach change listener to slider. If slider knob value changed, let the model know.
		 */
		private void attachChangeListenerToSlider() {
			ChangeListener myChangeListener = new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					if (ce.getSource() instanceof JSlider) {
						JSlider sliderP = (JSlider)ce.getSource(); // We know the source will be slider
						if(!sliderP.getValueIsAdjusting() && !progSetValue) { // The user changed knob location
							myModel.inUndoOperation = true;
							myModel.resetRedo();
							myModel.setKnobLocation(sliderP.getValue());
							int knobLoc = sliderP.getValue();
							int size = myModel.undo().size();
							myModel.doUndo(size - knobLoc -1);
						} 
					} 
					progSetValue = false;
				}
			};
			slider.addChangeListener(myChangeListener);
			
		}
		
		private void play() {
			// NEED TO IMPLEMENT
		}
		
	}
	
}
