package com.konloch.taskmanager;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The TaskManager starts with a default tick length of 10 milliseconds. Once created you will need to start it when appropriate.
 * It contains a thread and a bunch of useful API functions for destroying and re-creating the main thread safely.
 *
 * @author Konloch
 * @since 5/2/2021
 */
public class TaskManager
{
	private long tickLength = 10;
	private boolean signalStop;
	private Runnable onDestroyed;
	private Thread mainThread;
	
	//TODO
	// moving from CopyOnWrite might be a good idea, but I'd like to avoid using
	// synchronized in the task loop, so we'll need some third alternative to replace this.
	private CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();
	
	/**
	 * Constructs a new TaskManager with a default tick length of 10 milliseconds
	 */
	public TaskManager() {}
	
	/**
	 * Constructs a new TaskManager with a specified tick length in milliseconds
	 * @param tickLength any long as the value of the delay between task batches in milliseconds
	 */
	public TaskManager(long tickLength)
	{
		this.tickLength = tickLength;
	}
	
	/**
	 * Attempts to start the main thread
	 */
	public synchronized void start()
	{
		if(mainThread != null)
			return;
		
		//restart the signal flag if it was previously stopped
		if(signalStop)
			signalStop = false;
		
		mainThread = new Thread(()->
		{
			while(!signalStop)
			{
				try
				{
					batchProcessTasks();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			//on exit clear everything
			tasks.clear();
			
			//copy the runnable reference
			Runnable onDestroyedRunnable = onDestroyed;
			
			//destroy the original reference
			onDestroyed = null;
			
			//destroy the parent reference
			mainThread = null;
			
			//run the onDestroyed reference if one was passed
			if(onDestroyedRunnable != null)
				onDestroyedRunnable.run();
		});
		
		mainThread.start();
	}
	
	/**
	 * Process all the tasks and remove them from the task list if they have the singal stop flag enabled
	 * @throws InterruptedException if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
	 */
	void batchProcessTasks() throws InterruptedException
	{
		long clockedIn = System.currentTimeMillis();
		
		tasks.forEach(Task::run);
		tasks.removeIf(Task::isSignalStop);
		
		long clockedOut = System.currentTimeMillis();
		long sleepFor = tickLength-(clockedOut-clockedIn);
		
		if(sleepFor <= 0)
			sleepFor = 1;
		
		Thread.sleep(sleepFor);
	}
	
	/**
	 * Delay executing a TaskRunnable
	 *
	 * @param millis any long representing the delay in milliseconds to call the TaskRunnable
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager delay(long millis, TaskRunnable runnable)
	{
		Task task = new Task(runnable);
		task.setType(TaskType.DELAY);
		long started = System.currentTimeMillis();
		
		//run the task until
		task.setConditionMet(() -> System.currentTimeMillis()-started >= millis);
		
		tasks.add(task);
		return this;
	}
	
	/**
	 * Run a TaskRunnable every X milliseconds.
	 *
	 * @param millis any long representing the delay in milliseconds between each call to the TaskRunnable
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager delayLoop(long millis, TaskRunnable runnable)
	{
		Task task = new Task(runnable);
		task.setType(TaskType.DELAY_UNTIL_STOP);
		task.setRepeatingDelay(millis);
		long started = System.currentTimeMillis();
		
		//run the task until
		task.setConditionMet(() -> System.currentTimeMillis()-started >= millis);
		
		tasks.add(task);
		return this;
	}
	
	/**
	 * Run a TaskRunnable once.
	 *
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager doOnce(TaskRunnable runnable)
	{
		return delay(0, runnable);
	}
	
	/**
	 * Run a TaskRunnable until the ConditionMet returns false.
	 *
	 * @param conditionMet any ConditionMet for checking if it should be stopped
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager doWhile(ConditionMet conditionMet, TaskRunnable runnable)
	{
		Task task = new Task(runnable);
		task.setType(TaskType.CONDITIONAL_WHILE);
		task.setConditionMet(conditionMet);
		
		tasks.add(task);
		return this;
	}
	
	/**
	 * Run a TaskRunnable until it is stopped.
	 *
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager doForever(TaskRunnable runnable)
	{
		Task task = new Task(runnable);
		task.setType(TaskType.UNTIL_STOP);
		
		tasks.add(task);
		return this;
	}
	
	/**
	 * Loop a TaskRunnable x amount before it stops.
	 *
	 * @param amount any integer as the amount it should repeat
	 * @param runnable any TaskRunnable to be run
	 * @return this instance for method chaining
	 */
	public TaskManager loop(int amount, TaskRunnable runnable)
	{
		Task task = new Task(runnable);
		task.setType(TaskType.COUNTED_LOOP);
		task.setCounterMax(amount);
		
		tasks.add(task);
		return this;
	}
	
	/**
	 * Set the tick length between executing task batches.
	 *
	 * @param tickLength any long as the value of the delay between task batches in milliseconds
	 * @return this instance for method chaining
	 */
	public TaskManager setTickLength(long tickLength)
	{
		this.tickLength = tickLength;
		return this;
	}
	
	/**
	 * Destroy the TaskManager and stop the main thread, all states get returned to before it was started.
	 */
	public void destroy()
	{
		destroy(null);
	}
	
	/**
	 * Destroy the TaskManager and stop the main thread, all states get returned to before it was started.
	 * @param onDestroyed This gets called after the states have fully cleared. This means it is safe to re-run the same Task Manager in this implementation.
	 */
	public void destroy(Runnable onDestroyed)
	{
		this.onDestroyed = onDestroyed;
		signalStop = true;
	}
	
	/**
	 * Alert that this is a library.
	 *
	 * @param args program launch arguments
	 */
	public static void main(String[] args)
	{
		throw new RuntimeException("Incorrect usage - for information on how to use this correctly visit https://konloch.com/TaskManager/");
	}
}
