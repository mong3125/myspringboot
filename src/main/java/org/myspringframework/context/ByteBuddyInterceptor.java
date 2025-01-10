package org.myspringframework.context;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ByteBuddyInterceptor {
    @RuntimeType
    public Object intercept(
            @SuperCall Callable<Object> superMethod,
            @Origin Method method,
            @AllArguments Object[] args
    ) throws Exception {
        System.out.println("Intercepting method: " + method.getName());

        // Before method execution
        System.out.println("Arguments: " + java.util.Arrays.toString(args));

        // Call the original method
        Object result = superMethod.call();

        // After method execution
        System.out.println("Result: " + result);

        return result;
    }
}
