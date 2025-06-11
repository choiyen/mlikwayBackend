package project.MilkyWay.Config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Converter
public class DatabaseConverter implements AttributeConverter<String, String> {

    @Value("${spring.database.column.encrypt.key}")
    private String key;

    private Cipher encryptCipher;
    private Cipher decryptCipher;


    @PostConstruct
    public void init() throws Exception {
        encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, generateMysqlAESKey(key, "UTF-8"));
        decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, generateMysqlAESKey(key, "UTF-8"));

    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return new String(Hex.encodeHex(encryptCipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8)))).toUpperCase();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return new String(decryptCipher.doFinal(Hex.decodeHex(dbData.toCharArray())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public  static SecretKeySpec generateMysqlAESKey(final String key, final String encoding)
    {
        try {
            final byte[] finalKey = new byte[16];
            int i = 0;
            for(byte b : key.getBytes(encoding))
            {
                finalKey[i++%16] ^= b;
            }
            return new SecretKeySpec(finalKey, "AES");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}

