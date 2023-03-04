package net.royalur.backend.ssl;

import java.security.KeyStore;

/**
 * Holds information about keys to use for SSL.
 */
public class KeyInfo {

    public final KeyStore keyStore;
    public final String password;

    public KeyInfo(KeyStore keyStore, String password) {
        this.keyStore = keyStore;
        this.password = password;
    }
}
