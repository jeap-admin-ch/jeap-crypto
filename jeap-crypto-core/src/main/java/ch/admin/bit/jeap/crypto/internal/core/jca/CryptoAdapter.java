package ch.admin.bit.jeap.crypto.internal.core.jca;

import com.amazon.corretto.crypto.provider.AmazonCorrettoCryptoProvider;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Slf4j
public class CryptoAdapter {

    private static final String CORRETO_PROVIDER_NAME = AmazonCorrettoCryptoProvider.PROVIDER_NAME;

    private static boolean correttoEnabled;

    static {
        AmazonCorrettoCryptoProvider.install();
        try {
            AmazonCorrettoCryptoProvider.INSTANCE.assertHealthy();
            correttoEnabled = true;
        } catch (Throwable t) {
            log.error("Native corretto crypto provider is not available on this platform. Crypto performance will not be optimized.");
            correttoEnabled = false;
        }
    }

    public static Cipher createCipher(String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        if (correttoEnabled) {
            return Cipher.getInstance(algorithm, CORRETO_PROVIDER_NAME);
        }
        return Cipher.getInstance(algorithm);
    }

    public static KeyFactory getKeyFactoryInstance(String rsa) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (correttoEnabled) {
            return KeyFactory.getInstance("RSA", CORRETO_PROVIDER_NAME);
        }
        return KeyFactory.getInstance("RSA");
    }
}
