package ai.utiliti.bes.model.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigInteger;

@Converter
public class BigIntHexConverter implements AttributeConverter<BigInteger, String> {

    @Override
    public String convertToDatabaseColumn(BigInteger attribute) {
        String hexString = attribute.toString(16);
        return "0x" + hexString;
    }

    @Override
    public BigInteger convertToEntityAttribute(String dbData) {

        if (dbData.startsWith("0x")) {
            return new BigInteger(dbData.substring(2), 16);
        }

        return new BigInteger(dbData);
    }
}