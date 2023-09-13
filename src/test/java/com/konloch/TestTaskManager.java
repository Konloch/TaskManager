package com.konloch;

import com.konloch.taskmanager.TaskManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is just one big test that validates each function inside the TaskManager
 *
 * @author Konloch
 * @since 7/11/2022
 */
public class TestTaskManager
{
	private static boolean noErrors = true;
	private static boolean delayRanOnce = false;
	private static boolean delaysRunningBehind = false;
	private static boolean delaysRunningTooFast = false;
	private static boolean ranDelay1 = false;
	private static boolean ranDelay2 = false;
	private static boolean ranLoop1 = false;
	private static int loopCounter = 0;
	
	public static void main(String[] args)
	{
		TaskManager manager = new TaskManager();
		manager.start();
		
		System.out.println("Testing the Task Manager, please wait...");
		System.out.println();
		
		//test error handling
		manager.doOnce((task)->
		{
			throw new RuntimeException("Test Handling Errors");
		});
		
		long testDelays = System.currentTimeMillis();
		
		//verify delays run only once
		manager.doOnce((task)->
		{
			ranDelay1 = true;
			
			if(delayRanOnce)
				error("ERROR, ALREADY RAN ONCE");
			else
				delayRanOnce = true;
		});
		
		//verify delay executes once between a range of 30ms
		manager.delay(100, (task)->
		{
			ranDelay2 = true;
			
			if(System.currentTimeMillis()-testDelays <= 70)
			{
				error("DELAYS ARE NOT WORKING AS INTENDED - TOO SLOW");
				delaysRunningBehind = true;
			}
			else if(System.currentTimeMillis()-testDelays >= 130)
			{
				error("DELAYS ARE NOT WORKING AS INTENDED - TOO FAST");
				delaysRunningTooFast = true;
			}
		});
		
		//test for loops
		manager.loop(2, (task)->
		{
			if(!ranLoop1)
				ranLoop1 = true;
			
			if(loopCounter >= 2)
				error("ERROR, ALREADY RAN ONCE");
			else
				loopCounter++;
		});
		
		//run test-2
		manager.delay(1000, (task)-> test2(manager));
	}
	
	private static void test2(TaskManager manager)
	{
		//destroy the current manager
		manager.destroy(()->
		{
			//restart the current manager
			manager.start();
			
			//run test-3
			test3(manager);
		});
	}
	
	private static void test3(TaskManager manager)
	{
		AtomicInteger runCount1 = new AtomicInteger();
		AtomicInteger runCount2 = new AtomicInteger();
		AtomicInteger runCount3 = new AtomicInteger();
		
		//run some delay loop tests
		manager.delayLoop(10, (task)->
		{
			int rc = runCount1.incrementAndGet();
			if(rc == 5)
				task.stop();
			else if(rc > 5)
				error("STOP FUNCTION IS NOT WORKING AS INTENDED (RC-1)");
		});
		
		//test running forever
		manager.doForever((task)->
		{
			int rc = runCount2.incrementAndGet();
			if(rc == 5)
				task.stop();
			else if(rc > 5)
				error("STOP FUNCTION IS NOT WORKING AS INTENDED (RC-2)");
		});
		
		//test do while
		manager.doWhile(()->{
			return runCount3.incrementAndGet() < 5;
		}, (task)->{
			int rc = runCount3.get();
			if(rc > 5)
				error("DOWHILE FUNCTION IS NOT WORKING AS INTENDED (RC-3)");
		});
		
		//verify everything is running as expected
		manager.delay(1000, (task)->
		{
			if(!noErrors)
				return;
			
			if(ranDelay1 && delayRanOnce && ranDelay2 && !delaysRunningBehind && !delaysRunningTooFast)
				System.out.println(".delay PASSED ALL CHECKS");
			else
				System.err.println(".delay FAILED");
			
			if(ranDelay1 && delayRanOnce)
				System.out.println(".doOnce PASSED ALL CHECKS");
			else
				System.err.println(".doOnce FAILED");
			
			if(runCount2.get() == 5)
				System.out.println(".doForever PASSED ALL CHECKS");
			else
				System.err.println(".doForever FAILED");
			
			if(runCount3.get() == 5)
				System.out.println(".doWhile PASSED ALL CHECKS");
			else
				System.err.println(".doWhile FAILED");
			
			if(ranLoop1 && loopCounter == 2)
				System.out.println(".loop PASSED ALL CHECKS");
			else
				System.err.println(".loop FAILED");
			
			if(runCount1.get() == 5)
				System.out.println(".delayLoop PASSED ALL CHECKS");
			else
				System.err.println(".delayLoop FAILED");
			
			manager.destroy();
		});
	}
	
	private static void error(String reason)
	{
		noErrors = false;
		System.err.println(reason);
	}
}
