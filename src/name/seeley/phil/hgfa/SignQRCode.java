package name.seeley.phil.hgfa;

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
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class SignQRCode
{
  static final String EOL = "\n";

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, WriterException
  {
    if (args.length != 4)
    {
      System.err
          .println("Usage:"
              + SignQRCode.class.getName()
              + " <private key file> <qrcode image width> <qrcode image height> <data file>");
      System.exit(-1);
    }

    String privateKeyFilename = args[0];
    int width = Integer.parseInt(args[1]);
    int height = Integer.parseInt(args[2]);
    String dataFilename = args[3];

    BufferedReader keyReader = new BufferedReader(new FileReader(
        privateKeyFilename));

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

    QRCodeWriter qrWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrWriter.encode(data.toString(), BarcodeFormat.QR_CODE, width, height);

    MatrixToImageWriter.writeToFile(bitMatrix, "PNG", new File(dataFilename+".png"));
  }
}
