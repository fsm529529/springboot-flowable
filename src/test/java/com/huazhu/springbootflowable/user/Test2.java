package com.huazhu.springbootflowable.user;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test2 {

    @Test
    public void test() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
        Date parse = simpleDateFormat.parse("2019-03-02T02:43:16.671+0000");
        System.out.println(parse);
    }
}
