package ch.admin.bit.jeap.crypto.db;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestEntity {

    @Id
    private String id;

    @Convert(converter = JeapCryptoStringConverter.class)
    private String encryptedValue;

}
