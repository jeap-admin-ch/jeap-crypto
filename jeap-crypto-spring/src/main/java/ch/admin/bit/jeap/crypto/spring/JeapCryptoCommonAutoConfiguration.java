package ch.admin.bit.jeap.crypto.spring;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.api.CryptoServiceProvider;
import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.api.KeyIdCryptoService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;

@AutoConfiguration
public class JeapCryptoCommonAutoConfiguration {

    private final List<KeyIdCryptoService> keyIdCryptoServices;

    public JeapCryptoCommonAutoConfiguration(List<KeyIdCryptoService> keyIdCryptoServices) {
        this.keyIdCryptoServices = keyIdCryptoServices;
    }

    @Bean
    CryptoServiceProvider cryptoServiceProvider() {
        return new CryptoServiceProvider(keyIdCryptoServices);
    }

    @PostConstruct
    void validateNoDuplicateKeyIds() {
        keyIdCryptoServices.stream()
                .flatMap(keyIdCryptoService -> keyIdCryptoService.configuredKeyIds().stream())
                .collect(Collectors.groupingBy(keyId -> keyId)).values()
                .stream().filter(keyIdList -> keyIdList.size() > 1)
                .findFirst()
                .ifPresent(keyIds -> throwDuplicateKeyIdException(keyIds.get(0)));
    }

    private void throwDuplicateKeyIdException(KeyId keyId) {
        throw CryptoException.duplicateKeyId(keyId);
    }
}
