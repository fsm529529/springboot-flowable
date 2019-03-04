package com.huazhu.springbootflowable.task;

import com.huazhu.springbootflowable.SpringbootFlowableApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.FormService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class TaskTest extends SpringbootFlowableApplicationTests {

    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private FormRepositoryService formRepositoryService;
    @Autowired
    private FormService formService;

    @Test
    public void getUserTask() {
        //runtimeService.startProcessInstanceByKey("test");
        List<Task> list = taskService.createTaskQuery().taskAssignee("user:2").list();
        System.out.println(list);
    }

    @Test
    public void claimTask() {
        List<Task> list = taskService.createTaskQuery().list();
        taskService.claim(list.get(0).getId(),"user:1");

    }

    @Test
    public void testForm() {

    }
}
