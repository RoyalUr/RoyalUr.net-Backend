package net.sothatsit.royalurserver.ssl;

import jakarta.xml.bind.DatatypeConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility methods for setting up SSL encryption using Let's Encrypt.
 * Taken from the java-websocket SSLServerLetsEncryptExample.java.
 */
public class LetsEncryptSSL {

    /**
     * @param certFile The certificate file to use, cert.pem.
     * @param privateKeyFile The private key file to use, privkey.pem.
     * @param password The password of the key.
     * @return A key to use to support SSL-encrypted connections.
     */
    public static KeyInfo loadKey(File certFile, File privateKeyFile, String password) {
        try {
            byte[] certBytes = parseDERFromPEM(
                    getBytes(certFile),
                    "-----BEGIN CERTIFICATE-----",
                    "-----END CERTIFICATE-----"
            );
            byte[] keyBytes = parseDERFromPEM(
                    getBytes(privateKeyFile),
                    "-----BEGIN PRIVATE KEY-----",
                    "-----END PRIVATE KEY-----"
            );

            X509Certificate cert = generateCertificateFromDER(certBytes);
            RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[] {cert});

            return new KeyInfo(keystore, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);

        int beginIndex = data.indexOf(beginDelimiter);
        if (beginIndex < 0)
            throw new IllegalArgumentException("Malformed key, expected start delimiter");

        int endIndex = data.indexOf(endDelimiter);
        if (endIndex < 0)
            throw new IllegalArgumentException("Malformed key, expected end delimiter");

        String content = data.substring(beginIndex + beginDelimiter.length(), endIndex);
        return DatatypeConverter.parseBase64Binary(content);
    }

    public static RSAPrivateKey generatePrivateKeyFromDER(
            byte[] keyBytes
    ) throws InvalidKeySpecException, NoSuchAlgorithmException {

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    public static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    public static byte[] getBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
