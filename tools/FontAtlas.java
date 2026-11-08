import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Gera um atlas BMFont a partir de fontes OFL, sem dependência de runtime. */
class FontAtlas {
    public static void main(String[] args) throws Exception {
        Font font=Font.createFont(Font.TRUETYPE_FONT,new File(args[0])).deriveFont(Float.parseFloat(args[2]));
        int size=2048, cell=120, pad=3;
        BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(font);g.setColor(Color.WHITE);
        FontMetrics fm=g.getFontMetrics();
        String name=Paths.get(args[1]).getFileName().toString();
        StringBuilder out=new StringBuilder("info face=\""+font.getFamily()+"\" size="+args[2]+" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0\n");
        out.append("common lineHeight="+fm.getHeight()+" base="+fm.getAscent()+" scaleW="+size+" scaleH="+size+" pages=1 packed=0\npage id=0 file=\""+name+".png\"\nchars count=224\n");
        for(int c=32;c<256;c++) {
            int index=c-32,x=(index%16)*cell,y=(index/16)*cell;
            g.drawString(String.valueOf((char)c),x+pad,y+fm.getAscent());
            int width=Math.min(cell,Math.max(1,fm.charWidth(c)+pad*2));
            out.append("char id="+c+" x="+x+" y="+y+" width="+width+" height="+fm.getHeight()+" xoffset="+(-pad)+" yoffset=0 xadvance="+fm.charWidth(c)+" page=0 chnl=15\n");
        }
        g.dispose();
        ImageIO.write(image,"png",new File(args[1]+".png"));
        Files.write(Paths.get(args[1]+".fnt"),out.toString().getBytes(StandardCharsets.UTF_8));
    }
}
