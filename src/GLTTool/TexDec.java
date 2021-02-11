package GLTTool;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;






public class TexDec {
	
	static int bi;
	static File o;
	static byte[] buf;
	
 	final static  short[] cc38 = // convert 3-bit color to 8-bit color
 {
     0x00,0x24,0x49,0x6d, 0x92,0xb6,0xdb,0xff
 };

 	final static  short[] cc48 = // convert 4-bit color to 8-bit color
 {
     0x00,0x11,0x22,0x33, 0x44,0x55,0x66,0x77, 0x88,0x99,0xaa,0xbb, 0xcc,0xdd,0xee,0xff
 };
	
	final static short[] cc58 =
	{
	    0x00,0x08,0x10,0x19, 0x21,0x29,0x31,0x3a, 0x42,0x4a,0x52,0x5a, 0x63,0x6b,0x73,0x7b,
	    0x84,0x8c,0x94,0x9c, 0xa5,0xad,0xb5,0xbd, 0xc5,0xce,0xd6,0xde, 0xe6,0xef,0xf7,0xff
	};
	
	final static short [] cc68 = // convert 6-bit color to 8-bit color
		{
		    0x00,0x04,0x08,0x0c, 0x10,0x14,0x18,0x1c, 0x20,0x24,0x28,0x2d, 0x31,0x35,0x39,0x3d,
		    0x41,0x45,0x49,0x4d, 0x51,0x55,0x59,0x5d, 0x61,0x65,0x69,0x6d, 0x71,0x75,0x79,0x7d,
		    0x82,0x86,0x8a,0x8e, 0x92,0x96,0x9a,0x9e, 0xa2,0xa6,0xaa,0xae, 0xb2,0xb6,0xba,0xbe,
		    0xc2,0xc6,0xca,0xce, 0xd2,0xd7,0xdb,0xdf, 0xe3,0xe7,0xeb,0xef, 0xf3,0xf7,0xfb,0xff
		};
	
	public TexDec(byte[] buf){
		
		this.buf = buf;

	}
	
	/*CMPR employs a similar codec to DXT1, but with two differences:
	 * -CMPR is big-endian
	 * -CMPR blocks are 8x8 pixel blocks comprised of 4 4x4 pixel blocks; instead of writing 4 blocks as 
	 * 
	 * 1 2 3 4
	 * 
	 * they should be written as 
	 * 
	 * 1 2
	 * 3 4
	 */
	
	//http://wiki.tockdom.com/wiki/Image_Formats#CMPR
	//https://en.wikipedia.org/wiki/S3_Texture_Compression#DXT1
	
	public static void decCMPR(byte[] buf, int w, int h, String path, String parent, String name, String hash ) throws IOException {
		
		o = new File(parent+"\\"+name+"\\"+hash+".png");
		BufferedImage pn = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 		
		bi = 0; //buffer index variable
		
		//logic to write 8x8 blocks
		
 		int X = 0;
 		int Y = 0;
 		int dx = 0;
 		int dy = 0;
 		
 		int dyHack = 0;
 		
 		
 	while(Y<h-4) {
 			
 		
 		
 		if(dx>=8) {
 			
 			if(dyHack == X)
 				dy=4;
 			else dy = 0;
 			
 			dx=0;
 			
 		}
 		
 		
 		
 		
 	 	//System.out.println("X: "+X+" Y: "+Y+" dx: "+dx+" dy: "+dy);
 		rwblock(X+dx,Y+dy,pn);
 		
 		dyHack = X;
 		if(dx == 4 && dy == 4)  { X+=8;}
 		
 		
 		dx+=4;
 		
 		if(X>w-4) {
 			X= 0 ;
 			Y+=8;
 		}
 		
 		
 		
 		}
 		
 		
 		ImageIO.write(pn, "png", o);
 		
 }
	

 public static void rwblock(int X, int Y, BufferedImage pn) throws IOException {
	 
	 	int c0 = ((buf[bi++] & 0xff) <<8) | (buf[bi++] & 0xff);
		int c1 = ((buf[bi++] & 0xff) <<8) | (buf[bi++] & 0xff);
		
		int c2 = 0;
		int c3 = 0;
		
		if(c0>=c1) { //opaque
		
		 c2 = mixColors(c0, c1, 2, 1, 3);
		 c3 = mixColors(c0, c1, 1, 2, 3);
		
		} else { //1 bit alpha transparency
			
			 c2 = mixColors(c0, c1, 1, 1, 2);
			 c3 = 0;
			
		}
		
		c0 = toA888(c0); //scale RGB565 colors to ARGB88
		c1 = toA888(c1);
		c2 = toA888(c2);
		c3 = toA888(c3);
		
		int[] colors = new int[4];
		
		colors[0] = c0;
		colors[1] = c1;
		colors[2] = c2;
		colors[3] = c3;
		
		int[] block = getBlock();
		
		writeBlock(block, colors, X, Y, pn);
	 
 }
 
 public static int[] getBlock() throws IOException{
 		
 		int[] blocc = new int[16];
 		
 		//get all 16 2-bit indices
 		int ind = ( ((buf[bi++] &0xff) << 24) + ((buf[bi++]&0xff) << 16) + ((buf[bi++]&0xff) << 8) + ((buf[bi++]&0xff)));
 		
 		
	 		for(int i = 0 ; i <16; ++i) 
	 			 blocc[i] = (ind >> 30-(2*i)) & 0b11; //get each 2 bit index
 		
		return blocc;
 		
 		
 		}
 
private static void writeBlock(int[] block, int[] c,int x, int y, BufferedImage p) {
	
	int index = 0;
		
    for(int dy = 0; dy < 4; dy++) 
        for(int dx = 0; dx < 4; dx++) {
			p.setRGB(x+dx, y+dy, c[block[index++]]);

        }
	
	
}

/*Convert c0 and c1 to RGB888, calculate c2 and c3, convert back to RGB565.
 * src: https://github.com/notezway/DXT1Decoder/blob/master/src/net/dds/DXT1Decoder.java
 */
private static int mixColors(int color0, int color1, int mul1, int mul2, int div) { 
    float r0 = (color0 >>> 11) & 31;
    float g0 = (color0 >>> 5) & 63;
    float b0 = color0 & 31;
    float r1 = (color1 >>> 11) & 31;
    float g1 = (color1 >>> 5) & 63;
    float b1 = color1 & 31;
    float r = (r0 * mul1 + r1 * mul2) / div;
    float g = (g0 * mul1 + g1 * mul2) / div;
    float b = (b0 * mul1 + b1 * mul2) / div;
    return (Math.round(r) << 11) | (Math.round(g) << 5) | (Math.round(b));
}
	

public static int toA888(int c) {
	 int ret = 0xFF<<24 | cc58[ c >> 11        ] << 16	// red
			 | cc68[ c >>  5 & 0x3f ] <<  8	// green
			 | cc58[ c       & 0x1f ];		// blue


	 return ret;
	
}


private static void write5A3Block( int x, int y, BufferedImage pn) throws IOException {

	 for(int dy = 0; dy < 4; dy++) {
        for(int dx = 0; dx < 4; dx++) {
        	//pint(buf.length);

       	 	int color = (((buf[bi++]&0xFF) <<8) | (buf[bi++]&0xFF))&0xFFFF;
       	 	//pint(color);
       	 //	System.exit(0);
       	 	   
           
            pn.setRGB(x + dx, y + dy, (((color >> 15) & 0x1) == 0x1 ? ARGBto888(color, false) : ARGBto888(color, true) ));
        }
    }

	
}

public static int ARGBto888(int color, boolean alpha) {
	 
	 int ret  = 0;
	 
	 if(alpha)  //0AAARRRRGGGGBBBB
		 ret = cc38[(color>>12) &7] <<24 | cc48[((color >> 8) & 0xF)] <<16 |  cc48[((color >> 4) & 0xF)] << 8 | cc48[color & 0xF];
	
	 
	 else      //1RRRRRGGGGGBBBBB
		 ret = (0xFF<<24) | (cc58[((color >> 10) & 0x1F)] <<16) |  (cc58[((color >> 5) & 0x1F)] << 8) | cc58[color & 0x1F];
	 
	 //pint(ret);
	return ret;
	 
	 
}

//https://github.com/marco-calautti/Rainbow/wiki/RGB5A3
//http://wiki.tockdom.com/wiki/Image_Formats#RGB5A3
	
public static void decRGB5A3(byte[] buf, int w, int h, String path, String parent, String name, String hash ) throws IOException {

	o = new File(parent+"\\"+name+"\\"+hash+".png");
	BufferedImage pn = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		 
		int x = 0;
		int y = 0;
		int dx = 0;
		int dy = 0;
		
		int dyHack = 0;
		bi=0;
		
	 	while(y<h) {


	 		
	 		write5A3Block(x,y,pn);
	 	 	//System.out.println("X: "+X+" Y: "+Y+" dx: "+dx+" dy: "+dy);

		 	
	 		x+= 4;
		    if(x >= w) {
	            x = 0;
	            y += 4;
	        
	    }
	 		
	 		
	 		
	 		}
		
        
		ImageIO.write(pn, "png", o);

	 


}
			


	public static void decRGBA8(byte[] buf, int w, int h, String path, String parent, String name, String hash ) throws IOException {

		o = new File(parent+"\\"+name+"\\"+hash+".png");
		BufferedImage pn = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			 
			int x = 0;
			int y = 0;
			int dx = 0;
			int dy = 0;
			
			int dyHack = 0;
			bi=0;
			
		 	while(y<h) {


		 		
		 		writeRGB8Block(x,y,pn);
		 	 	//System.out.println("X: "+X+" Y: "+Y+" dx: "+dx+" dy: "+dy);

			 	
		 		x+= 4;
			    if(x >= w) {
		            x = 0;
		            y += 4;
		        
		    }
		 		
		 		
		 		
		 		}
			
	        
			ImageIO.write(pn, "png", o);

		 


	}
	
	private static void writeRGB8Block( int x, int y, BufferedImage pn) throws IOException {
		 
		 int[] AR = new int[16];
		 int[] GB = new int[16];
		 int[] colors = new int[16];
		 int ind = 0;
		 
		 for(int c = 0; c <16; ++c) 
			AR[c] = (((buf[bi++]&0xFF) <<8) | (buf[bi++]&0xFF))&0xFFFF;
		 
		 for(int c = 0; c <16; ++c) 
			GB[c] = (((buf[bi++]&0xFF) <<8) | (buf[bi++]&0xFF))&0xFFFF;
		 
		 for(int c = 0; c <16; ++c) {
			colors[c] = (AR[c]<<16) | GB[c]&0xFFFF;
		 
		// pint(colors[c]);
		
		 }

		 
		 
		 for(int dy = 0; dy < 4; dy++) {
	         for(int dx = 0; dx < 4; dx++) {

	                pn.setRGB(x + dx, y + dy, colors[ind++]);
	         }
	         }
	     }
	 			
				

		
		
	public static void pint(int txt) {
		
		System.out.println(Integer.toHexString(txt));
	}	
		
	}//end class
 			
 
 
 		
 		
 		
		
	



