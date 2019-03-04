package com.huazhu.springbootflowable.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * mvc配置类
 * @author: nss
 * @date: 2018/10/17 20:17
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {


	private final ObjectMapper mapper;

	@Autowired
	public MvcConfig(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//允许出现特殊字符和转义符
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		//允许出现单引号
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		//是否格式化输出
		mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
		//允许对象忽略json中不存在的属性,忽略无法转换的对象 “No serializer found for class com.xxx.xxx”
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,true);
		//列换成json时,将所有的long变成string
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		mapper.registerModule(simpleModule);

		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
		//为空和null的不序列化
		//Todo 别删
		//mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		jackson2HttpMessageConverter.setObjectMapper(mapper);
		converters.add(jackson2HttpMessageConverter);
		super.configureMessageConverters(converters);
	}

}
