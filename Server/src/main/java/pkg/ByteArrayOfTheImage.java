package pkg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class ByteArrayOfTheImage implements Serializable
{
    private byte[] byteOfImage;

    public ByteArrayOfTheImage(BufferedImage image)
    {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byteOfImage = baos.toByteArray();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public BufferedImage getBufferedImage()
    {
        try {
            return ImageIO.read(new ByteArrayInputStream(byteOfImage));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
