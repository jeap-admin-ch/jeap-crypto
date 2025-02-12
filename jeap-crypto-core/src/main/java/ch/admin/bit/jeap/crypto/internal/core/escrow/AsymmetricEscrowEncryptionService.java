package ch.admin.bit.jeap.crypto.internal.core.escrow;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.internal.core.jca.CryptoAdapter;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

public class AsymmetricEscrowEncryptionService implements EscrowEncryptionService {

    // See https://github.com/corretto/amazon-corretto-crypto-provider
    static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPPadding";

    @Override
    public EscrowDataKey encryptEscrowDataKey(DataKey DataKey, EscrowKeyType escrowKeyType, PublicKey escrowPublicKey) {
        if (escrowKeyType == EscrowKeyType.NONE) {
            return null;
        }

        try {
            byte[] ciphertext = encrypt(DataKey.plaintextDataKey(), escrowKeyType, escrowPublicKey);
            return new EscrowEncryptedDataKey(ciphertext, escrowKeyType);
        } catch (GeneralSecurityException exception) {
            throw CryptoException.generalSecurityException("Failed to encrypt escrow data key", exception);
        }
    }

    private byte[] encrypt(byte[] plaintext, EscrowKeyType escrowKeyType, PublicKey escrowPublicKey) throws GeneralSecurityException {
        Cipher cipher = CryptoAdapter.createCipher(transformation(escrowKeyType));
        cipher.init(Cipher.ENCRYPT_MODE, escrowPublicKey);
        cipher.update(plaintext);
        return cipher.doFinal();
    }

    private String transformation(EscrowKeyType escrowKeyType) {
        if (escrowKeyType == EscrowKeyType.RSA_4096) {
            return RSA_TRANSFORMATION;
        }
        throw CryptoException.unknownEscrowKeyType(escrowKeyType);
    }
}
