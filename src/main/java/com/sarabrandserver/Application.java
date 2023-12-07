package com.sarabrandserver;

import com.sarabrandserver.graal.MyRuntimeHints;
import com.sarabrandserver.util.VariantHelperMapper;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(value = {MyRuntimeHints.class})
@RegisterReflectionForBinding(value = VariantHelperMapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}