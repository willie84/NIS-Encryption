//package org.example;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
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

    //constructor
    public GenerateRSAKeys(String publicKeyFilename, String privateKeyFilename){
        this.publicKeyFilename = publicKeyFilename;
        this.privateKeyFilename = privateKeyFilename;
    }

    synchronized void generate (){

        try {

            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            // Create the public and private keys
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
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

    //method to encrypt a string message
    synchronized String encrypt (String publicKeyFilename, String input){

        try {

            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            String value = "";
            String key = readFileAsString(publicKeyFilename);
            //BASE64Decoder b64 = new BASE64Decoder();
            AsymmetricKeyParameter publicKey =
                    (AsymmetricKeyParameter) PublicKeyFactory.createKey(Base64.getDecoder().decode(key));
            AsymmetricBlockCipher e = new RSAEngine();
            e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
            e.init(true, publicKey);

            byte[] messageBytes = input.getBytes();
            int i = 0;
            int len = e.getInputBlockSize();
            while (i < messageBytes.length)
            {
                if (i + len > messageBytes.length)
                    len = messageBytes.length - i;

                byte[] hexEncodedCipher = e.processBlock(messageBytes, i, len);
                value = value + getHexString(hexEncodedCipher);
                i += e.getInputBlockSize();
            }

            //System.out.println(value);
            return value;

        }
        catch (Exception e) {
            System.out.println(e);
            return "Encryption unsuccessful:(";
        }
    }

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    private static String readFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        //System.out.println(fileData.toString());
        return fileData.toString();
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