package com.konloch.taskmanager;

import static com.konloch.taskmanager.TaskType.*;

/**
 * Represents a task and the current state of it being run
 *
 * @author Konloch
 * @since 5/2/2021
 */
public class Task
{
	private final TaskRunnable runnable;
	private boolean signalStop;
	private TaskType type;
	private int counter;
	private int counterMax;
	private long repeatingDelay;
	private ConditionMet conditionMet;
	
	/**
	 * Construct a new task
	 * @param runnable the runnable implementation of this task
	 */
	public Task(TaskRunnable runnable) {this.runnable = runnable;}
	
	/**
	 * Run the task and return the current signal stop state
	 * @return flipped value of the signal stop flag
	 */
	boolean run()
	{
		boolean isDelay = type == DELAY;
		boolean isDelayUntilStop = type == DELAY_UNTIL_STOP;
		boolean isConditionalWhile = type == CONDITIONAL_WHILE;
		
		if(type != null)
		{
			switch (type)
			{
				case UNTIL_STOP:
					safelyRun();
					break;
				
				case COUNTED_LOOP:
					safelyRun();
					if(counter++ + 1 >= counterMax)
						stop();
					break;
				
				case CONDITIONAL_WHILE:
				case DELAY:
				case DELAY_UNTIL_STOP:
					if(conditionMet.isConditionMet())
					{
						if(isDelay)
							stop();
						
						safelyRun();
						
						//requeue the conditional so it can be set later
						if(isDelayUntilStop)
						{
							long started = System.currentTimeMillis();
							setConditionMet(() -> System.currentTimeMillis()-started >= getRepeatingDelay());
						}
					}
					else if(isConditionalWhile)
						stop();
					break;
			}
		}
		
		return !signalStop;
	}
	
	public void safelyRun()
	{
		try
		{
			runnable.run(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			//stop on any errors
			stop();
		}
	}
	
	/**
	 * Signal to the TaskManager that this task has stopped
	 */
	public void stop()
	{
		signalStop = true;
	}
	
	/**
	 * Sets the task type
	 * @param type any Task type
	 */
	public void setType(TaskType type)
	{
		this.type = type;
	}
	
	/**
	 * Set the condition met value
	 *
	 * @param conditionMet the condition met implementation
	 */
	public void setConditionMet(ConditionMet conditionMet)
	{
		this.conditionMet = conditionMet;
	}
	
	/**
	 * Set the counter maximum value
	 *
	 * @param counterMax any integer as the counter max
	 */
	public void setCounterMax(int counterMax)
	{
		this.counterMax = counterMax;
	}
	
	/**
	 * Set the repeating delay
	 *
	 * @param repeatingDelay any long as the value of the repeating delay in milliseconds
	 */
	public void setRepeatingDelay(long repeatingDelay)
	{
		this.repeatingDelay = repeatingDelay;
	}
	
	/**
	 * Returns the value of the signalStop flag
	 *
	 * @return the value of the signalStop flag
	 */
	public boolean isSignalStop()
	{
		return signalStop;
	}
	
	/**
	 * Returns the repeating delay
	 *
	 * @return any long as the repeating delay in milliseconds
	 */
	public long getRepeatingDelay()
	{
		return repeatingDelay;
	}
}
