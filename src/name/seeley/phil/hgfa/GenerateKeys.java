package name.seeley.phil.hgfa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class GenerateKeys
{
  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) throws NoSuchAlgorithmException, IOException
  {
    if(args.length != 1)
    {
      System.err.println("Usage:"+GenerateKeys.class.getName()+" <key name>");
      System.exit(-1);
    }
    
    String keyName = args[0];
    
    KeyPair pair = KeyPairGenerator.getInstance("ECDSA").generateKeyPair();
    
    PublicKey publicKey = pair.getPublic();
    PrivateKey privateKey = pair.getPrivate();

    BufferedWriter writer = new BufferedWriter(new FileWriter(keyName+"-public.key"));
    
    writer.write(DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
    writer.newLine();
    writer.close();
    
    writer = new BufferedWriter(new FileWriter(keyName+"-private.key"));
    
    writer.write(DatatypeConverter.printBase64Binary(privateKey.getEncoded()));
    writer.newLine();
    writer.close();
  }
}
