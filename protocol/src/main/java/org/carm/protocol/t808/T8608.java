package org.carm.protocol.t808;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.carm.protocol.commons.Shape;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.commons.JT808;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@ToString
@Data
@Accessors(chain = true)
@Message(JT808.查询区域或线路数据)
public class T8608 extends JTMessage {

    /** @see Shape */
    @Field(length = 1, desc = "查询类型：1.圆形 2.矩形 3.多边形 4.路线")
    private int type;
    @Field(totalUnit = 4, desc = "区域列表")
    private int[] id;

}