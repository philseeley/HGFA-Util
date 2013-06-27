import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
    
    //for(Provider p: Security.getProviders())
      //System.out.println(p.getServices());
    
      String privatePEM = "MIGNAgEAMBMGByqGSM49AgEGCCqGSM49AwEEBHMwcQIBAQQeFCZcKqMf39aRZ307mP57xOvFR0GKOJBDs1hErGproAoGCCqGSM49AwEEoUADPgAEZFvqdcZ+KiZIxH7/vOruEkK5IP3WwZtoiLL+chQjEzb5nSIjLKKATk2Utz/SpQmS0EvOGTKm/EPCmb6j";
      String publicPEM = "MFUwEwYHKoZIzj0CAQYIKoZIzj0DAQQDPgAEZFvqdcZ+KiZIxH7/vOruEkK5IP3WwZtoiLL+chQjEzb5nSIjLKKATk2Utz/SpQmS0EvOGTKm/EPCmb6j";
    
    //String pem = "MFUwEwYHKoZIzj0CAQYIKoZIzj0DAQQDPgAEHj9KjjnZpuR+D6OIueQA37dDnRSZ4Wr/T/8kWu6OFmmLAc6KEdLaPEfOuiY9GVI9Ci9culI8arG5GSyi";
    //byte der[] = DatatypeConverter.parseBase64Binary(pem);
    
    //KeyPair pair = KeyPairGenerator.getInstance("ECDSA").generateKeyPair();
    KeyFactory fact = KeyFactory.getInstance("ECDSA");

    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(publicPEM));
    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(privatePEM));
    PublicKey publicKey = fact.generatePublic(publicKeySpec);
    PrivateKey key = fact.generatePrivate(privateKeySpec);
    System.out.println(DatatypeConverter.printBase64Binary(key.getEncoded()));
    System.out.println(DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
    
    //KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    //char[] password = "password".toCharArray();
    //ks.load(new FileInputStream("hgfa.jks"), password);

    //PrivateKey key = (PrivateKey) ks.getKey("signer", password);

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
    //Signature instance = Signature.getInstance("SHA1withRSA");//Signature: e/Mrqln3tenSYy/frpIuk8ZojXDJ2UZbmbycxaCx5OhY9EbzdJToZUPukxxsaJGkPYRotlBZJ+fvzgzuzr9KynTMRKtB9b9bsXxqvMpjQEPZ0WnG5A6fGBpzSCwFaL/THPepSKlGSukfhS0UyDL5SHs/QT3Ia9VLU3FhGmJxtZbZ0LlRP+UkEfBH02ADGWqNQuf6TCN/PhrshAeg615x+pQFh3nNNVey7xBcNaDCs37FCxOasvyxHuq9gScvB2sKajNSi48tuR5CfTgweMcLJrGANPIZN4nnxcMx8t3zj3D9y5h2wR4gX/sjsF5bDd9ligLjwbtpUhOxTZdcRP2QFA==

    //Signature instance = Signature.getInstance("MD2withRSA");//Signature: fGhE1PH2vK95QvjxuKLcATkCnU6SKnwMiOHCgAdDv2Qk7Ua3GAENZ49Ojbi6+Fll6dua0qBdqewYiyutb9nLf6sqdpdQFuo+6Z9MS+aDeNJnR9WIq4qdDs90h/nsOE8Z1olTsZWEn9HVWXbqd9YdyHSvbgl/Y+pPiS0qCTrOiuYTnRjNrDsSONOY6lb0bMj8v73zOFYJFnU6akTDJePOIFJKF+PcgQgMWHoCRO3syxB6jpq7RZPJOK7b0oQEeiQWfkKdknaEUKjeskofjeoGBjKv1+Bik9BFXN+m3k2XfoX8lDScnwHcNycPfKcE6y6PQxCPyB+5pDJH8yaQNmhmXw==
    //Signature instance = Signature.getInstance("SHA512withRSA");//Signature: cwxCzkZDFmxk/2/7YQ01CNk4f/oS5avfw8HOo/c5wZdCxees6pGpcKL/vWwJyGi3dHtKQIbBlO4slPhXxQIudtp8D9zRthcagoTdOTsLxyTWexLX3dQ9optWof6hJsT35/zGEjTxqzFgd/ID0WwqhF6Iaupd15ASckmO7bN94LOJo31BHuzUPYBlWAAvWkTqVATcW3WKvLFB1CxObA/Ed1btFTJ7HQo24qe6KkguW2XAvSgvFANszXU9GWxMDFj9jifZzEN9ylA6IJoN1S/UWYNTVS97On8Tzse/6Rn8ZOWFWGbdCwSXR+SsydZLX9vDSppcFzyZAUtlEV4nqQaPaA==
    Signature instance = Signature.getInstance("ECDSA");//Signature: cwxCzkZDFmxk/2/7YQ01CNk4f/oS5avfw8HOo/c5wZdCxees6pGpcKL/vWwJyGi3dHtKQIbBlO4slPhXxQIudtp8D9zRthcagoTdOTsLxyTWexLX3dQ9optWof6hJsT35/zGEjTxqzFgd/ID0WwqhF6Iaupd15ASckmO7bN94LOJo31BHuzUPYBlWAAvWkTqVATcW3WKvLFB1CxObA/Ed1btFTJ7HQo24qe6KkguW2XAvSgvFANszXU9GWxMDFj9jifZzEN9ylA6IJoN1S/UWYNTVS97On8Tzse/6Rn8ZOWFWGbdCwSXR+SsydZLX9vDSppcFzyZAUtlEV4nqQaPaA==
    
    instance.initSign(key);
    instance.update(data.toString().getBytes());
    byte[] signature = instance.sign();

    System.out.println("Signature: "+DatatypeConverter.printBase64Binary(signature));

    // signature[0] = '1';
    instance.initVerify(publicKey);
    //instance.initVerify(ks.getCertificate("signer"));
/*
    CertificateFactory factory = CertificateFactory.getInstance("X509");
    String pem = "MIIDSDCCAjCgAwIBAgIEUcl/DDANBgkqhkiG9w0BAQsFADBmMQswCQYDVQQGEwJBVTEMMAoGA1UE"+
    "CBMDVklDMRQwEgYDVQQHEwtLZWlsb3IgUGFyazENMAsGA1UEChMESEdGQTETMBEGA1UECxMKTWVt"+
    "YmVyc2hpcDEPMA0GA1UEAxMGU2lnbmVyMB4XDTEzMDYyNTExMjkxNloXDTE0MDYyNTExMjkxNlow"+
    "ZjELMAkGA1UEBhMCQVUxDDAKBgNVBAgTA1ZJQzEUMBIGA1UEBxMLS2VpbG9yIFBhcmsxDTALBgNV"+
    "BAoTBEhHRkExEzARBgNVBAsTCk1lbWJlcnNoaXAxDzANBgNVBAMTBlNpZ25lcjCCASIwDQYJKoZI"+
    "hvcNAQEBBQADggEPADCCAQoCggEBAIlzBvERQwyeeSk6CLVAB5GNZwVjCo+le8gH7v+4vYj990Vl"+
    "HiU5mO3nTfSf395if8pIOcpUML5Zlhuzb+7IJYiAJwgYIFfd9hvT5OP4/IFgI+Dg+k+NWymRbXmy"+
    "7FZ9iX+wrA3efExcxL5DzFV45mB3VhgYMoIAbWs8LOv+Q0tpTxmAn0xe8VIr4QPlWnOAtlwtASGa"+
    "9EZtzBtaU2YRLyx0ow+enHLMGk296n/ZwFk2CgbyLy/PpwR9PJZEe3a6NhrX6LWN6/eGb9nt0N+Y"+
    "fyXzC6qpPS689yW6z2Y1oTO8H1VdNQa/lTg+uutgYfVquxQ8L4Ew/q521I2NFEL2QjUCAwEAATAN"+
    "BgkqhkiG9w0BAQsFAAOCAQEAEy8xDxGg6aXeiL1DnO4TySzWLJpF3qQIKxCKFsUsrroNExIUHaYU"+
    "HS5Q+DI0AT0J1J7EypVSdNvoUSzHK/TbgkA0mI+wg0d+dkW6uoG348cF0YqHHokzZYrlmWlul/2c"+
    "2cELnCKgXCSoZ0XeMdiOyARFiPCX+c6dg3Bay25FfKIem/JjLa40dl9EN1RkYpiQU9WBewsaZ9ks"+
    "T1AHvvKOdzkIxfAMeEa4lovXgex4LD4Le970T8dLsQz9BqoY7KCAp79kMKinzTEBPU1coOe73qlv"+
    "XWUAVEDCMcylaCrNXbGnoERM2Yi8ykrLy34dd/hZylpfGt6jM/eNqDhi71ZhRA==";
    
    byte der[] = DatatypeConverter.parseBase64Binary(pem);

    Certificate cert = factory.generateCertificate(new ByteArrayInputStream(der));
    
    instance.initVerify(cert);
    */
    instance.update(data.toString().getBytes());
    System.out.println(instance.verify(signature));

    data.append("Sig:");
    data.append(DatatypeConverter.printBase64Binary(signature));

    QRCode qrcode = Encoder.encode(data.toString(), ErrorCorrectionLevel.L);

    ByteMatrix matrix = qrcode.getMatrix();

    int width = matrix.getWidth();
    int height = matrix.getHeight();

    BufferedImage image = new BufferedImage(width*4+40, height*4+40,
        BufferedImage.TYPE_INT_RGB);
    image.createGraphics();

    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width*4+40, height*4+40);
    // Paint and save the image using the ByteMatrix
    graphics.setColor(Color.BLACK);

    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        if (matrix.get(i, j) == 1)
        {
          graphics.fillRect(i*4+20, j*4+20, 4, 4);
        }
      }
    }
    ImageIO.write(image, "png", new File("test.png"));

  }

}
