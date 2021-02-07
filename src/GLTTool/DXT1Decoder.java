/*package GLTTool;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


public class DXT1Decoder {

    public final int magicWord = 0x20534444;
    public final int dxt1FormatCode = 0x31545844;

    private InputStream input;

    private int dwSize;
    private int dwFlags;
    private int dwHeight;
    private int dwWidth;
    private int dwPitchOrLinearSize;
    private int dwDepth;
    private int dwMipMapCount;

    //11 reserved dwords

    private int ddspf_dwSize;
    private int ddspf_dwFlags;
    private int ddspf_dwFourCC;
    private int ddspf_dwRGBBitCount;
    private int ddspf_dwRBitMask;
    private int ddspf_dwGBitMask;
    private int ddspf_dwBBitMask;
    private int ddspf_dwABitMask;

    private int dwCaps;
    private int dwCaps2;
    private int dwCaps3;
    private int dwCaps4;

    //1 reserved dword

    public DXT1Decoder(InputStream input) {
        this.input = input;
    }

    public BufferedImage getImage() {
        BufferedImage image = null;

        try {
            if(readDWord() != magicWord) {
                throw new IOException("Input stream does not contain DDS texture");
            }
            readHeader();
            if(ddspf_dwFourCC != dxt1FormatCode) {
                throw new IOException("Incorrect texture format");
            }
            image = readTextureData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void readHeader() throws IOException {
        dwSize = readDWord();
        dwFlags = readDWord();
        dwHeight = readDWord();
        dwWidth = readDWord();
        dwPitchOrLinearSize = readDWord();
        dwDepth = readDWord();
        dwMipMapCount = readDWord();
        input.skip(11*4);
        ddspf_dwSize = readDWord();
        ddspf_dwFlags = readDWord();
        ddspf_dwFourCC = readDWord();
        ddspf_dwRGBBitCount = readDWord();
        ddspf_dwRBitMask = readDWord();
        ddspf_dwGBitMask = readDWord();
        ddspf_dwBBitMask = readDWord();
        ddspf_dwABitMask = readDWord();
        dwCaps = readDWord();
        dwCaps2 = readDWord();
        dwCaps3 = readDWord();
        dwCaps4 = readDWord();
        input.skip(4);
    }

    private BufferedImage readTextureData() throws IOException {
        BufferedImage image = new BufferedImage(dwWidth, dwHeight, BufferedImage.TYPE_INT_RGB);
        int color0, color1;
        int codes;
        int imageX = 0, imageY = 0;
        while(imageY < dwHeight) {
            color0 = (input.read()) | (input.read() << 8);
            color1 = (input.read()) | (input.read() << 8);
            codes = readDWord();
            writeBlock(image, imageX, imageY, color0, color1, codes);
            imageX += 4;
            if(imageX >= dwWidth) {
                imageX = 0;
                imageY += 4;
            }
        }
        return image;
    }

    private void writeBlock(BufferedImage image, int x, int y, int color0, int color1, int codes) {
        int code;
        int rgb;
        int color2 = mixColors(color0, color1, 2, 1, 3);
        int color3 = mixColors(color0, color1, 1, 1, 2);
        int color4 = mixColors(color0, color1, 1, 2, 3);
        boolean c0gtc1 = color0 > color1;

        for(int dy = 0; dy < 4; dy++) {
            for(int dx = 0; dx < 4; dx++) {
                code = (codes >>> (2 * (dx + dy * 4))) & 3;
                rgb = code == 0 ? color0 : code == 1 ? color1 : code == 2 ? c0gtc1 ? color2 : color3 : code == 3 ? color4 : 0;
                image.setRGB(x + dx, y + dy, color565to888(rgb));
            }
        }
    }

    private int mixColors(int color0, int color1, int mul1, int mul2, int div) {
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

    private int color565to888(int c) {
        float r = (c >>> 11) & 31;
        float g = (c >>> 5) & 63;
        float b = (c) & 31;
        r /= 1<<5 - 1;
        r *= 1<<8 - 1;
        g /= 1<<6 - 1;
        g *= 1<<8 - 1;
        b /= 1<<5 - 1;
        b *= 1<<8 - 1;

        //gamma correction
        r = (float) Math.min(255, Math.pow(r / 255f, 1/1.11) * 255);
        g = (float) Math.min(255, Math.pow(g / 255f, 1/1.11) * 255);
        b = (float) Math.min(255, Math.pow(b / 255f, 1/1.11) * 255);

        return ((Math.round(r)) << 16) | ((Math.round(g)) << 8) | ((Math.round(b)));
    }

    private int readDWord() throws IOException {
        return (input.read()) | (input.read() << 8) | (input.read() << 16) | (input.read() << 24);
    }
}*/