package ch.admin.bit.jeap.crypto.internal.core.escrow;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import com.amazon.corretto.crypto.provider.AmazonCorrettoCryptoProvider;
import lombok.experimental.UtilityClass;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class PublicKeyParser {

    public PublicKey parsePublicKey(String key) {
        if (key != null) {
            String publicKeyCleanedUp = key
                    .replace("\n", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");

            try {
                KeyFactory kf = KeyFactory.getInstance("RSA", AmazonCorrettoCryptoProvider.INSTANCE);
                X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyCleanedUp));
                return kf.generatePublic(keySpecX509);
            } catch (GeneralSecurityException ex) {
                throw CryptoException.generalSecurityException("Unable to parse public escrow key", ex);
            }
        }
        return null;
    }
}
