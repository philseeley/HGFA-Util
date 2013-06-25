import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class Sign
{
  static final String EOL = System.getProperty("line.separator");

  public static void main(String[] args) throws NoSuchAlgorithmException,
      InvalidKeyException, SignatureException, KeyStoreException,
      CertificateException, FileNotFoundException, IOException,
      UnrecoverableKeyException, WriterException
  {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    char[] password = "password".toCharArray();
    ks.load(new FileInputStream("hgfa.jks"), password);

    PrivateKey key = (PrivateKey) ks.getKey("signer", password);

    BufferedReader buf = new BufferedReader(new FileReader(args[0]));

    StringBuffer data = new StringBuffer();

    String line;
    while ((line = buf.readLine()) != null)
    {
      data.append(line);
      data.append(EOL);
    }

    buf.close();
    
    System.out.println(data.toString());

    // Compute signature
    Signature instance = Signature.getInstance("SHA1withRSA");
    instance.initSign(key);
    instance.update(data.toString().getBytes());
    byte[] signature = instance.sign();

    System.out.println("Signature: "+DatatypeConverter.printBase64Binary(signature));

    data.append("Sig:");
    data.append(DatatypeConverter.printBase64Binary(signature));
    
    // signature[0] = '1';
    System.out.println("Signature: "
        + javax.xml.bind.DatatypeConverter.printBase64Binary(signature));
    instance.initVerify(ks.getCertificate("signer"));
    instance.update(data.toString().getBytes());

    System.out.println(instance.verify(signature));

    QRCode qrcode = Encoder.encode(data.toString(), ErrorCorrectionLevel.L);

    ByteMatrix matrix = qrcode.getMatrix();

    int width = matrix.getWidth();
    int height = matrix.getHeight();

    BufferedImage image = new BufferedImage(width*4, height*4,
        BufferedImage.TYPE_INT_RGB);
    image.createGraphics();

    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width*4, height*4);
    // Paint and save the image using the ByteMatrix
    graphics.setColor(Color.BLACK);

    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        if (matrix.get(i, j) == 1)
        {
          graphics.fillRect(i*4, j*4, 4, 4);
        }
      }
    }
    ImageIO.write(image, "png", new File("test.png"));

  }

}
