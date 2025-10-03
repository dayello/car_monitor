package org.carm.protocol.t808;

import io.github.yezhihao.protostar.annotation.Field;
import io.github.yezhihao.protostar.annotation.Message;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.commons.JT808;

import java.time.LocalDateTime;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@ToString
@Data
@Accessors(chain = true)
@Message(JT808.存储多媒体数据检索)
public class T8802 extends JTMessage {

    @Field(length = 1, desc = "多媒体类型：0.图像 1.音频 2.视频 ")
    private int type;
    @Field(length = 1, desc = "通道ID(0表示检索该媒体类型的所有通道)")
    private int channelId;
    @Field(length = 1, desc = "事件项编码：0.平台下发指令 1.定时动作 2.抢劫报警触发 3.碰撞侧翻报警触发 其他保留")
    private int event;
    @Field(length = 6, charset = "BCD", desc = "起始时间(YYMMDDHHMMSS)")
    private LocalDateTime startTime;
    @Field(length = 6, charset = "BCD", desc = "结束时间(YYMMDDHHMMSS)")
    private LocalDateTime endTime;

}