package project.MilkyWay.ComonType.Enum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumValue, Enum<?>> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValue enumAnnotation) {
        this.enumClass = enumAnnotation.enumClass(); // Enum 타입 지정
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null은 허용하도록 설정할 수도 있습니다
        }

        // enum 값들 중 하나인지 확인
        Object[] enumValues = enumClass.getEnumConstants();
        for (Object enumValue : enumValues) {
            if (enumValue.equals(value)) {
                return true; // 값이 enum에 포함되면 유효
            }
        }
        return false; // enum에 포함되지 않으면 유효하지 않음
    }
}
