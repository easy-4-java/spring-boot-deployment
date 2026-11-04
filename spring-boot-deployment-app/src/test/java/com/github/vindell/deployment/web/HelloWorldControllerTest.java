package com.github.vindell.deployment.web;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HelloWorldControllerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new HelloWorldController()).build();

    @Test
    public void rootGetPreservesResponse() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string("Hello App ."));
    }

    @Test
    public void rootPostIsRejected() throws Exception {
        mvc.perform(post("/")).andExpect(status().isMethodNotAllowed());
    }
}
