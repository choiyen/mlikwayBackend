package project.MilkyWay.ComonType;


import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import project.MilkyWay.ComonType.Enum.Cleandirection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CleandirectionTypeHandler extends BaseTypeHandler<Cleandirection>
{//https://amagrammer91.tistory.com/115

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Cleandirection parameter, JdbcType jdbcType) throws SQLException
    {

        ps.setString(i, parameter.name());  // `enum`의 `name()`을 사용하여 문자열로 설정

    }

    @Override
    public Cleandirection getNullableResult(ResultSet rs, String columnName) throws SQLException
    {

        String value = rs.getString(columnName);
        return value == null ? null : Cleandirection.valueOf(value);  // 문자열을 `enum`으로 변환

    }

    @Override
    public Cleandirection getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {

        String value = rs.getString(columnIndex);
        return value == null ? null : Cleandirection.valueOf(value);  // 문자열을 `enum`으로 변환

    }

    @Override
    public Cleandirection getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException
    {

        String value = cs.getString(columnIndex);
        return value == null ? null : Cleandirection.valueOf(value);  // 문자열을 `enum`으로 변환

    }
}
