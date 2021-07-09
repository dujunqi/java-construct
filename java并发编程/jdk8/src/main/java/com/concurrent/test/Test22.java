package com.concurrent.test;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
/**
 * 实现保护性暂停模式
 */
public class Test22 {
    /**
     * 这种设计模式和join方法进行对比：
     * 1.这里可以做一些其他事，等待下载结果的线程不用等下载线程全部执行完之后才能去到下载结果，而是在
     * 中途通过compete方法完成了下载结果的传递。  
     * 2.并且这里使用的都是局部变量，如response等，而不用像join方法一样使用全局变量
     */
    public static void main(String[] args) {
        GuardedObject guardedObject = new GuardedObject();
         // 实际的下载线程
        new Thread(() -> {
            // 子线程执行下载
            List<String> response = download();
            log.debug("download complete...");
            guardedObject.complete(response);

        }).start();
        
        // 这是等待下载结果的线程
        new Thread(()->{
            log.debug("waiting...");
            Object response = guardedObject.get();
            log.debug("get response: [{}] lines", ((List<String>) response).size());
        }).start();
       
    }
    
    public static List<String> download(){
        List<String> response = new ArrayList<>();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response.add("1");
        response.add("2");
        return response;
    }


}

class GuardedObject {
    private Object response;
    private final Object lock = new Object();

    public Object get() {
        synchronized (lock) {
            // 条件不满足则等待
            while (response == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }

    public void complete(Object response) {
        synchronized (lock) {
            // 条件满足，通知等待线程
            this.response = response;
            lock.notifyAll();
        }
    }
}