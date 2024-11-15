package ch.admin.bit.jeap.crypto.db;

import ch.admin.bit.jeap.crypto.api.KeyId;
import ch.admin.bit.jeap.crypto.db.config.JeapCryptoDbTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@EnableAutoConfiguration
@SpringBootTest(properties = {"jeap.crypto.db.key-id=test-key-id"})
@ContextConfiguration(classes = {JeapCryptoDbConfig.class, JeapCryptoDbTestConfig.class, TestEntityRepository.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestEntityRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JeapCryptoDbTestConfig jeapCryptoDbTestConfig;

    @Autowired
    private TestEntityRepository testEntityRepository;

    @Test
    void test_encrypt(){
        //given
        final String stringToEncrypt = "My String to be encrypted...";
        final TestEntity testEntity = new TestEntity(UUID.randomUUID().toString(), stringToEncrypt);

        //when
        testEntityRepository.saveAndFlush(testEntity);

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + testEntity.getId() +"'");
        final String encryptedValue = new String((byte[]) results.get(0).get("ENCRYPTED_VALUE"), StandardCharsets.UTF_8);
        assertThat(encryptedValue).isEqualTo(JeapCryptoDbTestConfig.ENCRYPTED_PREFIX + stringToEncrypt);

        verify(jeapCryptoDbTestConfig.testCryptoService(), times(1))
                .encrypt(stringToEncrypt.getBytes(StandardCharsets.UTF_8), KeyId.of("test-key-id"));

        verify(jeapCryptoDbTestConfig.testCryptoService(), never())
                .decrypt((JeapCryptoDbTestConfig.ENCRYPTED_PREFIX + stringToEncrypt).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void test_encrypt_emptyString(){
        //given
        final TestEntity testEntity = new TestEntity(UUID.randomUUID().toString(), "");

        //when
        testEntityRepository.saveAndFlush(testEntity);

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + testEntity.getId() +"'");
        assertThat(results.get(0).get("ENCRYPTED_VALUE")).isEqualTo(new byte[0]);
        verify(jeapCryptoDbTestConfig.testCryptoService(), never()).encrypt(any(), any());
    }

    @Test
    void test_encrypt_nullValue(){
        //given
        final TestEntity testEntity = new TestEntity(UUID.randomUUID().toString(), null);

        //when
        testEntityRepository.saveAndFlush(testEntity);

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + testEntity.getId() +"'");
        assertThat(results.get(0).get("ENCRYPTED_VALUE")).isNull();
        verify(jeapCryptoDbTestConfig.testCryptoService(), never()).encrypt(any(), any());
    }

    @Test
    @Transactional
    void test_decrypt(){
        //given
        final String entityId = UUID.randomUUID().toString();
        final String stringToEncrypt = "My String to be encrypted and decrypted...";
        final String encryptedString = JeapCryptoDbTestConfig.ENCRYPTED_PREFIX + stringToEncrypt;

        //when
        jdbcTemplate.execute("insert into test_entity (id, encrypted_value) values ('" + entityId + "','" + encryptedString + "')");

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + entityId +"'");
        final String encryptedValue = new String((byte[]) results.get(0).get("ENCRYPTED_VALUE"));
        assertThat(encryptedValue).isEqualTo(encryptedString);

        assertThat(testEntityRepository.getReferenceById(entityId).getEncryptedValue()).isEqualTo(stringToEncrypt);

        verify(jeapCryptoDbTestConfig.testCryptoService(), times(1))
                .decrypt(encryptedString.getBytes(StandardCharsets.UTF_8));

        verify(jeapCryptoDbTestConfig.testCryptoService(), never())
                .encrypt(stringToEncrypt.getBytes(StandardCharsets.UTF_8), KeyId.of("test-key-id"));
    }

    @Test
    @Transactional
    void test_decrypt_emptyString(){
        //given
        final String entityId = UUID.randomUUID().toString();

        //when
        jdbcTemplate.execute("insert into test_entity (id, encrypted_value) values ('" + entityId + "', '')");

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + entityId + "'");
        assertThat(results.get(0).get("ENCRYPTED_VALUE")).isEqualTo(new byte[0]);
        assertThat(testEntityRepository.getReferenceById(entityId).getEncryptedValue()).isEmpty();
        verify(jeapCryptoDbTestConfig.testCryptoService(), never()).decrypt(any());
    }

    @Test
    @Transactional
    void test_decrypt_nullValue(){
        //given
        final String entityId = UUID.randomUUID().toString();

        //when
        jdbcTemplate.execute("insert into test_entity (id, encrypted_value) values ('" + entityId + "', null)");

        //then
        final List<Map<String, Object>> results = jdbcTemplate.queryForList("select * from test_entity where id = '" + entityId + "'");
        assertThat(results.get(0).get("ENCRYPTED_VALUE")).isNull();
        assertThat(testEntityRepository.getReferenceById(entityId).getEncryptedValue()).isNull();
        verify(jeapCryptoDbTestConfig.testCryptoService(), never()).decrypt(any());
    }

}
