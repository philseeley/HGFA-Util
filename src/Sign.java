import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;
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

  public static void main(String[] args) throws NoSuchAlgorithmException,
      InvalidKeyException, SignatureException, KeyStoreException,
      CertificateException, FileNotFoundException, IOException,
      UnrecoverableKeyException, WriterException, InvalidKeySpecException
  {

    Security.addProvider(new BouncyCastleProvider());

    String privatePEM = "MIGNAgEAMBMGByqGSM49AgEGCCqGSM49AwEEBHMwcQIBAQQeFCZcKqMf39aRZ307mP57xOvFR0GKOJBDs1hErGproAoGCCqGSM49AwEEoUADPgAEZFvqdcZ+KiZIxH7/vOruEkK5IP3WwZtoiLL+chQjEzb5nSIjLKKATk2Utz/SpQmS0EvOGTKm/EPCmb6j";
    String publicPEM = "MFUwEwYHKoZIzj0CAQYIKoZIzj0DAQQDPgAEZFvqdcZ+KiZIxH7/vOruEkK5IP3WwZtoiLL+chQjEzb5nSIjLKKATk2Utz/SpQmS0EvOGTKm/EPCmb6j";

    KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");

    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
        DatatypeConverter.parseBase64Binary(publicPEM));
    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
        DatatypeConverter.parseBase64Binary(privatePEM));
    
    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
/*
    KeyPair pair = KeyPairGenerator.getInstance("ECDSA").generateKeyPair();
    
    PublicKey publicKey = pair.getPublic();
    PrivateKey privateKey = pair.getPrivate();
*/
    
    System.out.println(DatatypeConverter.printBase64Binary(privateKey.getEncoded()));
    System.out.println(DatatypeConverter.printBase64Binary(publicKey
        .getEncoded()));

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

    Signature instance = Signature.getInstance("ECDSA");

    instance.initSign(privateKey);
    instance.update(data.toString().getBytes());
    byte[] signature = instance.sign();

    System.out.println("Signature: "
        + DatatypeConverter.printBase64Binary(signature));

    // signature[0] = '1';
    instance.initVerify(publicKey);

    instance.update(data.toString().getBytes());
    System.out.println(instance.verify(signature));

    data.append("_SIG:");
    data.append(DatatypeConverter.printBase64Binary(signature));

    QRCode qrcode = Encoder.encode(data.toString(), ErrorCorrectionLevel.L);

    ByteMatrix matrix = qrcode.getMatrix();

    int width = matrix.getWidth();
    int height = matrix.getHeight();

    BufferedImage image = new BufferedImage(width * 4 + 40, height * 4 + 40,
        BufferedImage.TYPE_INT_RGB);
    image.createGraphics();

    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width * 4 + 40, height * 4 + 40);
    // Paint and save the image using the ByteMatrix
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
    ImageIO.write(image, "png", new File("test.png"));

  }

}
