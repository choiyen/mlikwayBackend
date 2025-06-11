package project.MilkyWay.ComonType;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringListTypeHandler extends BaseTypeHandler<List<String>>
{

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {

        // List<String>을 String으로 변환하여 저장 (쉼표로 구분된 문자열 등)
        ps.setString(i, String.join(",", parameter));

    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {

        String value = rs.getString(columnName);
        // 쉼표로 구분된 문자열을 List로 변환
        return value == null ? null : Arrays.asList(value.split(","));

    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {

        String value = rs.getString(columnIndex);
        return value == null ? null : Arrays.asList(value.split(","));

    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {

        String value = cs.getString(columnIndex);
        return value == null ? null : Arrays.asList(value.split(","));

    }
}
/*
SQL 데이터베이스는 기본적으로 컬렉션 타입을 지원하지 않음:
데이터베이스는 기본적으로 List나 Set과 같은 컬렉션 타입을 저장할 수 없습니다. 예를 들어, List<String> 타입을 직접 VARCHAR 컬럼에 저장하려고 하면, MyBatis는 이를 처리할 방법을 모릅니다.
따라서 List<String>을 데이터베이스에 저장하려면, List<String>을 문자열(String)로 변환하여 하나의 컬럼에 저장해야 한다.

 */