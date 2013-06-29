package name.seeley.phil.hgfa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class Sign
{
  static final String EOL = "\n";

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }
  
  public static void main(String[] args) throws IOException,
      NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
      SignatureException, WriterException
  {
    if(args.length != 2)
    {
      System.err.println("Usage:"+Sign.class.getName()+" <private key file> <data file>");
      System.exit(-1);
    }
    
    String privateKeyFilename = args[0];
    String dataFilename = args[1];
    
    BufferedReader keyReader = new BufferedReader(new FileReader(privateKeyFilename));
    
    String privateKeyText = keyReader.readLine();
    
    keyReader.close();
    
    KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");

    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
        DatatypeConverter.parseBase64Binary(privateKeyText));
    
    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

    BufferedReader dataReader = new BufferedReader(new FileReader(dataFilename));

    StringBuffer data = new StringBuffer();

    String line;
    while ((line = dataReader.readLine()) != null)
    {
      data.append(line);
      data.append(EOL);
    }

    dataReader.close();

    Signature signature = Signature.getInstance("ECDSA");

    signature.initSign(privateKey);
    signature.update(data.toString().getBytes());
    byte[] signatureBytes = signature.sign();

    data.append("_SIG:");
    data.append(DatatypeConverter.printBase64Binary(signatureBytes));
    
    QRCode qrCode = Encoder.encode(data.toString(), ErrorCorrectionLevel.L);

    ByteMatrix matrix = qrCode.getMatrix();

    int width = matrix.getWidth();
    int height = matrix.getHeight();

    BufferedImage image = new BufferedImage(width * 4 + 40, height * 4 + 40,
        BufferedImage.TYPE_INT_RGB);
    image.createGraphics();

    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width * 4 + 40, height * 4 + 40);
    graphics.setColor(Color.BLACK);

    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        if (matrix.get(i, j) == 1)
        {
          graphics.fillRect(i * 4 + 20, j * 4 + 20, 4, 4);
        }
      }
    }
    
    ImageIO.write(image, "png", new File(args[1]+".png"));
  }

}
