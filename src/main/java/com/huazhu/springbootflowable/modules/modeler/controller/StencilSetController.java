package com.huazhu.springbootflowable.modules.modeler.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huazhu.springbootflowable.common.response.R;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/stencil-sets")
@Slf4j
public class StencilSetController {

    @Autowired
    protected ObjectMapper objectMapper;

    @GetMapping("/editor")
    public R getStencilSetForEditor() throws IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(ResourceUtils.getFile("classpath:stencilset_bpmn.json")));
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(stream);
        } catch (Exception e) {
            log.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }finally {
            stream.close();
        }
        return R.ok(jsonNode);
    }
}
