package cn.tycoding.constant;

/**
 * 系统常量值
 *
 * @author tycoding
 * @date 2019-06-14
 */
public interface CommonConstant {

    /**
     * 群发消息Session Key前缀标识
     */
    String CHAT_COMMON_PREFIX = "CHAT_COMMON_";

    /**
     * 推送至指定用户消息
     *      推送方Session Key前缀标识
     */
    String CHAT_FROM_PREFIX = "CHAT_FROM_";

    /**
     * 推送至指定用户消息
     *      接收方Session Key前缀标识
     */
    String CHAT_TO_PREFIX = "_TO_";

    /**
     * 发送消息的类型
     *      File还是文字消息
     * */
    String CHAT_TYPE = "FILE";
}
