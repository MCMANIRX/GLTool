package GLTTool;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.io.RandomAccessFile;

import java.util.Scanner;



//import javax.imageio.ImageIO;




public class main {

	public static RandomAccessFile read;
	public static RandomAccessFile write;
	public static RandomAccessFile last;
	static RandomAccessFile pngRead;
	public static File in;
	public static File temp;
	public static File out;
	public static File out_dir;
	public static File iFPath;
	public static File lastDir;
	public static File enctxt;
	public static int index;
	static File pngdir;
	

	
	
	
	public static int pos;
	public static int texCnt;
	public static int offConst;
	public static String[] internalId;
	public static int[] offTbl;
	public static int[] oldOff;
	public static int[] texSzTbl;
	public static byte[] encTypes;
	public static boolean isOld;
	public static boolean isRLT;
	
	
	public static void main(String[] args) throws Exception {
		//UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel"); causes error?
	
		File inputFile;
		
		//cli or gui+cli
		
		if(args.length ==0) {
			
			new filefind();
			
	
			
			
			
			inputFile  = filefind.selectedfile;
			iFPath = filefind.selecteddir;
			
			read = new RandomAccessFile(inputFile, "rw");
			
			Scanner s = new Scanner(System.in);
			isOld = false;
			p("import (1) or export (2)");
			
			switch(s.nextLine()) {
			
			case "2":
				decode(filefind.selectedfile,filefind.name);
				break;
			case "1":
				encode(filefind.selectedfile, filefind.name);
				break;
			default:
				System.exit(0);
			
			
			}
		}
		
		else  {
			
			inputFile  = new File(args[1]);
			
			read = new RandomAccessFile(inputFile, "rw");
			iFPath = new File(inputFile.getParent());
			System.out.println(iFPath);
			
			switch(args[0]) {
			
			case "enc":
				encode(inputFile,inputFile.getName().replaceFirst("[.][^.]+$", ""));		
				break;
				
			case "dec":
				decode(inputFile,inputFile.getName().replaceFirst("[.][^.]+$", ""));
				break;
			default:
				p("java gltool enc|dec input");
				System.exit(0);
			}
		}
			
		
		
		
	}
	
	
		public static void decode(File dir, String fname) throws IOException, InterruptedException {

			out_dir = new File(iFPath.getAbsolutePath()+"\\"+fname);
			enctxt = new File(out_dir+"\\"+fname+"_enc.txt");

			;
			
			read.seek(0x0);
			
			if(read.readInt() != 0x50544C47) {
				
				p("Not a valid .glt file. exiting.");
				System.exit(-1);
				
			}
			out_dir.mkdir();

				
			getHeader(1); 
			
		
			for(int i = 0; i <texCnt; ++i) {
				
				byte[] buf;
				
				//create buffer for encoded images
				
				
				if(!isOld) {
					buf = new byte[texSzTbl[i]];
					pos = offTbl[i]+(texCnt*0x10)+0x20; 
				}
				else {
					 buf = new byte[texSzTbl[i]+oldOff[i]];
					pos = offTbl[i]+(texCnt*0x10)+0x10;
				}
				
				//pint(pos);
				//pint(buf.length);
				

		/*		
				byte encType = read.readByte();
				String etos = encToS(encType);
				
				p("tex "+i+" is a "+etos+"-encoded image");
*/
				//get width and height of image i
				
				int w = getDims(pos+0x9)[0];
				int h = getDims(pos+0x9)[1];
				
				
				int index = 0;
				
				//read image block data into buffer
				
				if(isOld&&!isRLT) {
					
					read.seek(read.getFilePointer()+oldOff[i]);
					
					while(read.getFilePointer() < ( (texSzTbl[i]-(0x20) + pos+0x10-oldOff[i] ))) 
																	
						buf[index++]=read.readByte();
					
				//p(Long.toHexString(read.getFilePointer()));
					
					
				}else {
				//	pint((texSzTbl[i]-0x20) + pos);
					read.seek(read.getFilePointer()+0xE);


					
					while(read.getFilePointer() < ( (texSzTbl[i]) + pos ) ) {
						buf[index++]=read.readByte();
						
						
					}
						
					}
				

		//decode image by type			    
			
				pos += texSzTbl[i];
				
							
				TexDec deck = new TexDec(buf);
				
				pint(encTypes[i]);
				
				switch(encTypes[i]) {
				
				case 0x6:
					deck.decCMPR( buf , w,h, iFPath.getAbsolutePath(),iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
				
				case 0x5:
					deck.decRGB5A3( buf , w,h, iFPath.getAbsolutePath(),iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
				
				}

				
				//p("C:\\Program Files\\Wiimm\\SZS\\wimgt.exe DECODE "+temp.getAbsolutePath()+" --dest "+temp.getParent()+"\\"+fname+"\\"+internalId[i]+".png");
			//	Process pr = rt.exec("C:\\Program Files\\Wiimm\\SZS\\wimgt.exe DECODE "+temp.getAbsolutePath()+" --dest "+temp.getParent()+"\\"+fname+"\\"+internalId[i]+".png");
				p("wrote file "+i);
				
				
				
				
			}
						
			
				read.close();

				
				
				p("done.");
				System.exit(0);
		
			
			
			
			
	}
		
		public static void encode(File dir, String fname) throws IOException, InterruptedException {
				
			
		
			
			pngdir = new File(dir.getParent()+"\\"+fname);
			
			File glt = new File(pngdir.getAbsolutePath()+"\\"+fname+".glt");
			
			enctxt = new File(pngdir.getAbsolutePath()+"\\"+fname+"_enc.txt");

			 //gets texture count, offsetTable, and textureSizeTable

			getHeader(0);
			
			File pngs[] = new File[texCnt];
			
			try {
			
		//	p("texCnt is "+texCnt);	
				
				//get .pngs
			
		 pngs = pngdir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			    	for(String fn : internalId) {
			    		if(name.equals((fn)+".png")) {
			    			return name.endsWith(".png");
			    		
			    	}  }
			    	
			        return false;
			    }});
			
			/*for(File tex : pngs) {
				
				//System.out.println(tex.getAbsolutePath());
				
				Runtime rt = Runtime.getRuntime();
				Thread.sleep(100);
				Process pr = rt.exec("\"C:\\Program Files\\Wiimm\\SZS\\wimgt.exe\" ENCODE "+tex+" --dest "+tex.getParent()+"\\@"+tex.getName().replaceFirst("[.][^.]+$", "")+".cmpr --transform=CMPR");
				Thread.sleep(500); //delay
			}//end loop
*/

			
			} catch(NullPointerException e) {
				
				System.out.println("png folder for \""+fname+"\" does not exist!!!");
				System.exit(0);
			}
			
			
			glt.delete();
			write = new RandomAccessFile(glt, "rw");
			
			//copy header
			write.seek(0x0);
			read.seek(0x0);
			

			
	
			

			
			//copy std header
			if(!isOld)
				
				while(write.getFilePointer() < 0x20) //in game
					write.writeInt(read.readInt());
				
			else {
				
				while(write.getFilePointer() < 0x10)  //padding
					write.writeInt(read.readInt());
				while(write.getFilePointer() < 0x20)  //padding
					write.writeInt(0x0);
			
			
			}
			//gets encoding types 
			
			encTypes = getEncs();
			
			for(int i = 0; i < texCnt; ++i) {
				
				offTbl[i] = 0x10;
				
				index = i;
				
				
				

				byte[] img; //buffer to recieve encoded image
				
				//encode image
				
				img = encTest.getTex(pngs[i], encTypes[i]);
				
				
				
				for(int sz: texSzTbl)
					offTbl[i] +=sz;
				
				texSzTbl[i] = (img.length)+0x20;
				
				/*while(texSzTbl[i] % 16 !=0)
					texSzTbl[i]++;*/
				
				
				
				
				
				
				

				pos = offTbl[i]+(texCnt*0x10)+0x20;
				

				

				
		
				p("writing tex "+i+" ("+internalId[i]+") at 0x"+Integer.toHexString(pos)+" as an "+encToS(encTypes[i])+"-encoded image of size "+Integer.toHexString(texSzTbl[i])+" ("+texSzTbl[i]+" bytes)");			
				
				
				write.seek(pos);
				

				
				
				
				//texture entry header generation
			
				write.writeInt(encTest.lod); //lod
				
				write.writeInt(0x0002);
				write.writeInt(0x05000500 | ((encTypes[i] <<16))); //encoding type byte + unknowns
			

				write.writeShort(0x00); //2 byte offset
				write.writeInt(encTest.wh);				//dimensions
				
				
				while(write.getFilePointer() < pos+0x20) //padding for .glts in the final game
					write.writeByte(0x0);
				
				
				
				pos+=0x20;
				
				
				
				int ind = 0 ;
				
				//write texture data
				
				while(write.getFilePointer() < pos+texSzTbl[i]-0x20)	{
					//pint(texSzTbl[i]-0x20);
				//	p(Long.toHexString(write.getFilePointer()));
					write.writeByte(img[ind++]);
				}
					
				
			
			p("texture "+i+" written");
				
			}
			
			read.close();
			
			writeHeader();
			write.close();
			
				
				
			p("done.");
			System.exit(-1);
			
		}
		
		
	public static void getHeader(int mode) throws IOException {
		
			read.seek(0x4);
			texCnt = read.readInt();
			if(read.readInt() !=0)
				isRLT=true;
		
			
			
			
			if(mode == 0) {
				
				internalId = new String[texCnt];
				offTbl = new int[texCnt];
				texSzTbl = new int[texCnt];
				encTypes = new byte[texCnt];
				
				//check if beta
				
				while(read.getFilePointer() < 0x20)
					if(read.readInt() != 0x0) {
						isOld=true;
						p("old");
						pos = 0x10;
						break;
					}
					else pos = (0x20);
			
			//pos = 0x20;
			read.seek(pos);
			
			for(int i = 0 ; i < texCnt; ++i) {
				internalId[i] = Integer.toHexString(read.readInt());
				pos+=0x10;
				read.seek(pos);
				
				
			}
			return;
			
			
			}
			
			
			
			if(mode ==1) {
				
				FileWriter enctype = new FileWriter(enctxt);
				
				
				internalId = new String[texCnt];
				offTbl = new int[texCnt];
				oldOff = new int[texCnt];
				texSzTbl = new int[texCnt];
				encTypes = new byte[texCnt];
				
				read.seek(0x10);
				
				while(read.getFilePointer() < 0x20)
					if(read.readInt() != 0x0) {
						isOld=true;
						p("old");
						pos = 0x0;
						break;
					}
					else pos = (0x10);
				
				
			
			
			read.seek(pos);
			int encpos = 0;
			
			enctype.write(""
					+ "\n0x8 = RGBA32\n"
					+ "0x7 = RGB565\n"
					+ "0x6 = CMPR\n"
					+"0x5 = RGB5A3\n" 
					+"0x4 = IA4\n"
					+"0x3 = I8\n"
					+"0x2 = I4\n#\n");
			
				for(int i = 0; i < texCnt; ++i) {
					
								
					read.seek(pos+=0x10);
					internalId[i] = Integer.toHexString(read.readInt());
					//p(internalId[i]);
					offTbl[i] = read.readInt();
					texSzTbl[i] = read.readInt();
					
					if(isOld) {
					oldOff[i] = 0x10-read.readInt();
					if(oldOff[i] == 0x10)
						oldOff[i] = 0x0;
					}

					
					if(!isOld)
						encpos = offTbl[i]+(texCnt*0x10)+0x29; //should be 0x20
					else
						encpos = offTbl[i]+(texCnt*0x10)+0x19;
					
	
				read.seek(encpos);
				
				byte encType = read.readByte();
				encTypes[i] = encType;
				
				String etos = encToS(encType);

					
					enctype.write(internalId[i]+": "+encType+"\n");
					
					
					if(!isOld)
						p("tex "+i+" ("+internalId[i]+") is "+etos+"-encoded (0x"+encType+") and at 0x"+Integer.toHexString(offTbl[i]+(texCnt*0x10)+0x20)+" of size "+Integer.toHexString(texSzTbl[i])+" ("+texSzTbl[i]+" bytes)");			
					else 
						p("tex "+i+" ("+internalId[i]+") is "+etos+"-encoded (0x"+encType+") and at 0x"+Integer.toHexString(offTbl[i]+(texCnt*0x10)+0x10)+" of size "+Integer.toHexString(texSzTbl[i])+" ("+texSzTbl[i]+" bytes)");	
						
						
						

								
				}
				

				
				
				
				
				enctype.close();
				
				if(isOld) 
					pos = (int)read.getFilePointer()-0x2;
				
			}
			}
			
			
	/*		 if(mode == 2) {
				
				
				 offTbl[0] = 0x10;
				 
				for(int i = 0; i < texCnt; ++i) {
				 	
				 	pngRead = new RandomAccessFile(tex0s[i], "r");
					
					pngRead.seek(0x4);
					texSzTbl[i] = pngRead.readInt()-0x20;
						
					if(i>0) 
						offTbl[i] = texSzTbl[i-1]+offTbl[i-1];
					
					p("tex "+(i+1)+" ("+internalId[i]+") of size "+Integer.toHexString(texSzTbl[i])+" ("+texSzTbl[i]+" bytes)");		
					pngRead.close();
				} 
				
			 }*/
			 
				
	
	public static short[] getDims(int toff) throws IOException {
		
		 //go to width, height offset
		if(isOld&&!isRLT)
			read.seek(toff+0x3);
		else
			read.seek(toff+0x5);
		
			
			
		
			
					
		short[] wh = new short[2];
		
		wh[0] = read.readShort();
		wh[1] = read.readShort();
		

		
		
		return wh;
								
	}
	
	public static byte[] getEncs() throws FileNotFoundException {
		
		byte[] encs = new byte[texCnt];
		
		Scanner encGet = new Scanner(enctxt);
		
		while(!(encGet.next().charAt(0) == '#'));
		
		for(int i = 0 ; i < texCnt; ++i) {
			
			
			
			encGet.next();
			encs[i] = Byte.parseByte(encGet.next());
	
				
			}	
		
		return encs;
		
		
	}
	
	public static void writeHeader() throws NumberFormatException, IOException {
		
		write.seek(0x20);
		for(int i = 0 ; i < texCnt; ++i) {
			
			write.writeInt((int)Long.parseLong(internalId[i],16)); //hash
			write.writeInt(offTbl[i]); //offset
			write.writeInt(texSzTbl[i]); //size
			write.seek(write.getFilePointer()+0x4);
		}
		
	}
	
	public static String encToS(int e) {
		
		switch(e) {
		
		case 0x8:
			return "RGBA32";
		case 0x7:
			return "RGB565";
		case 0x6:
			return "CMPR";
		case 0x5:
			return "RGB5A3";
		case 0x4:
			return "IA4";
		case 0x3:
			return "CI8";
		case 0x2:
			return "CI4";

			
		}
			
			
		return null;
		
		/*0x8 = RGBA32
0x7 = RGB565 
0x6 = CMPR
0x5 = RGB5A3 
0x4 = IA4
0x3 = I8
0x2 = I4
*/
		
	}
	
	
	
	public static void p(String txt) {
		
		System.out.println(txt);
	}
	
public static void pint(int txt) {
		
		System.out.println(Integer.toHexString(txt));
	}
	
	

}
