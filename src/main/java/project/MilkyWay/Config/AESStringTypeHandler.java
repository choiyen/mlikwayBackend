package project.MilkyWay.Config;

import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(String.class)
public class AESStringTypeHandler extends BaseTypeHandler<String> {

    private static final String KEY = "your-secret-key"; // 환경변수나 설정에서 주입하는 게 좋음

    private static final Cipher encryptCipher;
    private static final Cipher decryptCipher;

    static {
        try {
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, DatabaseConverter.generateMysqlAESKey(KEY, "UTF-8"));

            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, DatabaseConverter.generateMysqlAESKey(KEY, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Cipher 초기화 실패", e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            String encrypted = new String(Hex.encodeHex(encryptCipher.doFinal(parameter.getBytes(StandardCharsets.UTF_8)))).toUpperCase();
            ps.setString(i, encrypted);
        } catch (Exception e) {
            throw new SQLException("암호화 실패", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private String decrypt(String value) throws SQLException {
        if (value == null) return null;
        try {
            return new String(decryptCipher.doFinal(Hex.decodeHex(value.toCharArray())));
        } catch (Exception e) {
            throw new SQLException("복호화 실패", e);
        }
    }
}
