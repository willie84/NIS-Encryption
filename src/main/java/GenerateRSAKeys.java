//package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
//import sun.misc.BASE64Encoder;
import java.util.Base64;

public class GenerateRSAKeys{

    String publicKeyFilename;
    String privateKeyFilename;

    public GenerateRSAKeys(String publicKeyFilename, String privateKeyFilename){
        this.publicKeyFilename = publicKeyFilename;
        this.privateKeyFilename = privateKeyFilename;
    }

    synchronized void generate (){

        try {

            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            // Create the public and private keys
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

            SecureRandom random = createFixedRandom();
            generator.initialize(1024, random);

            KeyPair pair = generator.generateKeyPair();
            Key pubKey = pair.getPublic();
            Key privKey = pair.getPrivate();

            System.out.println("publicKey: "+Base64.getEncoder().encodeToString(pubKey.getEncoded()));
            System.out.println("privateKey: "+Base64.getEncoder().encodeToString(privKey.getEncoded()));

            BufferedWriter out = new BufferedWriter(new FileWriter(publicKeyFilename));
            out.write(Base64.getEncoder().encodeToString(pubKey.getEncoded()));
            System.out.println("Finished writing public key to file.");
            out.close();

            out = new BufferedWriter(new FileWriter(privateKeyFilename));
            out.write(Base64.getEncoder().encodeToString(privKey.getEncoded()));
            System.out.println("Finished writing private key to file.");
            out.close();


        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public static SecureRandom createFixedRandom()
    {
        return new FixedRand();
    }

    private static class FixedRand extends SecureRandom {

        MessageDigest sha;
        byte[] state;

        FixedRand() {
            try
            {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new RuntimeException("can't find SHA-1!");
            }
        }

        public void nextBytes(byte[] bytes){

            int    off = 0;

            sha.update(state);

            while (off < bytes.length)
            {
                state = sha.digest();

                if (bytes.length - off > state.length)
                {
                    System.arraycopy(state, 0, bytes, off, state.length);
                }
                else
                {
                    System.arraycopy(state, 0, bytes, off, bytes.length - off);
                }

                off += state.length;

                sha.update(state);
            }
        }
    }

}