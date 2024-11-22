package GLTTool;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.RandomAccess;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class filefind{
	public static String name;
	public static String path;	

	
	public JFileChooser fc = new JFileChooser();
	//JPanel pnl = new JPanel();
	public JFrame frame = new JFrame();
	public static File selectedfile;
	public static File selecteddir;
	public static File out; 
	public static RandomAccessFile auto_open;
	public static FileWriter auto_open_up;
	
	
	public filefind() throws IOException {
		
		frame.add(fc);
		fc.setDialogTitle("Select .glt files");
		FileNameExtensionFilter	ff = new FileNameExtensionFilter(".glt/rlt texture archives", "glt","rlt");
		fc.addChoosableFileFilter (ff);
		fc.setFileFilter(ff);
	
		

				//change this path if you want to use the gui+cli mode
			fc.setCurrentDirectory(new File("C:\\Users\\mcser\\Documents\\games\\Dolphin\\sluggers"));
	
		
		frame.setVisible(true);
	
		int result = fc.showOpenDialog(frame);
		 frame.setFocusable(true);
		 frame.setFocusableWindowState(true);
		 
		    
		    switch (result) {
		    case JFileChooser.APPROVE_OPTION:
		    	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		    	
		    	
		    	
		    	
		    	selectedfile = fc.getSelectedFile().getAbsoluteFile();
		    	selecteddir = new File(fc.getSelectedFile().getAbsoluteFile().getParent());
	
				 System.out.println(selectedfile);
				 System.out.println(selecteddir);
				 getFileName(selectedfile);
				 
				 frame.dispose();
	
			    	
		      break;
		    case JFileChooser.CANCEL_OPTION:
		      System.out.println("Bye!");
		      System.exit(0);
		      break;
		    case JFileChooser.ERROR_OPTION:
		      System.out.println("Error");
		      break;
		 
		  }
		    
	
		
		
		
		
       
		
}
	


	public String getFileName(File selectedfile) {
		
		// TODO Auto-generated method stub
		 name = selectedfile.getName().replaceFirst("[.][^.]+$", "");;
		System.out.println(name);
		
		
		
	return name;
	
	
	}


	public static void main(String[] args) {
		
		
		
	}


}