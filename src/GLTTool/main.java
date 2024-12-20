package GLTTool;




import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
	public static boolean toTex0;
	
	
	public static void main(String[] args) throws Exception {
		//UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel"); causes error?
		toTex0 = false;
		isRLT = false;

	
		File inputFile;
		String fname;
		
		//cli or gui+cli
		
		if(args.length ==0) {
			
			try {
			new filefind();
			} catch (IndexOutOfBoundsException e) {
				new filefind();
			}
	
			
			fname = filefind.name;
			
			inputFile  = filefind.selectedfile;

			
		//	inputFile = new File("C:\\Users\\mcser\\Documents\\games\\Dolphin\\sluggers\\toad_mario.glt");
		//	iFPath = new File("C:\\Users\\mcser\\Documents\\games\\Dolphin\\sluggers").getAbsoluteFile();
			iFPath = filefind.selecteddir;
			
			read = new RandomAccessFile(inputFile, "rw");

			
			Scanner s = new Scanner(System.in);
			isOld = false;
			p("import (1) or export (2)");
			
			switch(s.nextLine()) {
			
			case "2":
				decode(inputFile,fname);
				s.close();
				break;
			case "1":
				encode(inputFile, fname);
				s.close();
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
			
			
			
			if(args.length == 3 && args[2].equals("tex0"))
				 toTex0 = true;
				
				
			
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
			//toTex0=true;
			
			if(toTex0)
				p("Decode to TEX0 enabled");

			
			read.seek(0x0);
			
			//if(read.readInt() != 0x50544C47) {
				
			//	p("Not a valid .glt file. exiting.");
			//	System.exit(-1);
				
			//}
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
				//pint(read.getFilePointer());
				

		/*		
				byte encType = read.readByte();
				String etos = encToS(encType);
				
				p("tex "+i+" is a "+etos+"-encoded image");
*/
				//get width and height of image i
				
				int w = getDims(pos+0x9)[0];
				int h = getDims(pos+0x9)[1];
				
				int lod = 0;
				
				if(toTex0) {
				
					lod = TexEnc.cntMips(h, w, getLim());
				};

				
				
			
				
				//read image block data into buffer
				
				if(isOld&&!isRLT) {
					
					read.seek(read.getFilePointer()+oldOff[i]);
					
					//while(read.getFilePointer() < ( (texSzTbl[i]-(0x20) + pos+0x10-oldOff[i] ))) 
																	
					//	buf[index++]=read.readByte();
					
					read.read(buf);
					
				//p(Long.toHexString(read.getFilePointer()));
					
					
				}else {
				//	pint((texSzTbl[i]-0x20) + pos);
					read.seek(read.getFilePointer()+0xE);
					
					read.read(buf);
				//	buf, texSzTbl[i] + pos, index
					//while(read.getFilePointer() < (  ) ) 
						
						
						
					
						
					}
				

		//decode image by type			    
			
				pos += texSzTbl[i];
				
				
				//	pint(lod);
				
				if(toTex0) 
					tex0Gen(w,h,lod,texSzTbl[i],encTypes[i],internalId[i],iFPath.getAbsolutePath(),fname,buf);
				
						else {

				
			//	pint(encTypes[i]);
							
				TexDec.passBuff(buf);
				
				
				switch(encTypes[i]) {
				
				case 0x6:
					TexDec.decCMPR(w,h, iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
				
				case 0x5:
					TexDec.decRGB5A3(w,h, iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
					
				case 0x8:
					TexDec.decRGBA8(w,h, iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
				
				default:
					p("\""+encTypes[i]+"\" is not a known encoding format. defaulting to CMPR.");
					TexDec.decCMPR( w,h, iFPath.getAbsolutePath(),fname,internalId[i]);
					break;
				
				}
				
			

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
				
			
			ArrayList<String> reformatInternalIDs = null;

			
			pngdir = new File(dir.getParent()+"\\"+fname);
			

			
			enctxt = new File(pngdir.getAbsolutePath()+"\\"+fname+"_enc.txt");

			 //gets texture count, offsetTable, and textureSizeTable

			getHeader(0);

			//gets encoding types 
			
			encTypes = getEncs();
			
			
			String ext = "glt";
			
			if(isRLT)
				ext = "rlt";
			
			File xlt = new File(pngdir.getAbsolutePath()+"\\"+fname+"."+ext);
			
			File pngs[] = new File[texCnt];
			
			try {
			
		//	p("texCnt is "+texCnt);	
				
				//get .pngs
		 pngs = pngdir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			    	for(String fn : internalId) {
						boolean f = false;
			    		if(name.equals((fn)+".png")) {
							//System.out.println(fn);
							

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
			
			xlt.delete();
			write = new RandomAccessFile(xlt, "rw");
			
			//copy header
			write.seek(0x0);
			read.seek(0x0);
			

			
	
			

			
			//copy std header
			if(!isOld)
				
				while(write.getFilePointer() < 0x20) //in game
					write.writeInt(read.readInt());
				
			else {
				
				while(write.getFilePointer() < 0x10)  //header
					write.writeInt(read.readInt());
				
				if(!isRLT) 
					while(write.getFilePointer() < 0x20)  //padding
						write.writeInt(0x0);
			
			
			}

		
		////////Important logic for rearranging hashes and encoding bytes based on read order of file filter	

			int[] indexes = new int[internalId.length];
			int index_;

			for(int i =0; i < internalId.length; ++i){
				index_=0;
				int idx = pngs[i].getName().lastIndexOf('.');
				for(String s : internalId){
				 if (pngs[i].getName().substring(0,idx).equals(s))
				 {indexes[i] = index_;
					break;}
				index_++;
			}
		}

		String[] id_mirror = new String[internalId.length];
		byte[] enc_mirror = new byte[encTypes.length];

		int g = 0;
		for(int i :indexes){
			id_mirror[g] = internalId[i];
			enc_mirror[g++] = encTypes[i];
		}
		internalId = id_mirror;
		encTypes = enc_mirror;
			

		
			for(int i :indexes) {

				
				
					offTbl[i] = 0x10;
				
				index = i;
				
				
				

				byte[] img; //buffer to recieve encoded image
				
				//encode image
				
				int lim = getLim();
						
				img = TexEnc.getTex(pngs[i], encTypes[i],lim);
				
				
				
				for(int sz: texSzTbl)
					offTbl[i] +=sz;
				
				texSzTbl[i] = (img.length)+0x20;

				
				
				
				
				
				
				int pos1 = 0x10;
				if(!isRLT)
					pos1+=0x10;
				
				pos = offTbl[i]+(texCnt*0x10)+pos1;
				

				

				
		
				p("writing tex "+i+" ("+internalId[i]+") at 0x"+Integer.toHexString(pos)+" as an "+encToS(encTypes[i])+"-encoded image of size "+Integer.toHexString(texSzTbl[i])+" ("+texSzTbl[i]+" bytes)");			
				
				
				write.seek(pos);
				

				
				
				
				//texture entry header generation
			
				write.writeInt(TexEnc.lod); //lod
			
			if(encTypes[i]==0x6)
				write.writeInt(0x0002);
			else if(encTypes[i]==0x5)
				write.writeInt(0x0001);
				write.writeInt(0x05000500 | ((encTypes[i] <<16)) | ((encTypes[i]==0x5) ? 0x3 : 0x0)); //encoding type byte + unknowns
			

				write.writeShort(0x00); //2 byte offset
				write.writeInt(TexEnc.wh);				//dimensions
				
				
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
			if(read.readInt() !=0) {
				isRLT=true;
				pint(read.getFilePointer());
			}
			
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
			if(isRLT)
				enctype.write("rlt");
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
		
		if(encGet.next().equals("rlt")) //encode to .rlt
			isRLT=true;
		
		if(isRLT)
			p("encoding to RLT");
		
		while(!(encGet.next().charAt(0) == '#'));
		
		for(int i = 0 ; i < texCnt; ++i) {
			
			
			
			String hash = encGet.next();
			//int idx = hash.indexOf(':');
			//hash = hash.substring(0,idx);
			encs[i] = Byte.parseByte(encGet.next());
			//internalId[i] = hash;
	
				
			}	
		
		return encs;
		
		
	}
	
	public static void writeHeader() throws NumberFormatException, IOException {
		
		int pos = 0x10;
		if(!isRLT)
			pos+=0x10;
		write.seek(pos);
		
		for(int i = 0 ; i < texCnt; ++i) {

			String encodingType = encToS(encTypes[i]);
			encodingType = encodingType.length() > 4 ? encodingType.substring(0, 4) : encodingType;

			while(encodingType.length()< 4)
				encodingType+='\0';
			
			write.writeInt((int)Long.parseLong(internalId[i],16)); //hash
			write.writeInt(offTbl[i]); //offset
			write.writeInt(texSzTbl[i]); //size
			write.writeBytes(encodingType); // encoding type
			//write.seek(write.getFilePointer()+0x4);
		}
		
	}
	
	public static void tex0Gen(int w, int h, int lod, int size, int enct, String hash, String path, String name, byte[] bufr) throws IOException {
		
		File o = new File(path+"\\"+name+"\\"+hash+".tex0");
		o.delete();
		RandomAccessFile tex0 = new RandomAccessFile(o,"rw");
		
		//tex0.seek(0x0);
		
		int[] header = {0x54455830, size+0x40, lod, 0x0, 
				
						0x40, size+0x4+0x40,0x0, (w<<16)|h&0xFFFF,
						
						gltTex0(enct),lod,0x0,0x40<<24,
							
						0x0,0x0,0x0,0x0};
		
		for(int i: header)
			tex0.writeInt(i);
		
		tex0.write(bufr);
		tex0.writeInt(0x0);
		tex0.writeBytes(hash);
		
		tex0.close();
		
		
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
	
	public static int getLim() {
		int lim = 16;
			
			if(isRLT)
				lim = 4;
			
			return lim;
	}
	
	public static int gltTex0(int enct) {
		
		switch(enct) {
	
	
	case 0x2:
		return 0x0;

		
	case 0x3:
		return 0x1;

		
	case 0x4:
		return 0x2;
	
		
	case 0x5:
		return 0x5;
	
	
	case 0x6:
		return 0xe;
		
		
	case 0x7:
		return 0x4;
		
		
	case 0x8:
		return 0x6;
		}
		return -1;
	}
	
	/*            { 0x8, Decode_Gamecube.TextureFormats.RGBA32 },
{ 0x7, Decode_Gamecube.TextureFormats.RGB565 },
{ 0x6, Decode_Gamecube.TextureFormats.CMPR },
{ 0x5, Decode_Gamecube.TextureFormats.RGB5A3 },
{ 0x4, Decode_Gamecube.TextureFormats.IA4 },
{ 0x3, Decode_Gamecube.TextureFormats.I8 },
{ 0x2, Decode_Gamecube.TextureFormats.I4 },*/
	
	
	
	
	
	public static void p(String txt) {
		
		System.out.println(txt);
	}
	
public static void pint(int txt) {
		
		System.out.println(Integer.toHexString(txt));
	}

private static void pint(long txt) {
	System.out.println(Long.toHexString(txt));
	
}
	
	

}