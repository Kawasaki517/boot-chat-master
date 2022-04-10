package cn.tycoding.utils;

import cn.tycoding.entity.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author tycoding
 * @date 2019-06-15
 */
public class CoreUtil {

    /**
     * 按照时间顺序向List中push数据
     *
     * @param list
     */
    public static void push(List<Message> list) {
        list.sort(Comparator.comparing(Message::getTime));
    }

    /**
     * format date
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static List<Message> rankTime(List<Message> list) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //冒泡排序
        Message tempDate = null;
        for (int i = list.size()- 1; i > 0; --i) {
            for (int j = 0; j < i; ++j) {
                /**
                 * 从大到小的排序
                 */
                if (sdf.parse(list.get(j + 1).getTime()).before(sdf.parse(list.get(j).getTime()))) {
                    tempDate = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, tempDate);
                }
            }
        }
        return list;
    }
}
