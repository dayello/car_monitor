package org.carm.protocol.t808;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.carm.protocol.commons.Bit;
import org.carm.protocol.commons.transform.AttributeConverter;
import org.carm.protocol.commons.transform.AttributeConverterYue;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.commons.JT808;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@ToString
@Data
@Accessors(chain = true)
@Message(JT808.位置信息汇报)
public class T0200 extends JTMessage {

    /**
     * 使用 Bit.isTrue判断报警和状态标志位
     * @see Bit
     */
    @Field(length = 4, desc = "报警标志")
    private int warnBit;
    @Field(length = 4, desc = "状态")
    private int statusBit;
    @Field(length = 4, desc = "纬度")
    private int latitude;
    @Field(length = 4, desc = "经度")
    private int longitude;
    @Field(length = 2, desc = "高程(米)")
    private int altitude;
    @Field(length = 2, desc = "速度(1/10公里每小时)")
    private int speed;
    @Field(length = 2, desc = "方向")
    private int direction;
    @Field(length = 6, charset = "BCD", desc = "时间(YYMMDDHHMMSS)")
    private LocalDateTime deviceTime;
    @Field(converter = AttributeConverter.class, desc = "位置附加信息", version = {-1, 0})
    @Field(converter = AttributeConverterYue.class, desc = "位置附加信息(粤标)", version = 1)
    private Map<Integer, Object> attributes;

    public int getAttributeInt(int key) {
        if (attributes != null) {
            Integer value = (Integer) attributes.get(key);
            if (value != null) {
                return value;
            }
        }
        return 0;
    }

    public long getAttributeLong(int key) {
        if (attributes != null) {
            Long value = (Long) attributes.get(key);
            if (value != null) {
                return value;
            }
        }
        return 0L;
    }

    public double getLng() {
        return longitude / 1000000d;
    }

    public double getLat() {
        return latitude / 1000000d;
    }

    public float getSpeedKph() {
        return latitude / 10f;
    }
}